/*
 *  ch.cyberduck.ui.cocoa.CDMainWindow.java
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

//import java.util.Observer;
//import java.util.Observable;

//import ch.cyberduck.core.Status;
//import ch.cyberduck.ui.ObserverList;

import org.apache.log4j.Logger;

public class CDMainWindow extends NSWindow {//implements Observer {

    private static Logger log = Logger.getLogger(CDMainWindow.class);

    //public NSPopUpButton favoritePopUpButton;
    public NSWindow connectionSheet; /* IBOutlet */
    public NSTextField quickConnectField; /* IBOutlet */
    public NSDrawer drawer; /* IBOutlet */

    private NSMutableDictionary toolbarItems;

    public CDMainWindow() {
	super();
	log.debug("CDMainWindow");
//	ObserverList.instance().registerObserver((Observer)this);
    }

    public CDMainWindow(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
	log.debug("CDMainWindow");
//	ObserverList.instance().registerObserver((Observer)this);
    }

    public CDMainWindow(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
	log.debug("CDMainWindow");
//	ObserverList.instance().registerObserver((Observer)this);
    }

    /**
      * @return The string that appears in the title bar of the receiver.
      */
    public String title() {
	return "Connected to <host>";
    }
	

    public void awakeFromNib() {
	log.debug("CDMainWindow:awakeFromNib");
/*
	NSArray columns = connectedView.tableColumns();
	NSTableColumn c = columns.objectAtIndex(columns.count()-1);
	c.setDataCell(new CDServerItemView());
	    */
	this.setDelegate(this);


	// ----------------------------------------------------------
 // Toolbar
 // ----------------------------------------------------------
	
	NSToolbar toolbar = new NSToolbar("mainToolbar");
	this.toolbarItems = new NSMutableDictionary();

	this.addToolbarItem(toolbarItems, "New Connection", "New Connection", "New Connection", "Connect to remote host", this, new NSSelector("openConnectionSheet", new Class[] {null}), NSImage.imageNamed("server.tiff"));

	this.addToolbarItem(toolbarItems, "Back", "Back", "Back", "Show parent directory", this, new NSSelector("back", new Class[] {null}), NSImage.imageNamed("back.tiff"));

	this.addToolbarItem(toolbarItems, "Quick Connect", "Quick Connect", "Quick Connect", null, this, null, null);
	NSToolbarItem quickConnectItem = (NSToolbarItem)toolbarItems.objectForKey("Quick Connect");
	quickConnectItem.setView(quickConnectField);
	quickConnectItem.setMinSize(quickConnectField.frame().size());
	quickConnectItem.setMaxSize(quickConnectField.frame().size());
	/*
	this.addToolbarItem(toolbarItems, "Favorites", "Favorites", "Favorites", null, this, null, null);
	
	NSToolbarItem favoriteItem = (NSToolbarItem)toolbarItems.objectForKey("Favorites");
	favoriteItem.setView(favoritePopUpButton);
	favoriteItem.setMinSize(favoritePopUpButton.frame().size());
	favoriteItem.setMaxSize(favoritePopUpButton.frame().size());
 */

	this.addToolbarItem(toolbarItems, "Refresh", "Refresh", "Refresh", "Refresh directory listing", this, new NSSelector("refreshListing", new Class[] {null}), NSImage.imageNamed("refresh.tiff"));

	this.addToolbarItem(toolbarItems, "Download", "Download", "Download", "Download file", this, new NSSelector("download", new Class[] {null}), NSImage.imageNamed("download.tiff"));

	this.addToolbarItem(toolbarItems, "Upload", "Upload", "Upload", "Upload file", this, new NSSelector("upload", new Class[] {null}), NSImage.imageNamed("upload.tiff"));

	this.addToolbarItem(toolbarItems, "New Folder", "New Folder", "New Folder", "Create New Folder", this, new NSSelector("mkdir", new Class[] {null}), NSImage.imageNamed("folder.tiff"));

	this.addToolbarItem(toolbarItems, "Get Info", "Get Info", "Get Info", "Show file permissions", this, new NSSelector("showInfo", new Class[] {null}), NSImage.imageNamed("info.tiff"));

	this.addToolbarItem(toolbarItems, "Show Transcript", "Show Transcript", "Show Transcript", "Show connection transcript", this, new NSSelector("showTranscriptDrawer", new Class[] {null}), NSImage.imageNamed("transcript.tiff"));

	this.addToolbarItem(toolbarItems, "Delete", "Delete", "Delete", "Delete file", this, new NSSelector("deleteFile", new Class[] {null}), NSImage.imageNamed("delete.tiff"));

	toolbar.setDelegate(this);
	toolbar.setAllowsUserCustomization(true);
	toolbar.setAutosavesConfiguration(true);
	this.setToolbar(toolbar);
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
	return new NSArray(new Object[] {"New Connection", NSToolbarItem.SeparatorItemIdentifier, "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Back", "Refresh", "Download", "Upload", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Show Transcript"});
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Back", "Refresh", "Download", "Upload", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Show Transcript", NSToolbarItem.CustomizeToolbarItemIdentifier, NSToolbarItem.SpaceItemIdentifier});
    }

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
	return (NSToolbarItem)toolbarItems.objectForKey(itemIdentifier);
    }

    public boolean validateToolbarItem(NSToolbarItem item) {
	return true;
    }

    public void showTranscriptDrawer(NSObject sender) {
	drawer.toggle(this);
    }

    public void openConnectionSheet(NSObject sender) {
	NSApplication.sharedApplication().beginSheet(connectionSheet, this, this,
					      new NSSelector
					      (
	    "connectionSheetDidEnd",
	    new Class[]
	    {
		NSWindow.class, int.class, NSWindow.class
	    }
	    ),// end selector
					      this);
    }

    public void closeConnectionSheet(NSObject sender) {
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(connectionSheet, NSAlertPanel.AlternateReturn);
    }

    public void connectionSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	sheet.close();
    }

    /*
    public void update(Observable o, Object arg) {
	if(o instanceof Status) {
	    log.debug("Observable "+o+" sent argument "+ arg);
	    if(arg.equals(Status.TIME) || arg.equals(Status.PROGRESS) || arg.equals(Status.ERROR)) {
		statusLabel.setStringValue(selected.status.getMessage(Status.TIME) + " : " + selected.status.getMessage(Status.PROGRESS) + " : " + selected.status.getMessage(Status.ERROR));
	    }
	    else
		throw new IllegalArgumentException("Unknown observable argument: "+arg);
	}
	else
	    throw new IllegalArgumentException("Unknown observable: "+o);
    }
     */

    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

    public boolean windowShouldClose(NSWindow sender) {
	NSAlertPanel.beginAlertSheet(
			      "Really quit Cyberduck now?", //title
			      "Quit",// defaultbutton
			      "Cancel",//alternative button
			      null,//other button
			      sender,//window
			      this, //delegate
			      new NSSelector
			      (
	  "quitSheetDidEnd",
	  new Class[]
	  {
	      NSWindow.class, int.class, NSWindow.class
	  }
	  ),// end selector
			      null, // dismiss selector
			      sender, // context
			      "All connections to remote servers will be closed." // message
			      );
	return false;
    }

    // ----------------------------------------------------------
    // IB action methods
    // ----------------------------------------------------------

    public void quitSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	// if multi window app only close the one window with main.close()
	if(returncode == NSAlertPanel.DefaultReturn)
	    NSApplication.sharedApplication().terminate(this);
    }
}
