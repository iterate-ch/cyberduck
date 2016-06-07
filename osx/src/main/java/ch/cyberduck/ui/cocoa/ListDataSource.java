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

import ch.cyberduck.binding.application.NSDraggingInfo;
import ch.cyberduck.binding.application.NSDraggingSource;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSTableView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;

import org.apache.log4j.Logger;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class ListDataSource extends ProxyController implements NSTableView.DataSource, NSDraggingSource {
    private static Logger log = Logger.getLogger(ListDataSource.class);

    @Override
    public void tableView_setObjectValue_forTableColumn_row(NSTableView view, NSObject value, NSTableColumn tableColumn, NSInteger row) {
        //
    }

    @Override
    public boolean tableView_writeRowsWithIndexes_toPasteboard(NSTableView view, NSIndexSet rowIndexes, NSPasteboard pboard) {
        return false;
    }

    @Override
    public NSArray tableView_namesOfPromisedFilesDroppedAtDestination_forDraggedRowsWithIndexes(NSTableView view, final NSURL dropDestination, NSIndexSet rowIndexes) {
        return NSArray.array();
    }

    @Override
    public NSUInteger tableView_validateDrop_proposedRow_proposedDropOperation(NSTableView view, NSDraggingInfo draggingInfo, NSInteger row, NSUInteger operation) {
        return NSDraggingInfo.NSDragOperationNone;
    }

    @Override
    public boolean tableView_acceptDrop_row_dropOperation(NSTableView view, NSDraggingInfo draggingInfo, NSInteger row, NSUInteger operation) {
        return false;
    }

    @Override
    public NSUInteger draggingSourceOperationMaskForLocal(boolean flag) {
        return new NSUInteger(NSDraggingInfo.NSDragOperationMove.intValue() | NSDraggingInfo.NSDragOperationCopy.intValue());
    }

    @Override
    public void draggedImage_beganAt(NSImage image, NSPoint point) {
        log.trace("draggedImage_beganAt");
    }

    @Override
    public void draggedImage_endedAt_operation(NSImage image, NSPoint point, NSUInteger operation) {
        log.trace("draggedImage_endedAt_operation");
    }

    @Override
    public void draggedImage_movedTo(NSImage image, NSPoint point) {
        log.trace("draggedImage_movedTo");
    }

    @Override
    public boolean ignoreModifierKeysWhileDragging() {
        return false;
    }

    @Override
    public NSArray namesOfPromisedFilesDroppedAtDestination(final NSURL dropDestination) {
        return NSArray.array();
    }
}
