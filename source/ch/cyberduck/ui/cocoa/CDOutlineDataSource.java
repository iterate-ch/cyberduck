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
import ch.cyberduck.ui.cocoa.application.NSOutlineView;
import ch.cyberduck.ui.cocoa.application.NSPasteboard;
import ch.cyberduck.ui.cocoa.application.NSTableColumn;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSURL;
import ch.cyberduck.ui.cocoa.foundation.NSString;

/**
 * @version $Id$
 */
public abstract class CDOutlineDataSource extends CDController implements NSOutlineView.DataSource {

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

}
