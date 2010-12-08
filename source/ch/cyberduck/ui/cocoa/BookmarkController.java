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

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.spearce.jgit.transport.OpenSshConfig;

import java.util.*;

/**
 * @version $Id$
 */
public class BookmarkController extends WindowController {
    private static Logger log = Logger.getLogger(BookmarkController.class);

    @Outlet
    private NSPopUpButton protocolPopup;

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionChanged:"));
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : Protocol.getKnownProtocols()) {
            final String title = protocol.getDescription();
            this.protocolPopup.addItemWithTitle(title);
            final NSMenuItem item = this.protocolPopup.itemWithTitle(title);
            item.setRepresentedObject(protocol.getIdentifier());
            item.setImage(IconCache.iconNamed(protocol.icon(), 16));
        }
    }

    @Action
    public void protocolSelectionChanged(final NSPopUpButton sender) {
        log.debug("protocolSelectionChanged:" + sender);
        final Protocol selected = Protocol.forName(protocolPopup.selectedItem().representedObject());
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
        if(!selected.isWebUrlConfigurable()) {
            host.setWebURL(null);
        }
        if(selected.equals(Protocol.IDISK)) {
            final String member = Preferences.instance().getProperty("iToolsMember");
            if(StringUtils.isNotEmpty(member)) {
                // Account name configured in System Preferences
                host.getCredentials().setUsername(member);
                host.setDefaultPath(String.valueOf(Path.DELIMITER) + member);
            }
        }
        host.setProtocol(selected);
        this.readOpenSshConfiguration();
        this.itemChanged();
        this.init();
        this.reachable();
    }

    /**
     * Update this host credentials from the OpenSSH configuration file in ~/.ssh/config
     */
    private void readOpenSshConfiguration() {
        if(host.getProtocol().equals(Protocol.SFTP)) {
            final OpenSshConfig.Host entry = OpenSshConfig.create().lookup(host.getHostname());
            if(null != entry.getIdentityFile()) {
                host.getCredentials().setIdentity(LocalFactory.createLocal(entry.getIdentityFile().getAbsolutePath()));
            }
            if(StringUtils.isNotBlank(entry.getUser())) {
                host.getCredentials().setUsername(entry.getUser());
            }
        }
        else {
            host.getCredentials().setIdentity(null);
        }
    }

    @Outlet
    private NSPopUpButton encodingPopup;

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemWithTitle(DEFAULT);
        this.encodingPopup.menu().addItem(NSMenuItem.separatorItem());
        this.encodingPopup.addItemsWithTitles(NSArray.arrayWithObjects(MainController.availableCharsets()));
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
        log.debug("encodingSelectionChanged:" + sender);
        if(sender.selectedItem().title().equals(DEFAULT)) {
            host.setEncoding(null);
        }
        else {
            host.setEncoding(sender.selectedItem().title());
        }
        this.itemChanged();
    }

    @Outlet
    private NSTextField nicknameField;

    public void setNicknameField(NSTextField nicknameField) {
        this.nicknameField = nicknameField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("nicknameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.nicknameField);
    }

    @Outlet
    private NSTextField hostField;

    public void setHostField(NSTextField hostField) {
        this.hostField = hostField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("hostFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                hostField);
    }

    @Outlet
    private NSButton alertIcon;

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setEnabled(false);
        this.alertIcon.setImage(null);
        this.alertIcon.setTarget(this.id());
        this.alertIcon.setAction(Foundation.selector("launchNetworkAssistant:"));
    }

    @Action
    public void launchNetworkAssistant(final NSButton sender) {
        host.diagnose();
    }

    @Outlet
    private NSTextField portField;

    public void setPortField(NSTextField portField) {
        this.portField = portField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("portInputDidEndEditing:"),
                NSControl.NSControlTextDidChangeNotification,
                this.portField);
    }

    @Outlet
    private NSTextField pathField;

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("pathInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.pathField);
    }

    @Outlet
    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
        this.urlField.setAllowsEditingTextAttributes(true);
        this.urlField.setSelectable(true);
    }

    @Outlet
    private NSTextField usernameField;

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("usernameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.usernameField);
    }

    @Outlet
    private NSButton anonymousCheckbox;

    public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
        this.anonymousCheckbox = anonymousCheckbox;
        this.anonymousCheckbox.setTarget(this.id());
        this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
        this.anonymousCheckbox.setState(NSCell.NSOffState);
    }

    @Outlet
    private NSTextField webURLField;

    public void setWebURLField(NSTextField webURLField) {
        this.webURLField = webURLField;
        final NSTextFieldCell cell = this.webURLField.cell();
        cell.setPlaceholderString(host.getDefaultWebURL());
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("webURLInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.webURLField);
    }

    @Outlet
    private NSButton webUrlImage;

    public void setWebUrlImage(NSButton b) {
        this.webUrlImage = b;
        this.webUrlImage.setTarget(this.id());
        this.webUrlImage.setAction(Foundation.selector("openWebUrl:"));
        this.webUrlImage.setImage(IconCache.iconNamed("site.tiff", 16));
    }

    private NSImage favicon;

    /**
     *
     */
    private void updateFavicon() {
        if(Preferences.instance().getBoolean("bookmark.favicon.download")) {
            this.background(new AbstractBackgroundAction() {

                public void run() {
                    NSImage img;
                    if(StringUtils.isNotBlank(host.getProtocol().favicon())) {
                        img = IconCache.iconNamed(host.getProtocol().favicon(), 16);
                    }
                    else {
                        String url = host.getWebURL() + "/favicon.ico";
                        // Default favicon location
                        final NSData data = NSData.dataWithContentsOfURL(NSURL.URLWithString(url));
                        if(null == data) {
                            return;
                        }
                        img = NSImage.imageWithData(data);
                    }
                    if(null == img) {
                        return;
                    }
                    favicon = IconCache.instance().convert(img, 16);
                }

                @Override
                public void cleanup() {
                    if(null == favicon) {
                        return;
                    }
                    webUrlImage.setImage(favicon);
                }

                @Override
                public Object lock() {
                    return BookmarkController.this;
                }
            });
        }
    }

    @Action
    public void openWebUrl(final NSButton sender) {
        openUrl(host.getWebURL());
    }

    @Outlet
    private NSTextView commentField;

    public void setCommentField(NSTextView commentField) {
        this.commentField = commentField;
        this.commentField.setFont(NSFont.userFixedPitchFontOfSize(11f));
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("commentInputDidChange:"),
                NSText.TextDidChangeNotification,
                this.commentField);
    }

    /**
     * Calculate timezone
     */
    protected static final String AUTO = Locale.localizedString("Auto");

    @Outlet
    private NSPopUpButton timezonePopup;

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final String TIMEZONE_ID_PREFIXES =
            "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

    public void setTimezonePopup(NSPopUpButton timezonePopup) {
        this.timezonePopup = timezonePopup;
        this.timezonePopup.setTarget(this.id());
        this.timezonePopup.setAction(Foundation.selector("timezonePopupClicked:"));
        this.timezonePopup.removeAllItems();
        {
            this.timezonePopup.addItemWithTitle(UTC.getID());
        }
        this.timezonePopup.menu().addItem(NSMenuItem.separatorItem());
        final List<String> timezones = Arrays.asList(TimeZone.getAvailableIDs());
        Collections.sort(timezones, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return TimeZone.getTimeZone(o1).getID().compareTo(TimeZone.getTimeZone(o2).getID());
            }
        });
        for(String tz : timezones) {
            if(tz.matches(TIMEZONE_ID_PREFIXES)) {
                this.timezonePopup.addItemWithTitle(TimeZone.getTimeZone(tz).getID());
            }
        }
    }

    @Action
    public void timezonePopupClicked(NSPopUpButton sender) {
        String selected = sender.selectedItem().title();
        if(selected.equals(AUTO)) {
            host.setTimezone(null);
        }
        else {
            String[] ids = TimeZone.getAvailableIDs();
            for(String id : ids) {
                TimeZone tz;
                if((tz = TimeZone.getTimeZone(id)).getID().equals(selected)) {
                    host.setTimezone(tz);
                    break;
                }
            }
        }
        this.itemChanged();
    }

    @Outlet
    private NSPopUpButton connectmodePopup;

    private static final String CONNECTMODE_ACTIVE = Locale.localizedString("Active");
    private static final String CONNECTMODE_PASSIVE = Locale.localizedString("Passive");

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.setTarget(this.id());
        this.connectmodePopup.setAction(Foundation.selector("connectmodePopupClicked:"));
        this.connectmodePopup.removeAllItems();
        this.connectmodePopup.addItemWithTitle(DEFAULT);
        this.connectmodePopup.menu().addItem(NSMenuItem.separatorItem());
        this.connectmodePopup.addItemWithTitle(CONNECTMODE_ACTIVE);
        this.connectmodePopup.addItemWithTitle(CONNECTMODE_PASSIVE);
    }

    @Action
    public void connectmodePopupClicked(final NSPopUpButton sender) {
        if(sender.selectedItem().title().equals(DEFAULT)) {
            host.setFTPConnectMode(null);
        }
        else if(sender.selectedItem().title().equals(CONNECTMODE_ACTIVE)) {
            host.setFTPConnectMode(FTPConnectMode.PORT);
        }
        else if(sender.selectedItem().title().equals(CONNECTMODE_PASSIVE)) {
            host.setFTPConnectMode(FTPConnectMode.PASV);
        }
        this.itemChanged();
    }

    @Outlet
    private NSPopUpButton transferPopup;

    private static final String TRANSFER_NEWCONNECTION = Locale.localizedString("Open new connection");
    private static final String TRANSFER_BROWSERCONNECTION = Locale.localizedString("Use browser connection");

    public void setTransferPopup(NSPopUpButton transferPopup) {
        this.transferPopup = transferPopup;
        this.transferPopup.setTarget(this.id());
        this.transferPopup.setAction(Foundation.selector("transferPopupClicked:"));
        this.transferPopup.removeAllItems();
        this.transferPopup.addItemWithTitle(DEFAULT);
        this.transferPopup.menu().addItem(NSMenuItem.separatorItem());
        this.transferPopup.addItemWithTitle(TRANSFER_NEWCONNECTION);
        this.transferPopup.addItemWithTitle(TRANSFER_BROWSERCONNECTION);
    }

    @Action
    public void transferPopupClicked(final NSPopUpButton sender) {
        if(sender.selectedItem().title().equals(DEFAULT)) {
            host.setMaxConnections(null);
        }
        else if(sender.selectedItem().title().equals(TRANSFER_BROWSERCONNECTION)) {
            host.setMaxConnections(1);
        }
        else if(sender.selectedItem().title().equals(TRANSFER_NEWCONNECTION)) {
            host.setMaxConnections(-1);
        }
        this.itemChanged();
    }

    @Outlet
    private NSPopUpButton downloadPathPopup;

    private static final String CHOOSE = Locale.localizedString("Choose") + "…";

    public void setDownloadPathPopup(NSPopUpButton downloadPathPopup) {
        this.downloadPathPopup = downloadPathPopup;
        this.downloadPathPopup.setTarget(this.id());
        final Selector action = Foundation.selector("downloadPathPopupClicked:");
        this.downloadPathPopup.setAction(action);
        this.downloadPathPopup.removeAllItems();

        // Default download folder
        this.addDownloadPath(action, host.getDownloadFolder());
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.addDownloadPath(action, LocalFactory.createLocal(Preferences.instance().getProperty("queue.download.folder")));
        // Shortcut to the Desktop
        this.addDownloadPath(action, LocalFactory.createLocal("~/Desktop"));
        // Shortcut to user home
        this.addDownloadPath(action, LocalFactory.createLocal("~"));
        // Shortcut to user downloads for 10.5
        this.addDownloadPath(action, LocalFactory.createLocal("~/Downloads"));
        // Choose another folder

        // Choose another folder
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(CHOOSE, action, "");
        this.downloadPathPopup.lastItem().setTarget(this.id());
    }

    private void addDownloadPath(Selector action, Local f) {
        if(downloadPathPopup.menu().itemWithTitle(f.getDisplayName()) == null) {
            downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(f.getDisplayName(), action, "");
            downloadPathPopup.lastItem().setTarget(this.id());
            downloadPathPopup.lastItem().setImage(
                    IconCache.instance().iconForPath(f, 16)
            );
            downloadPathPopup.lastItem().setRepresentedObject(
                    f.getAbsolute());
            if(host.getDownloadFolder().equals(f)) {
                downloadPathPopup.selectItem(downloadPathPopup.lastItem());
            }
        }
    }

    private NSOpenPanel downloadPathPanel;

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
            host.setDownloadFolder(sender.representedObject());
            this.itemChanged();
        }
    }

    public void downloadPathPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            NSArray selected = sheet.filenames();
            if((selected.lastObject()) != null) {
                host.setDownloadFolder(selected.lastObject().toString());
            }
        }
        downloadPathPopup.itemAtIndex(new NSInteger(0)).setTitle(host.getDownloadFolder().getDisplayName());
        downloadPathPopup.itemAtIndex(new NSInteger(0)).setRepresentedObject(host.getDownloadFolder().getAbsolute());
        downloadPathPopup.itemAtIndex(new NSInteger(0)).setImage(IconCache.instance().iconForPath(host.getDownloadFolder(), 16));
        downloadPathPopup.selectItemAtIndex(new NSInteger(0));
        downloadPathPanel = null;
        this.itemChanged();
    }

    @Outlet
    private NSButton toggleOptionsButton;

    public void setToggleOptionsButton(NSButton toggleOptionsButton) {
        this.toggleOptionsButton = toggleOptionsButton;
    }

    /**
     *
     */
    public static class Factory {
        private static final Map<Host, BookmarkController> open
                = new HashMap<Host, BookmarkController>();

        public static BookmarkController create(final Host host) {
            if(open.containsKey(host)) {
                return open.get(host);
            }
            final BookmarkController c = new BookmarkController(host) {
                @Override
                public void windowWillClose(NSNotification notification) {
                    super.windowWillClose(notification);
                    Factory.open.remove(host);
                }
            };
            open.put(host, c);
            return c;
        }
    }

    /**
     * The bookmark
     */
    private Host host;

    /**
     * @param host The bookmark to edit
     */
    private BookmarkController(final Host host) {
        this.host = host;
        // Register for bookmark delete event. Will close this window.
        BookmarkCollection.defaultCollection().addListener(bookmarkCollectionListener);
        this.loadBundle();
    }

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

    @Override
    protected void invalidate() {
        Preferences.instance().setProperty("bookmark.toggle.options", this.toggleOptionsButton.state());
        BookmarkCollection.defaultCollection().removeListener(bookmarkCollectionListener);
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Bookmark";
    }

    @Override
    public void awakeFromNib() {
        this.cascade();
        this.init();
        this.setState(this.toggleOptionsButton, Preferences.instance().getBoolean("bookmark.toggle.options"));
        this.reachable();
        this.updateFavicon();

        super.awakeFromNib();
    }

    @Override
    protected double getMaxWindowHeight() {
        return window.frame().size.height.doubleValue();
    }

    @Override
    protected double getMaxWindowWidth() {
        return 600;
    }

    @Outlet
    private NSTextField pkLabel;

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
    }

    @Outlet
    private NSButton pkCheckbox;

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this.id());
        this.pkCheckbox.setAction(Foundation.selector("pkCheckboxSelectionChanged:"));
    }

    private NSOpenPanel publicKeyPanel;

    @Action
    public void pkCheckboxSelectionChanged(final NSButton sender) {
        log.debug("pkCheckboxSelectionChanged");
        if(sender.state() == NSCell.NSOnState) {
            publicKeyPanel = NSOpenPanel.openPanel();
            publicKeyPanel.setCanChooseDirectories(false);
            publicKeyPanel.setCanChooseFiles(true);
            publicKeyPanel.setAllowsMultipleSelection(false);
            publicKeyPanel.setMessage(Locale.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
            publicKeyPanel.setPrompt(Locale.localizedString("Choose"));
            publicKeyPanel.beginSheetForDirectory(LocalFactory.createLocal("~/.ssh").getAbsolute(), null, this.window(), this.id(),
                    Foundation.selector("pkSelectionPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            this.pkSelectionPanelDidEnd_returnCode_contextInfo(publicKeyPanel, NSPanel.NSCancelButton, null);
        }
    }

    public void pkSelectionPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
        log.debug("pkSelectionPanelDidEnd");
        if(returncode == NSPanel.NSOKButton) {
            NSArray selected = sheet.filenames();
            NSEnumerator enumerator = selected.objectEnumerator();
            NSObject next;
            while(((next = enumerator.nextObject()) != null)) {
                host.getCredentials().setIdentity(LocalFactory.createLocal(next.toString()));
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
        String input = hostField.stringValue();
        if(Protocol.isURL(input)) {
            host.init(Host.parse(input).<NSDictionary>getAsDictionary());
        }
        else {
            host.setHostname(input);
        }
        this.readOpenSshConfiguration();
        this.itemChanged();
        this.init();
        this.reachable();
    }

    private void reachable() {
        if(StringUtils.isNotBlank(host.getHostname())) {
            this.background(new AbstractBackgroundAction() {
                boolean reachable = false;

                public void run() {
                    reachable = host.isReachable();
                }

                @Override
                public void cleanup() {
                    alertIcon.setEnabled(!reachable);
                    alertIcon.setImage(reachable ? null : IconCache.iconNamed("alert.tiff"));
                }
            });
        }
        else {
            alertIcon.setImage(IconCache.iconNamed("alert.tiff"));
            alertIcon.setEnabled(false);
        }
    }

    @Action
    public void portInputDidEndEditing(final NSNotification sender) {
        try {
            host.setPort(Integer.parseInt(portField.stringValue()));
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
            host.getCredentials().setUsername(Preferences.instance().getProperty("connection.login.anon.name"));
        }
        if(sender.state() == NSCell.NSOffState) {
            host.getCredentials().setUsername(Preferences.instance().getProperty("connection.login.name"));
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
        BookmarkCollection.defaultCollection().collectionItemChanged(host);
    }

    private void init() {
        window.setTitle(host.getNickname());
        this.updateField(hostField, host.getHostname());
        hostField.setEnabled(host.getProtocol().isHostnameConfigurable());
        hostField.cell().setPlaceholderString(host.getProtocol().getDefaultHostname());
        this.updateField(nicknameField, host.getNickname());
        final String url;
        if(StringUtils.isNotBlank(host.getDefaultPath())) {
            url = host.toURL() + Path.normalize(host.getDefaultPath());
        }
        else {
            url = host.toURL();
        }
        urlField.setAttributedStringValue(
                HyperlinkAttributedStringFactory.create(NSMutableAttributedString.create(url, TRUNCATE_MIDDLE_ATTRIBUTES), url)
        );
        this.updateField(portField, String.valueOf(host.getPort()));
        portField.setEnabled(host.getProtocol().isHostnameConfigurable());
        this.updateField(pathField, host.getDefaultPath());
        this.updateField(usernameField, host.getCredentials().getUsername());
        usernameField.cell().setPlaceholderString(host.getProtocol().getUsernamePlaceholder());
        usernameField.setEnabled(!host.getCredentials().isAnonymousLogin());
        anonymousCheckbox.setEnabled(host.getProtocol().isAnonymousConfigurable());
        anonymousCheckbox.setState(host.getCredentials().isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
        protocolPopup.selectItemWithTitle(host.getProtocol().getDescription());
        if(null == host.getMaxConnections()) {
            transferPopup.selectItemWithTitle(DEFAULT);
        }
        else {
            transferPopup.selectItemWithTitle(
                    host.getMaxConnections() == 1 ? TRANSFER_BROWSERCONNECTION : TRANSFER_NEWCONNECTION);
        }
        encodingPopup.setEnabled(host.getProtocol().isEncodingConfigurable());
        connectmodePopup.setEnabled(host.getProtocol().isConnectModeConfigurable());
        if(host.getProtocol().isConnectModeConfigurable()) {
            if(null == host.getFTPConnectMode()) {
                connectmodePopup.selectItemWithTitle(DEFAULT);
            }
            else if(host.getFTPConnectMode().equals(FTPConnectMode.PASV)) {
                connectmodePopup.selectItemWithTitle(CONNECTMODE_PASSIVE);
            }
            else if(host.getFTPConnectMode().equals(FTPConnectMode.PORT)) {
                connectmodePopup.selectItemWithTitle(CONNECTMODE_ACTIVE);
            }
        }
        pkCheckbox.setEnabled(host.getProtocol().equals(Protocol.SFTP));
        if(host.getCredentials().isPublicKeyAuthentication()) {
            pkCheckbox.setState(NSCell.NSOnState);
            this.updateField(pkLabel, host.getCredentials().getIdentity().getAbbreviatedPath());
            pkLabel.setTextColor(NSColor.textColor());
        }
        else {
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(Locale.localizedString("No private key selected"));
            pkLabel.setTextColor(NSColor.disabledControlTextColor());
        }
        webURLField.setEnabled(host.getProtocol().isWebUrlConfigurable());
        final String webURL = host.getWebURL();
        webUrlImage.setToolTip(webURL);
        this.updateField(webURLField, host.getDefaultWebURL().equals(webURL) ? null : webURL);
        this.updateField(commentField, host.getComment());
        this.timezonePopup.setEnabled(!host.getProtocol().isUTCTimezone());
        if(null == host.getTimezone()) {
            if(host.getProtocol().isUTCTimezone()) {
                this.timezonePopup.setTitle(UTC.getID());
            }
            else {
                if(Preferences.instance().getBoolean("ftp.timezone.auto")) {
                    this.timezonePopup.setTitle(AUTO);
                }
                else {
                    this.timezonePopup.setTitle(
                            TimeZone.getTimeZone(Preferences.instance().getProperty("ftp.timezone.default")).getID()
                    );
                }
            }
        }
        else {
            this.timezonePopup.setTitle(host.getTimezone().getID());
        }
    }
}