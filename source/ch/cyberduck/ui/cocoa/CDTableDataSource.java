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

import java.util.Collection;

import com.apple.cocoa.application.*;

/**
* @version $Id$
 */
public abstract class CDTableDataSource {//implements NSTableView.DataSource {

	public abstract Collection values();
	
	public abstract int numberOfRowsInTableView(NSTableView tableView);
	
//	public abstract void tableViewSetObjectValueForLocation(NSTableView tableView, Object value, NSTableColumn tableColumn, int row);
	
	public abstract Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row);
}
