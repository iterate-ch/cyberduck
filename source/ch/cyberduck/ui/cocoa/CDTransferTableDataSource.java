/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

package ch.cyberduck.ui.cocoa;

import java.util.List;
import java.util.ArrayList;
import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDTransferTableDataSource extends NSObject {//implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDTransferTableDataSource.class);

    private List data;

    public CDTransferTableDataSource() {
	super();
	this.data = new ArrayList();
	log.debug("CDTransferTableDataSource");
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
	return data.size();
    }

    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
	log.debug("tableViewObjectValueForLocation:"+row);
        String identifier = (String)tableColumn.identifier();
	if(identifier.equals("TYPE")) {
	    //if.isDownload() return down.tiff
	    //if.isUpload() return up.tiff
	    return "Upload/Download";
	}
	if(identifier.equals("FILENAME"))
	    return "Filename";
	if(identifier.equals("PROGRESS"))
	    return "00% of 124445K";
	throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }
    
    //setValue()
    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
	log.debug("tableViewSetObjectValueForLocation");
    }

    public void addEntry(Object entry) {
	log.debug("addEntry("+entry+")");
	this.data.add(entry);
    }

    
    public Object getEntry(int row) {
	log.debug("getEntry("+row+")");
	return this.data.get(row);
    }
}
