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

import com.enterprisedt.net.ftp.FTPConnectMode;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionImpl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id$
 */
public class CDConnectionController extends CDSheetController {
    private static Logger log = Logger.getLogger(CDConnectionController.class);

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSPopUpButton bookmarksPopup;

    private CollectionListener bookmarkCollectionListener;

    private Map bookmarks = new HashMap();

    public void setBookmarksPopup(NSPopUpButton button) {
        this.bookmarksPopup = button;
        this.bookmarksPopup.setImage(NSImage.imageNamed("bookmarks.tiff"));
        this.bookmarksPopup.setToolTip(NSBundle.localizedString("Bookmarks", ""));
        Iterator iter = HostCollection.instance().iterator();
        while(iter.hasNext()) {
            Host bookmark = (Host) iter.next();
            bookmarksPopup.addItem(bookmark.getNickname());
            bookmarks.put(bookmark.getNickname(), bookmark);
        }
        HostCollection.instance().addListener(this.bookmarkCollectionListener = new CollectionListener() {
            public void collectionItemAdded(Object item) {
                Host bookmark = (Host) item;
                bookmarksPopup.addItem(bookmark.getNickname());
                bookmarks.put(bookmark.getNickname(), bookmark);
            }

            public void collectionItemRemoved(Object item) {
                Host bookmark = (Host) item;
                bookmarksPopup.removeItemWithTitle(bookmark.getNickname());
                bookmarks.remove(bookmark.getNickname());
            }

            public void collectionItemChanged(Object item) {
                ;
            }
        });
        this.bookmarksPopup.setTarget(this);
        final NSSelector action = new NSSelector("bookmarksPopupSelectionChanged", new Class[]{Object.class});
        this.bookmarksPopup.setAction(action);
    }

    public void bookmarksPopupSelectionChanged(final NSPopUpButton sender) {
        this.bookmarkSelectionDidChange((Host) bookmarks.get(sender.titleOfSelectedItem()));
    }

    private NSPopUpButton historyPopup;

    private Map history = new HashMap();

    protected static final File HISTORY_FOLDER
            = new File(Preferences.instance().getProperty("application.support.path"), "History");

    static {
        HISTORY_FOLDER.mkdirs();
    }

    public void setHistoryPopup(NSPopUpButton historyPopup) {
        this.historyPopup = historyPopup;
        this.historyPopup.setImage(NSImage.imageNamed("history.tiff"));
        this.historyPopup.setToolTip(NSBundle.localizedString("History", ""));
        File[] files = HISTORY_FOLDER.listFiles(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".duck");
            }
        });
        for(int i = 0; i < files.length; i++) {
            Host h = ((CDMainController)NSApplication.sharedApplication().delegate()).importBookmark(files[i]);
            historyPopup.addItem(h.getNickname());
            history.put(h.getNickname(), h);
        }
        this.historyPopup.setTarget(this);
        this.historyPopup.setAction(new NSSelector("historyPopupSelectionChanged", new Class[]{Object.class}));
    }

    public void historyPopupSelectionChanged(final NSPopUpButton sender) {
        this.bookmarkSelectionDidChange((Host) history.get(sender.titleOfSelectedItem()));
    }

    private NSPopUpButton rendezvousPopup;

    private void addItemToRendezvousPopup(String item) {
        this.rendezvousPopup.addItem(item);
    }

    private void removeItemFromRendezvousPopup(String item) {
        this.rendezvousPopup.removeItemWithTitle(item);
    }

    private RendezvousListener rendezvousListener;

    public void setRendezvousPopup(NSPopUpButton rendezvousPopup) {
        this.rendezvousPopup = rendezvousPopup;
        this.rendezvousPopup.setImage(NSImage.imageNamed("rendezvous16.tiff"));
        this.rendezvousPopup.setToolTip("Bonjour");
        this.rendezvousPopup.setTarget(this);
        this.rendezvousPopup.setAction(new NSSelector("rendezvousSelectionDidChange", new Class[]{Object.class}));
        for(Iterator iter = Rendezvous.instance().getServices().iterator(); iter.hasNext();) {
            this.addItemToRendezvousPopup(Rendezvous.instance().getDisplayedName((String) iter.next()));
        }
        Rendezvous.instance().addListener(rendezvousListener = new RendezvousListener() {
            public void serviceResolved(final String servicename) {
                CDConnectionController.this.invoke(new Runnable() {
                    public void run() {
                        addItemToRendezvousPopup(Rendezvous.instance().getDisplayedName(servicename));
                    }
                });
            }

            public void serviceLost(final String servicename) {
                CDConnectionController.this.invoke(new Runnable() {
                    public void run() {
                        removeItemFromRendezvousPopup(Rendezvous.instance().getDisplayedName(servicename));
                    }
                });
            }
        });
    }

    public void rendezvousSelectionDidChange(final NSPopUpButton sender) {
        this.bookmarkSelectionDidChange((Host) Rendezvous.instance().getServiceWithDisplayedName(
                rendezvousPopup.titleOfSelectedItem()));
    }

    private NSPopUpButton protocolPopup;

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.removeAllItems();
        this.protocolPopup.addItemsWithTitles(new NSArray(new String[]{Session.FTP_STRING, Session.FTP_TLS_STRING, Session.SFTP_STRING}));
        this.protocolPopup.setTarget(this);
        this.protocolPopup.setAction(new NSSelector("protocolSelectionDidChange", new Class[]{Object.class}));
    }

    public void protocolSelectionDidChange(final NSNotification sender) {
        log.debug("protocolSelectionDidChange:" + sender);
        if(protocolPopup.selectedItem().title().equals(Session.FTP_STRING)) {
            this.portField.setIntValue(Session.FTP_PORT);
        }
        if(protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING)) {
            this.portField.setIntValue(Session.FTP_PORT);
        }
        if(protocolPopup.selectedItem().title().equals(Session.SFTP_STRING)) {
            this.portField.setIntValue(Session.SSH_PORT);
        }
        this.connectmodePopup.setEnabled(protocolPopup.selectedItem().title().equals(Session.FTP_STRING)
                || protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING));
        this.pkCheckbox.setEnabled(protocolPopup.selectedItem().title().equals(Session.SFTP_STRING));
        this.updateURLLabel(null);
    }

    private NSComboBox hostField;
    private Object hostPopupDataSource;

    public void setHostPopup(NSComboBox hostPopup) {
        this.hostField = hostPopup;
        this.hostField.setTarget(this);
        this.hostField.setAction(new NSSelector("updateURLLabel", new Class[]{Object.class}));
        this.hostField.setUsesDataSource(true);
        this.hostField.setDataSource(this.hostPopupDataSource = new Object() {
            public int numberOfItemsInComboBox(final NSComboBox sender) {
                return HostCollection.instance().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(final NSComboBox sender, final int row) {
                if(row < this.numberOfItemsInComboBox(sender)) {
                    return ((Host) HostCollection.instance().get(row)).getHostname();
                }
                return null;
            }
        });
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("hostFieldTextDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.hostField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("getPasswordFromKeychain", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                this.hostField);
    }

    public void hostFieldTextDidChange(final NSNotification sender) {
        try {
            final Host h = Host.parse(hostField.stringValue());
            this.hostField.setStringValue(h.getHostname());
            if(h.getProtocol().equals(Session.FTP))
                this.protocolPopup.selectItemWithTitle(Session.FTP_STRING);
            if(h.getProtocol().equals(Session.FTP_TLS))
                this.protocolPopup.selectItemWithTitle(Session.FTP_TLS_STRING);
            if(h.getProtocol().equals(Session.SFTP))
                this.protocolPopup.selectItemWithTitle(Session.SFTP_STRING);
            this.portField.setStringValue(String.valueOf(h.getPort()));
            this.usernameField.setStringValue(h.getCredentials().getUsername());
            this.pathField.setStringValue(h.getDefaultPath());
        }
        catch(java.net.MalformedURLException e) {
            // ignore; just a hostname has been entered
        }
        final String hostname = hostField.stringValue();
        this.background(new BackgroundActionImpl(this) {
            boolean reachable = false;

            public void run() {
                reachable = new Host(hostname).isReachable();
            }

            public void cleanup() {
                alertIcon.setHidden(reachable);
            }
        }, this);
    }

    private NSButton alertIcon; // IBOutlet

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setHidden(true);
        this.alertIcon.setTarget(this);
        this.alertIcon.setAction(new NSSelector("launchNetworkAssistant", new Class[]{NSButton.class}));
    }

    public void launchNetworkAssistant(final NSButton sender) {
        try {
            Host.parse(urlLabel.stringValue()).diagnose();
        }
        catch(MalformedURLException e) {
            new Host(hostField.stringValue()).diagnose();
        }
    }

    private NSTextField pathField;

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("pathInputDidEndEditing", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                this.pathField);
    }

    public void pathInputDidEndEditing(final NSNotification sender) {
        if(null == pathField.stringValue() || "".equals(pathField.stringValue())) {
            return;
        }
        this.pathField.setStringValue(Path.normalize(pathField.stringValue()));
        this.updateURLLabel(sender);
    }

    private NSTextField portField;

    public void setPortField(NSTextField portField) {
        this.portField = portField;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("portFieldTextDidChange", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.portField);
    }

    public void portFieldTextDidChange(final NSNotification sender) {
        if(null == this.portField.stringValue() || this.portField.stringValue().equals("")) {
            if(protocolPopup.selectedItem().title().equals(Session.SFTP_STRING)) {
                this.portField.setStringValue("" + Session.SSH_PORT);
            }
            if(protocolPopup.selectedItem().title().equals(Session.FTP_STRING)) {
                this.portField.setStringValue("" + Session.FTP_PORT);
            }
            if(protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING)) {
                this.portField.setStringValue("" + Session.FTP_PORT);
            }
        }
    }

    private NSTextField usernameField;

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
        this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("getPasswordFromKeychain", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidEndEditingNotification,
                this.usernameField);
    }

    private NSTextField passField;

    public void setPassField(NSTextField passField) {
        this.passField = passField;
    }

    private NSTextField pkLabel;

    public void setPkLabel(NSTextField pkLabel) {
        this.pkLabel = pkLabel;
        this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
    }

    private NSButton keychainCheckbox;

    public void setKeychainCheckbox(NSButton keychainCheckbox) {
        this.keychainCheckbox = keychainCheckbox;
        this.keychainCheckbox.setState(NSCell.OffState);
    }

    private NSButton anonymousCheckbox; //IBOutlet

    public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
        this.anonymousCheckbox = anonymousCheckbox;
        this.anonymousCheckbox.setTarget(this);
        this.anonymousCheckbox.setAction(new NSSelector("anonymousCheckboxClicked", new Class[]{NSButton.class}));
        this.anonymousCheckbox.setState(NSCell.OffState);
    }

    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.OnState) {
            this.usernameField.setEnabled(false);
            this.usernameField.setStringValue(Preferences.instance().getProperty("ftp.anonymous.name"));
            this.passField.setEnabled(false);
        }
        if(sender.state() == NSCell.OffState) {
            this.usernameField.setEnabled(true);
            this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
            this.passField.setEnabled(true);
        }
    }

    private NSButton pkCheckbox;

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this);
        this.pkCheckbox.setAction(new NSSelector("pkCheckboxSelectionDidChange", new Class[]{Object.class}));
        this.pkCheckbox.setState(NSCell.OffState);
        this.pkCheckbox.setEnabled(
                Preferences.instance().getProperty("connection.protocol.default").equals(Session.SFTP));
    }

    private NSOpenPanel publicKeyPanel;

    public void pkCheckboxSelectionDidChange(final NSNotification sender) {
        log.debug("pkCheckboxSelectionDidChange");
        if(this.pkLabel.stringValue().equals(NSBundle.localizedString("No Private Key selected", ""))) {
            publicKeyPanel = NSOpenPanel.openPanel();
            publicKeyPanel.setCanChooseDirectories(false);
            publicKeyPanel.setCanChooseFiles(true);
            publicKeyPanel.setAllowsMultipleSelection(false);
            publicKeyPanel.beginSheetForDirectory(NSPathUtilities.stringByExpandingTildeInPath("~/.ssh"),
                    null,
                    null,
                    this.window(),
                    this,
                    new NSSelector("pkSelectionPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}),
                    null);
        }
        else {
            this.passField.setEnabled(true);
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
    }

    public void pkSelectionPanelDidEnd(NSOpenPanel window, int returncode, Object context) {
        if(NSPanel.OKButton == returncode) {
            NSArray selected = window.filenames();
            java.util.Enumeration enumerator = selected.objectEnumerator();
            while(enumerator.hasMoreElements()) {
                String pk = NSPathUtilities.stringByAbbreviatingWithTildeInPath(
                        (String) enumerator.nextElement());
                this.pkLabel.setStringValue(pk);
            }
            this.passField.setEnabled(false);
        }
        if(NSPanel.CancelButton == returncode) {
            this.passField.setEnabled(true);
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
        publicKeyPanel = null;
    }

    private NSTextField urlLabel;

    public void setUrlLabel(NSTextField urlLabel) {
        this.urlLabel = urlLabel;
    }

    private NSPopUpButton encodingPopup; // IBOutlet

    public void setEncodingPopup(NSPopUpButton encodingPopup) {
        this.encodingPopup = encodingPopup;
        this.encodingPopup.setEnabled(true);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItem(DEFAULT);
        this.encodingPopup.menu().addItem(new NSMenuItem().separatorItem());
        this.encodingPopup.addItemsWithTitles(new NSArray(((CDMainController)NSApplication.sharedApplication().delegate()).availableCharsets()));
        this.encodingPopup.selectItemWithTitle(DEFAULT);
    }

    private NSPopUpButton connectmodePopup; //IBOutlet

    private static final String CONNECTMODE_ACTIVE = NSBundle.localizedString("Active", "");
    private static final String CONNECTMODE_PASSIVE = NSBundle.localizedString("Passive", "");

    public void setConnectmodePopup(NSPopUpButton connectmodePopup) {
        this.connectmodePopup = connectmodePopup;
        this.connectmodePopup.removeAllItems();
        this.connectmodePopup.addItem(DEFAULT);
        this.connectmodePopup.menu().addItem(new NSMenuItem().separatorItem());
        this.connectmodePopup.addItemsWithTitles(new NSArray(new String[]{CONNECTMODE_ACTIVE, CONNECTMODE_PASSIVE}));
        this.connectmodePopup.selectItemWithTitle(DEFAULT);
        this.connectmodePopup.setEnabled(
                Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP));
    }

    /**
     *
     * @param parent
     */
    public CDConnectionController(final CDWindowController parent) {
        super(parent);
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Connection", this)) {
                log.fatal("Couldn't load Connection.nib");
            }
        }
    }

    public void awakeFromNib() {
        //ControlTextDidChangeNotification
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.hostField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.pathField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.portField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{NSNotification.class}),
                NSControl.ControlTextDidChangeNotification,
                this.usernameField);

        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP)) {
            this.protocolPopup.selectItemWithTitle(Session.FTP_STRING);
            this.portField.setIntValue(Session.FTP_PORT);
        }
        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP_TLS)) {
            this.protocolPopup.selectItemWithTitle(Session.FTP_TLS_STRING);
            this.portField.setIntValue(Session.FTP_PORT);
        }
        if(Preferences.instance().getProperty("connection.protocol.default").equals(Session.SFTP)) {
            this.protocolPopup.selectItemWithTitle(Session.SFTP_STRING);
            this.portField.setIntValue(Session.SSH_PORT);
        }
    }

    /**
     * Updating the password field with the actual password if any
     * is avaialble for this hostname
     */
    public void getPasswordFromKeychain(final NSNotification sender) {
        if(Preferences.instance().getBoolean("connection.login.useKeychain")) {
            if(hostField.stringValue() != null && !hostField.stringValue().equals("") &&
                    usernameField.stringValue() != null && !usernameField.stringValue().equals("")) {
                String protocol = Preferences.instance().getProperty("connection.protocol.default");
                if(protocolPopup.selectedItem().title().equals(Session.SFTP_STRING)) {
                    protocol = Session.SFTP;
                }
                else if(protocolPopup.selectedItem().title().equals(Session.FTP_STRING)) {
                    protocol = Session.FTP;
                }
                else if(protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING)) {
                    protocol = Session.FTP_TLS;
                }
                Login l = new Login(hostField.stringValue(), protocol, usernameField.stringValue(), null);
                String passFromKeychain = l.getInternetPasswordFromKeychain();
                if(null == passFromKeychain || passFromKeychain.equals("")) {
                    passFromKeychain = l.getPasswordFromKeychain(); //legacy support
                }
                if(passFromKeychain != null && !passFromKeychain.equals("")) {
                    log.info("Password for " + usernameField.stringValue() + " found in Keychain");
                    this.passField.setStringValue(passFromKeychain);
                }
            }
        }
    }

    private void bookmarkSelectionDidChange(final Host selected) {
        if(selected.getProtocol().equals(Session.FTP)) {
            this.protocolPopup.selectItemWithTitle(Session.FTP_STRING);
        }
        if(selected.getProtocol().equals(Session.FTP_TLS)) {
            this.protocolPopup.selectItemWithTitle(Session.FTP_TLS_STRING);
        }
        if(selected.getProtocol().equals(Session.SFTP)) {
            this.protocolPopup.selectItemWithTitle(Session.SFTP_STRING);
        }
        this.hostField.setStringValue(selected.getHostname());
        this.portField.setIntValue(selected.getPort());
        this.pathField.setStringValue(selected.getDefaultPath());
        this.usernameField.setStringValue(selected.getCredentials().getUsername());
        this.connectmodePopup.setEnabled(selected.getProtocol().equals(Session.FTP)
                || selected.getProtocol().equals(Session.FTP_TLS));
        if(null == selected.getFTPConnectMode()) {
            this.connectmodePopup.selectItemWithTitle(DEFAULT);
        }
        else if(selected.getFTPConnectMode().equals(FTPConnectMode.PASV)) {
            this.connectmodePopup.selectItemWithTitle(CONNECTMODE_PASSIVE);
        }
        else if(selected.getFTPConnectMode().equals(FTPConnectMode.ACTIVE)) {
            this.connectmodePopup.selectItemWithTitle(CONNECTMODE_ACTIVE);
        }
        this.pkCheckbox.setEnabled(selected.getProtocol().equals(Session.SFTP));
        if(selected.getCredentials().getPrivateKeyFile() != null) {
            this.pkCheckbox.setState(NSCell.OnState);
            this.pkLabel.setStringValue(
                    selected.getCredentials().getPrivateKeyFile());
        }
        else {
            this.pkCheckbox.setState(NSCell.OffState);
        }
        if(null == selected.getEncoding()) {
            this.encodingPopup.selectItemWithTitle(DEFAULT);
        }
        else {
            this.encodingPopup.selectItemWithTitle(selected.getEncoding());
        }
        this.updateURLLabel(null);
    }

    private void updateURLLabel(final NSNotification sender) {
        if("".equals(hostField.stringValue())) {
            urlLabel.setStringValue(hostField.stringValue());
        }
        else {
        String protocol = null;
        if(protocolPopup.selectedItem().title().equals(Session.SFTP_STRING)) {
            protocol = Session.SFTP + "://";
        }
        if(protocolPopup.selectedItem().title().equals(Session.FTP_STRING)) {
            protocol = Session.FTP + "://";
        }
        if(protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING)) {
            protocol = Session.FTP_TLS + "://";
        }
        urlLabel.setStringValue(protocol + usernameField.stringValue()
                + "@" + hostField.stringValue() + ":" + portField.stringValue()
                + pathField.stringValue());
        }
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            Host host = null;
            if(protocolPopup.selectedItem().title().equals(Session.SFTP_STRING)) {
                // SFTP has been selected as the protocol to connect with
                host = new Host(Session.SFTP,
                        hostField.stringValue(),
                        Integer.parseInt(portField.stringValue()),
                        pathField.stringValue());
                if(pkCheckbox.state() == NSCell.OnState) {
                    host.getCredentials().setPrivateKeyFile(pkLabel.stringValue());
                }
            }
            else if(protocolPopup.selectedItem().title().equals(Session.FTP_STRING)) {
                // FTP has been selected as the protocol to connect with
                host = new Host(Session.FTP,
                        hostField.stringValue(),
                        Integer.parseInt(portField.stringValue()),
                        pathField.stringValue());
            }
            else if(protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING)) {
                // FTP has been selected as the protocol to connect with
                host = new Host(Session.FTP_TLS,
                        hostField.stringValue(),
                        Integer.parseInt(portField.stringValue()),
                        pathField.stringValue());
            }
            else {
                throw new IllegalArgumentException("No protocol selected.");
            }
            if(protocolPopup.selectedItem().title().equals(Session.FTP_STRING) ||
                    protocolPopup.selectedItem().title().equals(Session.FTP_TLS_STRING)) 
            {
                if(connectmodePopup.titleOfSelectedItem().equals(DEFAULT)) {
                    host.setFTPConnectMode(null);
                }
                else if(connectmodePopup.titleOfSelectedItem().equals(CONNECTMODE_ACTIVE)) {
                    host.setFTPConnectMode(FTPConnectMode.ACTIVE);
                }
                else if(connectmodePopup.titleOfSelectedItem().equals(CONNECTMODE_PASSIVE)) {
                    host.setFTPConnectMode(FTPConnectMode.PASV);
                }
            }
            host.setCredentials(usernameField.stringValue(), passField.stringValue(), keychainCheckbox.state() == NSCell.OnState);
            if(encodingPopup.titleOfSelectedItem().equals(DEFAULT)) {
                host.setEncoding(null);
            }
            else {
                host.setEncoding(encodingPopup.titleOfSelectedItem());
            }
            ((CDBrowserController) parent).mount(host);
        }
    }

    protected void invalidate() {
        HostCollection.instance().removeListener(this.bookmarkCollectionListener);
        Rendezvous.instance().removeListener(this.rendezvousListener);
        super.invalidate();
    }
}
