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

import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDListDataSource extends CDController implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDListDataSource.class);

    public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value, NSTableColumn tableColumn, int row) {
        throw new RuntimeException("Not editable");
    }

    public boolean tableView_writeRowsWithIndexes_toPasteboard(NSTableView view, NSIndexSet rowIndexes, NSPasteboard pboard) {
        return false;
    }

    public NSArray tableView_namesOfPromisedFilesDroppedAtDestination_forDraggedRowsWithIndexes(NSTableView view, final NSURL dropDestination, NSIndexSet rowIndexes) {
        return NSArray.array();
    }

    public int tableView_validateDrop_proposedRow_proposedDropOperation(NSTableView view, NSDraggingInfo draggingInfo, int row, int operation) {
        return NSDraggingInfo.NSDragOperationNone;
    }

    public boolean tableView_acceptDrop_row_dropOperation(NSTableView view, NSDraggingInfo draggingInfo, int row, int operation) {
        return false;
    }

    public int draggingSourceOperationMaskForLocal(boolean flag) {
        return NSDraggingInfo.NSDragOperationMove | NSDraggingInfo.NSDragOperationCopy;
    }

    public void draggedImage_beganAt(NSImage image, NSPoint point) {
        log.trace("draggedImage_beganAt");
    }

    public void draggedImage_endedAt_operation(NSImage image, NSPoint point, int operation) {
        log.trace("draggedImage_endedAt_operation");
    }

    public void draggedImage_movedTo(NSImage image, NSPoint point) {
        log.trace("draggedImage_movedTo");
    }

    public boolean ignoreModifierKeysWhileDragging() {
        return false;
    }

    public NSArray namesOfPromisedFilesDroppedAtDestination(final NSURL dropDestination) {
        return NSArray.array();
    }
}
