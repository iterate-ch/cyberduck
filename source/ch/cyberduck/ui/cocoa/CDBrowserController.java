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
    
    private CDMainWindow mainWindow; // IBOutlet
    public void setMainWindow(CDMainWindow mainWindow) {
	this.mainWindow = mainWindow;
    }

    private CDBrowserView browserTable; // IBOutlet
    public void setBrowserTable(CDBrowserView browserTable) {
	this.browserTable = browserTable;
    }
    
    private NSTextField quickConnectField; // IBOutlet
    public void setQuickConnectField(NSTextField quickConnectField) {
	this.quickConnectField = quickConnectField;
    }
    
    private CDPathComboBox pathComboBox; // IBOutlet
    public void setPathComboBox(CDPathComboBox pathComboBox) {
	this.pathComboBox = pathComboBox;
    }

    private NSDrawer drawer; // IBOutlet
    public void setDrawer(NSDrawer drawer) {
	this.drawer = drawer;
    }

    private NSProgressIndicator progressIndicator; // IBOutlet
    public void setProgressIndicator(NSProgressIndicator progressIndicator) {
	this.progressIndicator = progressIndicator;
//	this.progressIndicator.setIndeterminate(true);
    }

    private NSTextField statusLabel; // IBOutlet
    public void setStatusLabel(NSTextField statusLabel) {
	this.statusLabel = statusLabel;
    }

    private NSMutableDictionary toolbarItems;

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------
    
    public CDBrowserController() {
	super();
	log.debug("CDMainController");
    }

    public NSWindow window() {
	return this.mainWindow;
    }

    public void update(Observable o, Object arg) {
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
		}
		// update status label
		if(msg.getTitle().equals(Message.PROGRESS)) {
		    statusLabel.setStringValue(msg.getDescription());
		}
		if(msg.getTitle().equals(Message.OPEN)) {
		    progressIndicator.startAnimation(this);
		    History.instance().add(host);
		}
		if(msg.getTitle().equals(Message.CONNECTED)) {
		    progressIndicator.stopAnimation(this);
		}
		if(msg.getTitle().equals(Message.SELECTION)) {
		    mainWindow.setTitle(host.getName());
		}
	    }
	}
    }
    

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void folderButtonPressed(NSObject sender) {
        log.debug("folderButtonPressed");
	CDFolderSheet sheet = new CDFolderSheet((Path)pathComboBox.getItem(pathComboBox.numberOfItems()-1));
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

    public void newBrowserMenuPressed(NSObject sender) {
	CDMainController controller = new CDMainController();
	NSApplication.loadNibNamed("Main", controller);
	controller.window().makeKeyAndOrderFront(this);
    }


    public void infoButtonPressed(NSObject sender) {
	log.debug("infoButtonPressed");
	Path path = (Path)((CDBrowserTableDataSource)browserTable.dataSource()).getEntry(browserTable.selectedRow());
	CDInfoController controller = new CDInfoController(path);
	controller.window().makeKeyAndOrderFront(this);
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
	sheet.orderOut(this);
	switch(returnCode) {
	    case(NSAlertPanel.DefaultReturn):
		Path path = (Path)contextInfo;
		path.delete();
	    case(NSAlertPanel.AlternateReturn):
		//
	}
    }

    public void refreshButtonPressed(NSObject sender) {
	log.debug("refreshButtonPressed");
	Path p = (Path)pathComboBox.getItem(0);
	p.list(true);
    }

    public void downloadButtonPressed(NSObject sender) {
	log.debug("downloadButtonPressed");
	Path path = (Path)((CDBrowserTableDataSource)browserTable.dataSource()).getEntry(browserTable.selectedRow());
	CDTransferController controller = new CDTransferController(path);
	controller.download();
    }

    public void uploadButtonPressed(NSObject sender) {
	log.debug("uploadButtonPressed");
	// @todo drag and drop
//	CDTransferController controller = new CDTransferController(path);
//	controller.upload();
    }

    /*
    public void backButtonPressed(NSObject sender) {
	log.debug("backButtonPressed");
	//@todoHistory
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

    public void connectFieldPressed(NSObject sender) {
	log.debug("connectFieldPressed");
	Host host = new Host(((NSControl)sender).stringValue(), new CDLoginController());
	host.addObserver(this);
	host.addObserver(browserTable);
	host.addObserver(pathComboBox);
	CDConnectionController controller = new CDConnectionController(host);
	controller.connect();
    }

    public void connectMenuPressed(NSObject sender) {
	log.debug("connectMenuPressed");
	this.connectButtonPressed(sender);
    }

    public void connectButtonPressed(NSObject sender) {
	log.debug("connectButtonPressed");
	CDConnectionSheet sheet = new CDConnectionSheet();
	NSApplication.sharedApplication().beginSheet(
					      sheet.window(),//sheet
					      mainWindow, //docwindow
					      sheet, //modal delegate
					      new NSSelector(
		      "connectionSheetDidEnd",
		      new Class[] { NSWindow.class, int.class, NSWindow.class }
		      ),// did end selector
					      null); //contextInfo
    }
    
    public void preferencesMenuPressed(NSObject sender) {
	CDPreferencesController controller = new CDPreferencesController();
	controller.window().makeKeyAndOrderFront(mainWindow);
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
	    return pathComboBox.numberOfItems() > 0;
	}
	if(label.equals("Download")) {
	    return browserTable.selectedRow() != -1;
	}
	if(label.equals("Delete")) {
	    return browserTable.selectedRow() != -1;
	}
	if(label.equals("New Folder")) {
	    return pathComboBox.numberOfItems() > 0;
	}
	if(label.equals("Get Info")) {
	    return browserTable.selectedRow() != -1;
	}
	return true;
    }


    public void donationSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("donationSheetDidEnd");
	sheet.orderOut(this);
	switch(returncode) {
	    case(NSAlertPanel.DefaultReturn):
		try {
		    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("donate.url")));
		}
		catch(java.net.MalformedURLException e) {
		    e.printStackTrace();
		}
	    case(NSAlertPanel.AlternateReturn):
		//
	}
        NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
    }

    public void closeDonationSheet(NSObject sender) {
	log.debug("closeDonationSheet");
	NSApplication.sharedApplication().endSheet(donationSheet, NSAlertPanel.AlternateReturn);
    }

    public void donateMenuPressed(NSObject sender) {
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("donate.url")));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }


    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

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
	return false;
    }
    
    // ----------------------------------------------------------
    // IB action methods
    // ----------------------------------------------------------

    public void closeSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	// if multi window app only close the one window with main.close()
	sheet.orderOut(this);
	if(returncode == NSAlertPanel.DefaultReturn)
	    NSApplication.sharedApplication().terminate(this);
    }


    
    // ----------------------------------------------------------
    // CDHostKeyVerification
    // ----------------------------------------------------------

    /**
	* Concrete Coccoa implementation of a SSH HostKeyVerification
     */
    private class CDHostKeyVerification extends AbstractHostKeyVerification {
	private String host;
	private String fingerprint;

	private boolean done;

	public CDHostKeyVerification() throws InvalidHostFileException {
	    super();
	    log.debug("CDHostKeyVerification");
	}

	public CDHostKeyVerification(String hostFile) throws InvalidHostFileException {
	    super(hostFile);
	}

	public void onDeniedHost(String hostname) {
	    log.debug("onDeniedHost");
	    NSAlertPanel.beginInformationalAlertSheet(
					       "Access denied", //title
					       "OK",// defaultbutton
					       null,//alternative button
					       null,//other button
					       null,//@todomainWindow,
					       this, //delegate
					       new NSSelector
					       (
	     "deniedHostSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "Access to the host " + hostname + " is denied from this system" // message
					       );
	    while(!this.done) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}

	public void onHostKeyMismatch(String host, String fingerprint, String actualHostKey) {
	    log.debug("onHostKeyMismatch");
	    this.host = host;
	    this.fingerprint = fingerprint;
	    NSAlertPanel.beginInformationalAlertSheet(
					       "Host key mismatch", //title
					       "Allow",// defaultbutton
					       "Deny",//alternative button
					       isHostFileWriteable() ? "Always" : null,//other button
					       null,//@todo mainWindow,
					       this, //delegate
					       new NSSelector
					       (
	     "keyMismatchSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "The host key supplied by " + host + " is: "
					       + actualHostKey +
					       "The current allowed key for " + host + " is: "
					       + fingerprint +"\nDo you want to allow the host access?");
	    while(!this.done) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}


	public void onUnknownHost(String host, String fingerprint) {
	    log.debug("onUnknownHost");
	    this.host = host;
	    this.fingerprint = fingerprint;
	    NSAlertPanel.beginInformationalAlertSheet(
					       "Unknown host", //title
					       "Allow",// defaultbutton
					       "Deny",//alternative button
					       isHostFileWriteable() ? "Always" : null,//other button
					       null,//@todo mainWindow,//window
					       this, //delegate
					       new NSSelector
					       (
	     "unknownHostSheetDidEnd",
	     new Class[]
	     {
		 NSWindow.class, int.class, NSWindow.class
	     }
	     ),// end selector
					       null, // dismiss selector
					       this, // context
					       "The host " + host
					       + " is currently unknown to the system. The host key fingerprint is: " + fingerprint+".");
	    while(!this.done) {
		try {
		    Thread.sleep(500); //milliseconds
		}
		catch(InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}


	public void deniedHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("deniedHostSheetDidEnd");
	    sheet.orderOut(this);
	    done = true;
	}

	public void keyMismatchSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("keyMismatchSheetDidEnd");
	    sheet.orderOut(this);
	    try {
		if(returncode == NSAlertPanel.DefaultReturn)
		    allowHost(host, fingerprint, false);
		if(returncode == NSAlertPanel.AlternateReturn) {
		    NSAlertPanel.beginInformationalAlertSheet(
						"Invalid host key", //title
						"OK",// defaultbutton
						null,//alternative button
						null,//other button
						null,//@todo mainWindow,
						this, //delegate
						null,// end selector
						null, // dismiss selector
						this, // context
						"Cannot continue without a valid host key." // message
						);
		    log.info("Cannot continue without a valid host key");
		}
		if(returncode == NSAlertPanel.OtherReturn) {
		    //
		}
		done = true;
	    }
	    catch(InvalidHostFileException e) {
		e.printStackTrace();
	    }
	}

	public void unknownHostSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	    log.debug("unknownHostSheetDidEnd");
	    sheet.orderOut(this);
	    try {
		if(returncode == NSAlertPanel.DefaultReturn)
		    allowHost(host, fingerprint, false); // allow host
		if(returncode == NSAlertPanel.AlternateReturn) {
		    NSAlertPanel.beginInformationalAlertSheet(
						"Invalid host key", //title
						"OK",// defaultbutton
						null,//alternative button
						null,//other button
						null,//@todo mainWindow,
						this, //delegate
						null,// end selector
						null, // dismiss selector
						this, // context
						"Cannot continue without a valid host key." // message
						);
		    log.info("Cannot continue without a valid host key");
		}
		if(returncode == NSAlertPanel.OtherReturn)
		    allowHost(host, fingerprint, true); // always allow host
		done = true;
	    }
	    catch(InvalidHostFileException e) {
		e.printStackTrace();
	    }
	}
    }    
}
