package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSBrowser;
import com.apple.cocoa.application.NSBrowserCell;
import com.apple.cocoa.application.NSTableColumn;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

/**
 * @version $Id$
 */
public class CDBrowserColumnViewModel extends CDTableDataSource {

    public CDBrowserColumnViewModel(CDBrowserController controller) {
        super(controller);
    }

	public int browserNumberOfRowsInColumn(NSBrowser sender, int col) {
		if(controller.isMounted()) {
			String absolute = this.pathOfColumn(sender, col);
			return this.childs(PathFactory.createPath(controller.workdir().getSession(),
													 absolute)).size();
		}
		return 0;
	}
		
	public void browserWillDisplayCell(NSBrowser sender, NSBrowserCell cell, int row, int col) {
		String absolute = this.pathOfColumn(sender, col);
		if(cell instanceof CDBrowserCell) {
			Path path = (Path)this.childs(PathFactory.createPath(controller.workdir().getSession(),
																absolute)).get(row);
			((CDBrowserCell)cell).setPath(path);
            if(this.controller.isConnected()) {
                //((CDBrowserCell)cell).setTextColor(NSColor.controlTextColor());
            }
            else {
                //((CDBrowserCell)cell).setTextColor(NSColor.disabledControlTextColor());
            }
		}
	}
	
	private String pathOfColumn(NSBrowser sender, int column) {
		if(0 == column) {
			return "/";
		}
		//Returns a string representing the path from the first column up to, but not including, the column at index column.
		return sender.pathToColumn(column);
	}
	
    public void sort(NSTableColumn tableColumn, final boolean ascending) {
		// column view is not sortable
	}
}