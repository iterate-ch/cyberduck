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

import ch.cyberduck.core.Bookmarks;
import ch.cyberduck.core.Host;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBookmarkTableDataSource extends CDTableDataSource {
    private static Logger log = Logger.getLogger(CDBookmarkTableDataSource.class);
	
	private Bookmarks bookmarks = CDBookmarksImpl.instance();
	
	private NSArray draggedRows;	// keep track of which row got dragged

	public int numberOfRowsInTableView(NSTableView tableView) {
		return bookmarks.size();
	}
	
	//getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//		log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String)tableColumn.identifier();
		if(identifier.equals("ICON")) {
			return NSImage.imageNamed("cyberduck-document.icns");
		}
		if(identifier.equals("BOOKMARK")) {
			return (Host)bookmarks.values().toArray()[row];
		}
		throw new IllegalArgumentException("Unknown identifier: "+identifier);
	}
	
//	public Host getEntry(int row) {
//		return (Host)bookmarks.values().toArray()[row];
//    }
	
	// ----------------------------------------------------------
 // Drop methods
 // ----------------------------------------------------------

	public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		log.debug("tableViewValidateDrop");
		NSPasteboard pboard = info.draggingPasteboard();
//		NSArray formats = new NSArray(NSPasteboard.FilenamesPboardType);
//		NSArray filesList = (NSArray)pasteboard.propertyListForType(pasteboard.availableTypeFromArray(formats));
		if(pboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
			Object o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
			log.debug("tableViewValidateDrop:"+o);
			if(o != null) {
				if(o instanceof NSArray) {
					NSArray filesList = (NSArray)o;
					for(int i = 0; i < filesList.count(); i++) {
						String file = (String)filesList.objectAtIndex(i);
						log.debug(file);
						if(file.indexOf(".duck") != -1) {
							return NSDraggingInfo.DragOperationGeneric;
						}
						return NSDraggingInfo.DragOperationNone;
					}
				}
			}
		}
		return NSDraggingInfo.DragOperationNone;
	}
	
	/**
		* Invoked by tableView when the mouse button is released over a table view that previously decided to allow a drop.
     * @param info contains details on this dragging operation.
     * @param row The proposed location is row and action is operation.
     * The data source should
     * incorporate the data from the dragging pasteboard at this time.
     */
    public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		log.debug("tableViewAcceptDrop:"+row+","+operation);
		NSPasteboard pboard = info.draggingPasteboard();
		// What type of data are we going to allow to be dragged?  The pasteboard might contain different formats
//		NSArray formats = new NSArray(NSPasteboard.FilenamesPboardType);
		if(pboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
			Object o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
			log.debug("tableViewAcceptDrop:"+o);
			if(o != null) {
				if(o instanceof NSArray) {
					NSArray filesList = (NSArray)o;
					//				NSArray filesList = (NSArray)pasteboard.propertyListForType(pasteboard.availableTypeFromArray(formats));
					for(int i = 0; i < filesList.count(); i++) {
						Host h = CDBookmarksImpl.instance().importBookmark(new java.io.File((String)filesList.objectAtIndex(i)));
						if(row != -1) {
							CDBookmarksImpl.instance().addItem(h, row);
							tableView.selectRow(row, false);
						}
						else {
							CDBookmarksImpl.instance().addItem(h);
							tableView.selectRow(tableView.numberOfRows()-1, false);
						}
						tableView.reloadData();
					}
					return true;
				}
			}
		}
		else if(pboard.availableTypeFromArray(new NSArray("BookmarkPBoardType")) != null) {
			// test to see if the string for the type we defined in the paste board.
			// if doesn't, do nothing.
			Object o = pboard.propertyListForType("BookmarkPBoardType");// get the data from paste board
			log.debug("tableViewAcceptDrop:"+o);
			if(o != null) {
				if(o instanceof NSArray) {
					for(int i = 0; i < draggedRows.count(); i++) {
						CDBookmarksImpl.instance().removeItem(((Integer)draggedRows.objectAtIndex(i)).intValue());
						tableView.reloadData();
					}
					NSArray elements = (NSArray)o;
					for(int i = 0; i < elements.count(); i++) {
						NSDictionary dict = (NSDictionary)elements.objectAtIndex(i);
						if(row != -1)
							CDBookmarksImpl.instance().addItem(new Host(dict), row);
						else
							CDBookmarksImpl.instance().addItem(new Host(dict));
						tableView.reloadData();
					}
					return true;
				}
			}
		}
		return false;
    }
	
	// ----------------------------------------------------------
 // Drag methods
 // ----------------------------------------------------------

	/**
		* The files dragged from the favorits drawer to the Finder --> bookmark files
     */
    private Host[] promisedDragBookmarks;
	private java.io.File[] promisedDragBookmarksFiles;

	/**    Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
		* The drag image and other drag-related information will be set up and provided by the table view once this call
		* returns with true.
		* @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
		* (data, owner, and so on).
		*@param rows is the list of row numbers that will be participating in the drag.
		*/
    public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
		log.debug("tableViewWriteRowsToPasteboard:"+rows);
		if(rows.count() > 0) {
			this.draggedRows = rows;
			this.promisedDragBookmarks = new Host[rows.count()];
			this.promisedDragBookmarksFiles = new java.io.File[rows.count()];
			// The types argument is the list of file types being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
			NSMutableArray types = new NSMutableArray();
			for(int i = 0; i < rows.count(); i++) {
				promisedDragBookmarks[i] = (Host)bookmarks.getItem(((Integer)rows.objectAtIndex(i)).intValue());
				types.addObject("duck");
			}
			NSEvent event = NSApplication.sharedApplication().currentEvent();
			NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
			NSRect imageRect = new NSRect(new NSPoint(dragPosition.x()-16, dragPosition.y()-16), new NSSize(32, 32));
			tableView.dragPromisedFilesOfTypes(types, imageRect, this, true, event);
		}
		// we return false because we don't want the table to draw the drag image
		return false;
    }
	
    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
		log.debug("finishedDraggingImage:"+operation);
		if(! (NSDraggingInfo.DragOperationNone == operation)) {
			if(promisedDragBookmarks != null) {
				for(int i = 0; i < promisedDragBookmarks.length; i++) {
					CDBookmarksImpl.instance().exportBookmark(promisedDragBookmarks[i], promisedDragBookmarksFiles[i]);
				}
				promisedDragBookmarks = null;
				promisedDragBookmarksFiles = null;
				draggedRows = null;
			}
		}
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
		for(int i = 0; i < promisedDragBookmarks.length; i++) {
			try {
				promisedDragBookmarksFiles[i] = new java.io.File(
													 java.net.URLDecoder.decode(dropDestination.getPath(), "utf-8"), 
													 promisedDragBookmarks[i].getNickname()+".duck"
													 );
				promisedDragNames.addObject(promisedDragBookmarks[i].getNickname());
			}
			catch(java.io.UnsupportedEncodingException e) {
				log.error(e.getMessage());	
			}
		}
		return promisedDragNames;
    }
}	