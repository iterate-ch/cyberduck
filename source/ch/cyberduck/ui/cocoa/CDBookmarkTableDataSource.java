package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDBookmarkTableDataSource extends CDTableDataSource {
	private static Logger log = Logger.getLogger(CDBookmarkTableDataSource.class);

	private static final File BOOKMARKS_FILE_USER = new File(NSPathUtilities.stringByExpandingTildeInPath("~/Library/Application Support/Cyberduck/Favorites.plist"));
	private static final File BOOKMARKS_FILE_SYSTEM = new File("/Library/Application Support/Cyberduck/Favorites.plist");

	private static final File BOOKMARKS_FILE;

	static {
		if(BOOKMARKS_FILE_SYSTEM.exists()) {
			BOOKMARKS_FILE = BOOKMARKS_FILE_SYSTEM;
		}
		else {
			BOOKMARKS_FILE_USER.getParentFile().mkdir();
			BOOKMARKS_FILE = BOOKMARKS_FILE_USER;
		}
	}

	private static CDBookmarkTableDataSource instance;

	private List data = new ArrayList();

	private CDBookmarkTableDataSource() {
		this.load();
	}

	public static CDBookmarkTableDataSource instance() {
		if(instance == null) {
			instance = new CDBookmarkTableDataSource();
		}
		return instance;
	}

	private int draggedRow = -1; // keep track of which row got dragged

	public int numberOfRowsInTableView(NSTableView tableView) {
		return this.size();
	}

	private static NSImage documentIcon = NSImage.imageNamed("cyberduck-document.icns");

	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		if(row < this.numberOfRowsInTableView(tableView)) {
			String identifier = (String)tableColumn.identifier();
			if(identifier.equals("ICON")) {
				return documentIcon;
			}
			if(identifier.equals("BOOKMARK")) {
				return this.getItem(row);
			}
//			if(identifier.equals("TOOLTIP")) {
//				Host h = this.getItem(row);
//				return h.getURL()+h.getDefaultPath();
//			}
			throw new IllegalArgumentException("Unknown identifier: "+identifier);
		}
		return null;
	}

	// ----------------------------------------------------------
	// Drop methods
	// ----------------------------------------------------------

	public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		log.debug("tableViewValidateDrop:row:"+row+",operation:"+operation);
		if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
			tableView.setDropRowAndDropOperation(row, NSTableView.DropOn);
			NSArray filesList = (NSArray)info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
			for(int i = 0; i < filesList.count(); i++) {
				String file = (String)filesList.objectAtIndex(i);
				// we do accept only bookmark files
				if(file.indexOf(".duck") != -1) {
					tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
					break;
				}
			}
			return NSDraggingInfo.DragOperationCopy;
		}
		return NSDraggingInfo.DragOperationNone;
	}

	/**
	 * Invoked by tableView when the mouse button is released over a table view that previously decided to allow a drop.
	 *
	 * @param info  contains details on this dragging operation.
	 * @param index The proposed location is row and action is operation.
	 *              The data source should
	 *              incorporate the data from the dragging pasteboard at this time.
	 */
	public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int index, int operation) {
		log.debug("tableViewAcceptDrop:row:"+index+",operation:"+operation);
		int row = index;
		if(row < 0) {
			row = 0;
		}
		if(row < tableView.numberOfRows()) {
			if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
				NSArray filesList = (NSArray)info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
				Queue q = new UploadQueue();
				Host h = this.getItem(row);
				Session session = SessionFactory.createSession(h);
				for(int i = 0; i < filesList.count(); i++) {
					String filename = (String)filesList.objectAtIndex(i);
					// Adding a previously exported bookmark file from the Finder
					if(filename.indexOf(".duck") != -1) {
						this.addItem(this.importBookmark(new java.io.File(filename)), row);
						tableView.reloadData();
						//tableView.selectRow(row, false);
						return true;
					}
					// drop of a file from the finder > upload to the remote host this bookmark points to
					Path p = PathFactory.createPath(session,
					    h.getDefaultPath(),
					    new java.io.File(filename).getName());
					p.setLocal(new Local(filename));
					q.addRoot(p);
				}
				// if anything has been added to the queue then process the queue
				if(q.numberOfRoots() > 0) {
					CDQueueController.instance().startItem(q);
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

	/**
	 * Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
	 * The drag image and other drag-related information will be set up and provided by the table view once this call
	 * returns with true.
	 *
	 * @param rows is the list of row numbers that will be participating in the drag.
	 * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
	 *         (data, owner, and so on).
	 */
	public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
		log.debug("tableViewWriteRowsToPasteboard:"+rows);
		if(rows.count() > 0) {
			this.draggedRow = ((Integer)rows.objectAtIndex(0)).intValue();
			this.promisedDragBookmarks = new Host[rows.count()];
			this.promisedDragBookmarksFiles = new java.io.File[rows.count()];
			for(int i = 0; i < rows.count(); i++) {
				promisedDragBookmarks[i] = (Host)this.getItem(((Integer)rows.objectAtIndex(i)).intValue());
			}
			if(pboard.setStringForType("duck", NSPasteboard.FilesPromisePboardType)) {
				log.debug("FilesPromisePboardType data sucessfully written to pasteboard");
			}
			pboard.setDataForType(null, NSPasteboard.FilesPromisePboardType);

			NSEvent event = NSApplication.sharedApplication().currentEvent();
			NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
			NSRect imageRect = new NSRect(new NSPoint(dragPosition.x()-16, dragPosition.y()-16), new NSSize(32, 32));
			tableView.dragPromisedFilesOfTypes(new NSArray("duck"), imageRect, this, true, event);
		}
		// we return false because we don't want the table to draw the drag image
		return false;
	}

	/**
	 * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
	 *         This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
	 *         Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
	 *         you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
	 *         blocking the destination application.
	 */
	public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
		log.debug("namesOfPromisedFilesDroppedAtDestination:"+dropDestination);
		NSMutableArray promisedDragNames = new NSMutableArray();
		for(int i = 0; i < promisedDragBookmarks.length; i++) {
			try {
				promisedDragBookmarksFiles[i] = new java.io.File(java.net.URLDecoder.decode(dropDestination.getPath(), "utf-8"),
				    promisedDragBookmarks[i].getNickname()+".duck");
				this.exportBookmark(promisedDragBookmarks[i], promisedDragBookmarksFiles[i]);
				promisedDragNames.addObject(promisedDragBookmarks[i].getNickname());
			}
			catch(java.io.UnsupportedEncodingException e) {
				log.error(e.getMessage());
			}
		}
		return promisedDragNames;
	}
	
	// ----------------------------------------------------------
	//	Data Manipulation
	// ----------------------------------------------------------

	public void addItem(Host item) {
		this.data.add(item);
		this.save();
	}

	public void addItem(Host item, int row) {
		this.data.add(row, item);
		this.save();
	}

	public void removeItem(int index) {
		if(index < this.size()) {
			this.data.remove(index);
		}
		this.save();
	}

	public void removeItem(Host item) {
		this.removeItem(this.data.lastIndexOf(item));
	}

	public Host getItem(int row) {
		Host result = null;
		if(row < this.size()) {
			result = (Host)this.data.get(row);
		}
		return result;
	}

	public int indexOf(Object o) {
		return this.data.indexOf(o);
	}

	public Collection values() {
		return data;
	}

	public int size() {
		return this.data.size();
	}

	public void clear() {
		this.data.clear();
	}

	public Iterator iterator() {
		return data.iterator();
	}

	public void save() {
		this.save(BOOKMARKS_FILE);
	}

	/**
	 * Saves this collection of bookmarks in to a file to the users's application support directory
	 * in a plist xml format
	 */
	public void save(java.io.File f) {
		log.debug("save");
		if(Preferences.instance().getProperty("favorites.save").equals("true")) {
			try {
				NSMutableArray list = new NSMutableArray();
				java.util.Iterator i = this.iterator();
				while(i.hasNext()) {
					Host bookmark = (Host)i.next();
					list.addObject(bookmark.getAsDictionary());
				}
				NSMutableData collection = new NSMutableData();
				String[] errorString = new String[]{null};
				collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
				    NSPropertyListSerialization.PropertyListXMLFormat,
				    errorString));
				if(errorString[0] != null) {
					log.error("Problem writing bookmark file: "+errorString[0]);
				}

				if(collection.writeToURL(f.toURL(), true)) {
					log.info("Bookmarks sucessfully saved to :"+f.toString());
				}
				else {
					log.error("Error saving Bookmarks to :"+f.toString());
				}
			}
			catch(java.net.MalformedURLException e) {
				log.error(e.getMessage());
			}
		}
	}

	public void load() {
		this.load(BOOKMARKS_FILE);
	}

	/**
	 * Deserialize all the bookmarks saved previously in the users's application support directory
	 */
	public void load(java.io.File f) {
		log.debug("load");
		if(f.exists()) {
			log.info("Found Bookmarks file: "+f.toString());
			NSData plistData = new NSData(f);
			String[] errorString = new String[]{null};
			Object propertyListFromXMLData =
			    NSPropertyListSerialization.propertyListFromData(plistData,
			        NSPropertyListSerialization.PropertyListImmutable,
			        new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
			        errorString);
			if(errorString[0] != null) {
				log.error("Problem reading bookmark file: "+errorString[0]);
			}
			else {
				log.debug("Successfully read Bookmarks: "+propertyListFromXMLData);
			}
			if(propertyListFromXMLData instanceof NSArray) {
				NSArray entries = (NSArray)propertyListFromXMLData;
				java.util.Enumeration i = entries.objectEnumerator();
				Object element;
				while(i.hasMoreElements()) {
					element = i.nextElement();
					if(element instanceof NSDictionary) { //new since 2.1
						this.data.add(new Host((NSDictionary)element));
					}
					if(element instanceof String) { //backward compatibilty <= 2.1beta5 (deprecated)
						try {
							this.addItem(new Host((String)element));
						}
						catch(java.net.MalformedURLException e) {
							log.error("Bookmark has invalid URL: "+e.getMessage());
						}
					}
				}
			}
		}
	}

	public Host importBookmark(java.io.File file) {
		log.info("Importing bookmark from "+file);
		NSData plistData = new NSData(file);
		String[] errorString = new String[]{null};
		Object propertyListFromXMLData =
		    NSPropertyListSerialization.propertyListFromData(plistData,
		        NSPropertyListSerialization.PropertyListImmutable,
		        new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
		        errorString);
		if(errorString[0] != null) {
			log.error("Problem reading bookmark file: "+errorString[0]);
		}
		else {
			log.debug("Successfully read bookmark file: "+propertyListFromXMLData);
		}
		if(propertyListFromXMLData instanceof NSDictionary) {
			return new Host((NSDictionary)propertyListFromXMLData);
		}
		log.error("Invalid file format:"+file);
		return null;
	}

	public void exportBookmark(Host bookmark, java.io.File file) {
		try {
			log.info("Exporting bookmark "+bookmark+" to "+file);
			NSMutableData collection = new NSMutableData();
			String[] errorString = new String[]{null};
			collection.appendData(NSPropertyListSerialization.dataFromPropertyList(bookmark.getAsDictionary(),
			    NSPropertyListSerialization.PropertyListXMLFormat,
			    errorString));
			if(errorString[0] != null) {
				log.error("Problem writing bookmark file: "+errorString[0]);
			}
			if(collection.writeToURL(file.toURL(), true)) {
				log.info("Bookmarks sucessfully saved in :"+file.toString());
				NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(file.getAbsolutePath());
			}
			else {
				log.error("Error saving Bookmarks in :"+file.toString());
			}
		}
		catch(java.net.MalformedURLException e) {
			log.error(e.getMessage());
		}
	}
}