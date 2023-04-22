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
import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.*;
import ch.cyberduck.core.diagnostics.ReachabilityDiagnosticsFactory;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHPrivateKeyConfigurator;
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
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class BookmarkController extends SheetController implements CollectionListener {
    private static final Logger log = LogManager.getLogger(BookmarkController.class);

    private static NSPoint cascade = new NSPoint(0, 0);

    protected final Preferences preferences
            = PreferencesFactory.get();

    protected final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final ProtocolFactory protocols = ProtocolFactory.get();

    private final List<BookmarkObserver> observers = new ArrayList<>();

    protected final Host bookmark;

    protected final LoginInputValidator validator;
    protected final LoginOptions options;

    private final HostPasswordStore keychain
            = PasswordStoreFactory.get();

    @Outlet
    protected NSPopUpButton protocolPopup;
    @Outlet
    protected NSTextField hostField;
    @Outlet
    protected NSButton alertIcon;
    @Outlet
    protected NSTextField portField;
    @Outlet
    protected NSTextField pathField;
    @Outlet
    protected NSTextField urlField;
    @Outlet
    protected NSTextField usernameField;
    @Outlet
    protected NSTextField usernameLabel;
    @Outlet
    protected NSTextField passwordField;
    @Outlet
    private NSTextField passwordLabel;
    @Outlet
    protected NSButton anonymousCheckbox;
    @Outlet
    protected NSPopUpButton privateKeyPopup;
    @Outlet
    protected NSOpenPanel privateKeyOpenPanel;

    /**
     * @param bookmark The bookmark to edit
     */
    public BookmarkController(final Host bookmark) {
        this(bookmark, new LoginOptions(bookmark.getProtocol()));
    }

    public BookmarkController(final Host bookmark, final LoginOptions options) {
        this(bookmark, new LoginInputValidator(bookmark, options), options);
    }

    public BookmarkController(final Host bookmark, final LoginInputValidator validator, final LoginOptions options) {
        super(validator);
        this.bookmark = bookmark;
        this.validator = validator;
        this.options = options;
    }

    public Host getBookmark() {
        return bookmark;
    }

    public void setProtocolPopup(final NSPopUpButton button) {
        this.protocolPopup = button;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setAutoenablesItems(false);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionChanged:"));
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                // Reload protocols which may have changed due to profile selection in Preferences
                loadProtocols();
                protocolPopup.selectItemAtIndex(protocolPopup.indexOfItemWithRepresentedObject(String.valueOf(bookmark.getProtocol().hashCode())));
            }
        });
    }

    private void loadProtocols() {
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.ftp, Protocol.Type.sftp, Protocol.Type.dav, Protocol.Type.smb)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.s3, Protocol.Type.swift, Protocol.Type.azure, Protocol.Type.b2, Protocol.Type.googlestorage)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.dropbox, Protocol.Type.box, Protocol.Type.onedrive, Protocol.Type.googledrive, Protocol.Type.nextcloud, Protocol.Type.owncloud, Protocol.Type.dracoon, Protocol.Type.brick, Protocol.Type.smb)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(EnumSet.of(Protocol.Type.file)))) {
            this.addProtocol(protocol);
        }
        this.protocolPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new ProfileProtocolPredicate())) {
            this.addProtocol(protocol);
        }
        if(preferences.getBoolean("preferences.profiles.enable")) {
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
            controller.window().makeKeyAndOrderFront(null);
            controller.setSelectedPanel(PreferencesController.PreferencesToolbarItem.profiles.name());
        }
        else {
            final Protocol selected = ProtocolFactory.get().forName(sender.selectedItem().representedObject());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Protocol selection changed to %s", selected));
            }
            bookmark.setPort(selected.getDefaultPort());
            if(!bookmark.getProtocol().isHostnameConfigurable()) {
                // Previously selected protocol had a default hostname. Change to default
                // of newly selected protocol.
                bookmark.setHostname(selected.getDefaultHostname());
            }
            if(!selected.isHostnameConfigurable()) {
                // Hostname of newly selected protocol is not configurable. Change to default.
                bookmark.setHostname(selected.getDefaultHostname());
            }
            if(StringUtils.isNotBlank(selected.getDefaultHostname())) {
                // Prefill with default hostname
                bookmark.setHostname(selected.getDefaultHostname());
            }
            if(Objects.equals(bookmark.getDefaultPath(), bookmark.getProtocol().getDefaultPath()) ||
                    !selected.isPathConfigurable()) {
                bookmark.setDefaultPath(selected.getDefaultPath());
            }
            bookmark.setProtocol(selected);
            final int port = HostnameConfiguratorFactory.get(selected).getPort(bookmark.getHostname());
            if(port != -1) {
                // External configuration found
                bookmark.setPort(port);
            }
            options.configure(selected);
            validator.configure(selected);
        }
        this.update();
    }

    public void setHostField(final NSTextField field) {
        this.hostField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("hostFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                field.id());
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(hostField, bookmark.getHostname());
                hostField.setEnabled(bookmark.getProtocol().isHostnameConfigurable());
                hostField.cell().setPlaceholderString(bookmark.getProtocol().getHostnamePlaceholder());
            }
        });
    }

    @Action
    public void hostFieldDidChange(final NSNotification sender) {
        final String input = hostField.stringValue();
        if(Scheme.isURL(input)) {
            try {
                final Host parsed = HostParser.parse(input);
                bookmark.setHostname(parsed.getHostname());
                bookmark.setProtocol(parsed.getProtocol());
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
            @Override
            public void change(final Host bookmark) {
                if(StringUtils.isNotBlank(bookmark.getHostname())) {
                    background(new AbstractBackgroundAction<Boolean>() {
                        boolean reachable = false;

                        @Override
                        public Boolean run() {
                            return reachable = ReachabilityFactory.get().isReachable(bookmark);
                        }

                        @Override
                        public void cleanup() {
                            alertIcon.setEnabled(!reachable);
                            alertIcon.setImage(reachable ? null : IconCacheFactory.<NSImage>get().iconNamed("alert.tiff"));
                        }
                    });
                }
                else {
                    alertIcon.setImage(IconCacheFactory.<NSImage>get().iconNamed("alert.tiff"));
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
                field.id());
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(portField, String.valueOf(bookmark.getPort()));
                portField.setEnabled(bookmark.getProtocol().isPortConfigurable());
            }
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

    public void setPathField(NSTextField field) {
        this.pathField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("pathInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                field.id());
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(pathField, bookmark.getDefaultPath());
                pathField.setEnabled(bookmark.getProtocol().isPathConfigurable());
            }
        });
    }

    @Action
    public void pathInputDidChange(final NSNotification sender) {
        bookmark.setDefaultPath(pathField.stringValue());
        this.update();
    }

    public void setUrlField(final NSTextField field) {
        this.urlField = field;
        this.urlField.setAllowsEditingTextAttributes(true);
        this.urlField.setSelectable(true);
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                urlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(new HostUrlProvider().withUsername(false).withPath(true).get(bookmark)));
            }
        });
    }

    public void setUsernameField(final NSTextField field) {
        this.usernameField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("usernameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                field.id());
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(usernameField, bookmark.getCredentials().getUsername());
                usernameField.cell().setPlaceholderString(bookmark.getProtocol().getUsernamePlaceholder());
                usernameField.setEnabled(options.user && !bookmark.getCredentials().isAnonymousLogin());
            }
        });
    }

    @Action
    public void usernameInputDidChange(final NSNotification sender) {
        bookmark.getCredentials().setUsername(StringUtils.trim(usernameField.stringValue()));
        this.update();
    }

    public void setUsernameLabel(final NSTextField usernameLabel) {
        this.usernameLabel = usernameLabel;
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                usernameLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        String.format("%s:", bookmark.getProtocol().getUsernamePlaceholder()),
                        TRUNCATE_TAIL_ATTRIBUTES
                ));
            }
        });
    }

    public void setAnonymousCheckbox(final NSButton button) {
        this.anonymousCheckbox = button;
        this.anonymousCheckbox.setTarget(this.id());
        this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                anonymousCheckbox.setEnabled(options.anonymous);
                anonymousCheckbox.setState(bookmark.getCredentials().isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
            }
        });
    }

    @Action
    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.NSOnState) {
            bookmark.getCredentials().setUsername(preferences.getProperty("connection.login.anon.name"));
            bookmark.getCredentials().setPassword(preferences.getProperty("connection.login.anon.pass"));
        }
        if(sender.state() == NSCell.NSOffState) {
            if(preferences.getProperty("connection.login.name").equals(
                    preferences.getProperty("connection.login.anon.name"))) {
                bookmark.getCredentials().setUsername(StringUtils.EMPTY);
            }
            else {
                bookmark.getCredentials().setUsername(preferences.getProperty("connection.login.name"));
            }
            bookmark.getCredentials().setPassword(null);
        }
        this.update();
    }

    public void setPasswordField(NSSecureTextField field) {
        this.passwordField = field;
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
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
                        try {
                            final String password = keychain.getPassword(bookmark.getProtocol().getScheme(),
                                    bookmark.getPort(),
                                    bookmark.getHostname(),
                                    bookmark.getCredentials().getUsername());
                            if(StringUtils.isNotBlank(password)) {
                                // Make sure password fetched from keychain and set in field is set in model
                                bookmark.getCredentials().setPassword(password);
                            }
                        }
                        catch(LocalAccessDeniedException e) {
                            // Ignore
                        }
                    }
                    updateField(passwordField, bookmark.getCredentials().getPassword());
                }
            }
        });
    }

    public void setPasswordLabel(NSTextField passwordLabel) {
        this.passwordLabel = passwordLabel;
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                passwordLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        String.format("%s:", options.getPasswordPlaceholder()), TRUNCATE_TAIL_ATTRIBUTES
                ));
            }
        });
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
        if(bookmark.getProtocol().isHostnameConfigurable()) {
            window.makeFirstResponder(hostField);
        }
        else {
            if(options.user) {
                window.makeFirstResponder(usernameField);
            }
        }
        this.update();
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setContentMinSize(window.frame().size);
        window.setContentMaxSize(new NSSize(600, window.frame().size.height.doubleValue()));
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                window.setTitle(BookmarkNameProvider.toString(bookmark));
            }
        });
        super.setWindow(window);
        cascade = this.cascade(cascade);
    }

    @Override
    public void windowWillClose(final NSNotification notification) {
        cascade = new NSPoint(this.window().frame().origin.x.doubleValue(),
                this.window().frame().origin.y.doubleValue() + this.window().frame().size.height.doubleValue());
        super.windowWillClose(notification);
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
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
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
            privateKeyOpenPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "bookmark.getCredentials()"));
            privateKeyOpenPanel.setPrompt(LocaleFactory.localizedString("Choose"));
            privateKeyOpenPanel.beginSheetForDirectory(LocalFactory.get(LocalFactory.get(), ".ssh").getAbsolute(), null, this.window(), this.id(),
                    Foundation.selector("privateKeyPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            bookmark.getCredentials().setIdentity(StringUtils.isBlank(selected) ? null : LocalFactory.get(selected));
        }
        this.update();
    }

    public void privateKeyPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
        switch(returncode) {
            case SheetCallback.DEFAULT_OPTION:
                final NSObject selected = privateKeyOpenPanel.URLs().lastObject();
                if(selected != null) {
                    final Local key = LocalFactory.get(Rococoa.cast(selected, NSURL.class).path());
                    bookmark.getCredentials().setIdentity(key);
                }
                break;
            case SheetCallback.ALTERNATE_OPTION:
                bookmark.getCredentials().setIdentity(null);
                break;
        }
        this.update();
    }

    @Override
    @Action
    public void helpButtonClicked(final ID sender) {
        BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help(bookmark.getProtocol()));
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
        observers.add(0, observer);
    }

    @Override
    public void collectionLoaded() {
        //
    }

    @Override
    public void collectionItemAdded(Object item) {
        //
    }

    @Override
    public void collectionItemRemoved(Object item) {
        if(item.equals(bookmark)) {
            final NSWindow window = window();
            if(null != window) {
                window.orderOut(null);
            }
        }
    }

    @Override
    public void collectionItemChanged(Object item) {
        this.update();
    }

    @Override
    public void callback(final int returncode) {
        //
    }

    public interface BookmarkObserver {
        void change(final Host bookmark);
    }

}
