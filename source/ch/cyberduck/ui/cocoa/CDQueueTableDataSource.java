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

import com.apple.cocoa.application.NSDraggingInfo;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSDictionary;

import java.net.URL;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDQueueTableDataSource extends CDTableDataSource {
    private static Logger log = Logger.getLogger(CDQueueTableDataSource.class);

    private int queuePboardChangeCount = NSPasteboard.pasteboardWithName("QueuePBoard").changeCount();

    public int numberOfRowsInTableView(NSTableView tableView) {
        return QueueList.instance().size();
    }

    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < numberOfRowsInTableView(tableView)) {
            String identifier = (String) tableColumn.identifier();
            if (identifier.equals("DATA")) {
                return QueueList.instance().getItem(row);
            }
            if (identifier.equals("PROGRESS")) {
                return QueueList.instance().getItem(row);
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
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
            tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
            return NSDraggingInfo.DragOperationCopy;
        }
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        if (this.queuePboardChangeCount < pboard.changeCount()) {
            if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                tableView.setDropRowAndDropOperation(row, NSTableView.DropAbove);
                return NSDraggingInfo.DragOperationCopy;
            }
        }
        log.debug("tableViewValidateDrop:DragOperationNone");
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
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.StringPboardType)) != null) {
            String droppedText = info.draggingPasteboard().stringForType(NSPasteboard.StringPboardType);// get the data from paste board
            if (droppedText != null) {
                log.info("NSPasteboard.StringPboardType:" + droppedText);
                try {
                    URL url = new URL(droppedText);
                    String file = url.getFile();
                    if (file.length() > 1) {
                        Host h = new Host(url.getProtocol(),
                                url.getHost(),
                                url.getPort(),
                                new Login(url.getHost(), url.getUserInfo(), null));
                        Path p = PathFactory.createPath(SessionFactory.createSession(h), file);
                        Queue q = new Queue(Queue.KIND_DOWNLOAD);
                        q.addRoot(p);
                        if (row != -1) {
                            QueueList.instance().addItem(q, row);
                        }
                        else {
                            QueueList.instance().addItem(q);
                        }
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
			if (this.queuePboardChangeCount < pboard.changeCount()) {
				log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
				if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
					Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
					log.debug("tableViewAcceptDrop:" + o);
					if (o != null) {
						NSArray elements = (NSArray) o;
						for (int i = 0; i < elements.count(); i++) {
							NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
							if (row != -1) {
								QueueList.instance().addItem(new Queue(dict), row);
								tableView.reloadData();
								tableView.selectRow(row, false);
							}
							else {
								QueueList.instance().addItem(new Queue(dict));
								tableView.reloadData();
								tableView.selectRow(tableView.numberOfRows() - 1, false);
							}
						}
						this.queuePboardChangeCount++;
						return true;
					}
				}
			}
		}
        return false;
    }
}