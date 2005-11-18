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

import ch.cyberduck.core.Path;

import com.apple.cocoa.application.NSDraggingInfo;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSMutableArray;

import java.util.List;

/**
 * @version $Id$
 */
public class CDBrowserListViewModel extends CDBrowserTableDataSource {

    public CDBrowserListViewModel(CDBrowserController controller) {
        super(controller);
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
        if (controller.isMounted()) {
            return this.childs(this.controller.workdir()).size();
        }
        return 0;
    }

    public void tableViewSetObjectValueForLocation(NSTableView view, Object value, NSTableColumn tableColumn, int row) {
        if (controller.isMounted()) {
            super.setObjectValueForItem((Path) this.childs(this.controller.workdir()).get(row), value, (String) tableColumn.identifier());
        }
    }

    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (controller.isMounted()) {
            List childs = this.childs(this.controller.workdir());
            if (row < childs.size()) {
                return super.objectValueForItem((Path) childs.get(row), (String) tableColumn.identifier());
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        if (controller.isMounted()) {
            Path destination = controller.workdir();
            if (row != -1 && row < tableView.numberOfRows()) {
                Path p = ((Path) this.childs(this.controller.workdir()).get(row));
                if(p.attributes.isDirectory()) {
                    destination = p;
                }
            }
            return super.validateDrop(tableView, destination, row, info);
        }
        return NSDraggingInfo.DragOperationNone;
    }

    public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        if (controller.isMounted()) {
            Path destination = controller.workdir();
            if (row != -1 && row < tableView.numberOfRows()) {
                destination = ((Path) this.childs(this.controller.workdir()).get(row));
            }
            return super.acceptDrop(tableView, destination, info);
        }
        return false;
    }

    // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------

    /**
     * Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
     * The drag image and other drag-related information will be set up and provided by the table view once this call
     * returns with true.
     *
     * @param rows is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard (data, owner, and so on).
     */
    public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
        if (controller.isMounted()) {
            NSMutableArray items = new NSMutableArray();
            List childs = this.childs(this.controller.workdir());
            for (int i = 0; i < rows.count(); i++) {
                items.addObject(childs.get(((Integer) rows.objectAtIndex(i)).intValue()));
            }
            return super.writeItemsToPasteBoard(tableView, items, pboard);
        }
        return false;
    }
}