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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDTransferTableDataSource extends NSObject {//implements NSTableView.DataSource {

    private static Logger log = Logger.getLogger(CDTransferTableDataSource.class);
    private NSMutableArray files;

    public CDTransferTableDataSource() {
	super();
	log.debug("CDTransferTableDataSource");
    }

    public void awakeFromNib() {
	this.setEntries(new NSMutableArray());
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
	if(null == files)
	    return 0;
	return files.count();
    }

    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
	log.debug("CDTransferTableDataSource.tableViewObjectValueForLocation("+row+")");
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
	log.debug("CDTransferTableDataSource.tableViewSetObjectValueForLocation() not implemented.");
    }

    
    public void addEntry(Object entry, int row) {
	this.files.insertObjectAtIndex(entry, row);
    }

    
    public void addEntry(Object entry) {
	log.debug("CDTransferTableDataSource.addEntry("+entry+")");
	this.files.addObject(entry);
    }

    
    public Object getEntry(int row) {
	log.debug("CDTransferTableDataSource.getEntry("+row+")");
	return this.files.objectAtIndex(row);
    }

    
    public NSArray getEntries() {
	return this.files;
    }

    
    public void setEntries(NSMutableArray files) {
	this.files = files;
    }
}
