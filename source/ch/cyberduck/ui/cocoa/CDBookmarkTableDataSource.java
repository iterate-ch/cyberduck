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

    private int draggedRow = -1; // keep track of which row got dragged

    public int numberOfRowsInTableView(NSTableView tableView) {
        return BookmarkList.instance().size();
    }

    private static NSImage documentIcon = NSImage.imageNamed("cyberduck-document.icns");

    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < this.numberOfRowsInTableView(tableView)) {
            String identifier = (String)tableColumn.identifier();
            if (identifier.equals("ICON")) {
                return documentIcon;
            }
            if (identifier.equals("BOOKMARK")) {
                return (Host)BookmarkList.instance().getItem(row);
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
            NSArray filesList = (NSArray)info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
            for (int i = 0; i < filesList.count(); i++) {
                String file = (String)filesList.objectAtIndex(i);
                // we do accept only bookmark files
                if (file.indexOf(".duck") != -1) {
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
        log.debug("tableViewAcceptDrop:row:" + index + ",operation:" + operation);
        int row = index;
        if (row < 0) {
            row = 0;
        }
        if (row < tableView.numberOfRows()) {
            if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                NSArray filesList = (NSArray)info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
                Queue q = new Queue(Queue.KIND_UPLOAD);
                Host h = BookmarkList.instance().getItem(row);
                Session session = SessionFactory.createSession(h);
                for (int i = 0; i < filesList.count(); i++) {
                    String filename = (String)filesList.objectAtIndex(i);
                    // Adding a previously exported bookmark file from the Finder
                    if (filename.indexOf(".duck") != -1) {
                        BookmarkList.instance().addItem(BookmarkList.instance().importBookmark(new java.io.File(filename)), row);
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
                if (q.numberOfRoots() > 0) {
                    QueueList.instance().addItem(q);
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
        log.debug("tableViewWriteRowsToPasteboard:" + rows);
        if (rows.count() > 0) {
            this.draggedRow = ((Integer)rows.objectAtIndex(0)).intValue();
            this.promisedDragBookmarks = new Host[rows.count()];
            this.promisedDragBookmarksFiles = new java.io.File[rows.count()];
            for (int i = 0; i < rows.count(); i++) {
                promisedDragBookmarks[i] = (Host)BookmarkList.instance().getItem(((Integer)rows.objectAtIndex(i)).intValue());
            }

            if (pboard.setStringForType("duck", NSPasteboard.FilesPromisePboardType)) {
                log.debug("FilesPromisePboardType data sucessfully written to pasteboard");
            }

            NSEvent event = NSApplication.sharedApplication().currentEvent();
            NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
            NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
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
        return promisedDragNames;
    }
}