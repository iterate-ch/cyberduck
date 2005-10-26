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
import com.apple.cocoa.application.NSOutlineView;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableColumn;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSArray;

import java.util.List;

/**
 * @version $Id$
 */
public class CDBrowserOutlineViewModel extends CDBrowserTableDataSource {

    public CDBrowserOutlineViewModel(CDBrowserController controller) {
        super(controller);
    }

    public int indexOf(NSView tableView, Path p) {
        //bug: the rowForItem method does not use p.equals() therefore only returns a valid value
        //if the exact reference is passed
        return ((NSOutlineView)tableView).rowForItem(p);
    }

    public boolean outlineViewIsItemExpandable(NSOutlineView outlineView, Path item) {
        if (null == item) {
            item = controller.workdir();
        }
        return item.attributes.isDirectory();
    }

    public int outlineViewNumberOfChildrenOfItem(NSOutlineView outlineView, Path item) {
        if (controller.isMounted()) {
            if (null == item) {
                item = controller.workdir();
            }
            List childs = this.childs(item);
            if (childs != null) {
                return childs.size();
            }
        }
        return 0;
    }

    /**
     * Invoked by outlineView, and returns the child item at the specified index. Children
     * of a given parent item are accessed sequentially. If item is null, this method should
     * return the appropriate child item of the root object
     */
    public Path outlineViewChildOfItem(NSOutlineView outlineView, int index, Path item) {
        if (null == item) {
            item = controller.workdir();
        }
        if (index < this.childs(item).size()) {
            return (Path) this.childs(item).get(index);
        }
        return null;
    }

    public void outlineViewSetObjectValueForItem(NSOutlineView outlineView, Object value,
                                                 NSTableColumn tableColumn, Path item) {
        super.setObjectValueForItem(item, value, (String) tableColumn.identifier());
    }

    public Object outlineViewObjectValueForItem(NSOutlineView outlineView, NSTableColumn tableColumn, Path item) {
        return super.objectValueForItem(item, (String) tableColumn.identifier());
    }

    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    /**
     *
     * @param outlineView
     * @param info
     * @param destination  The proposed parent
     * @param row  The proposed child location.
     */
    public int outlineViewValidateDrop(NSOutlineView outlineView, NSDraggingInfo info, Path destination, int row) {
		outlineView.setDropItemAndDropChildIndex(destination, NSOutlineView.DropOnItemIndex);
        return super.validateDrop(outlineView, destination, row, info);
    }

    public boolean outlineViewAcceptDrop(NSOutlineView outlineView, NSDraggingInfo info, Path destination, int row) {
        if (controller.isMounted()) {
            if (null == destination) {
                destination = controller.workdir();
            }
            return super.acceptDrop(outlineView, destination, info);
        }
        return false;
    }

    // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------

    public boolean outlineViewWriteItemsToPasteboard(NSOutlineView outlineView, NSArray items, NSPasteboard pboard) {
        return super.writeItemsToPasteBoard(outlineView, items, pboard);
    }
}