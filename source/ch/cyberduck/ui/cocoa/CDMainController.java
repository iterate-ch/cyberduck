/*
 *  ch.cyberduck.ui.cocoa.CDMainController.java
 *  Cyberduck
 *
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

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import ch.cyberduck.ui.CDBrowserTableDataSource;

import org.apache.log4j.Logger;

public class CDMainController extends NSObject {

    public NSPanel infoPanel;
    public NSWindow mainWindow;
    public NSTableView browserView;
    public NSWindow connectionSheet; /* IBOutlet */
    public NSTextField quickConnectField; /* IBOutlet */
    public NSPopUpButton pathPopUpButton; /* IBOutlet */
    public NSDrawer drawer; /* IBOutlet */

    private NSMutableDictionary toolbarItems;
    
    private static Logger log = Logger.getLogger(CDMainController.class);

    public CDMainController() {
	super();
	org.apache.log4j.BasicConfigurator.configure();
	log.debug("CDMainController");
    }

    

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    poublic void infoButtonPressed() {
	log.debug("infoButtonPressed");
	//infoPanel.show();
    }
    
    public void deleteButtonPressed() {
	log.debug("deleteButtonPressed");
    }

    public void refreshButtonPressed() {
	log.debug("refreshButtonPressed");
	CDBrowserTableDataSource source = (CDBrowserTableDataSource)browserTable.dataSource();
	Path p = source.get(browserTable.selectedRow());
	p.list();
	
    }

    public void downloadButtonPressed() {
	log.debug("downloadButtonPressed");
	CDBrowserTableDataSource source = (CDBrowserTableDataSource)browserTable.dataSource();
	Path p = source.get(browserTable.selectedRow());
//	p.download();
    }

    public void uploadButtonPressed() {
	log.debug("uploadButtonPressed");
	// @todo drag and drop
    }

    public void drawerButtonPressed(NSObject sender) {
	log.debug("drawerButtonPressed");
	drawer.toggle(this);
    }

    public void openConnectionSheet(NSObject sender) {
	this.makeFirstResponder(connectionSheet);
	//NSApplication.beginSheet( NSWindow sheet, NSWindow docWindow, Object modalDelegate, NSSelector didEndSelector, Object contextInfo)
	NSApplication.sharedApplication().beginSheet(
					      connectionSheet,
					      this,
					      this,
					      new NSSelector(
			  "connectionSheetDidEnd",
			  new Class[] { NSWindow.class, int.class, NSWindow.class }
			  ),// did end selector
					      null); //contextInfo
    }

    public void connectionSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	sheet.close();
    }
    
    public void awakeFromNib() {
	log.debug("awakeFromNib");
	// ----------------------------------------------------------
 // Toolbar
 // ----------------------------------------------------------

	NSToolbar toolbar = new NSToolbar("mainToolbar");
	this.toolbarItems = new NSMutableDictionary();

	this.addToolbarItem(toolbarItems, "New Connection", "New Connection", "New Connection", "Connect to host", this, new NSSelector("openConnectionSheet", new Class[] {null}), NSImage.imageNamed("server.tiff"));

	this.addToolbarItem(toolbarItems, "Back", "Back", "Back", "Show parent directory", this, new NSSelector("back", new Class[] {null}), NSImage.imageNamed("back.tiff"));

	//    private void addToolbarItem(NSMutableDictionary toolbarItems, String identifier, String label, String paletteLabel, String toolTip, Object target, NSSelector action, NSImage image) {

	this.addToolbarItem(toolbarItems, "Path", "Path", "Path", "Change working directory", this, null, null);
	NSToolbarItem pathPopUpButtonItem = (NSToolbarItem)toolbarItems.objectForKey("Path");
	pathPopUpButtonItem.setView(pathPopUpButton);
	pathPopUpButtonItem.setMinSize(pathPopUpButton.frame().size());
	//	pathPopUpButtonItem.setMaxSize(pathPopUpButton.frame().size());

	this.addToolbarItem(toolbarItems, "Quick Connect", "Quick Connect", "Quick Connect", "Connect to host", this, null, null);
	NSToolbarItem quickConnectItem = (NSToolbarItem)toolbarItems.objectForKey("Quick Connect");
	quickConnectItem.setView(quickConnectField);
	quickConnectItem.setMinSize(quickConnectField.frame().size());
	quickConnectItem.setMaxSize(quickConnectField.frame().size());

	this.addToolbarItem(toolbarItems, "Refresh", "Refresh", "Refresh", "Refresh directory listing", this, new NSSelector("refreshButtonPressed", new Class[] {null}), NSImage.imageNamed("refresh.tiff"));

	this.addToolbarItem(toolbarItems, "Download", "Download", "Download", "Download file", this, new NSSelector("download", new Class[] {null}), NSImage.imageNamed("download.tiff"));

	this.addToolbarItem(toolbarItems, "Upload", "Upload", "Upload", "Upload file", this, new NSSelector("uploadButtonPressed", new Class[] {null}), NSImage.imageNamed("upload.tiff"));

	this.addToolbarItem(toolbarItems, "New Folder", "New Folder", "New Folder", "Create New Folder", this, new NSSelector("mkdir", new Class[] {null}), NSImage.imageNamed("folder.tiff"));

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
	return new NSArray(new Object[] {"New Connection", NSToolbarItem.SeparatorItemIdentifier, "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Path", "Back", "Refresh", "Download", "Upload", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Toggle Drawer"});
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Path", "Back", "Refresh", "Download", "Upload", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Toggle Drawer", NSToolbarItem.CustomizeToolbarItemIdentifier, NSToolbarItem.SpaceItemIdentifier});
    }

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
	return (NSToolbarItem)toolbarItems.objectForKey(itemIdentifier);
    }

    public boolean validateToolbarItem(NSToolbarItem item) {
	return true;
    }


    // ----------------------------------------------------------
    // Application delegate methods
    // ----------------------------------------------------------
    
    public int applicationShouldTerminate(NSObject sender) {
	log.debug("applicationShouldTerminate");
        return NSApplication.TerminateNow;
    }

    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
	log.debug("applicationShouldTerminateAfterLastWindowClosed");
	return true;
    }
    
    public void donate(NSObject sender) {
	log.debug("donate");
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL("http://www.cyberduck.ch/donate/"));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }
}
