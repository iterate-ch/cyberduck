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

import ch.cyberduck.core.Path;
import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBrowserTableDataSource extends NSObject {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    private List data;

    public CDBrowserTableDataSource() {
	super();
	this.data = new ArrayList();
	log.debug("CDBrowserTableDataSource");
    }

    public void awakeFromNib() {
	//
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
	    if(p.isFile()) {
//		log.debug(p.getExtension());
		return NSWorkspace.sharedWorkspace().iconForFileType(p.getExtension());
	    }
	    if(p.isDirectory())
		return NSImage.imageNamed("folder.tiff");
	}
	if(identifier.equals("FILENAME"))
	    return p.getName();
	if(identifier.equals("SIZE"))
	    return p.status.getSizeAsString();
	if(identifier.equals("MODIFIED"))
	    return p.attributes.getModified();
	if(identifier.equals("OWNER"))
	    return p.attributes.getOwner();
	if(identifier.equals("PERMISSION"))
	    return p.attributes.getPermission().toString();
	return null;
//	throw new IllegalArgumentException("Unknown identifier: "+identifier);
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
	// Get the drag-n-drop pasteboard
	NSPasteboard pasteboard = info.draggingPasteboard();
	// What type of data are we going to allow to be dragged?  The pasteboard
 // might contain different formats
	NSArray formats = new NSArray(NSPasteboard.FileContentsPboardType);

	// find the best match of the types we'll accept and what's actually on the pasteboard
	// In the file format type that we're working with, get all data on the pasteboard
	NSArray filesList = (NSArray)pasteboard.propertyListForType(pasteboard.availableTypeFromArray(formats));
	// Insert the MP3 filenames into our songs array
	int i = 0;
	for(i = 0; i < filesList.count(); i++) {
	    log.debug(filesList.objectAtIndex(i));
	    //data.addEntry(fileseList.objectAtIndex(i), row+i);
	}
	tableView.reloadData();
	tableView.setNeedsDisplay(true);
	// Select the last song.
	tableView.selectRow(row+i-1, false);
	return true;
    }

    /**    Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
	* The drag image and other drag-related information will be set up and provided by the table view once this call
	* returns with true.
	* @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
	* (data, owner, and so on).
	*@param rows is the list of row numbers that will be participating in the drag.
	*/
    public  boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
	return true;
    }

	

    // ----------------------------------------------------------
    // Data access
    // ----------------------------------------------------------
    
    public void clear() {
	log.debug("clear");
	this.data.clear();
    }

    public void addEntry(Object entry, int row) {
	this.data.add(row, entry);
    }

    public void addEntry(Object entry) {
	this.data.add(entry);
    }
    
    public Object getEntry(int row) {
	return this.data.get(row);
    }

    public void removeEntry(Object o) {
	data.remove(data.indexOf(o));
    }

    public void removeEntry(int row) {
	data.remove(row);
    }

    public int indexOf(Object o) {
	return data.indexOf(o);
    }
}
