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

import java.util.List;
import java.util.ArrayList;
import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import ch.cyberduck.core.Path;

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
//	log.debug("tableViewObjectValueForLocation:"+tableColumn.identifier()+","+row);
        String identifier = (String)tableColumn.identifier();
        Path p = (Path)data.get(row);
	if(identifier.equals("TYPE")) {
	    //@todo if(p.isDownload())
	    return NSImage.imageNamed("download.tiff");
	    //if(p.isUpload()) return NSImage.imageNamed("upload.tiff");
	}
	if(identifier.equals("FILENAME"))
	    return p.getName();
	if(identifier.equals("PROGRESS"))
	    return null;
//	    return p.status.getCurrent()+"";
//	    return new NSProgressIndicator();
	if(identifier.equals("BUTTON")) {
	    return NSImage.imageNamed("stop.tiff");
//	    return new NSButtonCell(NSImage.imageNamed("stop.tiff"));
	}
	throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }
    
    //setValue()
    /**
	* Sets an attribute value for the record in aTableView at rowIndex. anObject is the new value,
     * and aTableColumn contains the identifier for the attribute, which you get by using NSTableColumn's
     * identifier method.
     */
    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
	log.debug("tableViewSetObjectValueForLocation");
    }

    public void addEntry(Object entry) {
	this.data.add(entry);
    }

    public void removeEntry(Object o) {
	data.remove(data.indexOf(o));
    }

    public void removeEntry(int row) {
	data.remove(row);
    }
    
    public Object getEntry(int row) {
	return this.data.get(row);
    }
    
    public int indexOf(Object o) {
	return data.indexOf(o);
    }    
}
