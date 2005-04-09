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

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

/**
 * @version $Id$
 */
public class CDBrowserColumnViewModel extends CDTableDataSource {
    private static Logger log = Logger.getLogger(CDBrowserColumnViewModel.class);

    public CDBrowserColumnViewModel(CDBrowserController controller) {
        super(controller);
    }

	public int browserNumberOfRowsInColumn(NSBrowser sender, int col) {
		if(controller.isMounted()) {
			String absolute = this.pathOfColumn(sender, col);
			return this.cache(PathFactory.createPath(controller.workdir().getSession(),
													 absolute)).size();
		}
		return 0;
	}
		
	public void browserWillDisplayCell(NSBrowser sender, NSBrowserCell browserCell, int row, int col) {
		String absolute = this.pathOfColumn(sender, col);
		Path path = (Path)this.cache(PathFactory.createPath(controller.workdir().getSession(),
																 absolute)).get(row);
		((CDBrowserCell)browserCell).setPath(path);
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