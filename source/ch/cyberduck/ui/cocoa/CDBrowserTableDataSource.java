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

import ch.cyberduck.core.Path;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

//import java.util.Observer;
//import java.util.Observable;

import java.util.List;
import java.util.ArrayList;

//import ch.cyberduck.core.Bookmark;
//import ch.cyberduck.core.Path;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBrowserTableDataSource extends NSObject {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    private List data;

    public CDBrowserTableDataSource() {
	super();
	this.setEntries(new ArrayList());
	log.debug("CDBrowserTableDataSource");
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
	return data.size();
    }

    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        String identifier = (String)tableColumn.identifier();
        Path p = (Path)data.get(row);
/*	if(identifier.equals("TYPE")) {
	    if(p.isFile())
		return NSImage.imageNamed("file.tiff");
	    if(p.isDirectory())
		return NSImage.imageNamed("folder.tiff");
	}
	*/
	if(identifier.equals("FILENAME"))
	    return p.getName();
	if(identifier.equals("SIZE"))
	    return p.getSize();
	if(identifier.equals("MODIFIED"))
	    return p.getModified();
	if(identifier.equals("OWNER"))
	    return p.getOwner();
	if(identifier.equals("PERMISSION"))
	    return p.getPermission().toString();
	return null;
//	throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }

    //setValue()
    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object value, NSTableColumn tableColumn, int row) {
        Path p = (Path)data.get(row);
	p.rename((String)value);
    }
    
    public void clear() {
	this.data.clear();
    }

    public void addEntry(Object entry, int row) {
	this.data.add(row, entry);
    }

    public void addEntry(Object entry) {
	log.debug("CDListingTableDataSource.addEntry("+entry+")");
	this.data.add(entry);
    }
    
    public Object getEntry(int row) {
	log.debug("CDListingTableDataSource.getEntry("+row+")");
	return this.data.get(row);
    }

    public List getEntries() {
	return this.data;
    }

    public void setEntries(List data) {
	this.data = data;
    }
}
