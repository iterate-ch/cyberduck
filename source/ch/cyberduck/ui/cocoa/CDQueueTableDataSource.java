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

import java.util.List;
import java.util.ArrayList;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDQueueTableDataSource extends CDTableDataSource {
    private static Logger log = Logger.getLogger(CDQueueTableDataSource.class);
	
	private List data; //Queue elements
	
	public CDQueueTableDataSource() {
		this.data = new ArrayList();
	}
		
	public int numberOfRowsInTableView(NSTableView tableView) {
		return data.size();
	}
	
	//getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//		log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String)tableColumn.identifier();
		Queue item = this.getEntry(row);
		if(identifier.equals("ICON")) {
			return this.getEntry(row);
		}
		if(identifier.equals("DATA")) {
			return this.getEntry(row);
		}
		if(identifier.equals("PROGRESS")) {
			return this.getEntry(row);
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
			this.removeEntry(row);
			tableView.reloadData();
		}
	}
	
	public void addEntry(Queue element) {
		this.data.add(element);
	}
	
	public void removeEntry(int row) {
		this.data.remove(row);
	}
	
	public void removeEntry(Queue o) {
		this.data.remove(this.data.indexOf(o));
	}
	
	public Queue getEntry(int row) {
		return (Queue)data.get(row);
    }
}	