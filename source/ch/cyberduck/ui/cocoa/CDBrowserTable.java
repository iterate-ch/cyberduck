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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Session;
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
public class CDBrowserTable extends NSTableView implements Observer {
    private static Logger log = Logger.getLogger(CDBrowserTable.class);
    
    private static final NSColor TABLE_CELL_SHADED_COLOR = NSColor.colorWithCalibratedRGB(0.929f, 0.953f, 0.996f, 1.0f);

    private CDBrowserTableDataSource browserModel;
    private CDBrowserTableDelegate browserDelegate;

    private Path workdir;

    public Path workdir() {
	return this.workdir;
    }

    public CDBrowserTable() {
	super();
	log.debug("CDBrowserTable");
    }

    public CDBrowserTable(NSRect frame) {
	super(frame);
	log.debug("CDBrowserTable:"+frame);
    }

    public CDBrowserTable(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }    

    public void awakeFromNib() {
	log.debug("awakeFromNib");
	this.setDataSource(this.browserModel = new CDBrowserTableDataSource());
	this.setDelegate(this.browserDelegate = new CDBrowserTableDelegate());
	this.setTarget(this);
	this.setDoubleAction(new NSSelector("browserTableViewDidClickTableRow", new Class[] {Object.class}));
	this.setDrawsGrid(false);
	this.setAutoresizesAllColumnsToFit(true);
	this.setAutosaveTableColumns(true);
	this.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
	this.registerForDraggedTypes(new NSArray(NSPasteboard.FilenamesPboardType));
    }

    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Session) {
	    if(arg instanceof Path) {
		this.workdir = (Path)arg;
		java.util.List cache = ((Path)arg).cache();
		java.util.Iterator i = cache.iterator();
//		log.debug("List size:"+cache.size());
		browserModel.clear();
		while(i.hasNext()) {
		    browserModel.addEntry((Path)i.next());
		}
		this.reloadData();
	    }
	}
    }

    public boolean performKeyEquivalent(NSEvent e) {
	if(e.modifierFlags() == NSEvent.CommandKeyMask) {
	//left arrow key
//60871 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - keyDown:NSEvent: type=KeyDown loc=(-666,951) time=166571.4 flags=0xa00000 win=0 winNum=256625 ctxt=0x2ad57 chars="?" unmodchars="?" repeat=0 keyCode=123
//	60871 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - character:?
	//up arrow key
//	61063 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - keyDown:NSEvent: type=KeyDown loc=(-666,951) time=166571.6 flags=0xa00000 win=0 winNum=256625 ctxt=0x2ad57 chars="?" unmodchars="?" repeat=0 keyCode=126
//	61063 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - character:?
	    if(e.keyCode() == 126) {
		log.debug(e);
		if(this.workdir != null) {
		    this.workdir.getParent().list();
		    return true;
		}
	    }
	//down arrow key
//	61254 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - keyDown:NSEvent: type=KeyDown loc=(-666,951) time=166571.7 flags=0xa00000 win=0 winNum=256625 ctxt=0x2ad57 chars="?" unmodchars="?" repeat=0 keyCode=125
//	61255 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - character:?
	    if(e.keyCode() == 125) {
		log.debug(e);
		this.browserTableViewDidClickTableRow(e);
		return true;
	    }
	//right arrow key
//	61438 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - keyDown:NSEvent: type=KeyDown loc=(-666,951) time=166571.9 flags=0xa00000 win=0 winNum=256625 ctxt=0x2ad57 chars="?" unmodchars="?" repeat=0 keyCode=124
//	61438 [main] DEBUG ch.cyberduck.ui.cocoa.CDBrowserTable  - character:?
	}
	return super.performKeyEquivalent(e);
    }

//    public void keyUp(NSEvent e) {
//	if(e.keyCode() == 36) //where is the static variable for the return key?!
//	    this.browserTableViewDidClickTableRow(e);
//    }

    public void browserTableViewDidClickTableRow(Object sender) {
	log.debug("browserTableViewDidClickTableRow:"+sender);
//	if(this.clickedRow() != -1) { //table header clicked
	if(this.numberOfSelectedRows() > 0) {
//	    p = (Path)browserModel.getEntry(this.clickedRow());
	    Path p = (Path)browserModel.getEntry(this.selectedRow()); //last row selected 
	    if(p.isFile() || this.numberOfSelectedRows() > 1) {
		NSEnumerator enum = this.selectedRowEnumerator();
		List items = new ArrayList();
		Session session = workdir.getSession().copy();
		while(enum.hasMoreElements()) {
		    items.add(browserModel.getEntry(((Integer)enum.nextElement()).intValue()).copy(session));
		}
		CDTransferController controller = new CDTransferController((Path[])items.toArray(new Path[]{}), Queue.KIND_DOWNLOAD);
		controller.transfer();
	    }
	    if(p.isDirectory())
		p.list();
	}
    }
    

        
        // ----------------------------------------------------------
    // BrowserTable delegate methods
    // ----------------------------------------------------------

    private class CDBrowserTableDelegate {

	boolean sortAscending = true;
	NSTableColumn lastClickedColumn;

	public void tableViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
	    log.debug("tableViewDidClickTableColumn");
	    if(lastClickedColumn == tableColumn) {
		sortAscending = !sortAscending;
	    }
	    else {
		if(lastClickedColumn != null)
		    tableView.setIndicatorImage(null, lastClickedColumn);
		lastClickedColumn = tableColumn;
		tableView.setHighlightedTableColumn(tableColumn);
	    }

	    tableView.setIndicatorImage(sortAscending ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), tableColumn);

	    final int higher = sortAscending ? 1 : -1 ;
	    final int lower = sortAscending ? -1 : 1;
	    final boolean ascending = sortAscending;
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
			   if(ascending)
			       return p1.getName().compareToIgnoreCase(p2.getName());
			   else
			       return -p1.getName().compareToIgnoreCase(p2.getName());
		       }
		   }
		   );
	    }
	    else if(tableColumn.identifier().equals("SIZE")) {
		Collections.sort(browserModel.list(),
		   new Comparator() {
		       public int compare(Object o1, Object o2) {
			   long p1 = ((Path)o1).status.getSize();
			   long p2 = ((Path)o2).status.getSize();
			   if (p1 > p2)
			       return lower;
			   else if (p1 < p2)
			       return higher;
			   else if (p1 == p2)
			       return 0;
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
			   if(ascending)
			       return p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
			   else
			       return -p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
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
			   if(ascending)
			       return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
			   else
			       return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
		       }
		   }
		   );
	    }
	    reloadData();
	}

	public void tableViewWillDisplayCell(NSTableView view, Object cell, NSTableColumn column, int row) {
	    if (view != null && cell != null && column != null) {
		if(cell instanceof NSTextFieldCell) {
		    if (row % 2 == 0) {
			((NSTextFieldCell)cell).setDrawsBackground(true);
			((NSTextFieldCell)cell).setBackgroundColor(TABLE_CELL_SHADED_COLOR);
		    }
		    else {
//			((NSTextFieldCell)cell).setDrawsBackground(false);
			((NSTextFieldCell)cell).setBackgroundColor(view.backgroundColor());
		    }
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
	    return false;
	}
    }

        // ----------------------------------------------------------
    // Model
    // ----------------------------------------------------------

    class CDBrowserTableDataSource {//implements NSTableView.DataSource {
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
		if(p.isDirectory())
		    return NSImage.imageNamed("folder.tiff");
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

	/**
	    * The files dragged from the browser to the Finder
	 */
	private Path[] promisedDragPaths;
	
    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

	/**
	    * Used by tableView to determine a valid drop target. info contains details on this dragging
	 * operation. The proposed
	 * location is row is and action is operation. Based on the mouse position, the table view
	 * will suggest a proposed drop location.
	 * This method must return a value that indicates which dragging operation the data source will
	 * perform. The data source may
	 * "retarget" a drop if desired by calling setDropRowAndDropOperation and returning something other than
	 * NSDraggingInfo.
	 * DragOperationNone. One may choose to retarget for various reasons (e.g. for better visual
								       * feedback when inserting into a sorted position).
	 */
	public int tableViewValidateDrop( NSTableView tableView, NSDraggingInfo info, int row, int operation) {
	    log.debug("tableViewValidateDrop");
	    tableView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
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
	    log.debug("tableViewAcceptDrop:"+row+","+operation);
	// Get the drag-n-drop pasteboard
	    NSPasteboard pasteboard = info.draggingPasteboard();
	// What type of data are we going to allow to be dragged?  The pasteboard might contain different formats
	    NSArray formats = new NSArray(NSPasteboard.FilenamesPboardType);
	    
	// find the best match of the types we'll accept and what's actually on the pasteboard
	// In the file format type that we're working with, get all data on the pasteboard
	    NSArray filesList = (NSArray)pasteboard.propertyListForType(pasteboard.availableTypeFromArray(formats));
	    Path[] roots = new Path[filesList.count()];
	    Session session = workdir().getSession().copy();
	    for(int i = 0; i < filesList.count(); i++) {
		log.debug(filesList.objectAtIndex(i));
		if(workdir instanceof FTPPath)
		    roots[i] = new FTPPath((FTPSession)session, workdir.getAbsolute(), new java.io.File((String)filesList.objectAtIndex(i)));
		if(workdir instanceof SFTPPath)
		    roots[i] = new SFTPPath((SFTPSession)session, workdir.getAbsolute(), new java.io.File((String)filesList.objectAtIndex(i)));
	    }
	    CDTransferController controller = new CDTransferController(roots, Queue.KIND_UPLOAD);
	    controller.transfer();
	    return true;
	}

		
	/**    Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
	    * The drag image and other drag-related information will be set up and provided by the table view once this call
	    * returns with true.
	    * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
	    * (data, owner, and so on).
	    *@param rows is the list of row numbers that will be participating in the drag.
	    */
	public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
	    log.debug("tableViewWriteRowsToPasteboard:"+rows);
	    Session session = workdir.getSession().copy(); //new
	    if(rows.count() > 0) {
		this.promisedDragPaths = new Path[rows.count()];
//		Path[] draggedPaths = new Path[rows.count()];
		NSMutableArray types = new NSMutableArray();
		for(int i = 0; i < rows.count(); i++) {
		    promisedDragPaths[i] = (Path)this.getEntry(((Integer)rows.objectAtIndex(i)).intValue()).copy(session);
//		    draggedPaths[i] = (Path)this.getEntry(((Integer)rows.objectAtIndex(i)).intValue());
		    if(promisedDragPaths[i].isFile()) {
			if(promisedDragPaths[i].getExtension() != null)
			    types.addObject(promisedDragPaths[i].getExtension());
			else
			    types.addObject(NSPathUtilities.FileTypeUnknown);
		    }
		    else if(promisedDragPaths[i].isDirectory()) {
			types.addObject("'fldr'");
		    }
		    else
			types.addObject(NSPathUtilities.FileTypeUnknown);
		}


		NSEvent event = NSApplication.sharedApplication().currentEvent();
		NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
		NSRect imageRect = new NSRect(new NSPoint(dragPosition.x()-16, dragPosition.y()-16), new NSSize(32, 32));

		CDBrowserTable.this.dragPromisedFilesOfTypes(types, imageRect, this, true, event);
		
	    // The types argument is the list of file types being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
	    }
	    // we return false because we don't want the table to draw the drag image
	    return false;
	}


	        // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------


	public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
	    log.debug("finishedDraggingImage:"+operation);
	    if(! (NSDraggingInfo.DragOperationNone == operation)) {
		if(promisedDragPaths != null) {
		    CDTransferController controller = new CDTransferController(promisedDragPaths, Queue.KIND_DOWNLOAD);
		    controller.transfer();
		    promisedDragPaths = null;
		}
	    }
	    CDBrowserTable.this.deselectAll(null);
	}

	public boolean ignoreModifierKeysWhileDragging() {
	    return false;
	}

	public int draggingSourceOperationMaskForLocal(boolean local) {
	    log.debug("draggingSourceOperationMaskForLocal:"+local);
	    if(local)
		return NSDraggingInfo.DragOperationNone;
	    else
		return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
	}

	/**
	    @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
	 * This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
	 * Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
	 * you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
	 * blocking the destination application.
	 */
	public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
	    log.debug("namesOfPromisedFilesDroppedAtDestination:"+dropDestination);
	    NSMutableArray promisedDragNames = new NSMutableArray();
	    for(int i = 0; i < promisedDragPaths.length; i++) {
		promisedDragPaths[i].setLocal(new java.io.File(dropDestination.getPath(), promisedDragPaths[i].getName()));
		promisedDragNames.addObject(promisedDragPaths[i].getName());
	    }
	    return promisedDragNames;
	}

	

    // ----------------------------------------------------------
    // Data access
    // ----------------------------------------------------------

	public void clear() {
	    log.debug("clear");
	    this.data.clear();
	}

	public void addEntry(Path entry, int row) {
	    this.data.add(row, entry);
	}

	public void addEntry(Path entry) {
	    if(entry.attributes.isVisible())
		this.data.add(entry);
	}

	public Path getEntry(int row) {
	    return (Path)this.data.get(row);
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
