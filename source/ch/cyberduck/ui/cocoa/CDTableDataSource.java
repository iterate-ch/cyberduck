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

import com.apple.cocoa.application.NSDraggingInfo;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;

/**
 * @version $Id$
 */
public abstract class CDTableDataSource {//implements NSTableView.DataSource {

    public abstract int numberOfRowsInTableView(NSTableView tableView);

//	public abstract void tableViewSetObjectValueForLocation(NSTableView tableView, Object value, NSTableColumn tableColumn, int row);

    public abstract Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row);

	// ----------------------------------------------------------
	// TableView Delegate methods
	// ----------------------------------------------------------
	
	/**
		* Returns true to permit aTableView to select the row at rowIndex, false to deny permission.
	 * The delegate can implement this method to disallow selection of particular rows.
	 */
	public boolean tableViewShouldSelectRow(NSTableView aTableView, int rowIndex) {
		return true;
	}
	
	
	/**
		* Returns true to permit aTableView to edit the cell at rowIndex in aTableColumn, false to deny permission.
	 * The delegate can implemen this method to disallow editing of specific cells.
	 */
	public boolean tableViewShouldEditLocation(NSTableView view, NSTableColumn tableColumn, int row) {
		return false;
	}
	
    // ----------------------------------------------------------
    //	NSDraggingSource
    // ----------------------------------------------------------

    public boolean ignoreModifierKeysWhileDragging() {
        return false;
    }

    public int draggingSourceOperationMaskForLocal(boolean local) {
//		if (local)
//			return NSDraggingInfo.DragOperationNone;
//		else
        return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
    }
}
