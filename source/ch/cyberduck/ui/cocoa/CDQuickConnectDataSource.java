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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Bookmarks;

import com.apple.cocoa.application.*;

/**
* @version $Id$
 */
public class CDQuickConnectDataSource { //implements NSComboBox.DataSource {
	
	private Bookmarks history = CDHistoryImpl.instance();
//	private Bookmarks favorties = CDBookmarksImpl.instance();
	
    // ----------------------------------------------------------
    // NSComboBox.DataSource
    // ----------------------------------------------------------
	
    public int numberOfItemsInComboBox(NSComboBox combo) {
		return history.values().size();
    }
	
    public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
		Host h = (Host)history.values().toArray()[row];
		return h.getHostname();
    }
    
    // ----------------------------------------------------------
    // NSTableView.DataSource
    // ----------------------------------------------------------
	
    public int numberOfRowsInTableView(NSTableView tableView) {
		return history.values().size();
    }
    
    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		String identifier = (String)tableColumn.identifier();
		if(identifier.equals("URL")) {
			Host h = (Host)history.values().toArray()[row];
			return h.getHostname();
		}
		throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }
	
}
