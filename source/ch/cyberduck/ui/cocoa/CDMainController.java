package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.cocoa.CDBrowserTableDataSource;
import org.apache.log4j.Logger;

/**
* @version $Id$
*/
public class CDMainController extends NSObject {
    private static Logger log = Logger.getLogger(CDMainController.class);

    public NSWindow mainWindow; // IBOutlet
    public NSWindow preferencesWindow; // IBOutlet
    public NSWindow infoWindow; // IBOutlet
    public NSPanel infoPanel; // IBOutlet
    public NSPanel newfolderSheet; // IBOutlet
    public NSPanel donationSheet; // IBOutlet
    public NSPanel connectionSheet; // IBOutlet

    public NSTextField quickConnectField; // IBOutlet
    public CDPathComboBox pathComboBox; // IBOutlet
    public NSDrawer drawer; // IBOutlet
    public NSTableView browserTable; // IBOutlet

    private NSMutableDictionary toolbarItems;

    public CDMainController() {
	super();
	log.debug("CDMainController");
	org.apache.log4j.BasicConfigurator.configure();
    }

    

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void folderButtonPressed(NSObject sender) {
        log.debug("folderButtonPressed");
	if(newfolderSheet == null)
	    NSApplication.loadNibNamed("Folder", this);
	NSApplication.sharedApplication().beginSheet(
					      newfolderSheet,//sheet
					      mainWindow, //docwindow
					      this, //delegate
					      new NSSelector(
			  "newfolderSheetDidEnd",
			  new Class[] { NSWindow.class, int.class, Object.class }
			  ),// did end selector
					      null); //contextInfo
    }

    public void newfolderSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        log.debug("newfolderSheetDidEnd");
	switch(returncode) {
	    case(NSAlertPanel.DefaultReturn):
		Path parent = (Path)pathComboBox.getItem(pathComboBox.numberOfItems()-1);
		//Path dir = new Path(parent, sheet.getPath());
		
		/*
		dir.mkdir();
		dir.list();//@todo path.getParent().list();
		 */
	    case(NSAlertPanel.AlternateReturn):
		//
	}
	sheet.close();
    }

    public void closeNewfolderSheet(NSObject sender) {
        log.debug("closeNewfolderSheet");
	NSApplication.sharedApplication().endSheet(newfolderSheet, ((NSButton)sender).tag());
    }
    
    public void infoButtonPressed(NSObject sender) {
	log.debug("infoButtonPressed");
	if(infoWindow == null)
	    NSApplication.loadNibNamed("Info", this);
	Path path = (Path)((CDBrowserTableDataSource)browserTable.dataSource()).getEntry(browserTable.selectedRow());
	((CDInfoWindow)infoWindow).update(path, new Message(Message.SELECTION));
	infoWindow.orderFront(this);
    }

    public void deleteButtonPressed(NSObject sender) {
	log.debug("deleteButtonPressed");
	Path path = (Path)((CDBrowserTableDataSource)browserTable.dataSource()).getEntry(browserTable.selectedRow());
	NSAlertPanel.beginInformationalAlertSheet(
					       "Delete", //title
					       "Delete",// defaultbutton
					       "Cancel",//alternative button
					       null,//other button
					       mainWindow,//window
					       this, //delegate
					       new NSSelector
					       (
	     "deleteSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, Object.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       path, // contextInfo
					       "Really delete the file '"+path.getName()+"'? This cannot be undone." // message
					       );
	}

    public void deleteSheetDidEnd(NSWindow sheet, int returnCode, Object contextInfo) {
	log.debug("deleteSheetDidEnd");
	sheet.close();
	switch(returnCode) {
	    case(NSAlertPanel.DefaultReturn):
		Path path = (Path)contextInfo;
		path.delete();
	    case(NSAlertPanel.AlternateReturn):
		//
	}
	sheet.close();
    }

    public void refreshButtonPressed(NSObject sender) {
	log.debug("refreshButtonPressed");
	Path p = (Path)pathComboBox.getItem(0);
	p.list();
    }

    public void downloadButtonPressed(NSObject sender) {
	log.debug("downloadButtonPressed");
	Path path = (Path)((CDBrowserTableDataSource)browserTable.dataSource()).getEntry(browserTable.selectedRow());
	path.download();
    }

    public void uploadButtonPressed(NSObject sender) {
	log.debug("uploadButtonPressed");
	// @todo drag and drop
    }

    /*
    public void backButtonPressed(NSObject sender) {
	log.debug("backButtonPressed");
	//
    }
     */

     public void upButtonPressed(NSObject sender) {
	 log.debug("upButtonPressed");
	 Path p = (Path)pathComboBox.getItem(0);
	 p.getParent().list();
     }
    
    public void drawerButtonPressed(NSObject sender) {
	log.debug("drawerButtonPressed");
	drawer.toggle(mainWindow);
    }

    public void connectButtonPressed(NSObject sender) {
	log.debug("connectButtonPressed");
	mainWindow.makeFirstResponder(connectionSheet);
	//NSApplication.beginSheet( NSWindow sheet, NSWindow docWindow, Object modalDelegate, NSSelector didEndSelector, Object contextInfo)
	NSApplication.sharedApplication().beginSheet(
					      connectionSheet,//sheet
					      mainWindow, //docwindow
					      this, //delegate
					      new NSSelector(
			  "connectionSheetDidEnd",
			  new Class[] { NSWindow.class, int.class, NSWindow.class }
			  ),// did end selector
					      null); //contextInfo
    }

    public void connectionSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("connectionSheetDidEnd");
	sheet.close();
    }
    
    public void preferencesButtonPressed(NSObject sender) {
	log.debug("preferencesButtonPressed");
	if(null == preferencesWindow)
	    NSApplication.loadNibNamed("Preferences", this);
        preferencesWindow.makeKeyAndOrderFront(this);
    }
    
    public void awakeFromNib() {
	log.debug("awakeFromNib");

	this.drawer.open();
	// ----------------------------------------------------------
 // Toolbar
 // ----------------------------------------------------------

	NSToolbar toolbar = new NSToolbar("mainToolbar");
	this.toolbarItems = new NSMutableDictionary();

	this.addToolbarItem(toolbarItems, "New Connection", "New Connection", "New Connection", "Connect to host", this, new NSSelector("connectButtonPressed", new Class[] {null}), NSImage.imageNamed("server.tiff"));

//	this.addToolbarItem(toolbarItems, "Back", "Back", "Back", "Show parent directory", this, new NSSelector("backButtonPressed", new Class[] {null}), NSImage.imageNamed("back.tiff"));

	//    private void addToolbarItem(NSMutableDictionary toolbarItems, String identifier, String label, String paletteLabel, String toolTip, Object target, NSSelector action, NSImage image) {

	this.addToolbarItem(toolbarItems, "Path", "Path", "Path", "Change working directory", this, null, null);
	NSToolbarItem pathComboBoxItem = (NSToolbarItem)toolbarItems.objectForKey("Path");
	pathComboBoxItem.setView(pathComboBox);
	pathComboBoxItem.setMinSize(pathComboBox.frame().size());
	pathComboBoxItem.setMaxSize(pathComboBox.frame().size());
//	pathComboBoxItem.setMaxSize(new NSSize(170, pathComboBox.frame().height()));

	this.addToolbarItem(toolbarItems, "Quick Connect", "Quick Connect", "Quick Connect", "Connect to host", this, null, null);
	NSToolbarItem quickConnectItem = (NSToolbarItem)toolbarItems.objectForKey("Quick Connect");
	quickConnectItem.setView(quickConnectField);
	quickConnectItem.setMinSize(quickConnectField.frame().size());
	quickConnectItem.setMaxSize(quickConnectField.frame().size());

	this.addToolbarItem(toolbarItems, "Refresh", "Refresh", "Refresh", "Refresh directory listing", this, new NSSelector("refreshButtonPressed", new Class[] {null}), NSImage.imageNamed("refresh.tiff"));

	this.addToolbarItem(toolbarItems, "Download", "Download", "Download", "Download file", this, new NSSelector("downloadButtonPressed", new Class[] {null}), NSImage.imageNamed("download.tiff"));

	this.addToolbarItem(toolbarItems, "Upload", "Upload", "Upload", "Upload file", this, new NSSelector("uploadButtonPressed", new Class[] {null}), NSImage.imageNamed("upload.tiff"));

	this.addToolbarItem(toolbarItems, "New Folder", "New Folder", "New Folder", "Create New Folder", this, new NSSelector("folderButtonPressed", new Class[] {null}), NSImage.imageNamed("newfolder.icns"));

	this.addToolbarItem(toolbarItems, "Get Info", "Get Info", "Get Info", "Show file permissions", this, new NSSelector("infoButtonPressed", new Class[] {null}), NSImage.imageNamed("info.tiff"));

	this.addToolbarItem(toolbarItems, "Toggle Drawer", "Toggle Drawer", "Toggle Drawer", "Show connection transcript", this, new NSSelector("drawerButtonPressed", new Class[] {NSObject.class}), NSImage.imageNamed("transcript.tiff"));

	this.addToolbarItem(toolbarItems, "Delete", "Delete", "Delete", "Delete file", this, new NSSelector("deleteButtonPressed", new Class[] {null}), NSImage.imageNamed("delete.tiff"));

	toolbar.setDelegate(this);
	toolbar.setAllowsUserCustomization(true);
	toolbar.setAutosavesConfiguration(true);
	mainWindow.setToolbar(toolbar);
    }


    // ----------------------------------------------------------
    // Toolbar delegate methods
    // ----------------------------------------------------------

    private void addToolbarItem(NSMutableDictionary toolbarItems, String identifier, String label, String paletteLabel, String toolTip, Object target, NSSelector action, NSImage image) {
	NSToolbarItem item = new NSToolbarItem(identifier);
	item.setLabel(label);
	item.setPaletteLabel(paletteLabel);
	item.setToolTip(toolTip);
	item.setImage(image);
	item.setTarget(target);
	item.setAction(action);
	item.setEnabled(true);

	toolbarItems.setObjectForKey(item, identifier);
    }

    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", NSToolbarItem.SeparatorItemIdentifier, "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Path", "Refresh", "Download", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Toggle Drawer"});
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Path", "Refresh", "Download", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Toggle Drawer", NSToolbarItem.CustomizeToolbarItemIdentifier, NSToolbarItem.SpaceItemIdentifier});
    }

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
	return (NSToolbarItem)toolbarItems.objectForKey(itemIdentifier);
    }

    public boolean validateToolbarItem(NSToolbarItem item) {
//	log.debug("validateToolbarItem");
	String label = item.label();
	if(label.equals("Path")) {
	    return pathComboBox.numberOfItems() > 0;
	}
	if(label.equals("Refresh")) {
	    //return ;
	}
	if(label.equals("Download")) {
	    return browserTable.numberOfRows() > 0;
	}
	if(label.equals("Delete")) {
	    return browserTable.numberOfRows() > 0;
	}
	if(label.equals("New Folder")) {
	    //return ;
	}
	if(label.equals("Get Info")) {
	    return browserTable.numberOfRows() > 0;
	}
	return true;
    }


    // ----------------------------------------------------------
    // Application delegate methods
    // ----------------------------------------------------------
    
    public int applicationShouldTerminate(NSObject sender) {
	log.debug("applicationShouldTerminate");
        Preferences.instance().store();
        NSApplication.loadNibNamed("Donate", this);
        if(Preferences.instance().getProperty("cyberduck.donate").equals("true")) {
            NSApplication.sharedApplication().beginSheet(
                                                donationSheet,//sheet
                                                mainWindow, //docwindow
                                                this, //delegate
                                                new NSSelector(
                            "donationSheetDidEnd",
                            new Class[] { NSWindow.class, int.class, NSWindow.class }
                            ),// did end selector
                                                null); //contextInfo
            return NSApplication.TerminateLater;
        }
        return NSApplication.TerminateNow;
    }

    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
	log.debug("applicationShouldTerminateAfterLastWindowClosed");
	return true;
    }
    
    public void donationSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("donationSheetDidEnd");
	sheet.close();
        NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
    }
    
    public void closeDonationSheet(NSObject sender) {
	log.debug("closeDonationSheet");
	NSApplication.sharedApplication().endSheet(donationSheet, NSAlertPanel.AlternateReturn);
    }
    
    public void donateButtonPressed(NSObject sender) {
	log.debug("donateButtonPressed");
        this.closeDonationSheet(this);
	log.debug("donate");
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL("http://www.cyberduck.ch/donate/"));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }
}
