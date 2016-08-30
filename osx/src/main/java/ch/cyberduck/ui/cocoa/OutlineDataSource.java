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

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSDraggingInfo;
import ch.cyberduck.binding.application.NSDraggingSource;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSOutlineView;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;

import org.apache.log4j.Logger;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class OutlineDataSource extends ProxyController implements NSOutlineView.DataSource, NSDraggingSource {
    private static Logger log = Logger.getLogger(OutlineDataSource.class);

    @Override
    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, NSObject value,
                                                                 final NSTableColumn tableColumn, NSObject item) {
        //
    }

    @Override
    public NSUInteger outlineView_validateDrop_proposedItem_proposedChildIndex(final NSOutlineView outlineView, final NSDraggingInfo info, NSObject destination, NSInteger row) {
        return NSDraggingInfo.NSDragOperationNone;
    }

    @Override
    public boolean outlineView_acceptDrop_item_childIndex(final NSOutlineView outlineView, final NSDraggingInfo info, NSObject item, NSInteger row) {
        return false;
    }

    @Override
    public boolean outlineView_writeItems_toPasteboard(final NSOutlineView outlineView, final NSArray items, final NSPasteboard pboard) {
        return false;
    }

    @Override
    public NSArray outlineView_namesOfPromisedFilesDroppedAtDestination_forDraggedItems(NSURL dropDestination, NSArray items) {
        return NSArray.array();
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
