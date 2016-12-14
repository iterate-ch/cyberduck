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
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.CollectionListener;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHPrivateKeyConfigurator;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.InputValidator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.ArrayList;
import java.util.List;

public class BookmarkController extends SheetController implements CollectionListener {
    private static final Logger log = Logger.getLogger(BookmarkController.class);

    private static NSPoint cascade = new NSPoint(0, 0);

    protected final Preferences preferences
            = PreferencesFactory.get();

    protected final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final List<BookmarkObserver> observers = new ArrayList<>();

    protected final Host bookmark;

    protected final Credentials credentials;

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
    protected NSButton anonymousCheckbox;
    @Outlet
    protected NSPopUpButton privateKeyPopup;
    @Outlet
    protected NSOpenPanel privateKeyOpenPanel;

    /**
     * @param bookmark The bookmark to edit
     */
    public BookmarkController(final Host bookmark) {
        this(bookmark, bookmark.getCredentials());
    }

    public BookmarkController(final Host bookmark, final Credentials credentials) {
        this(bookmark, credentials, new InputValidator() {
            @Override
            public boolean validate() {
                return true;
            }
        });
    }

    public BookmarkController(final Host bookmark, final Credentials credentials, final InputValidator validator) {
        super(validator);
        this.bookmark = bookmark;
        this.credentials = credentials;
    }

    public Host getBookmark() {
        return bookmark;
    }

    public void setProtocolPopup(final NSPopUpButton button) {
        this.protocolPopup = button;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionChanged:"));
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : ProtocolFactory.getEnabledProtocols()) {
            final String title = protocol.getDescription();
            this.protocolPopup.addItemWithTitle(title);
            this.protocolPopup.lastItem().setRepresentedObject(String.valueOf(protocol.hashCode()));
            this.protocolPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed(protocol.icon(), 16));
        }
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                protocolPopup.selectItemAtIndex(protocolPopup.indexOfItemWithRepresentedObject(String.valueOf(bookmark.getProtocol().hashCode())));
            }
        });
    }

    @Action
    public void protocolSelectionChanged(final NSPopUpButton sender) {
        final Protocol selected = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
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
        bookmark.setProtocol(selected);
        this.update();
    }

    public void setHostField(final NSTextField field) {
        this.hostField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("hostFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                field);
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(hostField, bookmark.getHostname());
                hostField.setEnabled(bookmark.getProtocol().isHostnameConfigurable());
                hostField.cell().setPlaceholderString(bookmark.getProtocol().getDefaultHostname());
            }
        });
    }

    @Action
    public void hostFieldDidChange(final NSNotification sender) {
        final String input = hostField.stringValue();
        if(Scheme.isURL(input)) {
            final Host parsed = HostParser.parse(input);
            bookmark.setHostname(parsed.getHostname());
            bookmark.setProtocol(parsed.getProtocol());
            bookmark.setPort(parsed.getPort());
            bookmark.setDefaultPath(parsed.getDefaultPath());
        }
        else {
            bookmark.setHostname(input);
        }
        this.update();
    }

    public void setAlertIcon(final NSButton button) {
        this.alertIcon = button;
        this.alertIcon.setEnabled(false);
        this.alertIcon.setImage(null);
        this.alertIcon.setTarget(this.id());
        this.alertIcon.setAction(Foundation.selector("launchNetworkAssistant:"));
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                if(StringUtils.isNotBlank(bookmark.getHostname())) {
                    background(new AbstractBackgroundAction<Boolean>() {
                        boolean reachable = false;

                        @Override
                        public Boolean run() throws BackgroundException {
                            if(!preferences.getBoolean("connection.hostname.check")) {
                                return reachable = true;
                            }
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
        ReachabilityFactory.get().diagnose(bookmark);
    }

    public void setPortField(final NSTextField field) {
        this.portField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("portInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.portField);
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
            bookmark.setPort(Integer.valueOf(portField.stringValue()));
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
                this.pathField);
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(pathField, bookmark.getDefaultPath());
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
                urlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(new HostUrlProvider(true, true).get(bookmark)));
            }
        });
    }

    public void setUsernameField(final NSTextField field) {
        this.usernameField = field;
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("usernameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.usernameField);
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                updateField(usernameField, credentials.getUsername());
                usernameField.cell().setPlaceholderString(bookmark.getProtocol().getUsernamePlaceholder());
                usernameField.setEnabled(!credentials.isAnonymousLogin());
            }
        });
    }

    @Action
    public void usernameInputDidChange(final NSNotification sender) {
        credentials.setUsername(usernameField.stringValue());
        this.update();
    }

    public void setUsernameLabel(final NSTextField usernameLabel) {
        this.usernameLabel = usernameLabel;
        this.addObserver(new BookmarkObserver() {
            @Override
            public void change(final Host bookmark) {
                usernameLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        StringUtils.isNotBlank(credentials.getUsernamePlaceholder()) ? String.format("%s:",
                                credentials.getUsernamePlaceholder()) : StringUtils.EMPTY,
                        LABEL_ATTRIBUTES
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
                anonymousCheckbox.setEnabled(bookmark.getProtocol().isAnonymousConfigurable());
                anonymousCheckbox.setState(credentials.isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
            }
        });
    }

    @Action
    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.NSOnState) {
            credentials.setUsername(preferences.getProperty("connection.login.anon.name"));
            credentials.setPassword(preferences.getProperty("connection.login.anon.pass"));
        }
        if(sender.state() == NSCell.NSOffState) {
            if(preferences.getProperty("connection.login.name").equals(
                    preferences.getProperty("connection.login.anon.name"))) {
                credentials.setUsername(StringUtils.EMPTY);
            }
            else {
                credentials.setUsername(preferences.getProperty("connection.login.name"));
            }
            credentials.setPassword(null);
        }
        this.update();
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
        window.makeFirstResponder(hostField);
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
                privateKeyPopup.setEnabled(bookmark.getProtocol().getType() == Protocol.Type.sftp);
                if(credentials.isPublicKeyAuthentication()) {
                    privateKeyPopup.selectItemAtIndex(privateKeyPopup.indexOfItemWithRepresentedObject(credentials.getIdentity().getAbsolute()));
                }
                else {
                    privateKeyPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
                }
                if(credentials.isPublicKeyAuthentication()) {
                    final Local key = credentials.getIdentity();
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
    public void privateKeyPopupClicked(final NSMenuItem sender) {
        final String selected = sender.representedObject();
        if(null == selected) {
            privateKeyOpenPanel = NSOpenPanel.openPanel();
            privateKeyOpenPanel.setCanChooseDirectories(false);
            privateKeyOpenPanel.setCanChooseFiles(true);
            privateKeyOpenPanel.setAllowsMultipleSelection(false);
            privateKeyOpenPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
            privateKeyOpenPanel.setPrompt(LocaleFactory.localizedString("Choose"));
            privateKeyOpenPanel.beginSheetForDirectory(OpenSSHPrivateKeyConfigurator.OPENSSH_CONFIGURATION_DIRECTORY.getAbsolute(), null, this.window(), this.id(),
                    Foundation.selector("privateKeyPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            credentials.setIdentity(StringUtils.isBlank(selected) ? null : LocalFactory.get(selected));
        }
        this.update();
    }

    public void privateKeyPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
        switch(returncode) {
            case SheetCallback.DEFAULT_OPTION:
                final NSObject selected = privateKeyOpenPanel.filenames().lastObject();
                if(selected != null) {
                    final Local key = LocalFactory.get(selected.toString());
                    credentials.setIdentity(key);
                }
                break;
            case SheetCallback.ALTERNATE_OPTION:
                credentials.setIdentity(null);
                break;
        }
        this.update();
    }

    @Override
    @Action
    public void helpButtonClicked(final ID sender) {
        new DefaultProviderHelpService().help(bookmark.getProtocol());
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
                window.close();
            }
        }
    }

    @Override
    public void collectionItemChanged(Object item) {
        this.update();
    }

    public interface BookmarkObserver {
        void change(final Host bookmark);
    }
}
