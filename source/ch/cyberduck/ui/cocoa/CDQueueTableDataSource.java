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

import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Queues;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSArray;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDQueueTableDataSource extends CDTableDataSource {
	private static Logger log = Logger.getLogger(CDQueueTableDataSource.class);

	public int numberOfRowsInTableView(NSTableView tableView) {
		return CDQueuesImpl.instance().size();
	}

	//getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//		log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String) tableColumn.identifier();
		Queue item = CDQueuesImpl.instance().getItem(row);
		if (identifier.equals("DATA")) {
			return CDQueuesImpl.instance().getItem(row);
		}
		if (identifier.equals("PROGRESS")) {
			return CDQueuesImpl.instance().getItem(row);
		}
		throw new IllegalArgumentException("Unknown identifier: " + identifier);
	}

	// ----------------------------------------------------------
	// Drop methods
	// ----------------------------------------------------------

	public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
		log.debug("tableViewValidateDrop:row:" + row + ",operation:" + operation);
		NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
		if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
			log.debug("tableViewValidateDrop:DragOperationGeneric");
			// means the drag operation can be desided by the table view
			// the tableview will draw rectangles or lines
			return NSDraggingInfo.DragOperationGeneric;
		}
		log.debug("tableViewValidateDrop:DragOperationNone");
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
		log.debug("tableViewAcceptDrop:row:" + row + ",operation:" + operation);
		// we are only interested in our private pasteboard with a description of the queue
		// encoded in as a xml.
		NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
		log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
		if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
			Object o = pboard.propertyListForType("QueuePBoardType");// get the data from paste board
			log.debug("tableViewAcceptDrop:" + o);
			if (o != null) {
				NSArray elements = (NSArray) o;
				for (int i = 0; i < elements.count(); i++) {
					NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
					if (row != -1) {
						CDQueuesImpl.instance().addItem(new Queue(dict), row);
						tableView.selectRow(row, false);
					}
					else {
						CDQueuesImpl.instance().addItem(new Queue(dict));
						tableView.selectRow(tableView.numberOfRows() - 1, false);
					}
					tableView.reloadData();
				}
				return true;
			}
		}
		return false;
	}
}