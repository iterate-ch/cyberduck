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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.cocoa.application.NSDraggingInfo;
import ch.cyberduck.ui.cocoa.application.NSPasteboard;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.application.NSTableView;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.rococoa.cocoa.foundation.NSUInteger;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.List;

/**
 * @version $Id$
 */
public class BrowserListViewModel extends BrowserTableDataSource implements NSTableView.DataSource {

    public BrowserListViewModel(BrowserController controller) {
        super(controller);
    }

    public NSInteger numberOfRowsInTableView(NSTableView view) {
        if(controller.isMounted()) {
            return new NSInteger(this.childs(this.controller.workdir()).size());
        }
        return new NSInteger(0);
    }

    public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value, NSTableColumn tableColumn, NSInteger row) {
        super.setObjectValueForItem(this.childs(this.controller.workdir()).get(row.intValue()), value, tableColumn.identifier());
    }

    public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, NSInteger row) {
        if(controller.isMounted()) {
            final List<Path> childs = this.childs(this.controller.workdir());
            if(row.intValue() < childs.size()) {
                return super.objectValueForItem(childs.get(row.intValue()), tableColumn.identifier());
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    public NSUInteger tableView_validateDrop_proposedRow_proposedDropOperation(NSTableView view, NSDraggingInfo draggingInfo, NSInteger row, NSUInteger operation) {
        if(controller.isMounted()) {
            Path destination = controller.workdir();
            final int draggingColumn = view.columnAtPoint(draggingInfo.draggingLocation()).intValue();
            if(0 == draggingColumn || 1 == draggingColumn) {
                if(row.intValue() != -1 && row.intValue() < view.numberOfRows().intValue()) {
                    Path p = this.childs(this.controller.workdir()).get(row.intValue());
                    if(p.attributes.isDirectory()) {
                        destination = p;
                    }
                }
            }
            return super.validateDrop(view, destination, row, draggingInfo);
        }
        return super.validateDrop(view, null, row, draggingInfo);
    }

    public boolean tableView_acceptDrop_row_dropOperation(NSTableView view, NSDraggingInfo draggingInfo, NSInteger row, NSUInteger operation) {
        if(controller.isMounted()) {
            Path destination = controller.workdir();
            if(row.intValue() != -1 && row.intValue() < view.numberOfRows().intValue()) {
                destination = this.childs(this.controller.workdir()).get(row.intValue());
            }
            return super.acceptDrop(view, destination, draggingInfo);
        }
        return super.acceptDrop(view, null, draggingInfo);
    }

    // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------

    /**
     * Invoked by view after it has been determined that a drag should begin, but before the drag has been started.
     * The drag image and other drag-related information will be set up and provided by the table view once this call
     * returns with true.
     *
     * @param rowIndexes is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard (data, owner, and so on).
     */
    public boolean tableView_writeRowsWithIndexes_toPasteboard(NSTableView view, NSIndexSet rowIndexes, NSPasteboard pboard) {
        if(controller.isMounted()) {
            NSMutableArray items = NSMutableArray.array();
            final AttributedList<Path> childs = this.childs(this.controller.workdir());
            for(NSUInteger index = rowIndexes.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = rowIndexes.indexGreaterThanIndex(index)) {
                items.addObject(NSString.stringWithString(childs.get(index.intValue()).getAbsolute()));
            }
            return super.writeItemsToPasteBoard(view, items, pboard);
        }
        return false;
    }

    public NSArray tableView_namesOfPromisedFilesDroppedAtDestination_forDraggedRowsWithIndexes(NSTableView view, final NSURL dropDestination, NSIndexSet rowIndexes) {
        return this.namesOfPromisedFilesDroppedAtDestination(dropDestination);
    }

//    public NSArray tableView_namesOfPromisedFilesDroppedAtDestination_forDraggedRowsWithIndexes(NSTableView view, final NSURL dropDestination, NSIndexSet rowIndexes) {
//        final NSMutableArray promisedDragNames = NSMutableArray.arrayWithCapacity(rowIndexes.count().intValue());
//        final List<Path> roots = new Collection<Path>();
//        final AttributedList<Path> childs = this.childs(this.controller.workdir());
//        for(NSUInteger index = rowIndexes.firstIndex(); index.intValue() != NSIndexSet.NSNotFound; index = rowIndexes.indexGreaterThanIndex(index)) {
//            Path promisedDragPath = childs.get(index.intValue());
//            promisedDragPath.setLocal(LocalFactory.createLocalLocal(dropDestination.path(), promisedDragPath.getName()));
//            if(rowIndexes.count().intValue() == 1) {
//                if(promisedDragPath.attributes.isFile()) {
//                    promisedDragPath.getLocal().touch();
//                }
//                if(promisedDragPath.attributes.isDirectory()) {
//                    promisedDragPath.getLocal().mkdir();
//                }
//            }
//            promisedDragNames.addObject(NSString.stringWithString(promisedDragPath.getLocal().getName()));
//            roots.add(promisedDragPath);
//        }
//        final Transfer q = new DownloadTransfer(roots);
//        if(q.numberOfRoots() > 0) {
//            controller.transfer(q);
//        }
//        return promisedDragNames;
//    }
}