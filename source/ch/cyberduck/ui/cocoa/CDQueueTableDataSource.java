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

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDQueueTableDataSource extends CDTableDataSource {
    private static Logger log = Logger.getLogger(CDQueueTableDataSource.class);
	
//	private List data; //Queue elements
	private Queues data = CDQueuesImpl.instance();
	
//	public CDQueueTableDataSource() {
//		this.data = new ArrayList();
//	}
		
	public int numberOfRowsInTableView(NSTableView tableView) {
		return data.size();
	}
	
	//getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//		log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String)tableColumn.identifier();
		Queue item = this.getEntry(row);
		if(identifier.equals("ICON")) {
			return data.getItem(row);
		}
		if(identifier.equals("DATA")) {
			return data.getItem(row);
		}
		if(identifier.equals("PROGRESS")) {
			return data.getItem(row);
		}
		if(identifier.equals("REMOVE")) {
			return NSImage.imageNamed("cancel.tiff");
		}
		throw new IllegalArgumentException("Unknown identifier: "+identifier);
	}
	
	//setValue()
	public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
		String identifier = (String)tableColumn.identifier();
		if(identifier.equals("REMOVE")) {
			data.removeItem(row);
			tableView.reloadData();
		}
	}
	
	public void addEntry(Queue element) {
		this.data.addItem(element);
	}
	
	public void removeEntry(int row) {
		this.data.removeItem(row);
	}
	
	public void removeEntry(Queue o) {
		this.data.removeItem(this.data.indexOf(o));
	}
	
	public Queue getEntry(int row) {
		return (Queue)data.getItem(row);
    }
}	