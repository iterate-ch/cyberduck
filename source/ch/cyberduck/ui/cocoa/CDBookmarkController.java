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
import ch.cyberduck.core.HostCollection;
import ch.cyberduck.core.CollectionListener;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;

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
        this.protocolPopup.addItemsWithTitles(new NSArray(new String[]{Session.FTP_STRING,
                Session.FTP_TLS_STRING,
                Session.SFTP_STRING}));
        this.protocolPopup.setTarget(this);
        this.protocolPopup.setAction(new NSSelector("protocolSelectionChanged", new Class[]{Object.class}));
    }

    public void protocolSelectionChanged(final NSPopUpButton sender) {
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
        HostCollection.instance().collectionItemChanged(this.host);
    }

    private NSPopUpButton encodingPopup; // IBOutlet

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemsWithTitles(new NSArray(((CDMainController)NSApplication.sharedApplication().delegate()).availableCharsets()));
        this.encodingPopup.setTitle(this.host.getEncoding());
        this.encodingPopup.setTarget(this);
        this.encodingPopup.setAction(new NSSelector("encodingSelectionChanged", new Class[]{Object.class}));
    }

    public void encodingSelectionChanged(final NSPopUpButton sender) {
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
        this.connectmodePopup.itemWithTitle(CONNECTMODE_ACTIVE).setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
        if (this.host.getProtocol().equals(Session.FTP) || this.host.getProtocol().equals(Session.FTP_TLS)) {
            if (host.getFTPConnectMode().equals(com.enterprisedt.net.ftp.FTPConnectMode.PASV)) {
                this.connectmodePopup.setTitle(CONNECTMODE_PASSIVE);
            }
            if (host.getFTPConnectMode().equals(com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE)) {
                this.connectmodePopup.setTitle(CONNECTMODE_ACTIVE);
            }
        }
    }

    public void connectmodePopupClicked(final NSPopUpButton sender) {
        if (sender.selectedItem().title().equals(CONNECTMODE_ACTIVE)) {
            this.host.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.ACTIVE);
        }
        if (sender.selectedItem().title().equals(CONNECTMODE_PASSIVE)) {
            this.host.setFTPConnectMode(com.enterprisedt.net.ftp.FTPConnectMode.PASV);
        }
    }

    private Host host;

    private static final Object lock = new Object();

    /**
     *
     */
    public static class Factory {
        private static Map open = new HashMap();

        public static CDBookmarkController create(final Host host) {
            if(open.containsKey(host)) {
                return (CDBookmarkController)open.get(host);
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
     *
     * @param host
     */
    private CDBookmarkController(final Host host) {
        this.host = host;
        HostCollection.instance().addListener(new CollectionListener() {
            public void collectionItemAdded(Object item) {
                ;
            }

            public void collectionItemRemoved(Object item) {
                if(item.equals(host)) {
                    HostCollection.instance().removeListener(this);
                    final NSWindow window = window();
                    if(null != window) {
                        window.close();
                    }
                }
            }

            public void collectionItemChanged(Object item) {
                ;
            }
        });
        synchronized(lock) {
            if (!NSApplication.loadNibNamed("Bookmark", this)) {
                log.fatal("Couldn't load Bookmark.nib");
            }
        }
    }

    public void windowWillClose(NSNotification notification) {
        HostCollection.instance().save();
        super.windowWillClose(notification);
    }

    public void awakeFromNib() {
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

    public void pkCheckboxSelectionChanged(final NSButton sender) {
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

    public void pkSelectionPanelDidEnd(NSOpenPanel sheet, int returncode, Object context) {
        log.debug("pkSelectionPanelDidEnd");
        if(returncode == NSPanel.OKButton) {
            NSArray selected = sheet.filenames();
            java.util.Enumeration enumerator = selected.objectEnumerator();
            while (enumerator.hasMoreElements()) {
                String pk = NSPathUtilities.stringByAbbreviatingWithTildeInPath(
                        (String) enumerator.nextElement());
                this.host.getCredentials().setPrivateKeyFile(pk);
                this.pkLabel.setStringValue(pk);
            }
        }
        if(returncode == NSPanel.CancelButton) {
            this.host.getCredentials().setPrivateKeyFile(null);
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
    }

    public void hostInputDidEndEditing(final NSNotification sender) {
        final String hostname = hostField.stringValue();
        this.host.setHostname(hostname);
        this.updateFields();
        HostCollection.instance().collectionItemChanged(this.host);
        new Thread() {
            public void run() {
                final int pool = NSAutoreleasePool.push();
                final boolean reachable = new Host(hostname).isReachable();
                NSAutoreleasePool.pop(pool);
                CDBookmarkController.this.invoke(new Runnable() {
                    public void run() {
                        synchronized(lock) {
                            alertIcon.setHidden(reachable);
                        }
                    }
                });
            }
        }.start();
    }

    public void portInputDidEndEditing(final NSNotification sender) {
        this.host.setPort(Integer.parseInt(portField.stringValue()));
        this.updateFields();
        HostCollection.instance().collectionItemChanged(this.host);
    }

    public void pathInputDidEndEditing(final NSNotification sender) {
        this.host.setDefaultPath(pathField.stringValue());
        this.updateFields();
        HostCollection.instance().collectionItemChanged(this.host);
    }

    public void nicknameInputDidEndEditing(final NSNotification sender) {
        this.host.setNickname(nicknameField.stringValue());
        this.updateFields();
        HostCollection.instance().collectionItemChanged(this.host);
    }

    public void usernameInputDidEndEditing(final NSNotification sender) {
        this.host.getCredentials().setUsername(usernameField.stringValue());
        this.updateFields();
        HostCollection.instance().collectionItemChanged(this.host);
    }

    private void updateFields() {
        try {
            this.host = Host.parse(hostField.stringValue());
        }
        catch(MalformedURLException e) {
            // ignore; just a hostname has been entered
        }
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
        this.connectmodePopup.setEnabled(this.host.getProtocol().equals(Session.FTP) ||
            this.host.getProtocol().equals(Session.FTP_TLS));
        this.pkCheckbox.setEnabled(this.host.getProtocol().equals(Session.SFTP));
        if (this.host.getCredentials().usesPublicKeyAuthentication()) {
            this.pkCheckbox.setState(NSCell.OnState);
            this.pkLabel.setStringValue(this.host.getCredentials().getPrivateKeyFile());
        }
        else {
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
    }
}
