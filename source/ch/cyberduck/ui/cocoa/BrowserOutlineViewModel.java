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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSURL;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;

import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class BrowserOutlineViewModel extends BrowserTableDataSource implements NSOutlineView.DataSource {
    protected static Logger log = Logger.getLogger(BrowserOutlineViewModel.class);

    public BrowserOutlineViewModel(BrowserController controller) {
        super(controller);
    }

    @Override
    public int indexOf(NSView tableView, Path p) {
        return ((NSOutlineView) tableView).rowForItem(p.<NSObject>getReference().unique()).intValue();
    }

    @Override
    public boolean contains(NSView tableView, Path p) {
        return this.indexOf(tableView, p) != -1;
    }

    protected AttributedList<Path> children(final OutlinePathReference path) {
        final Path lookup = controller.lookup(path);
        if(null == lookup) {
            return AttributedList.emptyList();
        }
        return super.children(lookup);
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
        if(log.isDebugEnabled()) {
            log.debug("outlineViewIsItemExpandable:" + item);
        }
        if(null == item) {
            return false;
        }
        final Path path = controller.lookup(new OutlinePathReference(item));
        if(null == path) {
            return false;
        }
        return path.attributes().isDirectory();
    }

    /**
     * @see NSOutlineView.DataSource
     */
    public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, NSObject item) {
        if(log.isDebugEnabled()) {
            log.debug("outlineView_numberOfChildrenOfItem:" + item);
        }
        if(controller.isMounted()) {
            if(null == item) {
                return new NSInteger(this.children(controller.workdir()).size());
            }
            NSEvent event = NSApplication.sharedApplication().currentEvent();
            if(event != null) {
                log.debug("Event:" + event.type());
                if(NSEvent.NSLeftMouseDragged == event.type()) {
                    final int draggingColumn = view.columnAtPoint(view.convertPoint_fromView(event.locationInWindow(), null)).intValue();
                    if(draggingColumn != 0) {
                        log.debug("Returning 0 to #outlineViewNumberOfChildrenOfItem for column:" + draggingColumn);
                        // See ticket #60
                        return new NSInteger(0);
                    }
                    if(!Preferences.instance().getBoolean("browser.view.autoexpand")) {
                        log.debug("Returning 0 to #outlineViewNumberOfChildrenOfItem while dragging because browser.view.autoexpand == false");
                        // See tickets #98 and #633
                        return new NSInteger(0);
                    }
                }
            }
            return new NSInteger(this.children(new OutlinePathReference(item)).size());
        }
        return new NSInteger(0);
    }

    /**
     * @see NSOutlineView.DataSource
     *      Invoked by outlineView, and returns the child item at the specified index. Children
     *      of a given parent item are accessed sequentially. If item is null, this method should
     *      return the appropriate child item of the root object
     */
    public NSObject outlineView_child_ofItem(final NSOutlineView outlineView, NSInteger index, NSObject item) {
        if(log.isDebugEnabled()) {
            log.debug("outlineView_child_ofItem:" + item);
        }
        final Path path;
        if(null == item) {
            path = controller.workdir();
        }
        else {
            path = controller.lookup(new OutlinePathReference(item));
        }
        if(null == path) {
            return null;
        }
        final AttributedList<Path> children = this.children(path);
        if(index.intValue() >= children.size()) {
            log.warn("Index " + index + " out of bound for " + item);
            return null;
        }
        return children.get(index.intValue()).<NSObject>getReference().unique();
    }

    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView outlineView, NSObject value,
                                                                 final NSTableColumn tableColumn, NSObject item) {
        super.setObjectValueForItem(controller.lookup(new OutlinePathReference(item)), value, tableColumn.identifier());
    }

    public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView view, final NSTableColumn tableColumn, NSObject item) {
        if(null == item) {
            return null;
        }
        return super.objectValueForItem(controller.lookup(new OutlinePathReference(item)), tableColumn.identifier());
    }

    public NSUInteger outlineView_validateDrop_proposedItem_proposedChildIndex(final NSOutlineView outlineView, final NSDraggingInfo draggingInfo, NSObject item, NSInteger row) {
        if(controller.isMounted()) {
            Path destination = null;
            if(null != item) {
                destination = controller.lookup(new OutlinePathReference(item));
            }
            if(!PathPasteboard.getPasteboard(controller.getSession().getHost()).isEmpty()
                    || draggingInfo.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                if(null == destination) {
                    // Dragging over empty rows
                    outlineView.setDropItem(null, NSOutlineView.NSOutlineViewDropOnItemIndex);
                    return super.validateDrop(outlineView, controller.workdir(), row, draggingInfo);
                }
                else {
                    // Dragging over file or folder
                    final int draggingColumn = outlineView.columnAtPoint(draggingInfo.draggingLocation()).intValue();
                    if(0 == draggingColumn && destination.attributes().isDirectory()) {
                        // Drop target is directory
                        outlineView.setDropItem(destination.<NSObject>getReference().unique(), NSOutlineView.NSOutlineViewDropOnItemIndex);
                        return super.validateDrop(outlineView, destination, row, draggingInfo);
                    }
                    else {
                        for(Path next : PathPasteboard.getPasteboard(controller.getSession().getHost()).getFiles(controller.getSession())) {
                            if(destination.equals(next)) {
                                // Do not allow dragging onto myself. Fix #4320
                                return NSDraggingInfo.NSDragOperationNone;
                            }
                        }
                        outlineView.setDropItem(null, NSOutlineView.NSOutlineViewDropOnItemIndex);
                        return super.validateDrop(outlineView, controller.workdir(), row, draggingInfo);
                    }
                }
            }
        }
        // Passing to super to look for URLs to mount
        if(draggingInfo.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            outlineView.setDropItem(null, NSOutlineView.NSOutlineViewDropOnItemIndex);
        }
        return super.validateDrop(outlineView, null, row, draggingInfo);
    }

    public boolean outlineView_acceptDrop_item_childIndex(final NSOutlineView outlineView, final NSDraggingInfo info, NSObject item, NSInteger row) {
        Path destination = null;
        if(controller.isMounted()) {
            if(null == item) {
                destination = controller.workdir();
            }
            else {
                destination = controller.lookup(new OutlinePathReference(item));
            }
        }
        return super.acceptDrop(outlineView, destination, info);
    }

    public NSArray outlineView_namesOfPromisedFilesDroppedAtDestination_forDraggedItems(NSURL dropDestination, NSArray items) {
        return this.namesOfPromisedFilesDroppedAtDestination(dropDestination);
    }

    public boolean outlineView_writeItems_toPasteboard(final NSOutlineView outlineView, final NSArray items, final NSPasteboard pboard) {
        return super.writeItemsToPasteBoard(outlineView, items, pboard);
    }
}