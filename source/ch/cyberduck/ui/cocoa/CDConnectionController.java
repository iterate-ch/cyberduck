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
import com.strangeberry.rendezvous.ServiceInfo;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
* @version $Id$
 */
public class CDConnectionController {
    private static Logger log = Logger.getLogger(CDConnectionController.class);

    private static final String FTP_STRING = "FTP (File Transfer)";
    private static final String SFTP_STRING = "SFTP (SSH Secure File Transfer)";
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSWindow sheet;
    public void setSheet(NSWindow sheet) {
	this.sheet = sheet;
    }

    private NSPopUpButton favoritesPopup;
    public void setFavoritesPopup(NSPopUpButton favoritesPopup) {
	this.favoritesPopup = favoritesPopup;
	this.favoritesPopup.setImage(NSImage.imageNamed("favorites.tiff"));

	CDFavoritesImpl.instance().load();
	Iterator i = CDFavoritesImpl.instance().getIterator();
	while(i.hasNext())
	    favoritesPopup.addItem(i.next().toString());
	
	this.favoritesPopup.setTarget(this);
	this.favoritesPopup.setAction(new NSSelector("favoritesSelectionChanged", new Class[] {Object.class}));
    }
    
    private NSPopUpButton rendezvousPopup;
    private RendezvousDataSource rendezvousDataSource;
    public void setRendezvousPopup(NSPopUpButton rendezvousPopup) {
	this.rendezvousPopup = rendezvousPopup;
	this.rendezvousPopup.setImage(NSImage.imageNamed("rendezvous.tiff"));
	this.rendezvousPopup.setTarget(this);
	this.rendezvousPopup.setAction(new NSSelector("rendezvousSelectionChanged", new Class[] {Object.class}));
	this.rendezvousDataSource = new RendezvousDataSource();
	Rendezvous.instance().addObserver(rendezvousDataSource);
	Rendezvous.instance().init();
    }

    private class RendezvousDataSource implements Observer {
	public void update(Observable o, Object arg) {
	    log.debug("update:"+o+","+arg);
	    if(o instanceof Rendezvous) {
		if(arg instanceof Message) {
		    Message msg = (Message)arg;
		    Map s = ((Rendezvous)o).getServices();
		    Iterator i = s.values().iterator();
		    //this.clear(rendezvousPopup);
		    while(i.hasNext())
			rendezvousPopup.addItem(((Host)i.next()).getURL());
		}
	    }
	}
    }

    private NSPopUpButton protocolPopup;
    public void setProtocolPopup(NSPopUpButton protocolPopup) {
	this.protocolPopup = protocolPopup;
	this.protocolPopup.setTarget(this);
	this.protocolPopup.setAction(new NSSelector("protocolSelectionChanged", new Class[] {Object.class}));
    }

    private NSComboBox hostPopup;
    public void setHostPopup(NSComboBox hostPopup) {
	this.hostPopup = hostPopup;
	this.hostPopup.setTarget(this);
	this.hostPopup.setAction(new NSSelector("hostSelectionChanged", new Class[] {Object.class}));
	this.hostPopup.setUsesDataSource(true);
	this.hostPopup.setDataSource(CDHistoryImpl.instance());
    }

//    private NSTextField hostField;
  //  public void setHostField(NSTextField hostField) {
//	this.hostField = hostField;
  //  }
    
//    private NSTextField pathField;
//    public void setPathField(NSTextField pathField) {
//	this.pathField = pathField;
//    }

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

    private NSTextField urlLabel;
    public void setUrlLabel(NSTextField urlLabel) {
	this.urlLabel = urlLabel;
    }

    public NSWindow window() {
	return this.sheet;
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
	this.init();
    }

    public void windowWillClose(NSNotification notification) {
	this.window().setDelegate(null);
	NSNotificationCenter.defaultCenter().removeObserver(this);
	allDocuments.removeObject(this);
    }
    
            
    private void init() {
	log.debug("init");
	// Notify the updateLabel() method if the user types.
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("updateLabel", new Class[]{Object.class}),
						  NSControl.ControlTextDidChangeNotification,
						  hostPopup);
//	NSNotificationCenter.defaultCenter().addObserver(
//						    this,
//						    new NSSelector("updateLabel", new Class[]{Object.class}),
//						    NSControl.ControlTextDidChangeNotification,
//						    pathField);
	NSNotificationCenter.defaultCenter().addObserver(
						    this,
						    new NSSelector("updateLabel", new Class[]{Object.class}),
						    NSControl.ControlTextDidChangeNotification,
						    portField);
	NSNotificationCenter.defaultCenter().addObserver(
						    this,
						    new NSSelector("updateLabel", new Class[]{Object.class}),
						    NSControl.ControlTextDidChangeNotification,
						    usernameField);
        this.usernameField.setStringValue(Preferences.instance().getProperty("connection.login.name"));

	//initalizaing protcol popup
//	this.protocolPopup.removeAllItems();
//	this.protocolPopup.addItemsWithTitles(new NSArray(new String[]{FTP_STRING, SFTP_STRING}));
//	this.protocolPopup.itemWithTitle(FTP_STRING).setTag(Session.FTP_PORT);
//	this.protocolPopup.itemWithTitle(FTP_STRING).setKeyEquivalent("F");
//	this.protocolPopup.itemWithTitle(FTP_STRING).setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
//	this.protocolPopup.itemWithTitle(SFTP_STRING).setTag(Session.SSH_PORT);
//	this.protocolPopup.itemWithTitle(SFTP_STRING).setKeyEquivalent("S");
//	this.protocolPopup.itemWithTitle(SFTP_STRING).setKeyEquivalentModifierMask(NSEvent.CommandKeyMask);
	this.protocolPopup.setTitle(Preferences.instance().getProperty("connection.protocol.default").equals("ftp") ? FTP_STRING : SFTP_STRING);
	
	this.portField.setIntValue(protocolPopup.selectedItem().tag());
//	this.pathField.setStringValue("~");
    }

    public void hostSelectionChanged(Object sender) {
	log.debug("hostSelectionChanged:"+sender);
	int index = hostPopup.indexOfSelectedItem();
	if(index != -1) {
	    this.updateFields(((CDHistoryImpl)CDHistoryImpl.instance()).getItemAtIndex(index));
	    this.updateLabel(sender);
	}
    }

    public void favoritesSelectionChanged(Object sender) {
	log.debug("favoritesSelectionChanged:"+sender);
	this.updateFields(CDFavoritesImpl.instance().getItem(favoritesPopup.titleOfSelectedItem()));
	this.updateLabel(sender);
    }

    public void rendezvousSelectionChanged(Object sender) {
	log.debug("rendezvousSelectionChanged:"+sender);
	Object selectedItem = Rendezvous.instance().getService(rendezvousPopup.titleOfSelectedItem());
	this.updateFields((Host)selectedItem);
	this.updateLabel(sender);
    }
    
    public void protocolSelectionChanged(Object sender) {
	log.debug("protocolSelectionChanged:"+sender);
	this.portField.setIntValue(protocolPopup.selectedItem().tag());
//	NSMenuItem selectedItem = protocolPopup.selectedItem();
//	if(selectedItem.tag() == Session.SSH_PORT)
//	    portField.setIntValue(Session.SSH_PORT);
//	if(selectedItem.tag() == Session.FTP_PORT)
//	    portField.setIntValue(Session.FTP_PORT);
	this.updateLabel(sender);
    }

    public void updateFields(Host selectedItem) {
	log.debug("updateFields:"+selectedItem);
	this.protocolPopup.selectItemWithTitle(selectedItem.getProtocol().equals("ftp") ? FTP_STRING : SFTP_STRING);
	this.hostPopup.setStringValue(selectedItem.getName());
	this.portField.setIntValue(protocolPopup.selectedItem().tag());
	this.usernameField.setStringValue(selectedItem.getLogin().getUsername());
    }

    public void updateLabel(Object sender) {
	NSMenuItem selectedItem = protocolPopup.selectedItem();
	String protocol = null;
	if(selectedItem.tag() == Session.SSH_PORT)
	    protocol = Session.SFTP+"://";
	else if(selectedItem.tag() == Session.FTP_PORT)
	    protocol = Session.FTP+"://";
	urlLabel.setStringValue(protocol+usernameField.stringValue()+"@"+hostPopup.stringValue()+":"+portField.stringValue());
    }

    public void closeSheet(NSButton sender) {
	log.debug("closeSheet");
        NSNotificationCenter.defaultCenter().removeObserver(this);
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }
    
    public void connectionSheetDidEnd(NSWindow sheet, int returncode, Object context) {
	log.debug("connectionSheetDidEnd");
	sheet.orderOut(null);
	switch(returncode) {
	    case(NSAlertPanel.DefaultReturn):
		int tag = protocolPopup.selectedItem().tag();
		Host host = null;
		switch(tag) {
		    case(Session.SSH_PORT):
			host = new Host(Session.SFTP, hostPopup.stringValue(), Integer.parseInt(portField.stringValue()), new Login(usernameField.stringValue(), passField.stringValue()));
			break;
		    case(Session.FTP_PORT):
			host = new Host(Session.FTP, hostPopup.stringValue(), Integer.parseInt(portField.stringValue()), new Login(usernameField.stringValue(), passField.stringValue()));
			break;
		    default:
			throw new IllegalArgumentException("No protocol selected.");
		}
		    browser.mount(host);

	    case(NSAlertPanel.AlternateReturn):
		//
	}
    }
}
