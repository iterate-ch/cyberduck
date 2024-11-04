package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSTokenField;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.CertificateStoreFactory;
import ch.cyberduck.core.DefaultCharsetProvider;
import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.ui.LoginInputValidator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.Selector;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class DefaultBookmarkController extends BookmarkController {
    private static final Logger log = LogManager.getLogger(DefaultBookmarkController.class);

    private static final String TIMEZONE_CONTINENT_PREFIXES =
        "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Outlet
    private NSTextField nicknameField;
    @Outlet
    private NSTokenField labelsField;
    @Outlet
    private NSPopUpButton certificatePopup;
    @Outlet
    private NSPopUpButton timezonePopup;
    @Outlet
    private NSPopUpButton encodingPopup;

    private final KeychainX509KeyManager x509KeyManager = new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), bookmark,
        CertificateStoreFactory.get());

    public DefaultBookmarkController(final Host bookmark) {
        this(bookmark, new LoginOptions(bookmark.getProtocol()));
    }

    public DefaultBookmarkController(final Host bookmark, final LoginOptions options) {
        super(bookmark, options);
    }

    public DefaultBookmarkController(final Host bookmark, final LoginInputValidator validator, final LoginOptions options) {
        super(bookmark, validator, options);
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        window.makeFirstResponder(hostField);
    }

    public void setNicknameField(final NSTextField f) {
        this.nicknameField = f;
        notificationCenter.addObserver(this.id(),
            Foundation.selector("nicknameFieldDidChange:"),
            NSControl.NSControlTextDidChangeNotification,
            f.id());
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(nicknameField, BookmarkNameProvider.toString(bookmark));
            }
        });
    }

    @Action
    public void nicknameFieldDidChange(final NSNotification sender) {
        bookmark.setNickname(nicknameField.stringValue());
        this.update();
    }

    public void setLabelsField(final NSTokenField f) {
        this.labelsField = f;
        notificationCenter.addObserver(this.id(),
            Foundation.selector("tokenFieldDidChange:"),
            NSControl.NSControlTextDidEndEditingNotification,
            f.id());
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                if(bookmark.getLabels().isEmpty()) {
                    f.setObjectValue(NSArray.array());
                }
                else {
                    f.setObjectValue(NSArray.arrayWithObjects(bookmark.getLabels().toArray(new String[bookmark.getLabels().size()])));
                }
            }
        });
    }

    @Action
    public void tokenFieldDidChange(final NSNotification sender) {
        final Set<String> labels = new HashSet<>();
        final NSArray dict = Rococoa.cast(labelsField.objectValue(), NSArray.class);
        final NSEnumerator i = dict.objectEnumerator();
        NSObject next;
        while(null != (next = i.nextObject())) {
            labels.add(next.toString());
        }
        bookmark.setLabels(labels);
        this.update();
    }

    @Override
    public void setPasswordField(final NSSecureTextField f) {
        super.setPasswordField(f);
        this.notificationCenter.addObserver(this.id(),
            Foundation.selector("passwordFieldTextDidEndEditing:"),
            NSControl.NSControlTextDidEndEditingNotification,
            f.id());
    }

    @Action
    public void passwordFieldTextDidEndEditing(NSNotification notification) {
        if(options.keychain && options.password) {
            if(StringUtils.isBlank(bookmark.getHostname())) {
                return;
            }
            if(StringUtils.isBlank(bookmark.getCredentials().getUsername())) {
                return;
            }
            if(StringUtils.isBlank(passwordField.stringValue())) {
                return;
            }
            try {
                keychain.addPassword(bookmark.getProtocol().getScheme(),
                    bookmark.getPort(),
                    bookmark.getHostname(),
                    bookmark.getCredentials().getUsername(),
                    // Remove control characters (char &lt;= 32) from both ends
                    StringUtils.strip(passwordField.stringValue())
                );
            }
            catch(LocalAccessDeniedException e) {
                log.error("Failure saving credentials for {} in keychain. {}", bookmark, e);
            }
        }
    }

    public void setCertificatePopup(final NSPopUpButton button) {
        this.certificatePopup = button;
        this.certificatePopup.setTarget(this.id());
        final Selector action = Foundation.selector("certificateSelectionChanged:");
        this.certificatePopup.setAction(action);
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                certificatePopup.setEnabled(options.certificate);
                certificatePopup.removeAllItems();
                certificatePopup.addItemWithTitle(LocaleFactory.localizedString("None"));
                if(options.certificate) {
                    certificatePopup.menu().addItem(NSMenuItem.separatorItem());
                    for(String certificate : x509KeyManager.list()) {
                        certificatePopup.addItemWithTitle(certificate);
                        certificatePopup.lastItem().setRepresentedObject(certificate);
                    }
                }
                if(bookmark.getCredentials().isCertificateAuthentication()) {
                    certificatePopup.selectItemAtIndex(certificatePopup.indexOfItemWithRepresentedObject(bookmark.getCredentials().getCertificate()));
                }
                else {
                    certificatePopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
                }
            }
        });
    }

    @Action
    public void certificateSelectionChanged(final NSPopUpButton sender) {
        bookmark.getCredentials().setCertificate(sender.selectedItem().representedObject());
        this.update();
    }

    public void setTimezonePopup(final NSPopUpButton button) {
        this.timezonePopup = button;
        this.timezonePopup.setTarget(this.id());
        this.timezonePopup.setAction(Foundation.selector("timezonePopupClicked:"));
        this.timezonePopup.removeAllItems();
        final List<String> timezones = Arrays.asList(TimeZone.getAvailableIDs());
        this.timezonePopup.addItemWithTitle(UTC.getID());
        this.timezonePopup.lastItem().setRepresentedObject(UTC.getID());
        this.timezonePopup.menu().addItem(NSMenuItem.separatorItem());
        timezones.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return TimeZone.getTimeZone(o1).getID().compareTo(TimeZone.getTimeZone(o2).getID());
            }
        });
        for(String tz : timezones) {
            if(tz.matches(TIMEZONE_CONTINENT_PREFIXES)) {
                this.timezonePopup.addItemWithTitle(String.format("%s", tz));
                this.timezonePopup.lastItem().setRepresentedObject(tz);
            }
        }
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                timezonePopup.setEnabled(!bookmark.getProtocol().isUTCTimezone());
                if(null == bookmark.getTimezone()) {
                    if(bookmark.getProtocol().isUTCTimezone()) {
                        timezonePopup.setTitle(UTC.getID());
                    }
                    else {
                        timezonePopup.setTitle(TimeZone.getTimeZone(preferences.getProperty("ftp.timezone.default")).getID());
                    }
                }
                else {
                    timezonePopup.setTitle(bookmark.getTimezone().getID());
                }
            }
        });
    }

    @Action
    public void timezonePopupClicked(final NSPopUpButton sender) {
        String selected = sender.selectedItem().representedObject();
        String[] ids = TimeZone.getAvailableIDs();
        for(String id : ids) {
            TimeZone tz;
            if((tz = TimeZone.getTimeZone(id)).getID().equals(selected)) {
                bookmark.setTimezone(tz);
                break;
            }
        }
        this.update();
    }

    public void setEncodingPopup(final NSPopUpButton button) {
        this.encodingPopup = button;
        this.encodingPopup.setTarget(this.id());
        final Selector action = Foundation.selector("encodingSelectionChanged:");
        this.encodingPopup.setAction(action);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemWithTitle(DEFAULT);
        this.encodingPopup.menu().addItem(NSMenuItem.separatorItem());
        for(String encoding : new DefaultCharsetProvider().availableCharsets()) {
            this.encodingPopup.addItemWithTitle(encoding);
            this.encodingPopup.lastItem().setRepresentedObject(encoding);
        }
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                encodingPopup.setEnabled(bookmark.getProtocol().isEncodingConfigurable());
                if(!bookmark.getProtocol().isEncodingConfigurable()) {
                    encodingPopup.selectItemWithTitle(DEFAULT);
                }
                else {
                    if(null == bookmark.getEncoding()) {
                        encodingPopup.selectItemWithTitle(DEFAULT);
                    }
                    else {
                        encodingPopup.selectItemAtIndex(encodingPopup.indexOfItemWithRepresentedObject(bookmark.getEncoding()));
                    }
                }
            }
        });
    }

    @Action
    public void encodingSelectionChanged(final NSPopUpButton sender) {
        if(sender.selectedItem().title().equals(DEFAULT)) {
            bookmark.setEncoding(null);
        }
        else {
            bookmark.setEncoding(sender.selectedItem().title());
        }
        this.update();
    }
}
