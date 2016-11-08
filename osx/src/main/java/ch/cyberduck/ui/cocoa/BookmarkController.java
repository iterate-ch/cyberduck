package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.DefaultCharsetProvider;
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
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class BookmarkController extends WindowController {
    private static final Logger log = Logger.getLogger(BookmarkController.class);

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final String TIMEZONE_CONTINENT_PREFIXES =
            "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

    private static final String CHOOSE = LocaleFactory.localizedString("Choose") + "…";

    private static NSPoint cascade = new NSPoint(0, 0);

    private final Preferences preferences = PreferencesFactory.get();

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    private final BookmarkCollection collection = BookmarkCollection.defaultCollection();
    /**
     * The bookmark
     */
    private final Host host;
    private final AbstractCollectionListener<Host> bookmarkCollectionListener = new AbstractCollectionListener<Host>() {
        @Override
        public void collectionItemRemoved(Host item) {
            if(item.equals(host)) {
                final NSWindow window = window();
                if(null != window) {
                    window.close();
                }
            }
        }
    };
    @Outlet
    private NSPopUpButton protocolPopup;
    @Outlet
    private NSPopUpButton encodingPopup;
    @Outlet
    private NSTextField nicknameField;
    @Outlet
    private NSTextField hostField;
    @Outlet
    private NSButton alertIcon;
    @Outlet
    private NSTextField portField;
    @Outlet
    private NSTextField pathField;
    @Outlet
    private NSTextField urlField;
    @Outlet
    private NSTextField usernameField;
    @Outlet
    private NSTextField usernameLabel;
    @Outlet
    private NSButton anonymousCheckbox;
    @Outlet
    private NSTextField webURLField;
    @Outlet
    private NSButton webUrlImage;
    @Outlet
    private NSImage favicon;
    @Outlet
    private NSTextView commentField;
    @Outlet
    private NSPopUpButton timezonePopup;
    @Outlet
    private NSPopUpButton connectmodePopup;
    @Outlet
    private NSPopUpButton transferPopup;
    @Outlet
    private NSPopUpButton downloadPathPopup;
    @Outlet
    private NSOpenPanel downloadPathPanel;
    @Outlet
    private NSButton toggleOptionsButton;
    @Outlet
    private NSTextField pkLabel;
    @Outlet
    private NSButton pkCheckbox;
    @Outlet
    private NSOpenPanel publicKeyPanel;

    /**
     * @param host The bookmark to edit
     */
    public BookmarkController(final Host host) {
        this.host = host;
        // Register for bookmark delete event. Will close this window.
        collection.addListener(bookmarkCollectionListener);
        this.loadBundle();
    }

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionChanged:"));
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : ProtocolFactory.getEnabledProtocols()) {
            final String title = protocol.getDescription();
            this.protocolPopup.addItemWithTitle(title);
            final NSMenuItem item = this.protocolPopup.itemWithTitle(title);
            item.setRepresentedObject(String.valueOf(protocol.hashCode()));
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed(protocol.icon(), 16));
        }
    }

    @Action
    public void protocolSelectionChanged(final NSPopUpButton sender) {
        final Protocol selected = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Protocol selection changed to %s", selected));
        }
        host.setPort(selected.getDefaultPort());
        if(!host.getProtocol().isHostnameConfigurable()) {
            // Previously selected protocol had a default hostname. Change to default
            // of newly selected protocol.
            host.setHostname(selected.getDefaultHostname());
        }
        if(!selected.isHostnameConfigurable()) {
            // Hostname of newly selected protocol is not configurable. Change to default.
            host.setHostname(selected.getDefaultHostname());
        }
        if(StringUtils.isNotBlank(selected.getDefaultHostname())) {
            // Prefill with default hostname
            host.setHostname(selected.getDefaultHostname());
        }
        host.setProtocol(selected);
        this.itemChanged();
        this.init();
        this.reachable();
    }

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemWithTitle(DEFAULT);
        this.encodingPopup.menu().addItem(NSMenuItem.separatorItem());
        this.encodingPopup.addItemsWithTitles(NSArray.arrayWithObjects(new DefaultCharsetProvider().availableCharsets()));
        if(null == host.getEncoding()) {
            this.encodingPopup.selectItemWithTitle(DEFAULT);
        }
        else {
            this.encodingPopup.selectItemWithTitle(host.getEncoding());
        }
        this.encodingPopup.setTarget(this.id());
        final Selector action = Foundation.selector("encodingSelectionChanged:");
        this.encodingPopup.setAction(action);
    }

    @Action
    public void encodingSelectionChanged(final NSPopUpButton sender) {
        if(sender.selectedItem().title().equals(DEFAULT)) {
            host.setEncoding(null);
        }
        else {
            host.setEncoding(sender.selectedItem().title());
        }
        this.itemChanged();
    }

    public void setNicknameField(NSTextField nicknameField) {
        this.nicknameField = nicknameField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("nicknameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.nicknameField);
    }

    public void setHostField(NSTextField hostField) {
        this.hostField = hostField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("hostFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                hostField);
    }

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setEnabled(false);
        this.alertIcon.setImage(null);
        this.alertIcon.setTarget(this.id());
        this.alertIcon.setAction(Foundation.selector("launchNetworkAssistant:"));
    }

    @Action
    public void launchNetworkAssistant(final NSButton sender) {
        ReachabilityFactory.get().diagnose(host);
    }

    public void setPortField(NSTextField portField) {
        this.portField = portField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("portInputDidEndEditing:"),
                NSControl.NSControlTextDidChangeNotification,
                this.portField);
    }

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("pathInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.pathField);
    }

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
        this.urlField.setAllowsEditingTextAttributes(true);
        this.urlField.setSelectable(true);
    }

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("usernameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.usernameField);
    }

    public void setUsernameLabel(NSTextField usernameLabel) {
        this.usernameLabel = usernameLabel;
    }

    public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
        this.anonymousCheckbox = anonymousCheckbox;
        this.anonymousCheckbox.setTarget(this.id());
        this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
        this.anonymousCheckbox.setState(NSCell.NSOffState);
    }

    public void setWebURLField(NSTextField webURLField) {
        this.webURLField = webURLField;
        final NSTextFieldCell cell = this.webURLField.cell();
        cell.setPlaceholderString(host.getDefaultWebURL());
        notificationCenter.addObserver(this.id(),
                Foundation.selector("webURLInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.webURLField);
    }

    public void setWebUrlImage(NSButton b) {
        this.webUrlImage = b;
        this.webUrlImage.setTarget(this.id());
        this.webUrlImage.setAction(Foundation.selector("openWebUrl:"));
        this.webUrlImage.setImage(IconCacheFactory.<NSImage>get().iconNamed("site.tiff", 16));
    }

    /**
     *
     */
    private void updateFavicon() {
        if(preferences.getBoolean("bookmark.favicon.download")) {
            this.background(new AbstractBackgroundAction<Void>() {
                @Override
                public Void run() throws BackgroundException {
                    final String f = host.getProtocol().favicon();
                    if(StringUtils.isNotBlank(f)) {
                        favicon = IconCacheFactory.<NSImage>get().iconNamed(f, 16);
                    }
                    else {
                        String url = host.getWebURL() + "/favicon.ico";
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
                    return null;
                }

                @Override
                public void cleanup() {
                    if(null != favicon) {
                        webUrlImage.setImage(favicon);
                    }
                }

                @Override
                public Object lock() {
                    return host;
                }
            });
        }
    }

    @Action
    public void openWebUrl(final NSButton sender) {
        BrowserLauncherFactory.get().open(host.getWebURL());
    }

    public void setCommentField(NSTextView commentField) {
        this.commentField = commentField;
        this.commentField.setFont(NSFont.userFixedPitchFontOfSize(11f));
        notificationCenter.addObserver(this.id(),
                Foundation.selector("commentInputDidChange:"),
                NSText.TextDidChangeNotification,
                this.commentField);
    }

    public void setTimezonePopup(NSPopUpButton timezonePopup) {
        this.timezonePopup = timezonePopup;
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
    }

    @Action
    public void timezonePopupClicked(NSPopUpButton sender) {
        String selected = sender.selectedItem().representedObject();
        String[] ids = TimeZone.getAvailableIDs();
        for(String id : ids) {
            TimeZone tz;
            if((tz = TimeZone.getTimeZone(id)).getID().equals(selected)) {
                host.setTimezone(tz);
                break;
            }
        }
        this.itemChanged();
    }

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.setTarget(this.id());
        this.connectmodePopup.setAction(Foundation.selector("connectmodePopupClicked:"));
        this.connectmodePopup.removeAllItems();
        for(FTPConnectMode m : FTPConnectMode.values()) {
            this.connectmodePopup.addItemWithTitle(m.toString());
            this.connectmodePopup.lastItem().setRepresentedObject(m.name());
            if(m.equals(FTPConnectMode.unknown)) {
                this.connectmodePopup.menu().addItem(NSMenuItem.separatorItem());
            }
        }
    }

    @Action
    public void connectmodePopupClicked(final NSPopUpButton sender) {
        host.setFTPConnectMode(FTPConnectMode.valueOf(sender.selectedItem().representedObject()));
        this.itemChanged();
    }

    public void setTransferPopup(NSPopUpButton transferPopup) {
        this.transferPopup = transferPopup;
        this.transferPopup.setTarget(this.id());
        this.transferPopup.setAction(Foundation.selector("transferPopupClicked:"));
        this.transferPopup.removeAllItems();
        final Host.TransferType unknown = Host.TransferType.unknown;
        this.transferPopup.addItemWithTitle(unknown.toString());
        this.transferPopup.lastItem().setRepresentedObject(unknown.name());
        this.transferPopup.menu().addItem(NSMenuItem.separatorItem());
        for(String name : preferences.getList("queue.transfer.type.enabled")) {
            final Host.TransferType t = Host.TransferType.valueOf(name);
            this.transferPopup.addItemWithTitle(t.toString());
            this.transferPopup.lastItem().setRepresentedObject(t.name());
        }
    }

    @Action
    public void transferPopupClicked(final NSPopUpButton sender) {
        host.setTransfer(Host.TransferType.valueOf(sender.selectedItem().representedObject()));
        this.itemChanged();
    }

    public void setDownloadPathPopup(NSPopUpButton downloadPathPopup) {
        this.downloadPathPopup = downloadPathPopup;
        this.downloadPathPopup.setTarget(this.id());
        final Selector action = Foundation.selector("downloadPathPopupClicked:");
        this.downloadPathPopup.setAction(action);
        this.downloadPathPopup.removeAllItems();

        // Default download folder
        this.addDownloadPath(action, new DownloadDirectoryFinder().find(host));
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.addDownloadPath(action, LocalFactory.get(preferences.getProperty("queue.download.folder")));
        // Shortcut to the Desktop
        this.addDownloadPath(action, LocalFactory.get("~/Desktop"));
        // Shortcut to user home
        this.addDownloadPath(action, LocalFactory.get("~"));
        // Shortcut to user downloads for 10.5
        this.addDownloadPath(action, LocalFactory.get("~/Downloads"));
        // Choose another folder

        // Choose another folder
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(CHOOSE, action, StringUtils.EMPTY);
        this.downloadPathPopup.lastItem().setTarget(this.id());
    }

    private void addDownloadPath(Selector action, Local f) {
        if(downloadPathPopup.menu().itemWithTitle(f.getDisplayName()) == null) {
            downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(f.getDisplayName(), action, StringUtils.EMPTY);
            downloadPathPopup.lastItem().setTarget(this.id());
            downloadPathPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().fileIcon(f, 16));
            downloadPathPopup.lastItem().setRepresentedObject(f.getAbsolute());
            if(new DownloadDirectoryFinder().find(host).equals(f)) {
                downloadPathPopup.selectItem(downloadPathPopup.lastItem());
            }
        }
    }

    @Action
    public void downloadPathPopupClicked(final NSMenuItem sender) {
        if(sender.title().equals(CHOOSE)) {
            downloadPathPanel = NSOpenPanel.openPanel();
            downloadPathPanel.setCanChooseFiles(false);
            downloadPathPanel.setCanChooseDirectories(true);
            downloadPathPanel.setAllowsMultipleSelection(false);
            downloadPathPanel.setCanCreateDirectories(true);
            downloadPathPanel.beginSheetForDirectory(null, null, this.window, this.id(),
                    Foundation.selector("downloadPathPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            final Local folder = LocalFactory.get(sender.representedObject());
            host.setDownloadFolder(folder);
            this.itemChanged();
        }
    }

    public void downloadPathPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            final NSObject selected = sheet.filenames().lastObject();
            if(selected != null) {
                host.setDownloadFolder(LocalFactory.get(selected.toString()));
            }
        }
        final NSMenuItem item = downloadPathPopup.itemAtIndex(new NSInteger(0));
        final Local folder = new DownloadDirectoryFinder().find(host);
        item.setTitle(folder.getDisplayName());
        item.setRepresentedObject(folder.getAbsolute());
        item.setImage(IconCacheFactory.<NSImage>get().fileIcon(folder, 16));
        downloadPathPopup.selectItem(item);
        downloadPathPanel = null;
        this.itemChanged();
    }

    public void setToggleOptionsButton(NSButton toggleOptionsButton) {
        this.toggleOptionsButton = toggleOptionsButton;
    }

    @Override
    public void invalidate() {
        preferences.setProperty("bookmark.toggle.options", this.toggleOptionsButton.state());
        collection.removeListener(bookmarkCollectionListener);
        notificationCenter.removeObserver(this.id());
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Bookmark";
    }

    @Override
    public void awakeFromNib() {
        this.init();
        this.setState(this.toggleOptionsButton, preferences.getBoolean("bookmark.toggle.options"));
        this.reachable();
        this.updateFavicon();
        window.makeFirstResponder(hostField);

        super.awakeFromNib();
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setContentMinSize(window.frame().size);
        window.setContentMaxSize(new NSSize(600, window.frame().size.height.doubleValue()));
        super.setWindow(window);
        cascade = this.cascade(cascade);
    }

    @Override
    public void windowWillClose(final NSNotification notification) {
        cascade = new NSPoint(this.window().frame().origin.x.doubleValue(),
                this.window().frame().origin.y.doubleValue() + this.window().frame().size.height.doubleValue());
        super.windowWillClose(notification);
    }

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
    }

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this.id());
        this.pkCheckbox.setAction(Foundation.selector("pkCheckboxSelectionChanged:"));
    }

    @Action
    public void pkCheckboxSelectionChanged(final NSButton sender) {
        if(sender.state() == NSCell.NSOnState) {
            publicKeyPanel = NSOpenPanel.openPanel();
            publicKeyPanel.setCanChooseDirectories(false);
            publicKeyPanel.setCanChooseFiles(true);
            publicKeyPanel.setAllowsMultipleSelection(false);
            publicKeyPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
            publicKeyPanel.setPrompt(LocaleFactory.localizedString("Choose"));
            publicKeyPanel.beginSheetForDirectory(LocalFactory.get("~/.ssh").getAbsolute(), null, this.window(), this.id(),
                    Foundation.selector("publicKeyPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            this.publicKeyPanelDidEnd_returnCode_contextInfo(publicKeyPanel, NSPanel.NSCancelButton, null);
        }
    }

    public void publicKeyPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
        if(returncode == NSPanel.NSOKButton) {
            final NSObject selected = publicKeyPanel.filenames().lastObject();
            if(selected != null) {
                final Local key = LocalFactory.get(selected.toString());
                host.getCredentials().setIdentity(key);
            }
        }
        if(returncode == NSPanel.NSCancelButton) {
            host.getCredentials().setIdentity(null);
        }
        this.init();
        this.itemChanged();
    }

    @Action
    public void hostFieldDidChange(final NSNotification sender) {
        final String input = hostField.stringValue();
        if(Scheme.isURL(input)) {
            final Host parsed = HostParser.parse(input);
            host.setHostname(parsed.getHostname());
            host.setProtocol(parsed.getProtocol());
            host.setPort(parsed.getPort());
            host.setDefaultPath(parsed.getDefaultPath());
        }
        else {
            host.setHostname(input);
        }
        this.itemChanged();
        this.init();
        this.reachable();
    }

    private void reachable() {
        if(StringUtils.isNotBlank(host.getHostname())) {
            this.background(new AbstractBackgroundAction<Boolean>() {
                boolean reachable = false;

                @Override
                public Boolean run() throws BackgroundException {
                    if(!preferences.getBoolean("connection.hostname.check")) {
                        return reachable = true;
                    }
                    return reachable = ReachabilityFactory.get().isReachable(host);
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

    @Action
    public void portInputDidEndEditing(final NSNotification sender) {
        try {
            host.setPort(Integer.valueOf(portField.stringValue()));
        }
        catch(NumberFormatException e) {
            host.setPort(-1);
        }
        this.itemChanged();
        this.init();
        this.reachable();
    }

    @Action
    public void pathInputDidChange(final NSNotification sender) {
        host.setDefaultPath(pathField.stringValue());
        this.itemChanged();
        this.init();
    }

    @Action
    public void nicknameInputDidChange(final NSNotification sender) {
        host.setNickname(nicknameField.stringValue());
        this.itemChanged();
        this.init();
    }

    @Action
    public void usernameInputDidChange(final NSNotification sender) {
        host.getCredentials().setUsername(usernameField.stringValue());
        this.itemChanged();
        this.init();
    }

    @Action
    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.NSOnState) {
            host.getCredentials().setUsername(preferences.getProperty("connection.login.anon.name"));
        }
        if(sender.state() == NSCell.NSOffState) {
            if(preferences.getProperty("connection.login.name").equals(
                    preferences.getProperty("connection.login.anon.name"))) {
                host.getCredentials().setUsername(StringUtils.EMPTY);
            }
            else {
                host.getCredentials().setUsername(preferences.getProperty("connection.login.name"));
            }
        }
        this.itemChanged();
        this.init();
    }

    @Action
    public void webURLInputDidChange(final NSNotification sender) {
        host.setWebURL(webURLField.stringValue());
        this.updateFavicon();
        this.itemChanged();
    }

    @Action
    public void commentInputDidChange(final NSNotification sender) {
        host.setComment(commentField.textStorage().string());
        this.itemChanged();
    }

    /**
     * Updates the window title and url label with the properties of this bookmark
     * Propagates all fields with the properties of this bookmark
     */
    private void itemChanged() {
        collection.collectionItemChanged(host);
    }

    private void init() {
        window.setTitle(BookmarkNameProvider.toString(host));
        this.updateField(hostField, host.getHostname());
        hostField.setEnabled(host.getProtocol().isHostnameConfigurable());
        hostField.cell().setPlaceholderString(host.getProtocol().getDefaultHostname());
        this.updateField(nicknameField, BookmarkNameProvider.toString(host));
        urlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(new HostUrlProvider(true, true).get(host)));
        this.updateField(portField, String.valueOf(host.getPort()));
        portField.setEnabled(host.getProtocol().isPortConfigurable());
        this.updateField(pathField, host.getDefaultPath());
        this.updateField(usernameField, host.getCredentials().getUsername());
        usernameField.cell().setPlaceholderString(host.getProtocol().getUsernamePlaceholder());
        usernameField.setEnabled(!host.getCredentials().isAnonymousLogin());
        usernameLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                StringUtils.isNotBlank(host.getCredentials().getUsernamePlaceholder()) ? String.format("%s:",
                        host.getCredentials().getUsernamePlaceholder()) : StringUtils.EMPTY,
                LABEL_ATTRIBUTES
        ));
        anonymousCheckbox.setEnabled(host.getProtocol().isAnonymousConfigurable());
        anonymousCheckbox.setState(host.getCredentials().isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
        protocolPopup.selectItemAtIndex(
                protocolPopup.indexOfItemWithRepresentedObject(String.valueOf(host.getProtocol().hashCode()))
        );
        transferPopup.selectItemAtIndex(transferPopup.indexOfItemWithRepresentedObject(host.getTransfer().name()));
        encodingPopup.setEnabled(host.getProtocol().isEncodingConfigurable());
        connectmodePopup.setEnabled(host.getProtocol().getType() == Protocol.Type.ftp);
        if(host.getProtocol().getType() == Protocol.Type.ftp) {
            connectmodePopup.selectItemAtIndex(connectmodePopup.indexOfItemWithRepresentedObject(host.getFTPConnectMode().name()));
        }
        pkCheckbox.setEnabled(host.getProtocol().getType() == Protocol.Type.sftp);
        if(host.getCredentials().isPublicKeyAuthentication()) {
            pkCheckbox.setState(NSCell.NSOnState);
            this.updateField(pkLabel, host.getCredentials().getIdentity().getAbbreviatedPath(), TRUNCATE_MIDDLE_ATTRIBUTES);
            pkLabel.setTextColor(NSColor.textColor());
        }
        else {
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(LocaleFactory.localizedString("No private key selected"));
            pkLabel.setTextColor(NSColor.disabledControlTextColor());
        }
        final String webURL = host.getWebURL();
        webUrlImage.setToolTip(webURL);
        this.updateField(webURLField, host.getDefaultWebURL().equals(webURL) ? null : webURL);
        this.updateField(commentField, host.getComment());
        timezonePopup.setEnabled(!host.getProtocol().isUTCTimezone());
        if(null == host.getTimezone()) {
            if(host.getProtocol().isUTCTimezone()) {
                timezonePopup.setTitle(UTC.getID());
            }
            else {
                timezonePopup.setTitle(TimeZone.getTimeZone(preferences.getProperty("ftp.timezone.default")).getID());
            }
        }
        else {
            timezonePopup.setTitle(host.getTimezone().getID());
        }
    }
}
