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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.application.NSControl;
import com.apple.cocoa.application.NSEvent;
import com.apple.cocoa.application.NSOpenPanel;
import com.apple.cocoa.application.NSPopUpButton;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSNotificationCenter;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDBookmarkController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDBookmarkController.class);

    private Host host;

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    public void windowWillClose(NSNotification notification) {
        CDBookmarkTableDataSource.instance().save();
        super.windowWillClose(notification);
    }

    private NSPopUpButton protocolPopup; // IBOutlet

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.removeAllItems();
        this.protocolPopup.addItemsWithTitles(new NSArray(new String[]{Session.FTP_STRING,
                Session.FTP_TLS_STRING,
                Session.SFTP_STRING}));
        this.protocolPopup.itemWithTitle(Session.FTP_STRING).setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
        this.protocolPopup.itemWithTitle(Session.FTP_STRING).setKeyEquivalent("f");
        this.protocolPopup.itemWithTitle(Session.SFTP_STRING).setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
        this.protocolPopup.itemWithTitle(Session.SFTP_STRING).setKeyEquivalent("s");
        this.protocolPopup.setTarget(this);
        this.protocolPopup.setAction(new NSSelector("protocolSelectionChanged", new Class[]{Object.class}));
    }

    public void protocolSelectionChanged(NSPopUpButton sender) {
        log.debug("protocolSelectionChanged:" + sender);
        if (protocolPopup.selectedItem().title().equals(Session.SFTP_STRING)) {
            this.host.setProtocol(Session.SFTP);
            this.host.setPort(Session.SSH_PORT);
        }
        if (protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING)) {
            this.host.setProtocol(Session.FTP_TLS);
            this.host.setPort(Session.FTP_PORT);
        }
        if (protocolPopup.selectedItem().title().equals(Session.FTP_STRING)) {
            this.host.setProtocol(Session.FTP);
            this.host.setPort(Session.FTP_PORT);
        }
        this.updateFields();
    }

    private NSPopUpButton encodingPopup; // IBOutlet

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        java.util.SortedMap charsets = java.nio.charset.Charset.availableCharsets();
        String[] items = new String[charsets.size()];
        java.util.Iterator iterator = charsets.values().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            items[i] = ((java.nio.charset.Charset) iterator.next()).name();
            i++;
        }
        this.encodingPopup.addItemsWithTitles(new NSArray(items));
        this.encodingPopup.setTitle(this.host.getEncoding());
        this.encodingPopup.setTarget(this);
        this.encodingPopup.setAction(new NSSelector("encodingSelectionChanged", new Class[]{Object.class}));
    }

    public void encodingSelectionChanged(NSPopUpButton sender) {
        log.debug("encodingSelectionChanged:" + sender);
        this.host.setEncoding(sender.titleOfSelectedItem());
    }

    private NSTextField nicknameField; // IBOutlet

    public void setNicknameField(NSTextField nicknameField) {
        this.nicknameField = nicknameField;
    }

    private NSTextField hostField; // IBOutlet

    public void setHostField(NSTextField hostField) {
        this.hostField = hostField;
    }

    private NSTextField portField; // IBOutlet

    public void setPortField(NSTextField portField) {
        this.portField = portField;
    }

    private NSTextField pathField; // IBOutlet

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
    }

    private NSTextField urlField; // IBOutlet

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
    }

    private NSTextField usernameField; // IBOutlet

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
    }

    private NSPopUpButton connectmodePopup; //IBOutlet

    private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active", "");
    private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive", "");

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.setTarget(this);
        this.connectmodePopup.setAction(new NSSelector("connectmodePopupClicked", new Class[]{NSPopUpButton.class}));
        this.connectmodePopup.removeAllItems();
        this.connectmodePopup.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
        this.connectmodePopup.itemWithTitle(CONNECTMODE_PASSIVE).setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
        this.connectmodePopup.itemWithTitle(CONNECTMODE_PASSIVE).setKeyEquivalent("p");
        this.connectmodePopup.itemWithTitle(CONNECTMODE_ACTIVE).setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
        this.connectmodePopup.itemWithTitle(CONNECTMODE_ACTIVE).setKeyEquivalent("a");
        if (this.host.getProtocol().equals(Session.FTP)) {
            if (host.getFTPConnectMode().equals(com.enterprisedt.net.ftp.FTPConnectMode.PASV)) {
                this.connectmodePopup.setTitle(CONNECTMODE_PASSIVE);
            }
            if (host.getFTPConnectMode().equals(com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE)) {
                this.connectmodePopup.setTitle(CONNECTMODE_ACTIVE);
            }
        }
    }

    public void connectmodePopupClicked(NSPopUpButton sender) {
        if (sender.selectedItem().title().equals(CONNECTMODE_ACTIVE)) {
            this.host.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE);
        }
        if (sender.selectedItem().title().equals(CONNECTMODE_PASSIVE)) {
            this.host.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.PASV);
        }
    }

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    public CDBookmarkController(Host host) {
        this.host = host;
        if (!NSApplication.loadNibNamed("Bookmark", this)) {
            log.fatal("Couldn't load Bookmark.nib");
        }
    }

    public void awakeFromNib() {
        super.awakeFromNib();

        this.cascade();
        this.window.setTitle(this.host.getNickname());
        // Live editing of values
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("hostInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                hostField);
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("portInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                portField);
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("pathInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                pathField);
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("nicknameInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                nicknameField);
        (NSNotificationCenter.defaultCenter()).addObserver(this,
                new NSSelector("usernameInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                usernameField);
        this.updateFields();
    }

    private NSTextField pkLabel;

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
        this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
    }

    private NSButton pkCheckbox;

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this);
        this.pkCheckbox.setAction(new NSSelector("pkCheckboxSelectionChanged", new Class[]{Object.class}));
    }

    public void pkCheckboxSelectionChanged(Object sender) {
        log.debug("pkCheckboxSelectionChanged");
        if (this.pkLabel.stringValue().equals(NSBundle.localizedString("No Private Key selected", ""))) {
            NSOpenPanel panel = NSOpenPanel.openPanel();
            panel.setCanChooseDirectories(false);
            panel.setCanChooseFiles(true);
            panel.setAllowsMultipleSelection(false);
            panel.beginSheetForDirectory(System.getProperty("user.home") + "/.ssh", null, null, this.window(),
                    this,
                    new NSSelector("pkSelectionPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
        }
        else {
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
            this.host.getCredentials().setPrivateKeyFile(null);
        }
    }

    public void pkSelectionPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
        log.debug("pkSelectionPanelDidEnd");
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn): {
                NSArray selected = sheet.filenames();
                java.util.Enumeration enumerator = selected.objectEnumerator();
                while (enumerator.hasMoreElements()) {
                    String pk = (String) enumerator.nextElement();
                    this.host.getCredentials().setPrivateKeyFile(pk);
                    this.pkLabel.setStringValue(pk);
                }
                break;
            }
            case (NSAlertPanel.AlternateReturn): {
                this.host.getCredentials().setPrivateKeyFile(null);
                this.pkCheckbox.setState(NSCell.OffState);
                this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
                break;
            }
        }
    }

    public void hostInputDidEndEditing(NSNotification sender) {
        log.debug("hostInputDidEndEditing");
        this.host.setHostname(hostField.stringValue());
        this.updateFields();
    }

    public void portInputDidEndEditing(NSNotification sender) {
        log.debug("hostInputDidEndEditing");
        this.host.setPort(Integer.parseInt(portField.stringValue()));
        this.updateFields();
    }

    public void pathInputDidEndEditing(NSNotification sender) {
        log.debug("pathInputDidEndEditing");
        this.host.setDefaultPath(pathField.stringValue());
        this.updateFields();
    }

    public void nicknameInputDidEndEditing(NSNotification sender) {
        log.debug("nicknameInputDidEndEditing");
        this.host.setNickname(nicknameField.stringValue());
        this.updateFields();
    }

    public void usernameInputDidEndEditing(NSNotification sender) {
        log.debug("usernameInputDidEndEditing");
        this.host.getCredentials().setUsername(usernameField.stringValue());
        this.updateFields();
    }

    private void updateFields() {
        this.window.setTitle(this.host.getNickname());
        this.urlField.setStringValue(this.host.getURL() + host.getDefaultPath());
        this.hostField.setStringValue(this.host.getHostname());
        this.portField.setStringValue("" + this.host.getPort());
        this.nicknameField.setStringValue(this.host.getNickname());
        this.pathField.setStringValue(this.host.getDefaultPath());
        this.usernameField.setStringValue(this.host.getCredentials().getUsername());
        if (this.host.getProtocol().equals(Session.FTP)) {
            this.protocolPopup.setTitle(Session.FTP_STRING);
        }
        if (this.host.getProtocol().equals(Session.FTP_TLS)) {
            this.protocolPopup.setTitle(Session.FTP_TLS_STRING);
        }
        if (this.host.getProtocol().equals(Session.SFTP)) {
            this.protocolPopup.setTitle(Session.SFTP_STRING);
        }
        this.connectmodePopup.setEnabled(this.host.getProtocol().equals(Session.FTP));
        this.pkCheckbox.setEnabled(this.host.getProtocol().equals(Session.SFTP));
        if (this.host.getCredentials().usesPublicKeyAuthentication()) {
            this.pkCheckbox.setState(NSCell.OnState);
            this.pkLabel.setStringValue(this.host.getCredentials().getPrivateKeyFile());
        }
        else {
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
        CDBookmarkTableDataSource.instance().collectionItemChanged(host);
    }
}
