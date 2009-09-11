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
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.util.HyperlinkAttributedStringFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;

import java.util.*;

import com.enterprisedt.net.ftp.FTPConnectMode;

/**
 * @version $Id$
 */
public class CDBookmarkController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDBookmarkController.class);

    @Outlet
    private NSPopUpButton protocolPopup;

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionChanged:"));
        this.protocolPopup.removeAllItems();
        final Protocol[] protocols = Protocol.getKnownProtocols();
        for(Protocol protocol : protocols) {
            final String title = protocol.getDescription();
            this.protocolPopup.addItemWithTitle(title);
            final NSMenuItem item = this.protocolPopup.itemWithTitle(title);
            item.setRepresentedObject(protocol.getIdentifier());
            item.setImage(CDIconCache.instance().iconForName(protocol.icon(), 16));
        }
    }

    public void protocolSelectionChanged(final NSPopUpButton sender) {
        log.debug("protocolSelectionChanged:" + sender);
        final Protocol selected = Protocol.forName(protocolPopup.selectedItem().representedObject());
        this.host.setPort(selected.getDefaultPort());
        if(this.host.getProtocol().getDefaultHostname().equals(this.host.getHostname())) {
            this.host.setHostname(selected.getDefaultHostname());
        }
        if(!selected.isWebUrlConfigurable()) {
            this.host.setWebURL(null);
        }
        if(selected.equals(Protocol.IDISK)) {
            final String member = CDDotMacController.instance().getAccountName();
            if(StringUtils.isNotEmpty(member)) {
                // Account name configured in System Preferences
                this.host.getCredentials().setUsername(member);
                this.host.setDefaultPath(Path.DELIMITER + member);
            }
        }
        this.host.setProtocol(selected);
        this.itemChanged();
        this.init();
        this.reachable();
    }

    @Outlet
    private NSPopUpButton encodingPopup;

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemWithTitle(DEFAULT);
        this.encodingPopup.menu().addItem(NSMenuItem.separatorItem());
        this.encodingPopup.addItemsWithTitles(NSArray.arrayWithObjects(CDMainController.availableCharsets()));
        if(null == this.host.getEncoding()) {
            this.encodingPopup.selectItemWithTitle(DEFAULT);
        }
        else {
            this.encodingPopup.selectItemWithTitle(this.host.getEncoding());
        }
        this.encodingPopup.setTarget(this.id());
        final Selector action = Foundation.selector("encodingSelectionChanged:");
        this.encodingPopup.setAction(action);
    }

    public void encodingSelectionChanged(final NSPopUpButton sender) {
        log.debug("encodingSelectionChanged:" + sender);
        if(sender.selectedItem().title().equals(DEFAULT)) {
            this.host.setEncoding(null);
        }
        else {
            this.host.setEncoding(sender.selectedItem().title());
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
                this.hostField);
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

    public void launchNetworkAssistant(final NSButton sender) {
        this.host.diagnose();
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
    private NSTextField webURLField;

    public void setWebURLField(NSTextField webURLField) {
        this.webURLField = webURLField;
        final NSTextFieldCell cell = this.webURLField.cell();
        cell.setPlaceholderString(
                host.getDefaultWebURL()
        );
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
        this.webUrlImage.setImage(CDIconCache.instance().iconForName("site", 16));
        this.updateFavicon();
    }

    private NSImage favicon;

    /**
     *
     */
    private void updateFavicon() {
        if(Preferences.instance().getBoolean("bookmark.favicon.download")) {
            this.background(new AbstractBackgroundAction() {

                public void run() {
                    // Default favicon location
                    final NSData data = NSData.dataWithContentsOfURL(NSURL.URLWithString(host.getWebURL() + "/favicon.ico"));
                    if(null == data) {
                        return;
                    }
                    favicon = CDIconCache.instance().convert(NSImage.imageWithData(data), 16);
                }

                public void cleanup() {
                    if(null == favicon) {
                        return;
                    }
                    webUrlImage.setImage(favicon);
                }
            });
        }
    }

    public void openWebUrl(final NSButton sender) {
        NSWorkspace.sharedWorkspace().openURL(NSURL.URLWithString(host.getWebURL()));
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
    private NSPopUpButton timezonePopup; //IBOutlet

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

    public void timezonePopupClicked(NSPopUpButton sender) {
        String selected = sender.selectedItem().title();
        if(selected.equals(AUTO)) {
            this.host.setTimezone(null);
        }
        else {
            String[] ids = TimeZone.getAvailableIDs();
            for(String id : ids) {
                TimeZone tz;
                if((tz = TimeZone.getTimeZone(id)).getID().equals(selected)) {
                    this.host.setTimezone(tz);
                    break;
                }
            }
        }
        this.itemChanged();
    }

    @Outlet
    private NSPopUpButton connectmodePopup; //IBOutlet

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

    public void connectmodePopupClicked(final NSPopUpButton sender) {
        if(sender.selectedItem().title().equals(DEFAULT)) {
            this.host.setFTPConnectMode(null);
        }
        else if(sender.selectedItem().title().equals(CONNECTMODE_ACTIVE)) {
            this.host.setFTPConnectMode(FTPConnectMode.ACTIVE);
        }
        else if(sender.selectedItem().title().equals(CONNECTMODE_PASSIVE)) {
            this.host.setFTPConnectMode(FTPConnectMode.PASV);
        }
        this.itemChanged();
    }

    @Outlet
    private NSPopUpButton transferPopup; //IBOutlet

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

    public void transferPopupClicked(final NSPopUpButton sender) {
        if(sender.selectedItem().title().equals(DEFAULT)) {
            this.host.setMaxConnections(null);
        }
        else if(sender.selectedItem().title().equals(TRANSFER_BROWSERCONNECTION)) {
            this.host.setMaxConnections(1);
        }
        else if(sender.selectedItem().title().equals(TRANSFER_NEWCONNECTION)) {
            this.host.setMaxConnections(-1);
        }
        this.itemChanged();
    }

    @Outlet
    private NSPopUpButton downloadPathPopup; //IBOutlet

    private static final String CHOOSE = Locale.localizedString("Choose") + "...";

    public void setDownloadPathPopup(NSPopUpButton downloadPathPopup) {
        this.downloadPathPopup = downloadPathPopup;
        this.downloadPathPopup.setTarget(this.id());
        final Selector action = Foundation.selector("downloadPathPopupClicked:");
        this.downloadPathPopup.setAction(action);
        this.downloadPathPopup.removeAllItems();

        // Default download folder
        this.addDownloadPath(action, host.getDownloadFolder());
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
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
        this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems() - 1).setTarget(this.id());
    }

    private void addDownloadPath(Selector action, Local f) {
        if(f.exists()) {
            this.downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(NSFileManager.defaultManager().displayNameAtPath(
                    f.getAbsolute()), action, "");
            this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems() - 1).setTarget(this.id());
            this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems() - 1).setImage(
                    CDIconCache.instance().iconForPath(f, 16)
            );
            this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems() - 1).setRepresentedObject(
                    f.getAbsolute());
            if(host.getDownloadFolder().equals(f)) {
                this.downloadPathPopup.selectItemAtIndex(this.downloadPathPopup.numberOfItems() - 1);
            }
        }
    }

    private NSOpenPanel downloadPathPanel;

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
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            NSArray selected = sheet.filenames();
            if((selected.lastObject()) != null) {
                host.setDownloadFolder(selected.lastObject().toString());
            }
        }
        else {
            host.setDownloadFolder(null);
        }
        downloadPathPopup.itemAtIndex(0).setTitle(NSFileManager.defaultManager().displayNameAtPath(
                host.getDownloadFolder().getAbsolute()));
        downloadPathPopup.itemAtIndex(0).setRepresentedObject(
                host.getDownloadFolder().getAbsolute());
        downloadPathPopup.itemAtIndex(0).setImage(
                CDIconCache.instance().iconForPath(host.getDownloadFolder(), 16));
        downloadPathPopup.selectItemAtIndex(0);
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
        private static final Map<Host, CDBookmarkController> open
                = new HashMap<Host, CDBookmarkController>();

        public static CDBookmarkController create(final Host host) {
            if(open.containsKey(host)) {
                return open.get(host);
            }
            final CDBookmarkController c = new CDBookmarkController(host) {
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
    private CDBookmarkController(final Host host) {
        this.host = host;
        // Register for bookmark delete event. Will close this window.
        HostCollection.defaultCollection().addListener(bookmarkCollectionListener);
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
        HostCollection.defaultCollection().removeListener(bookmarkCollectionListener);
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

        super.awakeFromNib();
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

    public void pkCheckboxSelectionChanged(final NSButton sender) {
        log.debug("pkCheckboxSelectionChanged");
        if(this.pkLabel.stringValue().equals(Locale.localizedString("No Private Key selected"))) {
            publicKeyPanel = NSOpenPanel.openPanel();
            publicKeyPanel.setCanChooseDirectories(false);
            publicKeyPanel.setCanChooseFiles(true);
            publicKeyPanel.setAllowsMultipleSelection(false);
            publicKeyPanel.beginSheetForDirectory(LocalFactory.createLocal("~/.ssh").getAbsolute(), null, this.window(), this.id(),
                    Foundation.selector("pkSelectionPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            this.host.getCredentials().setIdentity(null);
            this.itemChanged();
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
        publicKeyPanel = null;
        this.itemChanged();
    }

    public void hostFieldDidChange(final NSNotification sender) {
        String input = hostField.stringValue();
        if(Protocol.isURL(input)) {
            this.host.init(Host.parse(input).<NSDictionary>getAsDictionary());
        }
        else {
            this.host.setHostname(input);
        }
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

                public void cleanup() {
                    alertIcon.setEnabled(!reachable);
                    alertIcon.setImage(reachable ? null : NSImage.imageNamed("alert.tiff"));
                }
            });
        }
        else {
            alertIcon.setImage(NSImage.imageNamed("alert.tiff"));
            alertIcon.setEnabled(false);
        }
    }

    public void portInputDidEndEditing(final NSNotification sender) {
        try {
            this.host.setPort(Integer.parseInt(portField.stringValue()));
        }
        catch(NumberFormatException e) {
            this.host.setPort(-1);
        }
        this.itemChanged();
        this.init();
    }

    public void pathInputDidChange(final NSNotification sender) {
        this.host.setDefaultPath(pathField.stringValue());
        this.itemChanged();
        this.init();
    }

    public void nicknameInputDidChange(final NSNotification sender) {
        this.host.setNickname(nicknameField.stringValue());
        this.itemChanged();
    }

    public void usernameInputDidChange(final NSNotification sender) {
        this.host.getCredentials().setUsername(usernameField.stringValue());
        this.itemChanged();
    }

    public void webURLInputDidChange(final NSNotification sender) {
        this.host.setWebURL(webURLField.stringValue());
        this.updateFavicon();
        this.itemChanged();
    }

    public void commentInputDidChange(final NSNotification sender) {
        this.host.setComment(commentField.textStorage().string());
        this.itemChanged();
    }

    /**
     * Updates the window title and url label with the properties of this bookmark
     * Propagates all fields with the properties of this bookmark
     */
    private void itemChanged() {
        HostCollection.defaultCollection().collectionItemChanged(host);
    }

    private void init() {
        window.setTitle(host.getNickname());
        this.updateField(hostField, host.getHostname());
        hostField.setEnabled(host.getProtocol().isHostnameConfigurable());
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
        final NSTextFieldCell usernameCell = usernameField.cell();
        if(host.getProtocol().equals(Protocol.S3)) {
            usernameCell.setPlaceholderString(
                    Locale.localizedString("Access Key ID", "S3")
            );
        }
        else {
            usernameCell.setPlaceholderString("");
        }
        protocolPopup.selectItemWithTitle(host.getProtocol().getDescription());
        if(null == host.getMaxConnections()) {
            transferPopup.selectItemWithTitle(DEFAULT);
        }
        else {
            transferPopup.selectItemWithTitle(
                    host.getMaxConnections() == 1 ? TRANSFER_BROWSERCONNECTION : TRANSFER_NEWCONNECTION);
        }
        connectmodePopup.setEnabled(host.getProtocol().equals(Protocol.FTP)
                || host.getProtocol().equals(Protocol.FTP_TLS));
        encodingPopup.setEnabled(host.getProtocol().equals(Protocol.FTP)
                || host.getProtocol().equals(Protocol.FTP_TLS) || host.getProtocol().equals(Protocol.SFTP));
        if(host.getProtocol().equals(Protocol.FTP)
                || host.getProtocol().equals(Protocol.FTP_TLS)) {
            if(null == host.getFTPConnectMode()) {
                connectmodePopup.selectItemWithTitle(DEFAULT);
            }
            else if(host.getFTPConnectMode().equals(FTPConnectMode.PASV)) {
                connectmodePopup.selectItemWithTitle(CONNECTMODE_PASSIVE);
            }
            else if(host.getFTPConnectMode().equals(FTPConnectMode.ACTIVE)) {
                connectmodePopup.selectItemWithTitle(CONNECTMODE_ACTIVE);
            }
        }
        pkCheckbox.setEnabled(host.getProtocol().equals(Protocol.SFTP));
        if(host.getCredentials().isPublicKeyAuthentication()) {
            pkCheckbox.setState(NSCell.NSOnState);
            this.updateField(pkLabel, host.getCredentials().getIdentity().toURL());
        }
        else {
            pkCheckbox.setState(NSCell.NSOffState);
            pkLabel.setStringValue(Locale.localizedString("No Private Key selected"));
        }
        webURLField.setEnabled(host.getProtocol().isWebUrlConfigurable());
        webUrlImage.setToolTip(host.getWebURL());
        this.updateField(webURLField, host.getWebURL());
        this.updateField(commentField, host.getComment());
        final boolean tzEnabled = this.host.getProtocol().equals(Protocol.FTP)
                || this.host.getProtocol().equals(Protocol.FTP_TLS);
        this.timezonePopup.setEnabled(tzEnabled);
        if(null == this.host.getTimezone()) {
            if(tzEnabled) {
                if(Preferences.instance().getBoolean("ftp.timezone.auto")) {
                    this.timezonePopup.setTitle(AUTO);
                }
                else {
                    this.timezonePopup.setTitle(
                            TimeZone.getTimeZone(Preferences.instance().getProperty("ftp.timezone.default")).getID()
                    );
                }
            }
            else {
                this.timezonePopup.setTitle(UTC.getID());
            }
        }
        else {
            this.timezonePopup.setTitle(this.host.getTimezone().getID());
        }
    }
}