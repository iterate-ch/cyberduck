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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Login;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Rendezvous;
import ch.cyberduck.core.Favorites;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;
import java.util.Observable;
import java.util.Observer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.strangeberry.rendezvous.ServiceInfo;

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
	this.protocolPopup.setAction(new NSSelector("protocolSelectionChanged()", new Class[] {Object.class}));
    }

    private NSComboBox hostPopup;
    public void setHostPopup(NSComboBox hostPopup) {
	this.hostPopup = hostPopup;
	this.hostPopup.setTarget(this);
	this.hostPopup.setAction(new NSSelector("hostSelectionChanged", new Class[] {Object.class}));
	this.hostPopup.setUsesDataSource(true);
	this.hostPopup.setDataSource(CDHistoryImpl.instance());
    }

    /*
    private class HostDataSource implements NSComboBox.DataSource {
	private List data = new ArrayList();

	public void addItem(NSComboBox aComboBox, Object o) {
	    log.debug("HostDataSource:addItem:"+o);
	    this.data.add(0, o);
	    aComboBox.reloadData();
	}
	*/

	/**
	    * An NSComboBox, aComboBox, uses this method to perform incremental-or "smart"-searches when the user types into the text field. Your implementation should return the first complete string that starts with uncompletedString.
	 As the user types in the text field, the receiver uses this method to search for items from the pop-up list that start with what the user has typed. The receiver adds the new text to the end of the field and selects the new text, so when the user types another character, it replaces the new text.
	 */
//	public String comboBoxCompletedString(NSComboBox aComboBox, String uncompletedString) {
//	    log.debug("comboBoxCompletedString:"+uncompletedString);
//	    Iterator i = data.iterator();
//	    while(i.hasNext()) {
//		String h = ((Host)i.next()).getName();
//		if(h.startsWith(uncompletedString))
//		    return h;
//	    }
//	    return null;
//	}
//

    /*
	public int comboBoxIndexOfItem( NSComboBox combo, String aString) {
	    log.debug("comboBoxIndexOfItem:"+aString);
	    Iterator i = data.iterator();
	    int index = -1;
	    while(i.hasNext()) {
		index++;
		if(i.next().toString().equals(aString))
		    return index;
	    }
	    return index;
	}
     */

//	public Object getItem(int index) {
//	    return data.get(index);
//	}

	/**
	    * Implement this method to return the object that corresponds to the item at index in aComboBox
	 */
//	public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int index) {
//	    //log.debug("comboBoxObjectValueForItemAtIndex:"+index);
//	    return ((Host)data.get(index)).getName();
//	}

//	public int numberOfItemsInComboBox(NSComboBox combo) {
//	    //log.debug("numberOfItemsInComboBox");
//	    return data.size();
//	}

//	public void clear(NSComboBox aComboBox) {
//	    this.data.clear();
//	    aComboBox.reloadData();
//	}
//    }

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
//						    hostField);
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
	this.updateFields(((CDHistoryImpl)CDHistoryImpl.instance()).getItemAtIndex(hostPopup.indexOfSelectedItem()));
	this.updateLabel(sender);
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
//	Host selectedItem = (Host)hostDataSource.comboBoxObjectValueForItemAtIndex(hostPopup, hostPopup.indexOfSelectedItem());
	this.protocolPopup.selectItemWithTitle(selectedItem.getProtocol().equals("ftp") ? FTP_STRING : SFTP_STRING);
	//should not be called when usesDataSource is set to YES
//	this.hostPopup.selectItemWithObjectValue(selectedItem.getName());
//	this.hostField.setStringValue(selectedItem.getName());
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
//	urlLabel.setStringValue(protocol+usernameField.stringValue()+"@"+hostField.stringValue()+":"+portField.stringValue());
	urlLabel.setStringValue(protocol+usernameField.stringValue()+"@"+hostPopup.stringValue()+":"+portField.stringValue());
    }

    public void closeSheet(NSButton sender) {
	log.debug("closeSheet");
        NSNotificationCenter.defaultCenter().removeObserver(this);
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }
    
    public void connectionSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("connectionSheetDidEnd");
	sheet.orderOut(null);
	switch(returncode) {
	    case(NSAlertPanel.DefaultReturn):
		int tag = protocolPopup.selectedItem().tag();
		Host host = null;
		switch(tag) {
		    case(Session.SSH_PORT):
			try {
			    host = new Host(Session.SFTP, hostPopup.stringValue(), Integer.parseInt(portField.stringValue()), new Login(usernameField.stringValue(), passField.stringValue()));
//			    host = new Host(Session.SFTP, hostField.stringValue(), Integer.parseInt(portField.stringValue()), new Login(usernameField.stringValue(), passField.stringValue()));
			    host.setHostKeyVerificationController(new CDHostKeyController(browser.window()));
			}
			    catch(com.sshtools.j2ssh.transport.InvalidHostFileException e) {
		//This exception is thrown whenever an exception occurs open or reading from the host file.
				NSAlertPanel.beginCriticalAlertSheet(
				 "Error", //title
				 "OK",// defaultbutton
				 null,//alternative button
				 null,//other button
				 browser.window(), //docWindow
				 null, //modalDelegate
				 null, //didEndSelector
				 null, // dismiss selector
				 null, // context
				 "Could not open or read the host file: "+e.getMessage() // message
				 );
			    }
			    break;
		    case(Session.FTP_PORT):
			host = new Host(Session.FTP, hostPopup.stringValue(), Integer.parseInt(portField.stringValue()), new Login(usernameField.stringValue(), passField.stringValue()));
//			host = new Host(Session.FTP, hostField.stringValue(), Integer.parseInt(portField.stringValue()), new Login(usernameField.stringValue(), passField.stringValue()));
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
