package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDConnectionController implements Observer {
	private static Logger log = Logger.getLogger(CDConnectionController.class);

	private static final String FTP_STRING = NSBundle.localizedString("FTP (File Transfer)", "");
	private static final String SFTP_STRING = NSBundle.localizedString("SFTP (SSH Secure File Transfer)", "");

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------

	private NSWindow sheet;

	public void setSheet(NSWindow sheet) {
		this.sheet = sheet;
	}

	public NSWindow window() {
		return this.sheet;
	}

	private NSPopUpButton historyPopup;

	public void setHistoryPopup(NSPopUpButton historyPopup) {
		log.debug("setHistoryPopup");
		this.historyPopup = historyPopup;
		this.historyPopup.setImage(NSImage.imageNamed("history.tiff"));
		Iterator i = CDHistoryImpl.instance().iterator();
		while (i.hasNext())
			historyPopup.addItem(i.next().toString());
//		this.historyPopup.addItem("Clear");
		this.historyPopup.setTarget(this);
		this.historyPopup.setAction(new NSSelector("historySelectionChanged", new Class[]{Object.class}));
	}

	public void historySelectionChanged(Object sender) {
		log.debug("historySelectionChanged:" + sender);
//		if(historyPopup.titleOfSelectedItem().equals("Clear")) {
//			CDHistoryImpl.instance().clear();
//			historyPopup.removeAllItems();
//		}
		this.selectionChanged(CDHistoryImpl.instance().getItem(historyPopup.indexOfSelectedItem() - 1));
	}

	private NSPopUpButton bookmarksPopup;

	public void setBookmarksPopup(NSPopUpButton bookmarksPopup) {
		this.bookmarksPopup = bookmarksPopup;
		this.bookmarksPopup.setImage(NSImage.imageNamed("bookmarks.tiff"));
		Iterator i = CDBookmarksImpl.instance().iterator();
		while (i.hasNext())
			bookmarksPopup.addItem(i.next().toString());
		this.bookmarksPopup.setTarget(this);
		this.bookmarksPopup.setAction(new NSSelector("bookmarksSelectionChanged", new Class[]{Object.class}));
	}

	public void bookmarksSelectionChanged(Object sender) {
		log.debug("bookmarksSelectionChanged:" + sender);
		this.selectionChanged(CDBookmarksImpl.instance().getItem(bookmarksPopup.indexOfSelectedItem()));
	}

	private Rendezvous rendezvous;
	private NSPopUpButton rendezvousPopup;

	public void setRendezvousPopup(NSPopUpButton rendezvousPopup) {
		this.rendezvousPopup = rendezvousPopup;
		this.rendezvousPopup.setImage(NSImage.imageNamed("rendezvous.tiff"));
		this.rendezvousPopup.setTarget(this);
		this.rendezvousPopup.setAction(new NSSelector("rendezvousSelectionChanged", new Class[]{Object.class}));
		this.rendezvous = new Rendezvous();
		this.rendezvous.addObserver(this);
		this.rendezvous.init();
	}

	public void rendezvousSelectionChanged(Object sender) {
		log.debug("rendezvousSelectionChanged:" + sender);
		this.selectionChanged((Host) rendezvous.getService(rendezvousPopup.titleOfSelectedItem()));
	}

	public void update(Observable o, Object arg) {
		log.debug("update:" + o + "," + arg);
		if (o instanceof Rendezvous) {
			if (arg instanceof Message) {
				Message msg = (Message) arg;
				if (msg.getTitle().equals(Message.RENDEZVOUS_ADD))
					rendezvousPopup.addItem((String) msg.getContent());
				if (msg.getTitle().equals(Message.RENDEZVOUS_REMOVE))
					rendezvousPopup.removeItemWithTitle((String) msg.getContent());
//				rendezvousPopup.addItem(((Host)msg.getContent()).getURL());
			}
		}
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
	private CDQuickConnectDataSource quickConnectDataSource;

	public void setHostPopup(NSComboBox hostPopup) {
		log.debug("setHostPopup");
		this.hostPopup = hostPopup;
		this.hostPopup.setTarget(this);
		this.hostPopup.setAction(new NSSelector("hostSelectionChanged", new Class[]{Object.class}));
		this.hostPopup.setUsesDataSource(true);
		this.hostPopup.setDataSource(this.quickConnectDataSource = new CDQuickConnectDataSource());
	}

	public void hostSelectionChanged(Object sender) {
		log.debug("hostSelectionChanged:" + sender);
		int index = hostPopup.indexOfSelectedItem();
		if (index != -1) {
			this.selectionChanged(((CDHistoryImpl) CDHistoryImpl.instance()).getItem(index));
		}
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
//		this.keychainCheckbox.setState(Preferences.instance().getProperty("connection.login.useKeychain").equals("true") ? NSCell.OnState : NSCell.OffState);
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
			NSOpenPanel panel = new NSOpenPanel();
			panel.setCanChooseDirectories(false);
			panel.setCanChooseFiles(true);
			panel.setAllowsMultipleSelection(false);
			panel.beginSheetForDirectory(System.getProperty("user.home") + "/.ssh", null, null, this.window(), this, new NSSelector("pkSelectionPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
		}
		else {
			this.passField.setEnabled(true);
			this.pkCheckbox.setState(NSCell.OffState);
			this.pkLabel.setStringValue(NSBundle.localizedString("No Private Key selected", ""));
		}
	}

	public void pkSelectionPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
		sheet.orderOut(null);
		switch (returnCode) {
			case (NSPanel.OKButton):
				{
					NSArray selected = sheet.filenames();
					java.util.Enumeration enumerator = selected.objectEnumerator();
					while (enumerator.hasMoreElements()) {
						this.pkLabel.setStringValue((String) enumerator.nextElement());
					}
					this.passField.setEnabled(false);
					break;
				}
			case (NSPanel.CancelButton):
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

	private static NSMutableArray allDocuments = new NSMutableArray();

	private CDBrowserController browser;

	// ----------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------

	public CDConnectionController(CDBrowserController browser) {
		this.browser = browser;
		allDocuments.addObject(this);
		log.debug("CDConnectionController");
		if (false == NSApplication.loadNibNamed("Connection", this)) {
			log.fatal("Couldn't load Connection.nib");
			return;
		}
		//	this.init();
	}

	public void windowWillClose(NSNotification notification) {
		this.window().setDelegate(null);
		NSNotificationCenter.defaultCenter().removeObserver(this);
		allDocuments.removeObject(this);
	}


	private void awakeFromNib() {
		log.debug("awakeFromNib");
		// Notify the updateURLLabel() method if the user types.
		//ControlTextDidChangeNotification
		NSNotificationCenter.defaultCenter().addObserver(
														 this,
														 new NSSelector("updateURLLabel", new Class[]{Object.class}),
														 NSControl.ControlTextDidChangeNotification,
														 hostPopup);
		NSNotificationCenter.defaultCenter().addObserver(
														 this,
														 new NSSelector("updateURLLabel", new Class[]{Object.class}),
														 NSControl.ControlTextDidChangeNotification,
														 pathField);
		NSNotificationCenter.defaultCenter().addObserver(
														 this,
														 new NSSelector("updateURLLabel", new Class[]{Object.class}),
														 NSControl.ControlTextDidChangeNotification,
														 portField);
		NSNotificationCenter.defaultCenter().addObserver(
														 this,
														 new NSSelector("updateURLLabel", new Class[]{Object.class}),
														 NSControl.ControlTextDidChangeNotification,
														 usernameField);
		//NSControlTextDidEndEditingNotification
		NSNotificationCenter.defaultCenter().addObserver(
														 this,
														 new NSSelector("getPasswordFromKeychain", new Class[]{Object.class}),
														 NSControl.ControlTextDidEndEditingNotification,
														 hostPopup);
		NSNotificationCenter.defaultCenter().addObserver(
														 this,
														 new NSSelector("getPasswordFromKeychain", new Class[]{Object.class}),
														 NSControl.ControlTextDidEndEditingNotification,
														 usernameField);
		
		this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
		this.protocolPopup.setTitle(Preferences.instance().getProperty("connection.protocol.default").equals(Session.FTP) ? FTP_STRING : SFTP_STRING);
		this.portField.setIntValue(protocolPopup.selectedItem().tag());
		this.pkCheckbox.setEnabled(Preferences.instance().getProperty("connection.protocol.default").equals(Session.SFTP));
	}
	
	public void getPasswordFromKeychain(Object sender) {
		if(hostPopup.stringValue() != null && 
		   hostPopup.stringValue() != "" && 
		   usernameField.stringValue() != null && 
		   usernameField.stringValue() != "") {
			Login l = new Login(hostPopup.stringValue(), usernameField.stringValue());
			String passFromKeychain = l.getPasswordFromKeychain();
			if(passFromKeychain != null) {
				log.info("Password for "+usernameField.stringValue()+" found in Keychain");
				this.passField.setStringValue(passFromKeychain);
			}
			else {
//				this.passField.setStringValue("");
				log.info("Password for "+usernameField.stringValue()+" NOT found in Keychain");
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
		else
			this.pkCheckbox.setState(NSCell.OffState);
		this.updateURLLabel(null);
	}

	private void updateURLLabel(Object sender) {
		NSMenuItem selectedItem = protocolPopup.selectedItem();
		String protocol = null;
		if (selectedItem.tag() == Session.SSH_PORT)
			protocol = Session.SFTP + "://";
		else if (selectedItem.tag() == Session.FTP_PORT)
			protocol = Session.FTP + "://";
		urlLabel.setStringValue(protocol + usernameField.stringValue() + "@" + hostPopup.stringValue() + ":" + portField.stringValue() + "/" + pathField.stringValue());
	}

	public void closeSheet(NSButton sender) {
		log.debug("closeSheet");
		NSNotificationCenter.defaultCenter().removeObserver(this);
		this.rendezvous.deleteObserver(this);
		this.rendezvous.quit();
		// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void connectionSheetDidEnd(NSWindow sheet, int returncode, Object context) {
		log.debug("connectionSheetDidEnd");
		sheet.orderOut(null);
		this.rendezvous.deleteObserver(this);
		this.rendezvous.quit();
		switch (returncode) {
			case (NSAlertPanel.DefaultReturn):
				int tag = protocolPopup.selectedItem().tag();
				Host host = null;
				switch (tag) {
					case (Session.SSH_PORT):
						host = new Host(
						    Session.SFTP,
						    hostPopup.stringValue(),
						    Integer.parseInt(portField.stringValue()),
						    new Login(hostPopup.stringValue(), usernameField.stringValue(), passField.stringValue(), keychainCheckbox.state() == NSCell.OnState),
						    pathField.stringValue()
						);
						break;
					case (Session.FTP_PORT):
						host = new Host(
						    Session.FTP,
						    hostPopup.stringValue(),
						    Integer.parseInt(portField.stringValue()),
						    new Login(hostPopup.stringValue(), usernameField.stringValue(), passField.stringValue(), keychainCheckbox.state() == NSCell.OnState),
						    pathField.stringValue()
						);
						break;
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
