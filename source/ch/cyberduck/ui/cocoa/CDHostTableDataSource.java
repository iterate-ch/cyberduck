package ch.cyberduck.ui.cocoa;

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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.ArrayList;
import ch.cyberduck.core.Host;

/**
* @version $Id$
 */
public class CDHostTableDataSource extends NSObject {
    private static Logger log = Logger.getLogger(CDHostTableDataSource.class);

    private List data;

    public CDHostTableDataSource() {
	super();
	this.data = new ArrayList();
	log.debug("CDHostTableDataSource");
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
//	log.debug("CDFavoriteTableDataSource:numberOfRowsInTableView");
	return data.size();
    }

    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
	//	log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
	Host h = (Host)data.get(row);
	String identifier = (String)tableColumn.identifier();
	if(identifier.equals("STATUS")) {
	    //@todo 
	    return NSImage.imageNamed("blipBlue.tiff");
	}
	if(identifier.equals("BUTTON")) {
	    return NSImage.imageNamed("stop.tiff");
	}
	return h.getName();	
    }

    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
	log.debug("tableViewSetObjectValueForLocation");
    }

    public void clear() {
	this.data.clear();
    }

    public void addEntry(Object entry) {
	log.debug("addEntry("+entry+")");
	this.data.add(entry);
    }

    public Object getEntry(int row) {
	log.debug("getEntry("+row+")");
	return this.data.get(row);
    }

    public void removeEntry(Object o) {
	data.remove(data.indexOf(o));
    }
    
    public void removeEntry(int row) {
	data.remove(row);
    }
}
