package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.cocoa.application.NSDraggingInfo;
import ch.cyberduck.ui.cocoa.application.NSPasteboard;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.application.NSTableView;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSIndexSet;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSURL;

/**
 * @version $Id$
 */
public abstract class CDListDataSource extends CDController implements NSTableView.DataSource {

    public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value, NSTableColumn tableColumn, int row) {
        throw new RuntimeException("Not editable");
    }

    public boolean tableView_writeRowsWithIndexes_toPasteboard(NSTableView view, NSIndexSet rowIndexes, NSPasteboard pboard) {
        return false;
    }

    public NSArray tableView_namesOfPromisedFilesDroppedAtDestination_forDraggedRowsWithIndexes(NSTableView view, final NSURL dropDestination, NSIndexSet rowIndexes) {
        return NSArray.array();
    }

    public int tableView_validateDrop_proposedRow_proposedDropOperation(NSTableView view, NSObject info, int row, int operation) {
        return NSDraggingInfo.NSDragOperationNone;
    }

    public boolean tableView_acceptDrop_row_dropOperation(NSTableView view, NSObject info, int row, int operation) {
        return false;
    }

}
