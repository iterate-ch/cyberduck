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
	
	public int numberOfRowsInTableView(NSTableView tableView) {
		return CDQueuesImpl.instance().size();
	}
	
	//getValue()
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//		log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
		String identifier = (String)tableColumn.identifier();
		Queue item = CDQueuesImpl.instance().getItem(row);
		if(identifier.equals("DATA")) {
			return CDQueuesImpl.instance().getItem(row);
		}
		if(identifier.equals("PROGRESS")) {
			return CDQueuesImpl.instance().getItem(row);
		}
		throw new IllegalArgumentException("Unknown identifier: "+identifier);
	}
}	