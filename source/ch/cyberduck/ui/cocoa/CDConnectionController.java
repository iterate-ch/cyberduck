package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDConnectionController extends NSObject implements Observer {
    private static Logger log = Logger.getLogger(CDConnectionController.class);

    private static final String FTP_STRING = NSBundle.localizedString("FTP (File Transfer)", "");
//    private static final String FTP_SSL_STRING = NSBundle.localizedString("FTP-SSL (File Transfer over TLS/SSL)", "");
    private static final String SFTP_STRING = NSBundle.localizedString("SFTP (SSH Secure File Transfer)", "");
	
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
	
    private NSWindow window;

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }

    public NSWindow window() {
        return this.window;
    }
	
    /*
	 private NSPopUpButton historyPopup;
	 
	 public void setHistoryPopup(NSPopUpButton historyPopup) {
		 this.historyPopup = historyPopup;
		 this.historyPopup.setImage(NSImage.imageNamed("history.tiff"));
		 Iterator i = CDHistoryImpl.instance().iterator();
		 while (i.hasNext()) {
			 historyPopup.addItem(i.next().toString());
		 }
		 //		this.historyPopup.addItem("Clear");
		 this.historyPopup.setTarget(this);
		 this.historyPopup.setAction(new NSSelector("historySelectionChanged", new Class[]{Object.class}));
	 }
	 
	 public void historySelectionChanged(Object sender) {
		 log.debug("historySelectionChanged");
		 //		if(historyPopup.titleOfSelectedItem().equals("Clear")) {
		 //			CDHistoryImpl.instance().clear();
		 //			historyPopup.removeAllItems();
		 //		}
		 this.selectionChanged(CDHistoryImpl.instance().getItem(historyPopup.indexOfSelectedItem() - 1));
	 }
     */
	
    private NSPopUpButton bookmarksPopup;

    public void setBookmarksPopup(NSPopUpButton bookmarksPopup) {
        this.bookmarksPopup = bookmarksPopup;
        this.bookmarksPopup.setImage(NSImage.imageNamed("bookmarks.tiff"));
        Iterator i = CDBookmarkTableDataSource.instance().iterator();
        while (i.hasNext()) {
            bookmarksPopup.addItem(i.next().toString());
        }
        this.bookmarksPopup.setTarget(this);
        this.bookmarksPopup.setAction(new NSSelector("bookmarksSelectionChanged", new Class[]{Object.class}));
    }

    public void bookmarksSelectionChanged(Object sender) {
        log.debug("bookmarksSelectionChanged");
        this.selectionChanged(CDBookmarkTableDataSource.instance().getItem(bookmarksPopup.indexOfSelectedItem()));
    }

    private Rendezvous rendezvous;
    private NSPopUpButton rendezvousPopup;

    public void setRendezvousPopup(NSPopUpButton rendezvousPopup) {
        this.rendezvousPopup = rendezvousPopup;
        this.rendezvousPopup.setImage(NSImage.imageNamed("rendezvous16.tiff"));
        this.rendezvousPopup.setTarget(this);
        this.rendezvousPopup.setAction(new NSSelector("rendezvousSelectionChanged", new Class[]{Object.class}));
        this.rendezvous = new Rendezvous();
        this.rendezvous.addObserver(this);
        this.rendezvous.init();
    }

    public void rendezvousSelectionChanged(Object sender) {
        log.debug("rendezvousSelectionChanged:" + sender);
        this.selectionChanged((Host)rendezvous.getService(rendezvousPopup.titleOfSelectedItem()));
    }

    public void update(final Observable o, final Object arg) {
        log.debug("update:" + o + "," + arg);
        ThreadUtilities.instance().invokeLater(new Runnable() {
            public void run() {
                if (o instanceof Rendezvous) {
                    if (arg instanceof Message) {
                        Message msg = (Message)arg;
                        if (msg.getTitle().equals(Message.RENDEZVOUS_ADD)) {
                            rendezvousPopup.addItem((String)msg.getContent());
                        }
                        if (msg.getTitle().equals(Message.RENDEZVOUS_REMOVE)) {
                            rendezvousPopup.removeItemWithTitle((String)msg.getContent());
                        }
                    }
                }
            }
        });
    }

    private NSPopUpButton protocolPopup;

    public void setProtocolPopup(NSPopUpButton protocolPopup) {
        log.debug("setProtocolPopup");
        this.protocolPopup = protocolPopup;
        this.protocolPopup.setTarget(this);
        this.protocolPopup.setAction(new NSSelector("protocolSelectionChanged", new Class[]{Object.class}));
    }

    public void protocolSelectionChanged(Object sender) {
        log.debug("protocolSelectionChanged:" + sender);
        this.portField.setIntValue(protocolPopup.selectedItem().tag());
        this.pkCheckbox.setEnabled(protocolPopup.selectedItem().title().equals(SFTP_STRING));
        this.updateURLLabel(sender);
    }

    private NSComboBox hostPopup;
    private Object quickConnectDataSource;

    public void setHostPopup(NSComboBox hostPopup) {
        log.debug("setHostPopup");
        this.hostPopup = hostPopup;
        this.hostPopup.setTarget(this);
        this.hostPopup.setAction(new NSSelector("hostSelectionChanged", new Class[]{Object.class}));
        this.hostPopup.setUsesDataSource(true);
        this.hostPopup.setDataSource(this.quickConnectDataSource = new Object() {
            public int numberOfItemsInComboBox(NSComboBox combo) {
                return CDBookmarkTableDataSource.instance().size();
            }

            public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
                return CDBookmarkTableDataSource.instance().getItem(row).getHostname();
            }
        });
    }

    public void hostSelectionChanged(Object sender) {
        log.debug("hostSelectionChanged:" + sender);
        this.updateURLLabel(sender);
    }

    private NSTextField pathField;

    public void setPathField(NSTextField pathField) {
        this.pathField = pathField;
    }

    private NSTextField portField;

    public void setPortField(NSTextField portField) {
        this.portField = portField;
    }

    private NSTextField usernameField;

    public void setUsernameField(NSTextField usernameField) {
        this.usernameField = usernameField;
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
        //        this.keychainCheckbox.setState(Preferences.instance().getProperty("connection.login.useKeychain").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    private NSButton anonymousCheckbox; //IBOutlet

    public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
        this.anonymousCheckbox = anonymousCheckbox;
        this.anonymousCheckbox.setTarget(this);
        this.anonymousCheckbox.setAction(new NSSelector("anonymousCheckboxClicked", new Class[]{NSButton.class}));
        this.anonymousCheckbox.setState(NSCell.OffState);
    }

    public void anonymousCheckboxClicked(NSButton sender) {
        switch (sender.state()) {
            case NSCell.OnState:
                this.usernameField.setEnabled(false);
                this.usernameField.setStringValue(Preferences.instance().getProperty("ftp.anonymous.name"));
                this.passField.setEnabled(false);
                break;
            case NSCell.OffState:
                this.usernameField.setEnabled(true);
                this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
                this.passField.setEnabled(true);
                break;
        }
    }

    private NSButton pkCheckbox;

    public void setPkCheckbox(NSButton pkCheckbox) {
        this.pkCheckbox = pkCheckbox;
        this.pkCheckbox.setTarget(this);
        this.pkCheckbox.setAction(new NSSelector("pkCheckboxSelectionChanged", new Class[]{Object.class}));
        this.pkCheckbox.setState(NSCell.OffState);
    }

    public void pkCheckboxSelectionChanged(Object sender) {
        log.debug("pkCheckboxSelectionChanged");
        if (this.pkLabel.stringValue().equals(NSBundle.localizedString("No Private Key selected", ""))) {
            NSOpenPanel panel = NSOpenPanel.openPanel();
            panel.setCanChooseDirectories(false);
            panel.setCanChooseFiles(true);
            panel.setAllowsMultipleSelection(false);
            panel.beginSheetForDirectory(System.getProperty("user.home") + "/.ssh", null, null, this.window, this, new NSSelector("pkSelectionPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
        }
        else {
            this.passField.setEnabled(true);
            this.pkCheckbox.setState(NSCell.OffState);
            this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
        }
    }

    public void pkSelectionPanelDidEnd(NSOpenPanel window, int returnCode, Object contextInfo) {
        window.orderOut(null);
        switch (returnCode) {
            case (NSAlertPanel.DefaultReturn):
                {
                    NSArray selected = window.filenames();
                    java.util.Enumeration enumerator = selected.objectEnumerator();
                    while (enumerator.hasMoreElements()) {
                        this.pkLabel.setStringValue((String)enumerator.nextElement());
                    }
                    this.passField.setEnabled(false);
                    break;
                }
            case (NSAlertPanel.AlternateReturn):
                {
                    this.passField.setEnabled(true);
                    this.pkCheckbox.setState(NSCell.OffState);
                    this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
                    break;
                }
        }
    }

    private NSTextField urlLabel;

    public void setUrlLabel(NSTextField urlLabel) {
        this.urlLabel = urlLabel;
    }

    private static NSMutableArray instances = new NSMutableArray();

    private CDBrowserController browser;
	
    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------
	
    public CDConnectionController(CDBrowserController browser) {
        this.browser = browser;
        if (false == NSApplication.loadNibNamed("Connection", this)) {
            log.fatal("Couldn't load Connection.nib");
        }
        instances.addObject(this);
    }

    public void windowWillClose(NSNotification notification) {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        instances.removeObject(this);
    }


    private void awakeFromNib() {
        log.debug("awakeFromNib");
        // Notify the updateURLLabel() method if the user types.
        //ControlTextDidChangeNotification
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{Object.class}),
                NSControl.ControlTextDidChangeNotification,
                hostPopup);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{Object.class}),
                NSControl.ControlTextDidChangeNotification,
                pathField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{Object.class}),
                NSControl.ControlTextDidChangeNotification,
                portField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("updateURLLabel", new Class[]{Object.class}),
                NSControl.ControlTextDidChangeNotification,
                usernameField);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("getPasswordFromKeychain", new Class[]{Object.class}),
                NSControl.ControlTextDidEndEditingNotification,
                hostPopup);
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("getPasswordFromKeychain", new Class[]{Object.class}),
                NSControl.ControlTextDidEndEditingNotification,
                usernameField);

        this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
        this.protocolPopup.setTitle(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP) ? FTP_STRING : SFTP_STRING);
        this.portField.setIntValue(protocolPopup.selectedItem().tag());
        this.pkCheckbox.setEnabled(Preferences.instance().getProperty("connection.protocol.default").equals(Session.SFTP));
    }

    /**
     * Updating the password field with the actual password if any
     * is avaialble for this hostname
     */
    public void getPasswordFromKeychain(Object sender) {
        if (hostPopup.stringValue() != null &&
                !hostPopup.stringValue().equals("") &&
                usernameField.stringValue() != null &&
                !usernameField.stringValue().equals("")) {
            Login l = new Login(hostPopup.stringValue(), usernameField.stringValue(), null);
            String passFromKeychain = l.getPasswordFromKeychain();
            if (passFromKeychain != null && !passFromKeychain.equals("")) {
                log.info("Password for " + usernameField.stringValue() + " found in Keychain");
                this.passField.setStringValue(passFromKeychain);
            }
            else {
                log.info("Password for " + usernameField.stringValue() + " NOT found in Keychain");
                this.passField.setStringValue("");
            }
        }
    }

    private void selectionChanged(Host selectedItem) {
        log.debug("selectionChanged:" + selectedItem);
        this.protocolPopup.selectItemWithTitle(selectedItem.getProtocol().equals(Session.FTP) ? FTP_STRING : SFTP_STRING);
        this.hostPopup.setStringValue(selectedItem.getHostname());
        this.pathField.setStringValue(selectedItem.getDefaultPath());
        this.portField.setIntValue(protocolPopup.selectedItem().tag());
        this.usernameField.setStringValue(selectedItem.getLogin().getUsername());
        this.pkCheckbox.setEnabled(selectedItem.getProtocol().equals(Session.SFTP));
        if (selectedItem.getLogin().getPrivateKeyFile() != null) {
            this.pkCheckbox.setState(NSCell.OnState);
            this.pkLabel.setStringValue(selectedItem.getLogin().getPrivateKeyFile());
        }
        else {
            this.pkCheckbox.setState(NSCell.OffState);
        }
        this.updateURLLabel(null);
    }

    private void updateURLLabel(Object sender) {
        NSMenuItem selectedItem = protocolPopup.selectedItem();
        String protocol = null;
        if (selectedItem.tag() == Session.SSH_PORT) {
            protocol = Session.SFTP + "://";
        }
        else if (selectedItem.tag() == Session.FTP_PORT) {
            protocol = Session.FTP + "://";
        }
        /*
else if (selectedItem.tag() == Session.FTPS_PORT) {
protocol = Session.FTPS + "://";
}
         */
        urlLabel.setStringValue(protocol + usernameField.stringValue() + "@" + hostPopup.stringValue() + ":" + portField.stringValue() + "/" + pathField.stringValue());
    }

    public void closeSheet(NSButton sender) {
        log.debug("closeSheet");
        NSNotificationCenter.defaultCenter().removeObserver(this);
        // Rendezvous should not eat ressources if there is no need to do so
        this.rendezvous.deleteObserver(this);
        this.rendezvous.quit();
        // Ends a document modal session by specifying the window window, window. Also passes along a returnCode to the delegate.
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
    }

    public void connectionSheetDidEnd(NSWindow window, int returncode, Object context) {
        log.debug("connectionSheetDidEnd");
        window.orderOut(null);
        switch (returncode) {
            case (NSAlertPanel.DefaultReturn):
                // Every item in the protocol popup has a tag
                // The value of the tag is the default port number for the protocol selected
                // Even if another port is manually entered, we still want to connect with the
                // appropriate protocol
                int tag = protocolPopup.selectedItem().tag();
                Host host = null;
                switch (tag) {
                    case (Session.SSH_PORT):
                        // SFTP has been selected as the protocol to connect with
                        host = new Host(Session.SFTP,
                                hostPopup.stringValue(),
                                Integer.parseInt(portField.stringValue()),
                                new Login(hostPopup.stringValue(), usernameField.stringValue(), passField.stringValue(), keychainCheckbox.state() == NSCell.OnState),
                                pathField.stringValue());
                        break;
                    case (Session.FTP_PORT):
                        // FTP has been selected as the protocol to connect with
                        host = new Host(Session.FTP,
                                hostPopup.stringValue(),
                                Integer.parseInt(portField.stringValue()),
                                new Login(hostPopup.stringValue(), usernameField.stringValue(), passField.stringValue(), keychainCheckbox.state() == NSCell.OnState),
                                pathField.stringValue());
                        break;
                        /*
case (Session.FTPSSL_PORT):
                        // FTP-SSL has been selected as the protocol to connect with
host = new Host(Session.FTP,
                                        hostPopup.stringValue(),
                                        Integer.parseInt(portField.stringValue()),
                                        new Login(hostPopup.stringValue(), usernameField.stringValue(), passField.stringValue(), keychainCheckbox.state() == NSCell.OnState),
                                        pathField.stringValue());
break;
                         */
                    default:
                        throw new IllegalArgumentException("No protocol selected.");
                }
                if (pkCheckbox.state() == NSCell.OnState) {
                    host.getLogin().setPrivateKeyFile(pkLabel.stringValue());
                }
                browser.mount(host);
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }
}
