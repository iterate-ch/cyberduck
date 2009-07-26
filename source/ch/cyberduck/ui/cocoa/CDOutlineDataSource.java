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
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSURL;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.rococoa.cocoa.foundation.NSPoint;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDOutlineDataSource extends CDController implements NSOutlineView.DataSource, NSDraggingSource {
    private static Logger log = Logger.getLogger(CDOutlineDataSource.class);

    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, NSObject value,
                                                                 final NSTableColumn tableColumn, NSObject item) {
        throw new RuntimeException("Not editable");
    }

    public int outlineView_validateDrop_proposedItem_proposedChildIndex(final NSOutlineView outlineView, final NSDraggingInfo info, NSObject destination, int row) {
        return NSDraggingInfo.NSDragOperationNone;
    }

    public boolean outlineView_acceptDrop_item_childIndex(final NSOutlineView outlineView, final NSDraggingInfo info, NSObject item, int row) {
        return false;
    }

    public boolean outlineView_writeItems_toPasteboard(final NSOutlineView outlineView, final NSArray items, final NSPasteboard pboard) {
        return false;
    }

    public NSArray outlineView_namesOfPromisedFilesDroppedAtDestination_forDraggedItems(NSURL dropDestination, NSArray items) {
        return NSArray.array();
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
