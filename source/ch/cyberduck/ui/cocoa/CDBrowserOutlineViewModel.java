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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSArray;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * @version $Id$
 */
public class CDBrowserOutlineViewModel extends CDBrowserTableDataSource implements CDOutlineDataSource {
    protected static Logger log = Logger.getLogger(CDBrowserOutlineViewModel.class);

    public CDBrowserOutlineViewModel(CDBrowserController controller) {
        super(controller);
    }

    public int indexOf(NSView tableView, Path p) {
        //bug: the rowForItem method does not use p.equals() therefore only returns a valid value
        //if the exact reference is passed
        return ((NSOutlineView) tableView).rowForItem(p);
    }

    public boolean contains(NSView tableView, Path p) {
        return this.indexOf(tableView, p) != -1;
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public boolean outlineViewIsItemExpandable(final NSOutlineView view, final Path item) {
        if(log.isDebugEnabled()) {
            log.debug("outlineViewIsItemExpandable:" + item);
        }
        if(null == item) {
            return false;
        }
        return item.attributes.isDirectory();
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public int outlineViewNumberOfChildrenOfItem(final NSOutlineView view, Path item) {
        if(log.isDebugEnabled()) {
            log.debug("outlineViewNumberOfChildrenOfItem:" + item);
        }
        if(controller.isMounted()) {
            if(null == item) {
                return this.childs(controller.workdir()).size();
            }
            NSEvent event = NSApplication.sharedApplication().currentEvent();
            if(event != null) {
                log.debug("Event:" + event.type());
                if(NSEvent.LeftMouseDragged == event.type()) {
                    final int draggingColumn = view.columnAtPoint(view.convertPointFromView(event.locationInWindow(), null));
                    if(draggingColumn != 0) {
                        log.debug("Returning 0 to #outlineViewNumberOfChildrenOfItem for column:" + draggingColumn);
                        // See ticket #60
                        return 0;
                    }
                    if(!Preferences.instance().getBoolean("browser.view.autoexpand")) {
                        log.debug("Returning 0 to #outlineViewNumberOfChildrenOfItem:" + item.getName() + " while dragging because browser.view.autoexpand == false");
                        // See tickets #98 and #633
                        return 0;
                    }
                }
            }
            return this.childs(item).size();
        }
        return 0;
    }

    /**
     * @see NSOutlineView.DataSource
     *      Invoked by outlineView, and returns the child item at the specified index. Children
     *      of a given parent item are accessed sequentially. If item is null, this method should
     *      return the appropriate child item of the root object
     */
    public Path outlineViewChildOfItem(final NSOutlineView outlineView, int index, Path item) {
        if(null == item) {
            item = controller.workdir();
        }
        List childs = this.childs(item);
        if(childs.isEmpty()) {
            return null;
        }
        if(childs.size() < index) {
            return null;
        }
        return (Path) childs.get(index);
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public void outlineViewSetObjectValueForItem(final NSOutlineView outlineView, Object value,
                                                 final NSTableColumn tableColumn, Path item) {
        super.setObjectValueForItem(item, value, (String) tableColumn.identifier());
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public Object outlineViewObjectValueForItem(final NSOutlineView outlineView, final NSTableColumn tableColumn, Path item) {
        return super.objectValueForItem(item, (String) tableColumn.identifier());
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public int outlineViewValidateDrop(final NSOutlineView outlineView, final NSDraggingInfo info, Path destination, int row) {
        if(controller.isMounted()) {
            if(null != destination) {
                // Dragging over file or folder
                final int draggingColumn = outlineView.columnAtPoint(info.draggingLocation());
                if(0 == draggingColumn && destination.attributes.isDirectory()) {
                    // Drop target is directory
                    outlineView.setDropItemAndDropChildIndex(destination, NSOutlineView.DropOnItemIndex);
                    return super.validateDrop(outlineView, destination, row, info);
                }
                else {
                    outlineView.setDropItemAndDropChildIndex(null, NSOutlineView.DropOnItemIndex);
                    return super.validateDrop(outlineView, controller.workdir(), row, info);
                }
            }
            else {
                // Dragging over empty rows
                outlineView.setDropItemAndDropChildIndex(null, NSOutlineView.DropOnItemIndex);
                return super.validateDrop(outlineView, controller.workdir(), row, info);
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public boolean outlineViewAcceptDrop(final NSOutlineView outlineView, final NSDraggingInfo info, Path destination, int row) {
        if(controller.isMounted()) {
            if(null == destination) {
                destination = controller.workdir();
            }
            return super.acceptDrop(outlineView, destination, info);
        }
        return false;
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public boolean outlineViewWriteItemsToPasteboard(final NSOutlineView outlineView, final NSArray items, final NSPasteboard pboard) {
        return super.writeItemsToPasteBoard(outlineView, items, pboard);
    }
}