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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDBookmarkTableDataSource extends CDTableDataSource {
    private static Logger log = Logger.getLogger(CDBookmarkTableDataSource.class);

    private NSArray draggedRows; // keep track of which row got dragged

    public int numberOfRowsInTableView(NSTableView tableView) {
        return BookmarkList.instance().size();
    }

    private static NSImage documentIcon = NSImage.imageNamed("cyberduck-document.icns");

    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < this.numberOfRowsInTableView(tableView)) {
            String identifier = (String) tableColumn.identifier();
            if (identifier.equals("ICON")) {
                return documentIcon;
            }
            if (identifier.equals("BOOKMARK")) {
                return (Host) BookmarkList.instance().getItem(row);
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        return null;
    }


    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        log.debug("tableViewValidateDrop:row:" + row + ",operation:" + operation);
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
			tableView.setDropRowAndDropOperation(row, NSTableView.DropOn);
			Object o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
			if (o != null) {
				if (o instanceof NSArray) {
					NSArray filesList = (NSArray) o;
					for (int i = 0; i < filesList.count(); i++) {
						String file = (String) filesList.objectAtIndex(i);
						// we do accept only bookmark files
						if (file.indexOf(".duck") != -1) {
							tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
							break;
						}
					}
				}
			}
            return NSDraggingInfo.DragOperationCopy;
        }
        if (draggedRows != null) {
			// Reordering of the elements in the table view is supported
			// We declare our own pasteboard to hold the data temporarly
            NSPasteboard bookmarkPboard = NSPasteboard.pasteboardWithName("BookmarkPBoard");
            if (bookmarkPboard.availableTypeFromArray(new NSArray("BookmarkPBoardType")) != null) {
                tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
				tableView.deselectAll(null);
                return NSDraggingInfo.DragOperationMove;
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    /**
     * Invoked by tableView when the mouse button is released over a table view that previously decided to allow a drop.
     *
     * @param info contains details on this dragging operation.
     * @param row  The proposed location is row and action is operation.
     *             The data source should
     *             incorporate the data from the dragging pasteboard at this time.
     */
    public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        log.debug("tableViewAcceptDrop:row:" + row + ",operation:" + operation);
        if (row != -1 && row < tableView.numberOfRows()) {
            if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                Object o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
                log.debug("tableViewAcceptDrop:" + o);
                if (o != null) {
                    if (o instanceof NSArray) {
                        NSArray filesList = (NSArray) o;
                        Queue q = new Queue(Queue.KIND_UPLOAD);
                        Host h = BookmarkList.instance().getItem(row);
                        Session session = SessionFactory.createSession(h);
                        for (int i = 0; i < filesList.count(); i++) {
                            String filename = (String) filesList.objectAtIndex(i);
							// Adding a previously exportetd bookmark file from the Finder
                            if (filename.indexOf(".duck") != -1) {
                                BookmarkList.instance().addItem(BookmarkList.instance().importBookmark(new java.io.File(filename)), row);
								tableView.reloadData();
                                tableView.selectRow(row, false);
                            }
							// drop of a file from the finder > upload to the remote host this bookmark
							// points to
							else {
								Path p = PathFactory.createPath(session,
																h.getDefaultPath(),
																new java.io.File(filename).getName());
                                p.setLocal(new Local(filename));
                                q.addRoot(p);
                            }
                        }
						// if anything has been added to the queue then process the queue
                        if (q.numberOfRoots() > 0) {
                            QueueList.instance().addItem(q);
                            CDQueueController.instance().startItem(q);
                        }
						// the drag was sucessfull
                        return true;
                    }
                }
            }
            if (draggedRows != null) {
                NSPasteboard bookmarkPboard = NSPasteboard.pasteboardWithName("BookmarkPBoard");
                log.debug("availableTypeFromArray:BookmarkPBoardType: " + bookmarkPboard.availableTypeFromArray(new NSArray("BookmarkPBoardType")));
                if (bookmarkPboard.availableTypeFromArray(new NSArray("BookmarkPBoardType")) != null) {
                    Object o = bookmarkPboard.propertyListForType("BookmarkPBoardType");// get the data from paste board
                    log.debug("tableViewAcceptDrop:" + o);
                    if (o != null) {
                        if (o instanceof NSArray) {
                            for (int i = 0; i < draggedRows.count(); i++) {
								// remove the bookmark item from its old location
                                BookmarkList.instance().removeItem(((Integer) draggedRows.objectAtIndex(i)).intValue());
                            }
                            NSArray elements = (NSArray) o;
                            for (int i = 0; i < elements.count(); i++) {
                                NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                                if (row != -1 && row < BookmarkList.instance().size() - 1) {
                                    BookmarkList.instance().addItem(new Host(dict), row);
								}
                            }
                            draggedRows = null;
							tableView.reloadData();
							tableView.selectRow(row, false);
                            return true;
                        }
                    }
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
        log.debug("tableViewWriteRowsToPasteboard:" + rows);
        if (rows.count() > 0) {
            this.draggedRows = rows;
            this.promisedDragBookmarks = new Host[rows.count()];
            this.promisedDragBookmarksFiles = new java.io.File[rows.count()];
            // The types argument is the list of file types being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
            NSMutableArray fileTypes = new NSMutableArray();
            NSMutableArray bookmarkDictionaries = new NSMutableArray();
            for (int i = 0; i < rows.count(); i++) {
                promisedDragBookmarks[i] = (Host) BookmarkList.instance().getItem(((Integer) rows.objectAtIndex(i)).intValue());
                fileTypes.addObject("duck");
                bookmarkDictionaries.addObject(promisedDragBookmarks[i].getAsDictionary());
            }

            NSPasteboard bookmarkPboard = NSPasteboard.pasteboardWithName("BookmarkPBoard");
            bookmarkPboard.declareTypes(new NSArray("BookmarkPBoardType"), null);
            if (bookmarkPboard.setPropertyListForType(bookmarkDictionaries, "BookmarkPBoardType")) {
                log.debug("BookmarkPBoardType data sucessfully written to pasteboard");
            }
            else {
                log.error("Could not write BookmarkPBoardType data to pasteboard");
            }

            NSEvent event = NSApplication.sharedApplication().currentEvent();
            NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
            NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
            tableView.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
        }
        // we return false because we don't want the table to draw the drag image
        return false;
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     * This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     * Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     * you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     * blocking the destination application.
     */
    public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        NSMutableArray promisedDragNames = new NSMutableArray();
        for (int i = 0; i < promisedDragBookmarks.length; i++) {
            try {
                promisedDragBookmarksFiles[i] = new java.io.File(java.net.URLDecoder.decode(dropDestination.getPath(), "utf-8"),
                        promisedDragBookmarks[i].getNickname() + ".duck");
                BookmarkList.instance().exportBookmark(promisedDragBookmarks[i], promisedDragBookmarksFiles[i]);
                promisedDragNames.addObject(promisedDragBookmarks[i].getNickname());
            }
            catch (java.io.UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }
        }
        promisedDragBookmarks = null;
        promisedDragBookmarksFiles = null;
        draggedRows = null;
        return promisedDragNames;
    }
}