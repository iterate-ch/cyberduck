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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDBrowserTableDataSource extends CDTableDataSource {
	private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

	private List fullData;
	private List currentData;

	private Path workdir;

	public CDBrowserTableDataSource() {
		super();
		this.fullData = new ArrayList();
		this.currentData = new ArrayList();
	}

	public void setWorkdir(Path workdir) {
		this.workdir = workdir;
	}

	public Path workdir() {
		return this.workdir;
	}

	public int numberOfRowsInTableView(NSTableView tableView) {
		return currentData.size();
	}

	//getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//		log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String) tableColumn.identifier();
		Path p = (Path) this.currentData.get(row);
		if (identifier.equals("ICON")) {
			NSImage icon;
			if (p.isDirectory())
				icon = NSImage.imageNamed("folder16.tiff");
			else
				icon = NSWorkspace.sharedWorkspace().iconForFileType(p.getExtension());
			icon.setSize(new NSSize(16f, 16f));
			return icon;
		}
		if (identifier.equals("FILENAME")) {
			return p.getName();
//			return Codec.decode(p.getName());
		}
		else if (identifier.equals("SIZE"))
			return Status.getSizeAsString(p.status.getSize());
		else if (identifier.equals("MODIFIED"))
			return p.attributes.getModified();
		else if (identifier.equals("OWNER"))
			return p.attributes.getOwner();
		else if (identifier.equals("PERMISSIONS"))
			return p.attributes.getPermission().toString();
		throw new IllegalArgumentException("Unknown identifier: " + identifier);
	}

	//setValue()
//    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object value, NSTableColumn tableColumn, int row) {
//		log.debug("tableViewSetObjectValueForLocation:"+row);
//		Path p = (Path)currentData.get(row);
//		p.rename((String)value);
//    }

	/**
	 * The files dragged from the browser to the Finder
	 */
	private Path[] promisedDragPaths;

	// ----------------------------------------------------------
	// Drop methods
	// ----------------------------------------------------------

	public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		log.debug("tableViewValidateDrop:row:" + row + ",operation:" + operation);
		if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
			tableView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
			return NSTableView.DropAbove;
		}
		return NSDraggingInfo.DragOperationNone;
	}

	public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		log.debug("tableViewAcceptDrop:row:" + row + ",operation:" + operation);
		NSPasteboard pboard = info.draggingPasteboard();
		if (pboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
			Object o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
			log.debug("tableViewAcceptDrop:" + o);
			if (o != null) {
				if (o instanceof NSArray) {
					NSArray filesList = (NSArray) o;
					for (int i = 0; i < filesList.count(); i++) {
						log.debug(filesList.objectAtIndex(i));
						Path p = PathFactory.createPath(this.workdir().getSession().copy(),
						    this.workdir().getAbsolute(),
						    new Local((String) filesList.objectAtIndex(i)));
						CDQueueController.instance().addItem(
						    new Queue(p, Queue.KIND_UPLOAD),
						    true);
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

	/**    Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
	 * The drag image and other drag-related information will be set up and provided by the table view once this call
	 * returns with true.
	 * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
	 * (data, owner, and so on).
	 *@param rows is the list of row numbers that will be participating in the drag.
	 */
	public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
		log.debug("tableViewWriteRowsToPasteboard:" + rows);
		if (rows.count() > 0) {
			this.promisedDragPaths = new Path[rows.count()];
			// The fileTypes argument is the list of fileTypes being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
			NSMutableArray fileTypes = new NSMutableArray();
			NSMutableArray queueDictionaries = new NSMutableArray();
			// declare our dragged type in the paste board
			pboard.declareTypes(new NSArray(NSPasteboard.FilesPromisePboardType), null);
			for (int i = 0; i < rows.count(); i++) {
				Session session = this.workdir().getSession().copy();
				promisedDragPaths[i] = (Path) this.getEntry(((Integer) rows.objectAtIndex(i)).intValue()).copy(session);
				if (promisedDragPaths[i].isFile()) {
//					fileTypes.addObject(NSPathUtilities.FileTypeRegular);
					if (promisedDragPaths[i].getExtension() != null)
						fileTypes.addObject(promisedDragPaths[i].getExtension());
					else
						fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
				}
				else if (promisedDragPaths[i].isDirectory()) {
//					fileTypes.addObject(NSPathUtilities.FileTypeDirectory);
					fileTypes.addObject("'fldr'");
				}
				else
					fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
				queueDictionaries.addObject(new Queue(promisedDragPaths[i], Queue.KIND_DOWNLOAD).getAsDictionary());
			}
			// Writing data for private use when the item gets dragged to the transfer queue.
			NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
			queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
			if (queuePboard.setPropertyListForType(queueDictionaries, "QueuePBoardType"))
				log.debug("QueuePBoardType data sucessfully written to pasteboard");
			else
				log.error("Could not write QueuePBoardType data to pasteboard");

			NSEvent event = NSApplication.sharedApplication().currentEvent();
			NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
			NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));

			tableView.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
		}
		// we return false because we don't want the table to draw the drag image
		return false;
	}


//    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
//		log.debug("finishedDraggingImage:operation"+operation);
//		if(! (NSDraggingInfo.DragOperationNone == operation)) {
//			if(promisedDragPaths != null) {
//				for(int i = 0; i < promisedDragPaths.length; i++) {
//					CDQueueController.instance().addItemAndStart(new Queue(promisedDragPaths[i],
//																	   Queue.KIND_DOWNLOAD));
//				}
//				promisedDragPaths = null;
//			}
//		}
//    }

	/**
	 @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
	 * This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
	 * Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
	 * you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
	 * blocking the destination application.
	 */
	public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
		log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
		if (null == dropDestination) {
			return null; //return paths for interapplication communication
		}
		else {
			NSMutableArray promisedDragNames = new NSMutableArray();
			for (int i = 0; i < promisedDragPaths.length; i++) {
				try {
					this.promisedDragPaths[i].setLocal(new Local(java.net.URLDecoder.decode(dropDestination.getPath(), "UTF-8"), 
																 this.promisedDragPaths[i].getName()));
					CDQueueController.instance().addItem(
					    new Queue(this.promisedDragPaths[i], Queue.KIND_DOWNLOAD),
					    true);
					promisedDragNames.addObject(this.promisedDragPaths[i].getName());
				}
				catch (java.io.UnsupportedEncodingException e) {
					log.error(e.getMessage());
				}
			}
			this.promisedDragPaths = null;
			return promisedDragNames;
		}
	}

	// ----------------------------------------------------------
	// Delegate methods
	// ----------------------------------------------------------

	public boolean isSortedAscending() {
		return this.sortAscending;
	}

	public NSTableColumn selectedColumn() {
		return this.selectedColumn;
	}

	private boolean sortAscending = true;
	private NSTableColumn selectedColumn = null;

	public void sort(NSTableColumn tableColumn, final boolean ascending) {
		final int higher = ascending ? 1 : -1;
		final int lower = ascending ? -1 : 1;
		if (tableColumn.identifier().equals("TYPE")) {
			Collections.sort(this.values(),
			    new Comparator() {
				    public int compare(Object o1, Object o2) {
					    Path p1 = (Path) o1;
					    Path p2 = (Path) o2;
					    if (p1.isDirectory() && p2.isDirectory())
						    return 0;
					    if (p1.isFile() && p2.isFile())
						    return 0;
					    if (p1.isFile())
						    return higher;
					    return lower;
				    }
			    }
			);
		}
		else if (tableColumn.identifier().equals("FILENAME")) {
			Collections.sort(this.values(),
			    new Comparator() {
				    public int compare(Object o1, Object o2) {
					    Path p1 = (Path) o1;
					    Path p2 = (Path) o2;
					    if (ascending)
						    return p1.getName().compareToIgnoreCase(p2.getName());
					    else
						    return -p1.getName().compareToIgnoreCase(p2.getName());
				    }
			    }
			);
		}
		else if (tableColumn.identifier().equals("SIZE")) {
			Collections.sort(this.values(),
			    new Comparator() {
				    public int compare(Object o1, Object o2) {
					    long p1 = ((Path) o1).status.getSize();
					    long p2 = ((Path) o2).status.getSize();
					    if (p1 > p2)
						    return higher;
					    else if (p1 < p2)
						    return lower;
					    else
						    return 0;
				    }
			    }
			);
		}
		else if (tableColumn.identifier().equals("MODIFIED")) {
			Collections.sort(this.values(),
			    new Comparator() {
				    public int compare(Object o1, Object o2) {
					    Path p1 = (Path) o1;
					    Path p2 = (Path) o2;
					    if (ascending)
						    return p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
					    else
						    return -p1.attributes.getModifiedDate().compareTo(p2.attributes.getModifiedDate());
				    }
			    }
			);
		}
		else if (tableColumn.identifier().equals("OWNER")) {
			Collections.sort(this.values(),
			    new Comparator() {
				    public int compare(Object o1, Object o2) {
					    Path p1 = (Path) o1;
					    Path p2 = (Path) o2;
					    if (ascending)
						    return p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
					    else
						    return -p1.attributes.getOwner().compareToIgnoreCase(p2.attributes.getOwner());
				    }
			    }
			);
		}
	}

	public void tableViewDidClickTableColumn(NSTableView tableView, NSTableColumn tableColumn) {
		log.debug("tableViewDidClickTableColumn");
		if (this.selectedColumn == tableColumn) {
			this.sortAscending = !this.sortAscending;
		}
		else {
			if (selectedColumn != null)
				tableView.setIndicatorImage(null, selectedColumn);
			this.selectedColumn = tableColumn;
		}
		tableView.setIndicatorImage(this.sortAscending ? NSImage.imageNamed("NSAscendingSortIndicator") : NSImage.imageNamed("NSDescendingSortIndicator"), tableColumn);
		this.sort(tableColumn, sortAscending);
		tableView.reloadData();
	}

	// ----------------------------------------------------------
	// Data access
	// ----------------------------------------------------------

	public void clear() {
		this.fullData.clear();
		this.currentData.clear();
	}

	public void addEntry(Path entry) {
		if (entry.attributes.isVisible())
			this.fullData.add(entry);
		this.currentData = fullData;
	}

	public Path getEntry(int row) {
		return (Path) this.currentData.get(row);
	}

	public void removeEntry(Path o) {
		fullData.remove(fullData.indexOf(o));
		currentData.remove(currentData.indexOf(o));
	}

	public int indexOf(Path o) {
		return currentData.indexOf(o);
	}

	public void setActiveSet(List currentData) {
		this.currentData = currentData;
	}

	public List values() {
		return this.fullData;
	}
}