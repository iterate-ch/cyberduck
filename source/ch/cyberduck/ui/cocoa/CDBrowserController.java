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
import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.sftp.SFTPPath;
import ch.cyberduck.core.sftp.SFTPSession;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;
import java.util.*;

/**
* @version $Id$
*/
public class CDBrowserController implements Observer {
    private static Logger log = Logger.getLogger(CDBrowserController.class);
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
    
    private NSWindow mainWindow; // IBOutlet
    public void setMainWindow(NSWindow mainWindow) {
	this.mainWindow = mainWindow;
    }

//    private CDBrowserTableDataSource browserModel;
//    private CDBrowserTableDelegate browserDelegate;
    private NSTableView browserTable; // IBOutlet
    public void setBrowserTable(NSTableView browserTable) {
	this.browserTable = browserTable;
	this.browserTable.setTarget(this);
	this.browserTable.setDoubleAction(new NSSelector("browserTableViewDidClickTableRow", new Class[] {Object.class}));
/*	this.browserTable.setDataSource(browserModel = new CDBrowserTableDataSource());
	this.browserTable.setDelegate(browserDelegate = new CDBrowserTableDelegate());
	this.browserTable.setDrawsGrid(false);
	this.browserTable.setAutoresizesAllColumnsToFit(true);
	this.browserTable.setAutosaveTableColumns(true);
	this.browserTable.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
	this.browserTable.registerForDraggedTypes(new NSArray(NSPasteboard.FilenamesPboardType));*/
    }

    private NSTableView favoritesTable; // IBOutlet
    private CDFavoritesTableDelegate favoritesDelegate;
    public void setFavoritesTable(NSTableView favoritesTable) {
	this.favoritesTable = favoritesTable;
	this.favoritesTable.setDataSource(CDFavoritesImpl.instance());
	this.favoritesTable.setDelegate(favoritesDelegate = new CDFavoritesTableDelegate());
	this.favoritesTable.setTarget(this);
	this.favoritesTable.setDrawsGrid(false);
	this.favoritesTable.setAutoresizesAllColumnsToFit(true);
	this.favoritesTable.setDoubleAction(new NSSelector("favoritesTableViewDidClickTableRow", new Class[] {Object.class}));
	this.favoritesTable.setAutosaveTableColumns(true);
    }
    
    private NSComboBox quickConnectPopup;
    public void setQuickConnectPopup(NSComboBox quickConnectPopup) {
	this.quickConnectPopup = quickConnectPopup;
	this.quickConnectPopup.setTarget(this);
	this.quickConnectPopup.setAction(new NSSelector("quickConnectSelectionChanged", new Class[] {Object.class}));
	this.quickConnectPopup.setUsesDataSource(true);
	this.quickConnectPopup.setDataSource(CDHistoryImpl.instance());
    }
    
    private NSButton addFavoriteButton; // IBOutlet
    public void setAddFavoriteButton(NSButton addFavoriteButton) {
	this.addFavoriteButton = addFavoriteButton;
	this.addFavoriteButton.setImage(NSImage.imageNamed("add.tiff"));
	this.addFavoriteButton.setAlternateImage(NSImage.imageNamed("addPressed.tiff"));
	this.addFavoriteButton.setTarget(this);
	this.addFavoriteButton.setAction(new NSSelector("addFavoriteButtonClicked", new Class[] {Object.class}));
    }

    private NSButton removeFavoriteButton; // IBOutlet
    public void setRemoveFavoriteButton(NSButton removeFavoriteButton) {
	this.removeFavoriteButton = removeFavoriteButton;
	this.removeFavoriteButton.setImage(NSImage.imageNamed("remove.tiff"));
	this.removeFavoriteButton.setAlternateImage(NSImage.imageNamed("removePressed.tiff"));
	this.removeFavoriteButton.setTarget(this);
	this.removeFavoriteButton.setAction(new NSSelector("removeFavoriteButtonClicked", new Class[] {Object.class}));
    }
    
    private NSButton upButton; // IBOutlet
    public void setUpButton(NSButton upButton) {
	this.upButton = upButton;
	this.upButton.setImage(NSImage.imageNamed("up.tiff"));
	this.upButton.setTarget(this);
	this.upButton.setAction(new NSSelector("upButtonClicked", new Class[] {Object.class}));
    }

    private NSButton backButton; // IBOutlet
    public void setBackButton(NSButton backButton) {
	this.backButton = backButton;
	this.backButton.setImage(NSImage.imageNamed("back.tiff"));
	this.backButton.setTarget(this);
	this.backButton.setAction(new NSSelector("backButtonClicked", new Class[] {Object.class}));
    }

    private NSPopUpButton pathPopup; // IBOutlet
    public void setPathPopup(NSPopUpButton pathPopup) {
	this.pathPopup = pathPopup;
    }

    private NSDrawer logDrawer; // IBOutlet
    public void setLogDrawer(NSDrawer drawer) {
	this.logDrawer = drawer;
    }

    private NSDrawer favoritesDrawer; // IBOutlet
    public void setFavoritesDrawer(NSDrawer drawer) {
	this.favoritesDrawer = drawer;
    }
    
    private NSProgressIndicator progressIndicator; // IBOutlet
    public void setProgressIndicator(NSProgressIndicator progressIndicator) {
	this.progressIndicator = progressIndicator;
	this.progressIndicator.setIndeterminate(true);
	this.progressIndicator.setUsesThreadedAnimation(true);
    }

    private NSTextField statusLabel; // IBOutlet
    public void setStatusLabel(NSTextField statusLabel) {
	this.statusLabel = statusLabel;
    }

    /**
	* Keep references of controller objects because otherweise they get garbage collected
     * if not referenced here.
     */
    private static NSMutableArray allDocuments = new NSMutableArray();
    
//    private CDConnectionSheet connectionSheet;
    private CDPathController pathController;
//    private CDFavoritesController favoritesController;
    
    private NSToolbar toolbar;

    /**
     * The host this browser window is associated with
     */
    private Host host;

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------
    
    public CDBrowserController() {
	allDocuments.addObject(this);
	log.debug("CDBrowserController");
        if (false == NSApplication.loadNibNamed("Browser", this)) {
//            log.fatal("Couldn't load Browser.nib");
            return;
        }
//	this.init();
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
	NSPoint origin = this.window().frame().origin();
	this.window().setFrameOrigin(new NSPoint(origin.x() + 16, origin.y() - 16));
	pathController = new CDPathController(pathPopup);
//	favoritesController = new CDFavoritesController(favoritesTable);
	this.setupToolbar();
    }

    public void favoritesTableViewDidClickTableRow(Object sender) {
	log.debug("favoritesTableViewDidClickTableRow");
	if(favoritesTable.clickedRow() != -1) { //table header clicked
	    Host host = (Host)CDFavoritesImpl.instance().values().toArray()[favoritesTable.clickedRow()];
	    this.mount(host);
	}
    }

    public void browserTableViewDidClickTableRow(Object sender) {
	log.debug("browserTableViewDidClickTableRow");
	if(browserTable.clickedRow() != -1) { //table header clicked
	    CDBrowserTable.CDBrowserTableDataSource browserModel = (CDBrowserTable.CDBrowserTableDataSource)browserTable.dataSource();
	    Path p = (Path)browserModel.getEntry(browserTable.clickedRow());
	    if(p.isFile()) {
		this.downloadButtonClicked(sender);
	    }
	    if(p.isDirectory())
		p.list();
	}
    }

    private static final NSColor TABLE_CELL_SHADED_COLOR = NSColor.colorWithCalibratedRGB(0.929f, 0.953f, 0.996f, 1.0f);


    
    // ----------------------------------------------------------
    // FavoritesTable delegate methods
    // ----------------------------------------------------------

    private class CDFavoritesTableDelegate {
	public void tableViewWillDisplayCell(NSTableView view, Object cell, NSTableColumn column, int row) {
	    if(cell instanceof NSTextFieldCell) {
		if (! (view == null || cell == null || column == null)) {
		    if (row % 2 == 0) {
			((NSTextFieldCell)cell).setDrawsBackground(true);
			((NSTextFieldCell)cell).setBackgroundColor(TABLE_CELL_SHADED_COLOR);
		    }
		    else
			((NSTextFieldCell)cell).setBackgroundColor(view.backgroundColor());
		}
	    }
	}

	/**	Returns true to permit aTableView to select the row at rowIndex, false to deny permission.
	    * The delegate can implement this method to disallow selection of particular rows.
	    */
	public  boolean tableViewShouldSelectRow( NSTableView aTableView, int rowIndex) {
	    return true;
	}


	/**	Returns true to permit aTableView to edit the cell at rowIndex in aTableColumn, false to deny permission.
	    *The delegate can implemen this method to disallow editing of specific cells.
	    */
	public boolean tableViewShouldEditLocation( NSTableView view, NSTableColumn tableColumn, int row) {
//@todo	    return true;
	    return false;
	}
    }

    // ----------------------------------------------------------
    // 
    // ----------------------------------------------------------
    
    public NSWindow window() {
	return this.mainWindow;
    }

    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Session) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.ERROR)) {
		    NSAlertPanel.beginCriticalAlertSheet(
				   "Error", //title
				   "OK",// defaultbutton
				   null,//alternative button
				   null,//other button
				   mainWindow, //docWindow
				   null, //modalDelegate
				   null, //didEndSelector
				   null, // dismiss selector
				   null, // context
				   (String)msg.getContent() // message
				   );
		    progressIndicator.stopAnimation(this);
		    statusLabel.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
		}
		
		// update status label
		else if(msg.getTitle().equals(Message.PROGRESS)) {
		    statusLabel.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
		    statusLabel.display();
		}
		else if(msg.getTitle().equals(Message.TRANSCRIPT)) {
		    statusLabel.setAttributedStringValue(new NSAttributedString((String)msg.getContent()));
		}
		
		else if(msg.getTitle().equals(Message.OPEN)) {
		    progressIndicator.startAnimation(this);
		    CDBrowserTable.CDBrowserTableDataSource browserModel = (CDBrowserTable.CDBrowserTableDataSource)browserTable.dataSource();

		    browserModel.clear();
		    browserTable.reloadData();

		    mainWindow.setTitle(host.getProtocol()+":"+host.getName());
		}
		else if(msg.getTitle().equals(Message.CLOSE)) {
		    progressIndicator.stopAnimation(this);
		}
		
		else if(msg.getTitle().equals(Message.START)) {
		    progressIndicator.startAnimation(this);
		    //@todo disable toolbar
		}
		else if(msg.getTitle().equals(Message.STOP)) {
		    progressIndicator.stopAnimation(this);
		    statusLabel.setAttributedStringValue(new NSAttributedString(("Idle")));
		    //@todo enable toolbar
		}
	    }
	    if(arg instanceof Path) {
		java.util.List cache = ((Path)arg).cache();
		java.util.Iterator i = cache.iterator();
//		log.debug("List size:"+cache.size());
		CDBrowserTable.CDBrowserTableDataSource browserModel = (CDBrowserTable.CDBrowserTableDataSource)browserTable.dataSource();
		browserModel.clear();
		while(i.hasNext()) {
		    browserModel.addEntry((Path)i.next());
		}
		browserTable.reloadData();
	    }	    
	}
    }
    

    public void addFavoriteButtonClicked(Object sender) {
	if(this.host != null) {
	    CDFavoritesImpl.instance().addItem(host);
	    this.favoritesTable.reloadData();
	}
    }

    public void removeFavoriteButtonClicked(Object sender) {
	int row = favoritesTable.selectedRow();
	if(row != -1) {
	    CDFavoritesImpl.instance().removeItem(CDFavoritesImpl.instance().values().toArray()[row].toString());
	    this.favoritesTable.reloadData();
	}
    }


//    public void keyDown(NSEvent e) {
//	log.debug(e.toString());
//    }
//
//    public void keyUp(NSEvent e) {
//	log.debug(e.toString());
//    }
    
    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------
    
    public void toggleLogDrawer(Object sender) {
	logDrawer.toggle(this);
    }

    public void toggleFavoritesDrawer(Object sender) {
	favoritesDrawer.toggle(this);
    }

    public void gotoButtonClicked(Object sender) {
        log.debug("folderButtonClicked");
	Path current = (Path)pathController.getItem(0);
	CDGotoController controller = new CDGotoController(current);
	NSApplication.sharedApplication().beginSheet(
					      controller.window(),//sheet
					      mainWindow, //docwindow
					      controller, //modal delegate
					      new NSSelector(
			  "gotoSheetDidEnd",
			  new Class[] { NSPanel.class, int.class, Object.class }
			  ),// did end selector
					      current); //contextInfo
    }
    
    public void folderButtonClicked(Object sender) {
        log.debug("folderButtonClicked");
	Path parent = (Path)pathController.getItem(0);
	CDFolderController controller = new CDFolderController();
	NSApplication.sharedApplication().beginSheet(
					      controller.window(),//sheet
					      mainWindow, //docwindow
					      controller, //modal delegate
					      new NSSelector(
			  "newfolderSheetDidEnd",
			  new Class[] { NSPanel.class, int.class, Object.class }
			  ),// did end selector
					      parent); //contextInfo
    }


    public void infoButtonClicked(Object sender) {
	log.debug("infoButtonClicked");
	CDBrowserTable.CDBrowserTableDataSource browserModel = (CDBrowserTable.CDBrowserTableDataSource)browserTable.dataSource();
	Path path = (Path)browserModel.getEntry(browserTable.selectedRow());
	CDInfoController controller = new CDInfoController(path);
	controller.window().makeKeyAndOrderFront(null);
    }

    public void deleteButtonClicked(Object sender) {
	log.debug("deleteButtonClicked");
	NSEnumerator enum = browserTable.selectedRowEnumerator();
	Vector files = new Vector();
	StringBuffer alertText = new StringBuffer("Really delete the following files? This cannot be undone.");
	CDBrowserTable.CDBrowserTableDataSource browserModel = (CDBrowserTable.CDBrowserTableDataSource)browserTable.dataSource();
	while(enum.hasMoreElements()) {
	    int selected = ((Integer)enum.nextElement()).intValue();
	    Path p = (Path)browserModel.getEntry(selected);
	    files.add(p);
	    alertText.append("\n- "+p.getName());
	}
	NSAlertPanel.beginCriticalAlertSheet(
				      "Delete", //title
				      "Delete",// defaultbutton
				      "Cancel",//alternative button
				      null,//other button
				      this.window(),//window
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
				      files, // contextInfo
				      alertText.toString()
//					  "Really delete the file '"+path.getName()+"'? This cannot be undone." // message
				      );
    }
    
    public void deleteSheetDidEnd(NSWindow sheet, int returnCode, Object contextInfo) {
	log.debug("deleteSheetDidEnd");
	sheet.orderOut(null);
	switch(returnCode) {
	    case(NSAlertPanel.DefaultReturn):
		Vector files = (Vector)contextInfo;
		Iterator i = files.iterator();
		while(i.hasNext()) {
		    Path p = (Path)i.next();
		    p.delete();
		    p.getParent().list();
		}
		break;
	    case(NSAlertPanel.AlternateReturn):
		break;
	}
    }

    public void refreshButtonClicked(Object sender) {
	log.debug("refreshButtonClicked");
//	Path p = host.getSession().workdir();
	Path p = (Path)pathController.getItem(0);
	p.list();
    }

    public void downloadButtonClicked(Object sender) {
	log.debug("downloadButtonClicked");
	NSEnumerator enum = browserTable.selectedRowEnumerator();
	Path path = null;
	CDBrowserTable.CDBrowserTableDataSource browserModel = (CDBrowserTable.CDBrowserTableDataSource)browserTable.dataSource();
	while(enum.hasMoreElements()) {
	    int selected = ((Integer)enum.nextElement()).intValue();
	    path = (Path)browserModel.getEntry(selected);
	    CDTransferController controller = new CDTransferController(path, Queue.KIND_DOWNLOAD);
	    controller.transfer(path.status.isResume());
	}
    }

    public void uploadButtonClicked(Object sender) {
	log.debug("uploadButtonClicked");
	NSOpenPanel panel = new NSOpenPanel();
	panel.setCanChooseDirectories(true);
	panel.setCanChooseFiles(true);
	panel.setAllowsMultipleSelection(true);
	panel.beginSheetForDirectory(System.getProperty("user.home"), null, null, mainWindow, this, new NSSelector("openPanelDidEnd", new Class[]{NSOpenPanel.class, int.class, Object.class}), null);
    }

    public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
	sheet.orderOut(null);
	switch(returnCode) {
	    case(NSPanel.OKButton): {
		NSArray selected = sheet.filenames();
		String filename;
		java.util.Enumeration enumerator = selected.objectEnumerator();
		while (enumerator.hasMoreElements()) {
		    filename = (String)enumerator.nextElement();
		    log.debug(filename+" selected to upload");
		    Session session = host.getSession().copy();
		    Path path = null;
		    Path parent = (Path)pathController.getItem(0);
		    if(session instanceof ch.cyberduck.core.ftp.FTPSession) {
			path = new FTPPath((FTPSession)session, parent.getAbsolute(), new java.io.File(filename));
		    }
		    else if(session instanceof ch.cyberduck.core.sftp.SFTPSession) {
			path = new SFTPPath((SFTPSession)session, parent.getAbsolute(), new java.io.File(filename));
		    }
		    CDTransferController controller = new CDTransferController(path, Queue.KIND_UPLOAD);
		    controller.transfer(path.status.isResume());
		}
		break;
	    }
	    case(NSPanel.CancelButton): {
		break;
	    }
	}
    }
	
    public void backButtonClicked(Object sender) {
	log.debug("backButtonClicked");
	host.getSession().getPreviousPath().list();
    }

     public void upButtonClicked(Object sender) {
	 log.debug("upButtonClicked");
	 Path p = (Path)pathController.getItem(0);
	 p.getParent().list();
     }
    
    public void drawerButtonClicked(Object sender) {
	log.debug("drawerButtonClicked");
	logDrawer.toggle(mainWindow);
    }

    public void quickConnectSelectionChanged(Object sender) {
	log.debug("quickConnectSelectionChanged");
	String input = ((NSControl)sender).stringValue();
	Host host = CDHistoryImpl.instance().getItem(input);
	if(null == host) {
	    int index;
	    if((index = input.indexOf('@')) != -1)
		host = new Host(input.substring(index+1, input.length()), new Login(input.substring(0, index)));
	    else
		host = new Host(input, new Login());
	}
	this.mount(host);
    }

    public void connectButtonClicked(Object sender) {
	log.debug("connectButtonClicked");
	//todo keep reference?
	CDConnectionController controller = new CDConnectionController(this);
	NSApplication.sharedApplication().beginSheet(
					      controller.window(),//sheet
					      mainWindow, //docwindow
					      controller, //modal delegate
					      new NSSelector(
		      "connectionSheetDidEnd",
		      new Class[] { NSWindow.class, int.class, Object.class }
		      ),// did end selector
					      null); //contextInfo
    }

    public void disconnectButtonClicked(Object sender) {
	this.unmount();
    }

    private boolean isMounting = false;

    public void mount(Host host) {
	this.isMounting = true;
	this.unmount();
	this.host = host;

	if(host.getProtocol().equals(Session.SFTP)) {
	    try {
		host.setHostKeyVerificationController(new CDHostKeyController(this.window()));
	    }
	    catch(com.sshtools.j2ssh.transport.InvalidHostFileException e) {
		//This exception is thrown whenever an exception occurs open or reading from the host file.
		NSAlertPanel.beginCriticalAlertSheet(
				       "Error", //title
				       "OK",// defaultbutton
				       null,//alternative button
				       null,//other button
				       this.window(), //docWindow
				       null, //modalDelegate
				       null, //didEndSelector
				       null, // dismiss selector
				       null, // context
				       "Could not open or read the host file: "+e.getMessage() // message
				       );
	    }
	}
	
	CDHistoryImpl.instance().addItem(host);
	//oops- ugly
	this.host.getLogin().setController(new CDLoginController(this.window(), host.getLogin()));
	
	Session session = host.getSession();	
	session.addObserver((Observer)this);
	session.addObserver((Observer)pathController);

	session.mount();
	this.isMounting = false;
    }

    public void unmount() {
	if(this.host != null)
	    this.host.closeSession();
    }

        // ----------------------------------------------------------
    // Toolbar
    // ----------------------------------------------------------

    private void setupToolbar() {
	this.toolbar = new NSToolbar("Cyberduck Toolbar");
	toolbar.setDelegate(this);
	toolbar.setAllowsUserCustomization(true);
	toolbar.setAutosavesConfiguration(true);
//	toolbar.setDisplayMode(NSToolbar.NSToolbarDisplayModeIconAndLabel);
	this.window().setToolbar(toolbar);
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
	    item.setImage(NSImage.imageNamed("connect.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("connectButtonClicked", new Class[] {Object.class}));
	}
//	else if (itemIdentifier.equals("Path")) {
//	    item.setLabel("Path");
//	    item.setPaletteLabel("Path");
//	    item.setToolTip("Change working directory");
//	    item.setView(pathBox);
//	    item.setMinSize(pathBox.frame().size());
//	    item.setMaxSize(pathBox.frame().size());
//	}
	else if (itemIdentifier.equals("Quick Connect")) {
	    item.setLabel("Quick Connect");
	    item.setPaletteLabel("Quick Connect");
	    item.setToolTip("Connect to host");
	    item.setView(quickConnectPopup);
	    item.setMinSize(quickConnectPopup.frame().size());
	    item.setMaxSize(quickConnectPopup.frame().size());
	}
	else if (itemIdentifier.equals("Refresh")) {
	    item.setLabel("Refresh");
	    item.setPaletteLabel("Refresh");
	    item.setToolTip("Refresh directory listing");
	    item.setImage(NSImage.imageNamed("refresh.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("refreshButtonClicked", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Download")) {
	    item.setLabel("Download");
	    item.setPaletteLabel("Download");
	    item.setToolTip("Download file");
	    item.setImage(NSImage.imageNamed("download.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("downloadButtonClicked", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Upload")) {
	    item.setLabel("Upload");
	    item.setPaletteLabel("Upload");
	    item.setToolTip("Upload local file to the remote host");
	    item.setImage(NSImage.imageNamed("upload.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("uploadButtonClicked", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Get Info")) {
	    item.setLabel("Get Info");
	    item.setPaletteLabel("Get Info");
	    item.setToolTip("Show file attributes");
	    item.setImage(NSImage.imageNamed("info.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("infoButtonClicked", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Delete")) {
	    item.setLabel("Delete");
	    item.setPaletteLabel("Delete");
	    item.setToolTip("Delete file");
	    item.setImage(NSImage.imageNamed("delete.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("deleteButtonClicked", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("New Folder")) {
	    item.setLabel("New Folder");
	    item.setPaletteLabel("New Folder");
	    item.setToolTip("Create New Folder");
	    item.setImage(NSImage.imageNamed("newfolder.icns"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("folderButtonClicked", new Class[] {Object.class}));
	}
	else if (itemIdentifier.equals("Disconnect")) {
	    item.setLabel("Disconnect");
	    item.setPaletteLabel("Disconnect");
	    item.setToolTip("Disconnect");
	    item.setImage(NSImage.imageNamed("disconnect.tiff"));
	    item.setTarget(this);
	    item.setAction(new NSSelector("disconnectButtonClicked", new Class[] {Object.class}));
	}
	else {
	    // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
	    // Returning null will inform the toolbar this kind of item is not supported.
	    item = null;
	}
	return item;
    }

	 
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", NSToolbarItem.SeparatorItemIdentifier, "Quick Connect", "Refresh", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Download", "Upload"});
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", "Quick Connect", "Refresh", "Download", "Upload", "Delete", "New Folder", "Get Info", "Disconnect", NSToolbarItem.CustomizeToolbarItemIdentifier, NSToolbarItem.SpaceItemIdentifier, NSToolbarItem.SeparatorItemIdentifier, NSToolbarItem.FlexibleSpaceItemIdentifier, });
    }

    public boolean validateToolbarItem(NSToolbarItem item) {
//	log.debug("validateToolbarItem:"+item.label());
	String label = item.label();
	backButton.setEnabled(pathController.numberOfItems() > 0);
	upButton.setEnabled(pathController.numberOfItems() > 0);
	pathPopup.setEnabled(pathController.numberOfItems() > 0);
	if(label.equals("New Connection")) {
	    return !this.isMounting;
	}
	if(label.equals("Refresh")) {
	    return pathController.numberOfItems() > 0;
	}
	else if(label.equals("Download")) {
	    return browserTable.selectedRow() != -1;
	}
	else if(label.equals("Upload")) {
	    return pathController.numberOfItems() > 0;
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
	else if (label.equals("Disconnect")) {
	    return this.host != null && host.getSession() != null && host.getSession().isConnected();
	}
	return true;
    }


    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

    public boolean windowShouldClose(NSWindow sender) {
	if(host != null) {
	    if(host.getSession().isConnected()) {
		NSAlertPanel.beginCriticalAlertSheet(
			       "Close session?", //title
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
	       NSWindow.class, int.class, Object.class
	   }
	   ),// end selector
			       null, // dismiss selector
			       null, // context
			       "The connection to the host "+host.getName()+" will be closed." // message
			       );
		return false;
	    }
	}
	return true;
    }

    public void windowWillClose(NSNotification notification) {
	this.window().setDelegate(null);
//	NSNotificationCenter.defaultCenter().removeObserver(this);
	allDocuments.removeObject(this);
    }

    
    // ----------------------------------------------------------
    // IB action methods
    // ----------------------------------------------------------

    public void closeSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
	// if multi window app only close the one window with main.close()
	sheet.orderOut(null);
	if(returncode == NSAlertPanel.DefaultReturn) {
	    this.unmount();
	    this.window().close();
	}
    }

    public boolean validateMenuItem(_NSObsoleteMenuItemProtocol aCell) {
	log.debug("validateMenuItem:"+aCell);
        String sel = aCell.action().name();
	log.debug("validateMenuItem:"+sel);
        if (sel.equals("gotoButtonClicked:")) {
	    return browserTable.selectedRow() != -1;
        }
        if (sel.equals("infoButtonClicked:")) {
	    return browserTable.selectedRow() != -1;
        }
        if (sel.equals("folderButtonClicked:")) {
	    return browserTable.selectedRow() != -1;
        }
        if (sel.equals("deleteButtonClicked:")) {
	    return browserTable.selectedRow() != -1;
        }
        if (sel.equals("refreshButtonClicked:")) {
	    return browserTable.selectedRow() != -1;
        }
        return true;
    }


}