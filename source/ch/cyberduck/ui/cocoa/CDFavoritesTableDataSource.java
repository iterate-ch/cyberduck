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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Favorites;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDFavoritesTableDataSource {//implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);
	
	Favorites favorites = CDFavoritesImpl.instance();
	
	public int numberOfRowsInTableView(NSTableView tableView) {
		return favorites.values().size();
	}
	
	//getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String)tableColumn.identifier();
		if(identifier.equals("FAVORITE")) {
			Host h = (Host)favorites.values().toArray()[row];
			return h.getNickname();
		}
		throw new IllegalArgumentException("Unknown identifier: "+identifier);
	}
	
	public Host getEntry(int row) {
		return (Host)favorites.values().toArray()[row];
    }
	
	// ----------------------------------------------------------
 // Drag methods
 // ----------------------------------------------------------

	/**
		* The files dragged from the favorits drawer to the Finder --> bookmark files
     */
    private Host[] promisedDragFavorites;
	private java.io.File[] promisedDragFavoritesFiles;

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
			this.promisedDragFavorites = new Host[rows.count()];
			this.promisedDragFavoritesFiles = new java.io.File[rows.count()];
			// The types argument is the list of file types being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
			NSMutableArray types = new NSMutableArray();
			for(int i = 0; i < rows.count(); i++) {
				promisedDragFavorites[i] = (Host)this.getEntry(((Integer)rows.objectAtIndex(i)).intValue());
				types.addObject("cyck");
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
			if(promisedDragFavorites != null) {
				for(int i = 0; i < promisedDragFavorites.length; i++) {
					try {
						NSMutableDictionary element = new NSMutableDictionary();
						element.setObjectForKey(promisedDragFavorites[i].getNickname(), Favorites.NICKNAME);
						element.setObjectForKey(promisedDragFavorites[i].getHostname(), Favorites.HOSTNAME);
						element.setObjectForKey(promisedDragFavorites[i].getPort()+"", Favorites.PORT);
						element.setObjectForKey(promisedDragFavorites[i].getProtocol(), Favorites.PROTOCOL);
						element.setObjectForKey(promisedDragFavorites[i].getLogin().getUsername(), Favorites.USERNAME);
						element.setObjectForKey(promisedDragFavorites[i].getDefaultPath(), Favorites.PATH);
						
						NSMutableData collection = new NSMutableData();
						collection.appendData(NSPropertyListSerialization.XMLDataFromPropertyList(element));
						if(collection.writeToURL(promisedDragFavoritesFiles[i].toURL(), true))
							log.info("Favorite sucessfully saved to :"+promisedDragFavoritesFiles[i].toString());
						else
							log.error("Error saving Favorite to :"+promisedDragFavoritesFiles[i].toString());
					}
					catch(java.net.MalformedURLException e) {
						log.error(e.getMessage());
					}
				}
				promisedDragFavorites = null;
				promisedDragFavoritesFiles = null;
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
		for(int i = 0; i < promisedDragFavorites.length; i++) {
			promisedDragFavoritesFiles[i] = new java.io.File(dropDestination.getPath(), promisedDragFavorites[i].getNickname());
			promisedDragNames.addObject(promisedDragFavorites[i].getNickname());
		}
		return promisedDragNames;
    }
}	