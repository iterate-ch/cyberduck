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

/**
* @version $Id$
 */
public class CDFavoritesTableDataSource extends NSObject {
    private static Logger log = Logger.getLogger(CDHostTableDataSource.class);

    private List data;

    public CDFavoritesTableDataSource() {
	super();
	this.data = new ArrayList();
	log.debug("CDFavoritesTableDataSource");
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
	return data.size();
    }

    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
	return null;
    }

    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
	//
    }

    public void clear() {
	this.data.clear();
    }

    public void addEntry(Object entry) {
	this.data.add(entry);
    }

    public Object getEntry(int row) {
	return this.data.get(row);
    }

    public void removeEntry(Object o) {
	data.remove(data.indexOf(o));
    }

    public void removeEntry(int row) {
	data.remove(row);
    }

    public int indexOf(Object o) {
	return data.indexOf(o);
    }    
}
