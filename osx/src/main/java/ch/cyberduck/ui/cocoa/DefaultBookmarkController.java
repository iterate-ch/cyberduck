package ch.cyberduck.ui.cocoa;

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
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.DefaultCharsetProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;

import org.rococoa.Foundation;
import org.rococoa.Selector;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class DefaultBookmarkController extends BookmarkController {

    private static final String TIMEZONE_CONTINENT_PREFIXES =
            "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Outlet
    private NSTextField nicknameField;
    @Outlet
    private NSPopUpButton certificatePopup;
    @Outlet
    private NSPopUpButton timezonePopup;
    @Outlet
    private NSPopUpButton encodingPopup;

    public DefaultBookmarkController(final Host bookmark) {
        super(bookmark, bookmark.getCredentials());
    }

    public void setNicknameField(final NSTextField field) {
        this.nicknameField = field;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("nicknameFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.nicknameField);
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

    public void setCertificatePopup(final NSPopUpButton button) {
        this.certificatePopup = button;
        this.certificatePopup.setTarget(this.id());
        final Selector action = Foundation.selector("certificateSelectionChanged:");
        this.certificatePopup.setAction(action);
        this.certificatePopup.removeAllItems();
        this.certificatePopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        this.certificatePopup.menu().addItem(NSMenuItem.separatorItem());
        for(String certificate : new KeychainX509KeyManager(bookmark).list()) {
            this.certificatePopup.addItemWithTitle(certificate);
            this.certificatePopup.lastItem().setRepresentedObject(certificate);
        }
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                certificatePopup.setEnabled(bookmark.getProtocol().getScheme() == Scheme.https);
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
        Collections.sort(timezones, new Comparator<String>() {
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
