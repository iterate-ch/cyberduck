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
import org.apache.log4j.Logger;
import java.util.Observer;
import java.util.Observable;
import com.sshtools.j2ssh.transport.InvalidHostFileException;
import com.sshtools.j2ssh.transport.AbstractHostKeyVerification;
import ch.cyberduck.ui.cocoa.CDBrowserTableDataSource;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.History;
import ch.cyberduck.core.Favorites;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

/**
* @version $Id$
*/
public class CDBrowserController implements Observer {
    private static Logger log = Logger.getLogger(CDBrowserController.class);

    static {
	org.apache.log4j.BasicConfigurator.configure();
    }
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
    
    private NSWindow mainWindow; // IBOutlet
    public void setMainWindow(NSWindow mainWindow) {
	this.mainWindow = mainWindow;
//	this.mainWindow.setDelegate(this);
    }

    private CDBrowserView browserTable; // IBOutlet
    public void setBrowserTable(CDBrowserView browserTable) {
	this.browserTable = browserTable;
    }
    
    private NSTextField quickConnectField; // IBOutlet
    public void setQuickConnectField(NSTextField quickConnectField) {
	this.quickConnectField = quickConnectField;
    }
    
    private NSPopUpButton pathPopup; // IBOutlet
    public void setPathPopup(NSPopUpButton pathPopup) {
	this.pathPopup = pathPopup;
    }

    private NSDrawer drawer; // IBOutlet
    public void setDrawer(NSDrawer drawer) {
	this.drawer = drawer;
    }

    private NSProgressIndicator progressIndicator; // IBOutlet
    public void setProgressIndicator(NSProgressIndicator progressIndicator) {
	this.progressIndicator = progressIndicator;
    }

    private NSTextField statusLabel; // IBOutlet
    public void setStatusLabel(NSTextField statusLabel) {
	this.statusLabel = statusLabel;
    }

//    private NSMutableDictionary toolbarItems;

    private CDConnectionSheet connectionSheet;
    private CDPathController pathController;

    private NSToolbarItem pathItem;
    private NSToolbarItem quickConnectItem;

    /**
     * The host this browser windowis associated with
     */
    private Host host;

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------
    
    public CDBrowserController() {
	log.debug("CDBrowserController");
        if (false == NSApplication.loadNibNamed("Browser", this)) {
            log.error("Couldn't load Browser.nib");
            return;
        }
    }
    
    public void finalize() throws Throwable {
	super.finalize();
	log.debug("finalize");
//	toolbar.setDelegate(null);
	host.deleteObserver((Observer)this);
	host.deleteObserver((Observer)browserTable);
	host.deleteObserver((Observer)pathController);
    }

    public NSWindow window() {
	return this.mainWindow;
    }


    public String windowNibName() {
        return "Browser";
    }
    
    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Host) {
	    if(arg instanceof Message) {
		Host host = (Host)o;
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.ERROR)) {
		    //public static void beginAlertSheet( String title, String defaultButton, String alternateButton, String otherButton, NSWindow docWindow, Object modalDelegate, NSSelector didEndSelector, NSSelector didDismissSelector, Object contextInfo, String message)
		    NSAlertPanel.beginAlertSheet(
				   "Error", //title
				   "OK",// defaultbutton
				   null,//alternative button
				   null,//other button
				   mainWindow, //docWindow
				   null, //modalDelegate
				   null, //didEndSelector
				   null, // dismiss selector
				   null, // context
				   msg.getDescription() // message
				   );
		    progressIndicator.stopAnimation(this);
		    statusLabel.setStringValue("Error: "+msg.getDescription());
		}
		// update status label
		if(msg.getTitle().equals(Message.PROGRESS)) {
		    statusLabel.setStringValue(msg.getDescription());
		}
		if(msg.getTitle().equals(Message.OPEN)) {
		    progressIndicator.startAnimation(this);
		    mainWindow.setTitle(host.getName());
		    History.instance().add(host);
		}
		if(msg.getTitle().equals(Message.CONNECTED)) {
		    progressIndicator.stopAnimation(this);
		}
	    }
	}
    }
    

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void toggleDrawer(Object sender) {
	drawer.toggle(this);
    }
    
    public void folderButtonPressed(Object sender) {
        log.debug("folderButtonPressed");
	CDFolderSheet sheet = new CDFolderSheet((Path)pathController.getItem(pathController.numberOfItems()-1));
	NSApplication.sharedApplication().beginSheet(
					      sheet.window(),//sheet
					      mainWindow, //docwindow
					      sheet, //modal delegate
					      new NSSelector(
			  "newfolderSheetDidEnd",
			  new Class[] { NSPanel.class, int.class, Object.class }
			  ),// did end selector
					      null); //contextInfo
    }


    public void infoButtonPressed(Object sender) {
	log.debug("infoButtonPressed");
	Path path = (Path)((CDBrowserTableDataSource)browserTable.dataSource()).getEntry(browserTable.selectedRow());
	CDInfoController controller = new CDInfoController(path);
	controller.window().makeKeyAndOrderFront(null);
    }

    public void deleteButtonPressed(Object sender) {
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
	sheet.orderOut(null);
	switch(returnCode) {
	    case(NSAlertPanel.DefaultReturn):
		Path path = (Path)contextInfo;
		path.delete();
	    case(NSAlertPanel.AlternateReturn):
		//
	}
    }

    public void refreshButtonPressed(Object sender) {
	log.debug("refreshButtonPressed");
	Path p = (Path)pathController.getItem(0);
	p.list(true);
    }

    public void downloadButtonPressed(Object sender) {
	log.debug("downloadButtonPressed");
	Path path = (Path)((CDBrowserTableDataSource)browserTable.dataSource()).getEntry(browserTable.selectedRow());
	CDTransferController controller = new CDTransferController(path);
	controller.download();
    }

    public void uploadButtonPressed(Object sender) {
	// @todo drag and drop
	log.debug("uploadButtonPressed");
	NSOpenPanel panel = new NSOpenPanel();
	panel.setCanChooseFiles(true);
	panel.setAllowsMultipleSelection(false);
	panel.beginSheetForDirectory(System.getProperty("user.home"), null, null, mainWindow, this, new NSSelector("openPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
    }

    public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
	sheet.orderOut(null);
	switch(returnCode) {
	    case(NSPanel.OKButton): {
		NSArray selected = sheet.filenames();
		String filename;
		if((filename = (String)selected.lastObject()) != null) {
//		    Path path = (Path)pathController.getItem(pathController.numberOfItems()-1);
//		    path.upload(new java.io.File(filename));
		}
		return;
	    }
	    case(NSPanel.CancelButton): {
		return;
	    }
	}
    }
	
    public void backButtonPressed(Object sender) {
	log.debug("backButtonPressed");
	//@todoHistory
    }

     public void upButtonPressed(Object sender) {
	 log.debug("upButtonPressed");
	 Path p = (Path)pathController.getItem(0);
	 p.getParent().list();
     }
    
    public void drawerButtonPressed(Object sender) {
	log.debug("drawerButtonPressed");
	drawer.toggle(mainWindow);
    }

    public void connectFieldPressed(Object sender) {
	log.debug("connectFieldPressed");
	Host host = new Host(((NSControl)sender).stringValue(), new CDLoginController(this));
	this.openConnection(host);
    }

    public void connectButtonPressed(Object sender) {
	log.debug("connectButtonPressed");
	NSApplication.sharedApplication().beginSheet(
					      connectionSheet.window(),//sheet
					      mainWindow, //docwindow
					      connectionSheet, //modal delegate
					      new NSSelector(
		      "connectionSheetDidEnd",
		      new Class[] { NSWindow.class, int.class, NSWindow.class }
		      ),// did end selector
					      null); //contextInfo
    }

    public void openConnection(Host host) {
	this.host = host;
	host.addObserver((Observer)this);
	host.addObserver((Observer)browserTable);
	host.addObserver((Observer)pathController);
//@todo ?	CDConnectionController controller = new CDConnectionController(host);
	host.openSession();
    }

    public void closeConnection(Host host) {
	host.deleteObservers();
	host.closeSession();
    }

    
    public void awakeFromNib() {
	log.debug("awakeFromNib");

	this.pathController = new CDPathController(pathPopup);
	this.connectionSheet = new CDConnectionSheet(this);

	this.setupToolbar();
    }

    // ----------------------------------------------------------
    // Toolbar
    // ----------------------------------------------------------

    private void setupToolbar() {
	NSToolbar toolbar = new NSToolbar("Cyberduck Toolbar");
	toolbar.setDelegate(this);
	toolbar.setAllowsUserCustomization(true);
	toolbar.setAutosavesConfiguration(true);
	toolbar.setDisplayMode(NSToolbar.NSToolbarDisplayModeIconAndLabel);
//	this.toolbarItems = new NSMutableDictionary();

	mainWindow.setToolbar(toolbar);
    }

    
    // ----------------------------------------------------------
    // Toolbar delegate methods
    // ----------------------------------------------------------

    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdentifier, boolean flag) {
//    return (NSToolbarItem)toolbarItems.objectForKey(itemIdentifier);

	NSToolbarItem item = new NSToolbarItem(itemIdentifier);

	if (itemIdentifier.equals("New Connection")) {
	    item.setLabel("New Connection");
	    item.setPaletteLabel("New Connection");
	    item.setToolTip("Connect to remote host");
	    item.setImage(NSImage.imageNamed("server.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("connectButtonPressed", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Path")) {
	    item.setLabel("Path");
	    item.setPaletteLabel("Path");
	    item.setToolTip("Change working directory");
	    item.setView(pathPopup);
	    item.setMinSize(pathPopup.frame().size());
	    item.setMaxSize(pathPopup.frame().size());
	}
	else if (itemIdentifier.equals("Quick Connect")) {
	    item.setLabel("Quick Connect");
	    item.setPaletteLabel("Quick Connect");
	    item.setToolTip("Connect to host");
	    item.setView(quickConnectField);
	    item.setMinSize(quickConnectField.frame().size());
	    item.setMaxSize(quickConnectField.frame().size());
	}
	else if (itemIdentifier.equals("Back")) {
	    item.setLabel("Back");
	    item.setPaletteLabel("Back");
	    item.setToolTip("Show previous directory");
	    item.setImage(NSImage.imageNamed("back.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("backButtonPressed", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Refresh")) {
	    item.setLabel("Refresh");
	    item.setPaletteLabel("Refresh");
	    item.setToolTip("Refresh directory listing");
	    item.setImage(NSImage.imageNamed("refresh.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("refreshButtonPressed", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Download")) {
	    item.setLabel("Download");
	    item.setPaletteLabel("Download");
	    item.setToolTip("Download file");
	    item.setImage(NSImage.imageNamed("download.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("downloadButtonPressed", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Upload")) {
	    item.setLabel("Upload");
	    item.setPaletteLabel("Upload");
	    item.setToolTip("Upload file");
	    item.setImage(NSImage.imageNamed("upload.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("uploadButtonPressed", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Get Info")) {
	    item.setLabel("Get Info");
	    item.setPaletteLabel("Get Info");
	    item.setToolTip("Show file attributes");
	    item.setImage(NSImage.imageNamed("info.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("infoButtonPressed", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Delete")) {
	    item.setLabel("Delete");
	    item.setPaletteLabel("Delete");
	    item.setToolTip("Delete file");
	    item.setImage(NSImage.imageNamed("delete.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("deleteButtonPressed", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("New Folder")) {
	    item.setLabel("New Folder");
	    item.setPaletteLabel("New Folder");
	    item.setToolTip("Create New Folder");
	    item.setImage(NSImage.imageNamed("newfolder.icns"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("folderButtonPressed", new Class[] {Object.class}));
	}
	else {
	    // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
	    // Returning null will inform the toolbar this kind of item is not supported.
	    item = null;
	}
	return item;
	
	/*
	this.addToolbarItem(toolbarItems, "New Connection", "New Connection", "New Connection", "Connect to host", this, new NSSelector("connectButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("server.tiff"));
	
//	this.addToolbarItem(toolbarItems, "Back", "Back", "Back", "Go back", this, new NSSelector("backButtonPressed", new Class[] {null}), NSImage.imageNamed("back.tiff"));

	this.addToolbarItem(toolbarItems, "Path", "Path", "Path", "Change working directory", pathController, null, null);
	NSToolbarItem pathItem = (NSToolbarItem)toolbarItems.objectForKey("Path");
	pathItem.setView(pathPopup);
	pathItem.setMinSize(pathPopup.frame().size());
	pathItem.setMaxSize(pathPopup.frame().size());
//	pathItem.setMaxSize(new NSSize(170, pathController.frame().height()));

	this.addToolbarItem(toolbarItems, "Quick Connect", "Quick Connect", "Quick Connect", "Connect to host", this, null, null);
	NSToolbarItem quickConnectItem = (NSToolbarItem)toolbarItems.objectForKey("Quick Connect");
	quickConnectItem.setView(quickConnectField);
	quickConnectItem.setMinSize(quickConnectField.frame().size());
	quickConnectItem.setMaxSize(quickConnectField.frame().size());

	this.addToolbarItem(toolbarItems, "Refresh", "Refresh", "Refresh", "Refresh directory listing", this, new NSSelector("refreshButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("refresh.tiff"));

	this.addToolbarItem(toolbarItems, "Download", "Download", "Download", "Download file", this, new NSSelector("downloadButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("download.tiff"));

	this.addToolbarItem(toolbarItems, "Upload", "Upload", "Upload", "Upload file", this, new NSSelector("uploadButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("upload.tiff"));

	this.addToolbarItem(toolbarItems, "New Folder", "New Folder", "New Folder", "Create New Folder", this, new NSSelector("folderButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("newfolder.icns"));

	this.addToolbarItem(toolbarItems, "Get Info", "Get Info", "Get Info", "Show file permissions", this, new NSSelector("infoButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("info.tiff"));

	this.addToolbarItem(toolbarItems, "Toggle Drawer", "Toggle Drawer", "Toggle Drawer", "Show connection transcript", this, new NSSelector("drawerButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("transcript.tiff"));

	this.addToolbarItem(toolbarItems, "Delete", "Delete", "Delete", "Delete file", this, new NSSelector("deleteButtonPressed", new Class[] {Object.class}), NSImage.imageNamed("delete.tiff"));
	 */
    }


	/*
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
	 */

	 
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", NSToolbarItem.SeparatorItemIdentifier, "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Path", "Refresh", "Download", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Toggle Drawer"});
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Path", "Refresh", "Download", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Toggle Drawer", NSToolbarItem.CustomizeToolbarItemIdentifier, NSToolbarItem.SpaceItemIdentifier});
    }

    public void toolbarWillAddItem(NSNotification notification) {
	NSToolbarItem addedItem = (NSToolbarItem) notification.userInfo().objectForKey("item");
	if(addedItem.itemIdentifier().equals("Path")) {
	    pathItem = addedItem;
	    pathItem.setTarget(pathController);
	    pathItem.setAction(new NSSelector("selectionChanged", new Class[] { Object.class } ));
	}
	if(addedItem.itemIdentifier().equals("Quick Connect")) {
	    quickConnectItem = addedItem;
	    quickConnectItem.setTarget(this);
	    quickConnectItem.setAction(new NSSelector("connectFieldPressed", new Class[] { Object.class } ));
	}    
    }

    public void toolbarDidRemoveItem(NSNotification notif) {
	NSToolbarItem removedItem = (NSToolbarItem) notif.userInfo().objectForKey("item");
	if (removedItem == pathItem) {
	    pathItem = null;
	}
	if (removedItem == quickConnectItem) {
	    quickConnectItem = null;
	}
    }

    public boolean validateToolbarItem(NSToolbarItem item) {
//	log.debug("validateToolbarItem");
	String label = item.label();
	if(label.equals("Path")) {
	    return pathController.numberOfItems() > 0;
	}
	else if(label.equals("Refresh")) {
	    return pathController.numberOfItems() > 0;
	}
	else if(label.equals("Download")) {
	    return browserTable.selectedRow() != -1;
	}
	else if(label.equals("Delete")) {
	    return browserTable.selectedRow() != -1;
	}
	else if(label.equals("New Folder")) {
	    return pathController.numberOfItems() > 0;
	}
	else if(label.equals("Get Info")) {
	    return browserTable.selectedRow() != -1;
	}
	return true;
    }
}


    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------
/*
    public boolean windowShouldClose(NSWindow sender) {
	NSAlertPanel.beginAlertSheet(
			      "Really close?", //title
			      "Close",// defaultbutton
			      "Cancel",//alternative button
			      null,//other button
			      sender,//window
			      this, //delegate
			      new NSSelector
			      (
	  "closeSheetDidEnd",
	  new Class[]
	  {
	      NSWindow.class, int.class, NSWindow.class
	  }
	  ),// end selector
			      null, // dismiss selector
			      sender, // context
			      "All connections to remote servers will be closed." // message
			      );
	//@todo return the actual selection
	return false;
    }
    
    // ----------------------------------------------------------
    // IB action methods
    // ----------------------------------------------------------

    public void closeSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	// if multi window app only close the one window with main.close()
	sheet.orderOut(null);
	if(returncode == NSAlertPanel.DefaultReturn)
	    NSApplication.sharedApplication().terminate(this);
    }
*/
