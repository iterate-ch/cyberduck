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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;

import org.apache.log4j.Logger;
import org.jets3t.service.Constants;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.enterprisedt.net.ftp.FTPConnectMode;

/**
 * @version $Id$
 */
public class CDBookmarkController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDBookmarkController.class);

    private NSPopUpButton protocolPopup; // IBOutlet

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.removeAllItems();
        this.protocolPopup.addItemsWithTitles(new NSArray(Protocol.getProtocolDescriptions()));
        final Protocol[] protocols = Protocol.getKnownProtocols();
        for(int i = 0; i < protocols.length; i++) {
            this.protocolPopup.itemWithTitle(protocols[i].getDescription()).setRepresentedObject(protocols[i]);
        }
        this.protocolPopup.setTarget(this);
        this.protocolPopup.setAction(new NSSelector("protocolSelectionChanged", new Class[]{Object.class}));
    }

    public void protocolSelectionChanged(final NSPopUpButton sender) {
        log.debug("protocolSelectionChanged:" + sender);
        final Protocol selected = (Protocol) protocolPopup.selectedItem().representedObject();
        this.host.setProtocol(selected);
        this.host.setPort(selected.getDefaultPort());
        if(selected.equals(Protocol.S3)) {
            this.host.setHostname(Constants.S3_HOSTNAME);
        }
        else {
            if(Constants.S3_HOSTNAME.equals(this.host.getHostname())) {
                this.host.setHostname(Preferences.instance().getProperty("connection.hostname.default"));
            }
        }
        this.itemChanged();
    }

    private NSPopUpButton encodingPopup; // IBOutlet

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItem(DEFAULT);
        this.encodingPopup.menu().addItem(new NSMenuItem().separatorItem());
        this.encodingPopup.addItemsWithTitles(new NSArray(
                ((CDMainController) NSApplication.sharedApplication().delegate()).availableCharsets()));
        if(null == this.host.getEncoding()) {
            this.encodingPopup.selectItemWithTitle(DEFAULT);
        }
        else {
            this.encodingPopup.selectItemWithTitle(this.host.getEncoding());
        }
        this.encodingPopup.setTarget(this);
        final NSSelector action = new NSSelector("encodingSelectionChanged", new Class[]{Object.class});
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

    private NSTextField nicknameField; // IBOutlet

    public void setNicknameField(NSTextField nicknameField) {
        this.nicknameField = nicknameField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("nicknameInputDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.nicknameField);
    }

    private NSTextField hostField; // IBOutlet

    public void setHostField(NSTextField hostField) {
        this.hostField = hostField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("hostFieldDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.hostField);
    }

    private NSButton alertIcon; // IBOutlet

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setHidden(true);
        this.alertIcon.setTarget(this);
        this.alertIcon.setAction(new NSSelector("launchNetworkAssistant", new Class[]{NSButton.class}));
    }

    public void launchNetworkAssistant(final NSButton sender) {
        this.host.diagnose();
    }

    private NSTextField portField; // IBOutlet

    public void setPortField(NSTextField portField) {
        this.portField = portField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("portInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.portField);
    }

    private NSTextField pathField; // IBOutlet

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("pathInputDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.pathField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("pathInputDidEnd", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                this.pathField);
    }

    private NSTextField urlField; // IBOutlet

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    private NSTextField usernameField; // IBOutlet

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("usernameInputDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.usernameField);
    }

    private NSTextField webURLField;

    public void setWebURLField(NSTextField webURLField) {
        this.webURLField = webURLField;
        ((NSTextFieldCell) this.webURLField.cell()).setPlaceholderString(
                host.getDefaultWebURL()
        );
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("webURLInputDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.webURLField);
    }

    private NSTextView commentField; // IBOutlet

    public void setCommentField(NSTextView commentField) {
        this.commentField = commentField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("commentInputDidChange", new Class[]{NSNotification.class}),
                NSText.TextDidChangeNotification,
                this.commentField);
    }

    private NSPopUpButton timezonePopup; //IBOutlet

    public void setTimezonePopup(NSPopUpButton timezonePopup) {
        this.timezonePopup = timezonePopup;
        this.timezonePopup.setTarget(this);
        final NSSelector action = new NSSelector("timezonePopupClicked", new Class[]{NSPopUpButton.class});
        this.timezonePopup.setAction(action);
        this.timezonePopup.removeAllItems();
        this.timezonePopup.addItem(DEFAULT);
        this.timezonePopup.menu().addItem(new NSMenuItem().separatorItem());
        String[] ids = TimeZone.getAvailableIDs();
        for(int i = 0; i < ids.length; i++) {
            this.timezonePopup.addItem(TimeZone.getTimeZone(ids[i]).getDisplayName());
        }
        if(this.host.getTimezone().equals(TimeZone.getDefault())) {
            this.timezonePopup.setTitle(DEFAULT);
        }
        else {
            this.timezonePopup.setTitle(this.host.getTimezone().getDisplayName());
        }
    }

    public void timezonePopupClicked(NSPopUpButton sender) {
        String selected = sender.selectedItem().title();
        if(selected.equals(DEFAULT)) {
            this.host.setTimezone(null);
        }
        else {
            String[] ids = TimeZone.getAvailableIDs();
            TimeZone tz = null;
            for(int i = 0; i < ids.length; i++) {
                if((tz = TimeZone.getTimeZone(ids[i])).getDisplayName().equals(selected)) {
                    this.host.setTimezone(tz);
                    break;
                }
            }
        }
        this.itemChanged();
    }

    private NSPopUpButton connectmodePopup; //IBOutlet

    private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active", "");
    private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive", "");

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.setTarget(this);
        final NSSelector action = new NSSelector("connectmodePopupClicked", new Class[]{NSPopUpButton.class});
        this.connectmodePopup.setAction(action);
        this.connectmodePopup.removeAllItems();
        this.connectmodePopup.addItem(DEFAULT);
        this.connectmodePopup.menu().addItem(new NSMenuItem().separatorItem());
        this.connectmodePopup.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
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

    private NSPopUpButton transferPopup; //IBOutlet

    public void setTransferPopup(NSPopUpButton transferPopup) {
        this.transferPopup = transferPopup;
        this.transferPopup.setTarget(this);
        this.transferPopup.setAction(new NSSelector("transferPopupClicked", new Class[]{NSPopUpButton.class}));
        if(null == host.getMaxConnections()) {
            this.transferPopup.selectItemAtIndex(DEFAULT_INDEX);
        }
        else {
            this.transferPopup.selectItemAtIndex(
                    host.getMaxConnections().intValue() == 1 ? USE_BROWSER_SESSION_INDEX : USE_QUEUE_SESSION_INDEX);
        }
    }

    private final int DEFAULT_INDEX = 0;
    private final int USE_QUEUE_SESSION_INDEX = 2;
    private final int USE_BROWSER_SESSION_INDEX = 3;

    public void transferPopupClicked(final NSPopUpButton sender) {
        if(sender.indexOfSelectedItem() == DEFAULT_INDEX) {
            this.host.setMaxConnections(null);
        }
        else if(sender.indexOfSelectedItem() == USE_BROWSER_SESSION_INDEX) {
            this.host.setMaxConnections(new Integer(1));
        }
        else if(sender.indexOfSelectedItem() == USE_QUEUE_SESSION_INDEX) {
            this.host.setMaxConnections(new Integer(-1));
        }
        this.itemChanged();
    }

    private NSPopUpButton downloadPathPopup; //IBOutlet

    private static final String CHOOSE = NSBundle.localizedString("Choose", "")+"...";

    public void setDownloadPathPopup(NSPopUpButton downloadPathPopup) {
        this.downloadPathPopup = downloadPathPopup;
        this.downloadPathPopup.setTarget(this);
        final NSSelector action = new NSSelector("downloadPathPopupClicked", new Class[]{NSPopUpButton.class});
        this.downloadPathPopup.setAction(action);
        this.downloadPathPopup.removeAllItems();

        // Default download folder
        this.addDownloadPath(action, host.getDownloadFolder());
        this.downloadPathPopup.menu().addItem(new NSMenuItem().separatorItem());
        // Shortcut to the Desktop
        this.addDownloadPath(action, new Local("~/Desktop"));
        // Shortcut to user home
        this.addDownloadPath(action, new Local("~"));
        // Shortcut to user downloads for 10.5
        this.addDownloadPath(action, new Local("~/Downloads"));
        // Choose another folder

        // Choose another folder
        this.downloadPathPopup.menu().addItem(new NSMenuItem().separatorItem());
        this.downloadPathPopup.menu().addItem(CHOOSE, action, "");
        this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems()-1).setTarget(this);
    }

    private void addDownloadPath(NSSelector action, Local f) {
        if(f.exists()) {
            this.downloadPathPopup.menu().addItem(NSPathUtilities.displayNameAtPath(
                    f.getAbsolute()), action, "");
            this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems()-1).setTarget(this);
            this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems()-1).setImage(
                    CDIconCache.instance().iconForPath(f, 16)
            );
            this.downloadPathPopup.itemAtIndex(this.downloadPathPopup.numberOfItems()-1).setRepresentedObject(
                    f.getAbsolute());
            if(host.getDownloadFolder().equals(f)) {
                this.downloadPathPopup.selectItemAtIndex(this.downloadPathPopup.numberOfItems()-1);
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
            downloadPathPanel.beginSheetForDirectory(null, null, null, this.window, this, new NSSelector("downloadPathPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
        }
        else {
            host.setDownloadFolder(NSPathUtilities.stringByAbbreviatingWithTildeInPath(
                    sender.representedObject().toString()));
            this.itemChanged();
        }
    }

    public void downloadPathPanelDidEnd(NSOpenPanel sheet, int returncode, Object contextInfo) {
        if(returncode == CDSheetCallback.DEFAULT_OPTION) {
            NSArray selected = sheet.filenames();
            String filename;
            if ((filename = (String) selected.lastObject()) != null) {
                host.setDownloadFolder(
                        NSPathUtilities.stringByAbbreviatingWithTildeInPath(filename));
            }
        }
        else {
            host.setDownloadFolder(null);
        }
        this.downloadPathPopup.itemAtIndex(0).setTitle(NSPathUtilities.displayNameAtPath(
                host.getDownloadFolder().getAbsolute()));
        this.downloadPathPopup.itemAtIndex(0).setRepresentedObject(
                host.getDownloadFolder().getAbsolute());
        this.downloadPathPopup.itemAtIndex(0).setImage(
                CDIconCache.instance().iconForPath(host.getDownloadFolder(), 16));
        this.downloadPathPopup.selectItemAtIndex(0);
        this.downloadPathPanel = null;
        this.itemChanged();
    }

    /**
     *
     */
    public static class Factory {
        private static final Map open = new HashMap();

        public static CDBookmarkController create(final Host host) {
            if(open.containsKey(host)) {
                return (CDBookmarkController) open.get(host);
            }
            final CDBookmarkController c = new CDBookmarkController(host) {
                public void windowWillClose(NSNotification notification) {
                    Factory.open.remove(host);
                    super.windowWillClose(notification);
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
        HostCollection.defaultCollection().addListener(new AbstractCollectionListener() {
            public void collectionItemRemoved(Object item) {
                if(item.equals(host)) {
                    HostCollection.defaultCollection().removeListener(this);
                    final NSWindow window = window();
                    if(null != window) {
                        window.close();
                    }
                }
            }
        });
        this.loadBundle();
    }

    protected String getBundleName() {
        return "Bookmark";
    }

    public void awakeFromNib() {
        this.cascade();
        this.init();
    }

    private NSTextField pkLabel;

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
    }

    private NSButton pkCheckbox;

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this);
        this.pkCheckbox.setAction(new NSSelector("pkCheckboxSelectionChanged", new Class[]{Object.class}));
    }

    private NSOpenPanel publicKeyPanel;

    public void pkCheckboxSelectionChanged(final NSButton sender) {
        log.debug("pkCheckboxSelectionChanged");
        if(this.pkLabel.stringValue().equals(NSBundle.localizedString("No Private Key selected", ""))) {
            publicKeyPanel = NSOpenPanel.openPanel();
            publicKeyPanel.setCanChooseDirectories(false);
            publicKeyPanel.setCanChooseFiles(true);
            publicKeyPanel.setAllowsMultipleSelection(false);
            publicKeyPanel.beginSheetForDirectory(NSPathUtilities.stringByExpandingTildeInPath("~/.ssh"), null, null, this.window(),
                    this,
                    new NSSelector("pkSelectionPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
        }
        else {
            this.host.getCredentials().setPrivateKeyFile(null);
            this.itemChanged();
        }
    }

    public void pkSelectionPanelDidEnd(NSOpenPanel sheet, int returncode, Object context) {
        log.debug("pkSelectionPanelDidEnd");
        if(returncode == NSPanel.OKButton) {
            NSArray selected = sheet.filenames();
            java.util.Enumeration enumerator = selected.objectEnumerator();
            while(enumerator.hasMoreElements()) {
                String pk = NSPathUtilities.stringByAbbreviatingWithTildeInPath(
                        (String) enumerator.nextElement());
                this.host.getCredentials().setPrivateKeyFile(pk);
            }
        }
        if(returncode == NSPanel.CancelButton) {
            this.host.getCredentials().setPrivateKeyFile(null);
        }
        publicKeyPanel = null;
        this.itemChanged();
    }

    public void hostFieldDidChange(final NSNotification sender) {
        if(StringUtils.isURL(hostField.stringValue())) {
            this.host.init(Host.parse(hostField.stringValue()).getAsDictionary());
        }
        else {
            this.host.setHostname(hostField.stringValue());
        }
        this.itemChanged();
        this.background(new BackgroundActionImpl(this) {
            boolean reachable = false;

            public void run() {
                reachable = host.isReachable();
            }

            public void cleanup() {
                alertIcon.setHidden(reachable);
            }
        }, this);
    }

    public void portInputDidEndEditing(final NSNotification sender) {
        try {
            this.host.setPort(Integer.parseInt(portField.stringValue()));
        }
        catch(NumberFormatException e) {
            this.host.setPort(-1);
        }
        this.itemChanged();
    }

    public void pathInputDidChange(final NSNotification sender) {
        this.host.setDefaultPath(pathField.stringValue());
        this.itemChanged();
    }

    public void pathInputDidEnd(final NSNotification sender) {
        this.itemChanged();
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
        this.itemChanged();
    }

    public void commentInputDidChange(final NSNotification sender) {
        this.host.setComment(commentField.textStorage().stringReference().string());
        this.itemChanged();
    }

    /**
     * Updates the window title and url label with the properties of this bookmark
     * Propagates all fields with the properties of this bookmark
     */
    private void itemChanged() {
        HostCollection.defaultCollection().collectionItemChanged(host);
        this.init();
    }

    private void init() {
        this.window.setTitle(this.host.getNickname());
        this.updateField(this.hostField, this.host.getHostname());
        this.hostField.setEnabled(!this.host.getProtocol().equals(Protocol.S3));
        this.updateField(this.nicknameField, this.host.getNickname());
        if(StringUtils.hasText(this.host.getDefaultPath())) {
            this.updateField(this.urlField, this.host.toURL() + Path.normalize(this.host.getDefaultPath()));
        }
        else {
            this.updateField(this.urlField, this.host.toURL());
        }
        this.updateField(this.portField, String.valueOf(this.host.getPort()));
        this.portField.setEnabled(!this.host.getProtocol().equals(Protocol.S3));
        this.updateField(this.pathField, this.host.getDefaultPath());
        this.updateField(this.usernameField, this.host.getCredentials().getUsername());
        if(this.host.getProtocol().equals(Protocol.S3)) {
            ((NSTextFieldCell) this.usernameField.cell()).setPlaceholderString(
                    NSBundle.localizedString("Access Key ID", "S3")
            );
        }
        else {
            ((NSTextFieldCell) this.usernameField.cell()).setPlaceholderString("");
        }
        this.protocolPopup.selectItemWithTitle(this.host.getProtocol().getDescription());
        this.connectmodePopup.setEnabled(this.host.getProtocol().equals(Protocol.FTP)
                || this.host.getProtocol().equals(Protocol.FTP_TLS));
        if(this.host.getProtocol().equals(Protocol.FTP)
                || this.host.getProtocol().equals(Protocol.FTP_TLS)) {
            if(null == host.getFTPConnectMode()) {
                this.connectmodePopup.selectItemWithTitle(DEFAULT);
            }
            else if(host.getFTPConnectMode().equals(FTPConnectMode.PASV)) {
                this.connectmodePopup.selectItemWithTitle(CONNECTMODE_PASSIVE);
            }
            else if(host.getFTPConnectMode().equals(FTPConnectMode.ACTIVE)) {
                this.connectmodePopup.selectItemWithTitle(CONNECTMODE_ACTIVE);
            }
        }
        this.pkCheckbox.setEnabled(this.host.getProtocol().equals(Protocol.SFTP));
        if(this.host.getCredentials().usesPublicKeyAuthentication()) {
            this.pkCheckbox.setState(NSCell.OnState);
            this.updateField(this.pkLabel, this.host.getCredentials().getPrivateKeyFile());
        }
        else {
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
        this.updateField(this.webURLField, this.host.getWebURL());
        this.updateField(this.commentField, this.host.getComment());
    }
}