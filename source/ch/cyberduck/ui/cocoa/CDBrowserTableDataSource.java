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
public class CDBrowserTableDataSource extends CDTableDataSource {//implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);
	
    private List data;
    private Path workdir;
    
    public CDBrowserTableDataSource() {
		super();
		this.data = new ArrayList();
		log.debug("CDBrowserTableDataSource");
    }
    
    public void setWorkdir(Path workdir) {
		this.workdir = workdir;
    }
	
    public Path workdir() {
		return this.workdir;
    }
    
    public int numberOfRowsInTableView(NSTableView tableView) {
		return data.size();
    }
    
//    public void tableViewSortDescriptorsDidChange(NSTableView tableView, NSArray oldDescriptors) {
//		log.debug("tableViewSortDescriptorsDidChange:"+oldDescriptors);
//			}
    
    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		//	log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String)tableColumn.identifier();
		Path p = (Path)this.data.get(row);
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
		Session session = this.workdir().getSession().copy();
		for(int i = 0; i < filesList.count(); i++) {
			log.debug(filesList.objectAtIndex(i));
			if(this.workdir() instanceof FTPPath)
				roots[i] = new FTPPath((FTPSession)session, this.workdir().getAbsolute(), new java.io.File((String)filesList.objectAtIndex(i)));
			if(this.workdir() instanceof SFTPPath)
				roots[i] = new SFTPPath((SFTPSession)session, this.workdir().getAbsolute(), new java.io.File((String)filesList.objectAtIndex(i)));
		}
		CDTransferController controller = new CDTransferController(roots, Queue.KIND_UPLOAD);
		controller.transfer();
		return true;
    }
    
	// ----------------------------------------------------------
 // Drag methods
 // ----------------------------------------------------------
	
    
    /**    Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
		* The drag image and other drag-related information will be set up and provided by the table view once this call
		* returns with true.
		* @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
		* (data, owner, and so on).
		*@param rows is the list of row numbers that will be participating in the drag.
		*/
    public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
		log.debug("tableViewWriteRowsToPasteboard:"+rows);
		Session session = this.workdir().getSession().copy(); //new
		if(rows.count() > 0) {
			this.promisedDragPaths = new Path[rows.count()];
			// The types argument is the list of file types being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
			NSMutableArray types = new NSMutableArray();
			for(int i = 0; i < rows.count(); i++) {
				promisedDragPaths[i] = (Path)this.getEntry(((Integer)rows.objectAtIndex(i)).intValue()).copy(session);
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
			
			tableView.dragPromisedFilesOfTypes(types, imageRect, this, true, event);
		}
		// we return false because we don't want the table to draw the drag image
		return false;
    }
    
	
    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
		log.debug("finishedDraggingImage:"+operation);
		if(! (NSDraggingInfo.DragOperationNone == operation)) {
			if(promisedDragPaths != null) {
				CDTransferController controller = new CDTransferController(promisedDragPaths, Queue.KIND_DOWNLOAD);
				controller.transfer();
				promisedDragPaths = null;
			}
		}
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
			try {
				promisedDragPaths[i].setLocal(new java.io.File(java.net.URLDecoder.decode(dropDestination.getPath(), "utf-8"), promisedDragPaths[i].getName()));
				promisedDragNames.addObject(promisedDragPaths[i].getName());
			}
			catch(java.io.UnsupportedEncodingException e) {
				log.error(e.getMessage());	
			}
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
    
    public Collection values() {
		return this.data;
    }
}    