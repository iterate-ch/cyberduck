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
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSTextFieldCell;
import ch.cyberduck.binding.application.NSTokenField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.*;
import ch.cyberduck.core.diagnostics.Reachability;
import ch.cyberduck.core.diagnostics.ReachabilityDiagnosticsFactory;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.FilesystemBookmarkResolverFactory;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHPrivateKeyConfigurator;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.LoginInputValidator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

public class BookmarkController extends BundleController {
    private static final Logger log = LogManager.getLogger(BookmarkController.class);

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final String TIMEZONE_CONTINENT_PREFIXES =
            "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

    private final ProtocolFactory protocols = ProtocolFactory.get();
    private final List<BookmarkObserver> observers = new ArrayList<>();
    private final Host bookmark;
    private final LoginInputValidator validator;
    private final LoginOptions options;

    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final HostPasswordStore keychain
            = PasswordStoreFactory.get();

    @Outlet
    private NSView contentView;
    @Outlet
    private NSView optionsView;
    @Outlet
    private NSPopUpButton protocolPopup;
    @Outlet
    private NSTextField hostField;
    @Outlet
    private NSButton alertIcon;
    @Outlet
    private NSTextField portField;
    @Outlet
    private NSTextField pathField;
    @Outlet
    private NSTextField usernameField;
    @Outlet
    private NSTextField usernameLabel;
    @Outlet
    private NSSecureTextField passwordField;
    @Outlet
    private NSTextField passwordLabel;
    @Outlet
    private NSButton anonymousCheckbox;
    @Outlet
    private NSPopUpButton privateKeyPopup;
    @Outlet
    private NSOpenPanel privateKeyOpenPanel;
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
    @Outlet
    private NSPopUpButton ftpModePopup;
    @Outlet
    private NSTextField webURLField;
    @Outlet
    private NSButton webUrlImage;

    private NSWindow window;

    public BookmarkController(final Host bookmark, final LoginInputValidator validator, final LoginOptions options) {
        this.bookmark = bookmark;
        this.validator = validator;
        this.options = options;
    }

    @Override
    public void invalidate() {
        observers.clear();
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Bookmark";
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        this.update();
    }

    public void setWindow(final NSWindow window) {
        this.window = window;
    }

    public interface BookmarkObserver {
        void change(final Host bookmark);
    }

    /**
     * Notify all observers
     */
    protected void update() {
        for(BookmarkObserver observer : observers) {
            observer.change(bookmark);
        }
    }

    public void addObserver(final BookmarkObserver observer) {
        observers.add(observer);
    }

    public void focus(final NSWindow window) {
        if(bookmark.getProtocol().isHostnameConfigurable()) {
            window.makeFirstResponder(hostField);
        }
        else {
            if(options.user) {
                window.makeFirstResponder(usernameField);
            }
        }
    }

    @Override
    public NSView view() {
        return contentView;
    }

    public NSView getContentView() {
        return contentView;
    }

    public void setContentView(final NSView contentView) {
        this.contentView = contentView;
    }

    public NSView getOptionsView() {
        return optionsView;
    }

    public void setOptionsView(final NSView optionsView) {
        this.optionsView = optionsView;
    }

    public void setProtocolPopup(final NSPopUpButton button) {
        this.protocolPopup = button;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setAutoenablesItems(false);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionChanged:"));
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.ftp, Protocol.Type.sftp, Protocol.Type.dav, Protocol.Type.smb)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.s3, Protocol.Type.swift, Protocol.Type.azure, Protocol.Type.b2, Protocol.Type.googlestorage)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.dropbox, Protocol.Type.box, Protocol.Type.onedrive, Protocol.Type.googledrive, Protocol.Type.nextcloud, Protocol.Type.owncloud, Protocol.Type.dracoon, Protocol.Type.brick)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.file, Protocol.Type.none)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new ProfileProtocolPredicate())) {
            this.addProtocol(protocol);
        }
        this.addObserver(bookmark -> protocolPopup.selectItemAtIndex(protocolPopup.indexOfItemWithRepresentedObject(String.valueOf(bookmark.getProtocol().hashCode()))));
        if(HostPreferencesFactory.get(bookmark).getBoolean("preferences.profiles.enable")) {
            this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
            this.protocolPopup.addItemWithTitle(String.format("%s%s", LocaleFactory.localizedString("More Options", "Bookmark"), "…"));
        }
    }

    private void addProtocol(final Protocol protocol) {
        final String title = protocol.getDescription();
        this.protocolPopup.addItemWithTitle(title);
        this.protocolPopup.lastItem().setRepresentedObject(String.valueOf(protocol.hashCode()));
        this.protocolPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed(protocol.icon(), 16));
        if(protocol.isDeprecated()) {
            this.protocolPopup.lastItem().setEnabled(false);
        }
    }

    @Action
    public void protocolSelectionChanged(final NSPopUpButton sender) {
        if(null == sender.selectedItem().representedObject()) {
            final PreferencesController controller = PreferencesControllerFactory.instance();
            controller.display();
            controller.setSelectedPanel(PreferencesController.PreferencesToolbarItem.profiles.name());
        }
        else {
            final Protocol selected = protocols.forName(sender.selectedItem().representedObject());
            final String hostname = HostnameConfiguratorFactory.get(selected).getHostname(selected.getDefaultHostname());
            if(StringUtils.isNotBlank(hostname)) {
                // Prefill with default hostname
                bookmark.setHostname(hostname);
            }
            if(Objects.equals(bookmark.getDefaultPath(), bookmark.getProtocol().getDefaultPath()) || !selected.isPathConfigurable()) {
                bookmark.setDefaultPath(selected.getDefaultPath());
            }
            log.debug("Protocol selection changed to {}", selected);
            bookmark.setProtocol(selected);
            bookmark.setPort(HostnameConfiguratorFactory.get(selected).getPort(bookmark.getHostname()));
            bookmark.setCredentials(CredentialsConfiguratorFactory.get(selected).configure(bookmark));
            options.configure(selected);
            validator.configure(selected);
        }
        this.update();
    }

    public void setNicknameField(final NSTextField f) {
        this.nicknameField = f;
        this.nicknameField.superview().setHidden(!HostPreferencesFactory.get(bookmark).getBoolean("bookmark.name.configurable"));
        notificationCenter.addObserver(this.id(),
                Foundation.selector("nicknameFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                f.id());
        this.addObserver(bookmark -> updateField(nicknameField, BookmarkNameProvider.toString(bookmark)));
    }

    @Action
    public void nicknameFieldDidChange(final NSNotification sender) {
        bookmark.setNickname(nicknameField.stringValue());
        this.update();
    }

    public void setLabelsField(final NSTokenField f) {
        this.labelsField = f;
        this.labelsField.superview().setHidden(!HostPreferencesFactory.get(bookmark).getBoolean("bookmark.labels.configurable"));
        notificationCenter.addObserver(this.id(),
                Foundation.selector("tokenFieldDidChange:"),
                NSControl.NSControlTextDidEndEditingNotification,
                f.id());
        this.addObserver(bookmark -> {
            if(bookmark.getLabels().isEmpty()) {
                f.setObjectValue(NSArray.array());
            }
            else {
                f.setObjectValue(NSArray.arrayWithObjects(bookmark.getLabels().toArray(new String[bookmark.getLabels().size()])));
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

    public void setHostField(final NSTextField field) {
        this.hostField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("hostFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                field.id());
        this.addObserver(bookmark -> {
            updateField(hostField, bookmark.getHostname());
            hostField.setEnabled(bookmark.getProtocol().isHostnameConfigurable());
            hostField.cell().setPlaceholderString(bookmark.getProtocol().getHostnamePlaceholder());
        });
    }

    @Action
    public void hostFieldDidChange(final NSNotification sender) {
        final String input = hostField.stringValue();
        if(Scheme.isURL(input)) {
            try {
                final Host parsed = HostParser.parse(input);
                if(!bookmark.getProtocol().getScheme().equals(parsed.getProtocol().getScheme())) {
                    bookmark.setProtocol(parsed.getProtocol());
                }
                bookmark.setHostname(parsed.getHostname());
                bookmark.setPort(parsed.getPort());
                bookmark.setDefaultPath(parsed.getDefaultPath());
            }
            catch(HostParserException e) {
                e.getProtocol().ifPresent(p -> {
                    bookmark.setHostname("");
                    bookmark.setProtocol(p);
                });
                log.warn(e);
            }
        }
        else {
            bookmark.setHostname(input);
            bookmark.setCredentials(CredentialsConfiguratorFactory.get(bookmark.getProtocol()).configure(bookmark));
        }
        this.update();
    }

    public void setAlertIcon(final NSButton button) {
        this.alertIcon = button;
        this.alertIcon.setEnabled(false);
        this.alertIcon.setImage(null);
        this.alertIcon.setTarget(this.id());
        if(new ReachabilityDiagnosticsFactory().isAvailable()) {
            this.alertIcon.setAction(Foundation.selector("launchNetworkAssistant:"));
        }
        this.addObserver(new BookmarkObserver() {
            private final Reachability reachability = ReachabilityFactory.get();

            @Override
            public void change(final Host bookmark) {
                if(StringUtils.isNotBlank(bookmark.getHostname())) {
                    background(new AbstractBackgroundAction<Boolean>() {
                        boolean reachable = false;

                        @Override
                        public Boolean run() {
                            return reachable = reachability.isReachable(bookmark);
                        }

                        @Override
                        public void cleanup() {
                            alertIcon.setEnabled(!reachable);
                            alertIcon.setImage(reachable ? null : IconCacheFactory.<NSImage>get().iconNamed("NSCaution"));
                        }
                    });
                }
                else {
                    alertIcon.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSCaution"));
                    alertIcon.setEnabled(false);
                }
            }
        });
    }

    @Action
    public void launchNetworkAssistant(final NSButton sender) {
        ReachabilityDiagnosticsFactory.get().diagnose(bookmark);
    }

    public void setPortField(final NSTextField field) {
        this.portField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("portInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                portField.id());
        this.addObserver(bookmark -> {
            updateField(portField, String.valueOf(bookmark.getPort()));
            portField.setEnabled(bookmark.getProtocol().isPortConfigurable());
        });
    }

    @Action
    public void portInputDidChange(final NSNotification sender) {
        try {
            bookmark.setPort(Integer.parseInt(portField.stringValue()));
        }
        catch(NumberFormatException e) {
            bookmark.setPort(-1);
        }
        this.update();
    }

    public void setPathField(final NSTextField field) {
        this.pathField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("pathInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                pathField.id());
        this.addObserver(bookmark -> {
            updateField(pathField, bookmark.getDefaultPath());
            pathField.setEnabled(bookmark.getProtocol().isPathConfigurable());
            pathField.cell().setPlaceholderString(bookmark.getProtocol().getPathPlaceholder());
        });
    }

    @Action
    public void pathInputDidChange(final NSNotification sender) {
        bookmark.setDefaultPath(pathField.stringValue());
        this.update();
    }

    public void setUsernameField(final NSTextField field) {
        this.usernameField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("usernameFieldTextDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                usernameField.id());
        this.addObserver(bookmark -> {
            updateField(usernameField, bookmark.getCredentials().getUsername());
            usernameField.cell().setPlaceholderString(bookmark.getProtocol().getUsernamePlaceholder());
            usernameField.setEnabled(options.user && !bookmark.getCredentials().isAnonymousLogin());
        });
    }

    @Action
    public void usernameFieldTextDidEndEditing(final NSNotification sender) {
        bookmark.getCredentials().setUsername(StringUtils.trim(usernameField.stringValue()));
        this.update();
    }

    public void setUsernameLabel(final NSTextField label) {
        this.usernameLabel = label;
        this.addObserver(bookmark -> usernameLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                String.format("%s:", bookmark.getProtocol().getUsernamePlaceholder()),
                TRUNCATE_TAIL_ATTRIBUTES
        )));
    }

    public void setAnonymousCheckbox(final NSButton button) {
        this.anonymousCheckbox = button;
        this.anonymousCheckbox.setTarget(this.id());
        this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
        this.addObserver(bookmark -> {
            anonymousCheckbox.setEnabled(options.anonymous);
            anonymousCheckbox.setState(bookmark.getCredentials().isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
        });
    }

    @Action
    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.NSOnState) {
            bookmark.getCredentials().setUsername(HostPreferencesFactory.get(bookmark).getProperty("connection.login.anon.name"));
            bookmark.getCredentials().setPassword(HostPreferencesFactory.get(bookmark).getProperty("connection.login.anon.pass"));
        }
        if(sender.state() == NSCell.NSOffState) {
            if(HostPreferencesFactory.get(bookmark).getProperty("connection.login.name").equals(
                    HostPreferencesFactory.get(bookmark).getProperty("connection.login.anon.name"))) {
                bookmark.getCredentials().setUsername(StringUtils.EMPTY);
            }
            else {
                bookmark.getCredentials().setUsername(HostPreferencesFactory.get(bookmark).getProperty("connection.login.name"));
            }
            bookmark.getCredentials().setPassword(null);
        }
        this.update();
    }

    public void setPasswordField(final NSSecureTextField field) {
        this.passwordField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("passwordFieldTextDidEndEditing:"),
                NSControl.NSControlTextDidEndEditingNotification,
                field.id());
        this.addObserver(bookmark -> {
            passwordField.cell().setPlaceholderString(options.getPasswordPlaceholder());
            passwordField.setEnabled(options.password && !bookmark.getCredentials().isAnonymousLogin());
            if(options.password) {
                if(options.keychain) {
                    if(StringUtils.isBlank(bookmark.getHostname())) {
                        return;
                    }
                    if(StringUtils.isBlank(bookmark.getCredentials().getUsername())) {
                        return;
                    }
                    final String password = keychain.findLoginPassword(bookmark);
                    if(StringUtils.isNotBlank(password)) {
                        // Make sure password fetched from keychain and set in field is set in model
                        bookmark.getCredentials().setPassword(password);
                    }
                }
                updateField(passwordField, bookmark.getCredentials().getPassword());
            }
        });
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

    public void setPasswordLabel(final NSTextField passwordLabel) {
        this.passwordLabel = passwordLabel;
        this.addObserver(bookmark -> passwordLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                String.format("%s:", options.getPasswordPlaceholder()), TRUNCATE_TAIL_ATTRIBUTES
        )));
    }

    public void setCertificatePopup(final NSPopUpButton button) {
        this.certificatePopup = button;
        this.certificatePopup.setTarget(this.id());
        final Selector action = Foundation.selector("certificateSelectionChanged:");
        this.certificatePopup.setAction(action);
        this.addObserver(bookmark -> {
            certificatePopup.setEnabled(options.certificate);
            certificatePopup.removeAllItems();
            certificatePopup.addItemWithTitle(LocaleFactory.localizedString("None"));
            if(options.certificate) {
                certificatePopup.menu().addItem(NSMenuItem.separatorItem());
                for(String certificate : new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), bookmark,
                        CertificateStoreFactory.get()).list()) {
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
        timezones.sort(Comparator.comparing(o -> TimeZone.getTimeZone(o).getID()));
        for(String tz : timezones) {
            if(tz.matches(TIMEZONE_CONTINENT_PREFIXES)) {
                this.timezonePopup.addItemWithTitle(String.format("%s", tz));
                this.timezonePopup.lastItem().setRepresentedObject(tz);
            }
        }
        this.addObserver(bookmark -> {
            timezonePopup.setEnabled(!bookmark.getProtocol().isUTCTimezone());
            if(null == bookmark.getTimezone()) {
                if(bookmark.getProtocol().isUTCTimezone()) {
                    timezonePopup.setTitle(UTC.getID());
                }
                else {
                    timezonePopup.setTitle(TimeZone.getTimeZone(HostPreferencesFactory.get(bookmark).getProperty("ftp.timezone.default")).getID());
                }
            }
            else {
                timezonePopup.setTitle(bookmark.getTimezone().getID());
            }
            timezonePopup.superview().setHidden(bookmark.getProtocol().isUTCTimezone());
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
        this.addObserver(bookmark -> {
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

    public void setFtpModePopup(final NSPopUpButton button) {
        this.ftpModePopup = button;
        this.ftpModePopup.setTarget(this.id());
        this.ftpModePopup.setAction(Foundation.selector("ftpModePopupClicked:"));
        this.ftpModePopup.removeAllItems();
        for(FTPConnectMode m : FTPConnectMode.values()) {
            this.ftpModePopup.addItemWithTitle(m.toString());
            this.ftpModePopup.lastItem().setRepresentedObject(m.name());
            if(m.equals(FTPConnectMode.unknown)) {
                this.ftpModePopup.menu().addItem(NSMenuItem.separatorItem());
            }
        }
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(Host bookmark) {
                ftpModePopup.setEnabled(bookmark.getProtocol().getType() == Protocol.Type.ftp);
                ftpModePopup.selectItemAtIndex(ftpModePopup.indexOfItemWithRepresentedObject(bookmark.getFTPConnectMode().name()));
            }
        });
    }

    @Action
    public void ftpModePopupClicked(final NSPopUpButton sender) {
        bookmark.setFTPConnectMode(FTPConnectMode.valueOf(sender.selectedItem().representedObject()));
        this.update();
    }

    public void setWebURLField(final NSTextField field) {
        this.webURLField = field;
        final NSTextFieldCell cell = this.webURLField.cell();
        notificationCenter.addObserver(this.id(),
                Foundation.selector("webURLInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                field.id());
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(Host bookmark) {
                updateField(webURLField, bookmark.getWebURL());
                cell.setPlaceholderString(new DefaultWebUrlProvider().toUrl(bookmark).getUrl());
            }
        });
    }

    @Action
    public void webURLInputDidChange(final NSNotification sender) {
        bookmark.setWebURL(webURLField.stringValue());
        this.update();
    }

    public void setWebUrlImage(final NSButton button) {
        this.webUrlImage = button;
        this.webUrlImage.setTarget(this.id());
        this.webUrlImage.setAction(Foundation.selector("webUrlButtonClicked:"));
        this.webUrlImage.setImage(IconCacheFactory.<NSImage>get().iconNamed("site.tiff", 16));
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(Host bookmark) {
                if(HostPreferencesFactory.get(bookmark).getBoolean("bookmark.favicon.download")) {
                    background(new AbstractBackgroundAction<NSImage>() {
                        @Override
                        public NSImage run() {
                            final NSImage favicon;
                            final String f = bookmark.getProtocol().favicon();
                            if(StringUtils.isNotBlank(f)) {
                                favicon = IconCacheFactory.<NSImage>get().iconNamed(f, 16);
                            }
                            else {
                                String url = String.format("%sfavicon.ico", new DefaultWebUrlProvider().toUrl(bookmark).getUrl());
                                // Default favicon location
                                final NSData data = NSData.dataWithContentsOfURL(NSURL.URLWithString(url));
                                if(null == data) {
                                    return null;
                                }
                                favicon = NSImage.imageWithData(data);
                            }
                            if(null != favicon) {
                                favicon.setSize(new NSSize(16, 16));
                            }
                            return favicon;
                        }

                        @Override
                        public void cleanup(final NSImage favicon, final BackgroundException failure) {
                            if(null != favicon) {
                                webUrlImage.setImage(favicon);
                            }
                        }
                    });
                }
                webUrlImage.setToolTip(new DefaultWebUrlProvider().toUrl(bookmark).getUrl());
            }
        });
    }

    @Action
    public void webUrlButtonClicked(final NSButton sender) {
        BrowserLauncherFactory.get().open(new DefaultWebUrlProvider().toUrl(bookmark).getUrl());
    }

    public void setPrivateKeyPopup(final NSPopUpButton button) {
        this.privateKeyPopup = button;
        this.privateKeyPopup.setTarget(this.id());
        final Selector action = Foundation.selector("privateKeyPopupClicked:");
        this.privateKeyPopup.setAction(action);
        this.privateKeyPopup.removeAllItems();
        this.privateKeyPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        this.privateKeyPopup.lastItem().setRepresentedObject(StringUtils.EMPTY);
        this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Local key : new OpenSSHPrivateKeyConfigurator().list()) {
            this.privateKeyPopup.addItemWithTitle(key.getAbbreviatedPath());
            this.privateKeyPopup.lastItem().setRepresentedObject(key.getAbsolute());
        }
        // Choose another folder
        this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
        this.privateKeyPopup.addItemWithTitle(String.format("%s…", LocaleFactory.localizedString("Choose")));
        this.addObserver(bookmark -> {
            privateKeyPopup.setEnabled(options.publickey);
            if(bookmark.getCredentials().isPublicKeyAuthentication()) {
                privateKeyPopup.selectItemAtIndex(privateKeyPopup.indexOfItemWithRepresentedObject(bookmark.getCredentials().getIdentity().getAbsolute()));
            }
            else {
                privateKeyPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
            }
            if(bookmark.getCredentials().isPublicKeyAuthentication()) {
                final Local key = bookmark.getCredentials().getIdentity();
                if(-1 == privateKeyPopup.indexOfItemWithRepresentedObject(key.getAbsolute()).intValue()) {
                    final NSInteger index = new NSInteger(0);
                    privateKeyPopup.insertItemWithTitle_atIndex(key.getAbbreviatedPath(), index);
                    privateKeyPopup.itemAtIndex(index).setRepresentedObject(key.getAbsolute());
                }
            }
        });
    }

    @Action
    public void privateKeyPopupClicked(final NSPopUpButton sender) {
        final String selected = sender.selectedItem().representedObject();
        if(null == selected) {
            privateKeyOpenPanel = NSOpenPanel.openPanel();
            privateKeyOpenPanel.setCanChooseDirectories(false);
            privateKeyOpenPanel.setCanChooseFiles(true);
            privateKeyOpenPanel.setAllowsMultipleSelection(false);
            privateKeyOpenPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
            privateKeyOpenPanel.setPrompt(LocaleFactory.localizedString("Choose"));
            privateKeyOpenPanel.beginSheetForDirectory(LocalFactory.get("~/.ssh").getAbsolute(), null, window, this.id(),
                    Foundation.selector("privateKeyPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            bookmark.getCredentials().setIdentity(StringUtils.isBlank(selected) ? null : LocalFactory.get(selected));
        }
        this.update();
    }

    @Action
    public void privateKeyPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
        switch(returncode) {
            case SheetCallback.DEFAULT_OPTION:
                final NSObject url = privateKeyOpenPanel.URLs().lastObject();
                if(url != null) {
                    final Local selected = LocalFactory.get(Rococoa.cast(url, NSURL.class).path());
                    selected.setBookmark(FilesystemBookmarkResolverFactory.get().create(selected));
                    bookmark.getCredentials().setIdentity(selected);
                }
                break;
            case SheetCallback.ALTERNATE_OPTION:
                bookmark.getCredentials().setIdentity(null);
                break;
        }
        this.update();
    }
}
