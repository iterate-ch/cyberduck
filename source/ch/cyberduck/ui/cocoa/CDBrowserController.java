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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.History;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.sftp.SFTPPath;
import ch.cyberduck.core.sftp.SFTPSession;

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
//	this.mainWindow.setDelegate(this);
    }

//    private CDBrowserView browserTable; // IBOutlet
//    public void setBrowserTable(CDBrowserView browserTable) {
//	this.browserTable = browserTable;
  //  }

    private NSTableView browserTable; // IBOutlet
    public void setBrowserTable(NSTableView browserTable) {
	this.browserTable = browserTable;
    }

    private CDBrowserTableDataSource browserModel;
    
    private NSTextField quickConnectField; // IBOutlet
    public void setQuickConnectField(NSTextField quickConnectField) {
	this.quickConnectField = quickConnectField;
	this.quickConnectField.setTarget(this);
	this.quickConnectField.setAction(new NSSelector("connectFieldClicked", new Class[] { Object.class } ));
    }

    private NSButton upButton; // IBOutlet
    public void setUpButton(NSButton upButton) {
	this.upButton = upButton;
	this.upButton.setImage(NSImage.imageNamed("up.tiff"));
    }

    private NSPopUpButton pathPopup; // IBOutlet
    public void setPathPopup(NSPopUpButton pathPopup) {
	this.pathPopup = pathPopup;
    }

    private NSBox pathBox; // IBOutlet
    public void setPathBox(NSBox pathBox) {
	this.pathBox = pathBox;
    }
    
    private NSDrawer drawer; // IBOutlet
    public void setDrawer(NSDrawer drawer) {
	this.drawer = drawer;
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

//    private NSMutableDictionary toolbarItems;

    /**
	* Keep references of controller objects because otherweise they get garbage collected
     * if not referenced here.
     */
    private NSArray references = new NSArray();
    
    private CDConnectionSheet connectionSheet;
    private CDPathController pathController;

    private NSToolbar toolbar;

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
            log.fatal("Couldn't load Browser.nib");
            return;
        }
	this.init();
    }

    private void init() {
	log.debug("init");

	browserTable.setDataSource(browserModel = new CDBrowserTableDataSource());
	browserTable.setDelegate(this);
	browserTable.registerForDraggedTypes(new NSArray(NSPasteboard.FilenamesPboardType));
	browserTable.setTarget(this);
	browserTable.setDrawsGrid(false);
	browserTable.setAutoresizesAllColumnsToFit(true);
	browserTable.setDoubleAction(new NSSelector("tableViewDidClickTableRow", new Class[] {Object.class}));
	browserTable.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
	browserTable.setAutosaveTableColumns(true);

	connectionSheet = new CDConnectionSheet(this);
	pathController = new CDPathController(pathPopup);

	pathPopup.setTarget(pathController);
	pathPopup.setAction(new NSSelector("selectionChanged", new Class[] { Object.class } ));

	this.setupToolbar();
    }

    // ----------------------------------------------------------
    // BrowserTable delegate methods
    // ----------------------------------------------------------

    public void tableViewDidClickTableRow(Object sender) {
	log.debug("tableViewDidClickTableRow");
	if(browserTable.clickedRow() != -1) { //table header clicked
	    Path p = (Path)browserModel.getEntry(browserTable.clickedRow());
	    if(p.isFile()) {
		this.downloadButtonClicked(sender);
	    }
	    if(p.isDirectory())
		p.list();
	}
    }

    public void tableViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
	log.debug("tableViewDidClickTableColumn");
	String identifier = (String)tableColumn.identifier();
	NSArray columns = tableView.tableColumns();
	java.util.Enumeration enumerator = columns.objectEnumerator();
	while (enumerator.hasMoreElements()) {
	    NSTableColumn c = (NSTableColumn)enumerator.nextElement();
	    if(c.identifier()!=identifier)
		tableView.setIndicatorImage(null, c);
	}
	//@todo desscending
//	if(tableView.indicatorImage(tableColumn) != null && tableView.indicatorImage(tableColumn).name().equals("NSAscendingSortIndicator")) {
//	    tableView.setIndicatorImage(NSImage.imageNamed("NSDescendingSortIndicator"), tableColumn);
//	    a = false;
//	}
//	else {
	tableView.setIndicatorImage(NSImage.imageNamed("NSAscendingSortIndicator"), tableColumn);
//	}
	final boolean ascending = true;
	final int higher = ascending ? 1 : -1 ;
	final int lower = ascending ? -1 : 1;
	if(tableColumn.identifier().equals("TYPE")) {
	    Collections.sort(browserModel.list(),
		      new Comparator() {
			  public int compare(Object o1, Object o2) {
			      Path p1 = (Path) o1;
			      Path p2 = (Path) o2;
			      if(p1.isDirectory() && p2.isDirectory())
				  return 0;
			      if(p1.isFile() && p2.isFile())
				  return 0;
			      if(p1.isFile())
				  return higher;
			      return lower;
			  }
		      }
		      );
	}
	else if(tableColumn.identifier().equals("FILENAME")) {
	    Collections.sort(browserModel.list(),
		      new Comparator() {
			  public int compare(Object o1, Object o2) {
			      Path p1 = (Path)o1;
			      Path p2 = (Path)o2;
			      if(ascending) {
				  return p1.getName().compareToIgnoreCase(p2.getName());
			      }
			      else {
				  return -p1.getName().compareToIgnoreCase(p2.getName());
			      }
			  }
		      }
		      );
	}
	else if(tableColumn.identifier().equals("SIZE")) {
	    Collections.sort(browserModel.list(),
				new Comparator() {
				    public int compare(Object o1, Object o2) {
					int p1 = ((Path)o1).status.getSize();
					int p2 = ((Path)o2).status.getSize();
					if (p1 > p2) {
					    return lower;
					}
					else if (p1 < p2) {
					    return higher;
					}
					else if (p1 == p2) {
					    return 0;
					}
					return 0;
				    }
				}
				);
	}
	else if(tableColumn.identifier().equals("MODIFIED")) {
	    Collections.sort(browserModel.list(),
		      new Comparator() {
			  public int compare(Object o1, Object o2) {
			      Path p1 = (Path) o1;
			      Path p2 = (Path) o2;
			      return p1.attributes.getModified().compareToIgnoreCase(p2.attributes.getModified());
			  }
		      }
		      );
	}
	else if(tableColumn.identifier().equals("OWNER")) {
	    Collections.sort(browserModel.list(),
		      new Comparator() {
			  public int compare(Object o1, Object o2) {
			      Path p1 = (Path) o1;
			      Path p2 = (Path) o2;
			      return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
			  }
		      }
		      );
	}
	browserTable.reloadData();
    }


    private static final NSColor TABLE_CELL_SHADED_COLOR = NSColor.colorWithCalibratedRGB(0.929f, 0.953f, 0.996f, 1.0f);

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
        String identifier = (String)tableColumn.identifier();
//	if(identifier.equals("FILENAME"))
//	    return true;
	return false;
    }


    // ----------------------------------------------------------
    // 
    // ----------------------------------------------------------
    
    public void finalize() throws Throwable {
	super.finalize();
	log.debug("finalize");
    }

    public NSWindow window() {
	return this.mainWindow;
    }


    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Session) {
	    if(arg instanceof Message) {
//		Host host = (Host)o;
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
				   msg.getDescription() // message
				   );
		    progressIndicator.stopAnimation(this);
		    //@tdodo use attributed string???
		    statusLabel.setAttributedStringValue(new NSAttributedString(msg.getDescription()));
		}
		
		// update status label
		else if(msg.getTitle().equals(Message.PROGRESS)) {
		    statusLabel.setAttributedStringValue(new NSAttributedString(msg.getDescription()));
		}
		else if(msg.getTitle().equals(Message.TRANSCRIPT)) {
		    statusLabel.setAttributedStringValue(new NSAttributedString(msg.getDescription()));
		}
		
		else if(msg.getTitle().equals(Message.OPEN)) {
		    progressIndicator.startAnimation(this);

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
		browserModel.clear();
		while(i.hasNext()) {
		    browserModel.addEntry((Path)i.next());
		}
		browserTable.reloadData();
	    }	    
	}
    }
    

    // ----------------------------------------------------------
    // Selector methods for the toolbar items
    // ----------------------------------------------------------

    public void toggleDrawer(Object sender) {
	drawer.toggle(this);
    }

//    public void changePathButtonClicked(Object sender) {
//
//  }
    
    public void folderButtonClicked(Object sender) {
        log.debug("folderButtonClicked");
//	CDFolderController sheet = new CDFolderController(host.getSession().workdir());
	Path parent = (Path)pathController.getItem(0);
	CDFolderController controller = new CDFolderController(parent);
	this.references = references.arrayByAddingObject(controller);
	NSApplication.sharedApplication().beginSheet(
					      controller.window(),//sheet
					      mainWindow, //docwindow
					      controller, //modal delegate
					      new NSSelector(
			  "newfolderSheetDidEnd",
			  new Class[] { NSPanel.class, int.class, Object.class }
			  ),// did end selector
					      null); //contextInfo
    }


    public void infoButtonClicked(Object sender) {
	log.debug("infoButtonClicked");
	Path path = (Path)browserModel.getEntry(browserTable.selectedRow());
	CDInfoController controller = new CDInfoController(path);
	this.references = references.arrayByAddingObject(controller);
	controller.window().makeKeyAndOrderFront(null);
    }

    public void deleteButtonClicked(Object sender) {
	log.debug("deleteButtonClicked");
	NSEnumerator enum = browserTable.selectedRowEnumerator();
	Vector files = new Vector();
	StringBuffer alertText = new StringBuffer("Really delete the following files? This cannot be undone.");
	while(enum.hasMoreElements()) {
	    int selected = ((Integer)enum.nextElement()).intValue();
	    Path p = (Path)browserModel.getEntry(selected);
	    files.add(p);
	    alertText.append("\n"+p.getName());
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
		    ((Path)i.next()).delete();
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
	p.list(true);
    }

    public void downloadButtonClicked(Object sender) {
	log.debug("downloadButtonClicked");
	NSEnumerator enum = browserTable.selectedRowEnumerator();
	Path path = null;
	while(enum.hasMoreElements()) {
	    int selected = ((Integer)enum.nextElement()).intValue();
	    path = (Path)browserModel.getEntry(selected);
	    CDTransferController controller = new CDTransferController(path, Queue.KIND_DOWNLOAD);
	    this.references = references.arrayByAddingObject(controller);
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
		    this.references = references.arrayByAddingObject(controller);
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
	//@todoHistory
    }

     public void upButtonClicked(Object sender) {
	 log.debug("upButtonClicked");
	 Path p = (Path)pathController.getItem(0);
	 p.getParent().list();
     }
    
    public void drawerButtonClicked(Object sender) {
	log.debug("drawerButtonClicked");
	drawer.toggle(mainWindow);
    }

    public void connectFieldClicked(Object sender) {
	log.debug("connectFieldClicked");
	Host host = new Host(((NSControl)sender).stringValue(), new CDLoginController(this.window()));
	if(host.getProtocol().equals(Session.SFTP)) {
	    try {
		host.setHostKeyVerification(new CDHostKeyController(this.window()));
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
	this.mount(host);
    }

    public void connectButtonClicked(Object sender) {
	log.debug("connectButtonClicked");
//keep a reference instead	CDConnectionSheet sheet = new CDConnectionSheet(this);
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

    public void disconnectButtonClicked(Object sender) {
	this.unmount();
	//@todo show disconnected state in gui
    }

    public void mount(Host host) {
	if(this.host != null)
	    this.unmount();
	this.host = host;
	History.instance().add(host);

	Session session = host.getSession();
	
	session.addObserver((Observer)this);
	session.addObserver((Observer)pathController);

	session.mount();
    }

    public void unmount() {
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
	else if (itemIdentifier.equals("Path")) {
	    item.setLabel("Path");
	    item.setPaletteLabel("Path");
	    item.setToolTip("Change working directory");
	    item.setView(pathBox);
	    item.setMinSize(pathBox.frame().size());
	    item.setMaxSize(pathBox.frame().size());
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
	    item.setAction(new NSSelector("backButtonClicked", new Class[] {Object.class}));
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
//	else if (itemIdentifier.equals("Toggle Drawer")) {
//	    item.setLabel("Toggle Drawer");
//	    item.setPaletteLabel("Toggle Drawer");
//	    item.setToolTip("Show connection transcript");
//	    item.setImage(NSImage.imageNamed("transcript.icns"));
//	    item.setTarget(this);
//	    item.setAction(new NSSelector("drawerButtonClicked", new Class[] {Object.class}));
//	}
	else {
	    // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
	    // Returning null will inform the toolbar this kind of item is not supported.
	    item = null;
	}
	return item;
	
	/*
	
//	this.addToolbarItem(toolbarItems, "Back", "Back", "Back", "Go back", this, new NSSelector("backButtonClicked", new Class[] {null}), NSImage.imageNamed("back.tiff"));

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
	return new NSArray(new Object[] {"New Connection", NSToolbarItem.SeparatorItemIdentifier, "Path", "Refresh", "Download", "Upload", "Delete", "New Folder", "Get Info"});
    }

    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	return new NSArray(new Object[] {"New Connection", "Quick Connect", NSToolbarItem.SeparatorItemIdentifier, "Path", "Refresh", "Download", "Upload", "Delete", "New Folder", "Get Info", NSToolbarItem.FlexibleSpaceItemIdentifier, "Disconnect", NSToolbarItem.CustomizeToolbarItemIdentifier, NSToolbarItem.SpaceItemIdentifier});
    }

//    public void toolbarWillAddItem(NSNotification notification) {
//	NSToolbarItem addedItem = (NSToolbarItem) notification.userInfo().objectForKey("item");
//	if(addedItem.itemIdentifier().equals("Path")) {
//	    pathItem = addedItem;
//	    pathItem.setTarget(pathController);
//	    pathItem.setAction(new NSSelector("selectionChanged", new Class[] { Object.class } ));
//	}
//	if(addedItem.itemIdentifier().equals("Quick Connect")) {
//	    quickConnectItem = addedItem;
//	    quickConnectItem.setTarget(this);
//	    quickConnectItem.setAction(new NSSelector("connectFieldClicked", new Class[] { Object.class } ));
//	}    
  //  }

//    public void toolbarDidRemoveItem(NSNotification notif) {
//	NSToolbarItem removedItem = (NSToolbarItem) notif.userInfo().objectForKey("item");
//	if (removedItem == pathItem) {
//	    pathItem = null;
//	}
//	if (removedItem == quickConnectItem) {
//	    quickConnectItem = null;
//	}
  //  }

    public boolean validateToolbarItem(NSToolbarItem item) {
//	log.debug("validateToolbarItem:"+item.label());
	String label = item.label();
	upButton.setEnabled(pathController.numberOfItems() > 0);
	pathPopup.setEnabled(pathController.numberOfItems() > 0);
//not called because it is a custom view
	//if(label.equals("Path")) {
//	    return pathController.numberOfItems() > 0;
//	}
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
	    return this.host != null;
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
			       "End session?", //title
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
			       null, // context
			       "The connection to the remote host will be closed." // message
			       );
	//@todo return the actual selection
		return false;
	    }
	}
	return true;
    }
    
    // ----------------------------------------------------------
    // IB action methods
    // ----------------------------------------------------------

    public void closeSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
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

        if (sel.equals("infoButtonClicked:")) {
	    return browserTable.selectedRow() != -1;
        }
        return true;
    }


    // ----------------------------------------------------------
    // Model
    // ----------------------------------------------------------
    
    class CDBrowserTableDataSource implements NSTableView.DataSource {
	private List data;

	public CDBrowserTableDataSource() {
	    super();
	    this.data = new ArrayList();
	    log.debug("CDBrowserTableDataSource");
	}

	public int numberOfRowsInTableView(NSTableView tableView) {
	    return data.size();
	}
	
    //getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//	log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
	    String identifier = (String)tableColumn.identifier();
	    Path p = (Path)data.get(row);
	    if(identifier.equals("TYPE")) {
//	    tableView.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
		if(p.isDirectory()) {
		    return NSImage.imageNamed("folder.tiff");
		}
		return NSWorkspace.sharedWorkspace().iconForFileType(p.getExtension());
	    }
	    if(identifier.equals("FILENAME"))
		return p.getName();
	    else if(identifier.equals("SIZE"))
		return p.status.getSizeAsString();
	    else if(identifier.equals("MODIFIED"))
		return p.attributes.getModified();
	    else if(identifier.equals("OWNER"))
		return p.attributes.getOwner();
	    else if(identifier.equals("PERMISSION"))
		return p.attributes.getPermission().toString();
	    throw new IllegalArgumentException("Unknown identifier: "+identifier);
	}
	
    //setValue()
	public void tableViewSetObjectValueForLocation(NSTableView tableView, Object value, NSTableColumn tableColumn, int row) {
	    log.debug("tableViewSetObjectValueForLocation:"+row);
	    Path p = (Path)data.get(row);
	    p.rename((String)value);
	}

	
    // ----------------------------------------------------------
    // Drag&Drop methods
    // ----------------------------------------------------------

	/**
	    * Used by tableView to determine a valid drop target. info contains details on this dragging operation. The proposed
	 * location is row is and action is operation. Based on the mouse position, the table view will suggest a proposed drop location.
	 * This method must return a value that indicates which dragging operation the data source will perform. The data source may
	 * "retarget" a drop if desired by calling setDropRowAndDropOperation and returning something other than NSDraggingInfo.
	 * DragOperationNone. One may choose to retarget for various reasons (e.g. for better visual feedback when inserting into a sorted
								       * position).
	 */
	public int tableViewValidateDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation) {
	    log.debug("tableViewValidateDrop");
	    tableView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
	//tableView.setDropRowAndDropOperation(tableView.numberOfRows(), NSTableView.DropAbove);
//	    return NSTableView.DropOn;
	    return NSTableView.DropAbove;
	}

	/**
	    * Invoked by tableView when the mouse button is released over a table view that previously decided to allow a drop.
	 * @param info contains details on this dragging operation.
	 * @param row The proposed location is row and action is operation.
	 * The data source should
	 * incorporate the data from the dragging pasteboard at this time.
	 */
	public boolean tableViewAcceptDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation) {
	    log.debug("tableViewAcceptDrop");
	    if(host != null) {
	// Get the drag-n-drop pasteboard
		NSPasteboard pasteboard = info.draggingPasteboard();
	// What type of data are we going to allow to be dragged?  The pasteboard
 // might contain different formats
		NSArray formats = new NSArray(NSPasteboard.FilenamesPboardType);
		
	// find the best match of the types we'll accept and what's actually on the pasteboard
	// In the file format type that we're working with, get all data on the pasteboard
		NSArray filesList = (NSArray)pasteboard.propertyListForType(pasteboard.availableTypeFromArray(formats));
		int i = 0;
		for(i = 0; i < filesList.count(); i++) {
		    log.debug(filesList.objectAtIndex(i));
		    String filename = (String)filesList.objectAtIndex(i);
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
		    references = references.arrayByAddingObject(controller);
		    controller.transfer(path.status.isResume());
		}
		tableView.reloadData();
		tableView.setNeedsDisplay(true);
	// Select the last song.
		tableView.selectRow(row+i-1, false);
		return true;
	    }
	    return false;
	}

	/**    Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
	    * The drag image and other drag-related information will be set up and provided by the table view once this call
	    * returns with true.
	    * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
	    * (data, owner, and so on).
	    *@param rows is the list of row numbers that will be participating in the drag.
	    */
	public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
//	    if(rows.count() > 1)
//		return false;
//
//	    Path p = (Path)this.getEntry(((Integer)rows.objectAtIndex(0)).intValue());
//	    String filename = p.getLocal().getAbsolutePath();
//	    pboard.declareTypes(new NSArray(NSPasteboard.FilenamesPboardType), this);
//	    pboard.setPropertyListForType(new NSArray(filename), NSPasteboard.FilenamesPboardType);

	
//	    [self dragImage:iconImage at:dragPoint offset:NSMakeSize(0,0)
//	       event:event
//	  pasteboard:pb source:self slideBack:YES];
	//@todo
//	Path p = (Path)this.getEntry(((Integer)rows.objectAtIndex(0)).intValue());
//	pboard.declareTypes(new NSArray(NSPasteboard.FilenamesPboardType), null);
//	pboard.setStringForType(p.getLocal().toString(), NSPasteboard.FilenamesPboardType);

	    return false;
	}


	
    // ----------------------------------------------------------
    // Data access
    // ----------------------------------------------------------

	public void clear() {
	    log.debug("clear");
	    this.data.clear();
	}

	public void addEntry(Path entry, int row) {
//	log.debug("addEntry:"+entry);
	    this.data.add(row, entry);
	}

	public void addEntry(Path entry) {
//	log.debug("addEntry:"+entry);
	    if(entry.attributes.isVisible())
		this.data.add(entry);
	}

	public Object getEntry(int row) {
	    return this.data.get(row);
	}

	public void removeEntry(Path o) {
	    data.remove(data.indexOf(o));
	}

	public void removeEntry(int row) {
	    data.remove(row);
	}

	public int indexOf(Path o) {
	    return data.indexOf(o);
	}

	public List list() {
	    return this.data;
	}
    }    
}