package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDQueueTableDataSource extends NSObject {
    private static Logger log = Logger.getLogger(CDQueueTableDataSource.class);

    private static CDQueueTableDataSource instance;

    public static final String ICON_COLUMN = "ICON";
    public static final String PROGRESS_COLUMN = "PROGRESS";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    /**
     *
     * @param view
     */
    public int numberOfRowsInTableView(NSTableView view) {
        synchronized(QueueCollection.instance()) {
            return QueueCollection.instance().size();
        }
    }

    /**
     *
     * @param view
     * @param tableColumn
     * @param row
     */
    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        synchronized(QueueCollection.instance()) {
            if (row < numberOfRowsInTableView(view)) {
                final String identifier = (String) tableColumn.identifier();
                final Queue queue = (Queue)QueueCollection.instance().get(row);
                if (identifier.equals(ICON_COLUMN)) {
                    return queue;
                }
                if (identifier.equals(PROGRESS_COLUMN)) {
                    return QueueCollection.instance().getController(row).view();
                }
                if (identifier.equals(TYPEAHEAD_COLUMN)) {
                    return queue.getName();
                }
                throw new IllegalArgumentException("Unknown identifier: " + identifier);
            }
            return null;
        }
    }

    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        log.debug("tableViewValidateDrop:row:" + row + ",operation:" + operation);
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
            tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
            return NSDraggingInfo.DragOperationCopy;
        }
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
            return NSDraggingInfo.DragOperationCopy;
        }
        log.debug("tableViewValidateDrop:DragOperationNone");
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
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
            String droppedText = info.draggingPasteboard().stringForType(NSPasteboard.StringPboardType);// get the data from paste board
            if (droppedText != null) {
                log.info("NSPasteboard.StringPboardType:" + droppedText);
                try {
                    Host h = Host.parse(droppedText);
                    String file = h.getDefaultPath();
                    if (file.length() > 1) {
                        Path p = PathFactory.createPath(SessionFactory.createSession(h), file);
                        Queue q = new DownloadQueue();
                        q.addRoot(p);
                        CDQueueController.instance().startItem(q);
                        return true;
                    }
                }
                catch (java.net.MalformedURLException e) {
                    log.error(e.getMessage());
                }
            }
        }
        else {
            // we are only interested in our private pasteboard with a description of the queue
            // encoded in as a xml.
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
            if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
                log.debug("tableViewAcceptDrop:" + o);
                if (o != null) {
                    NSArray elements = (NSArray) o;
                    synchronized(QueueCollection.instance()) {
                        for (int i = 0; i < elements.count(); i++) {
                            NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                            QueueCollection.instance().add(row, QueueFactory.createQueue(dict));
                            tableView.reloadData();
                            tableView.selectRow(row, false);
                        }
                        pboard.setPropertyListForType(null, "QueuePBoardType");
                    }
                    return true;
                }
            }
        }
        return false;
    }
}