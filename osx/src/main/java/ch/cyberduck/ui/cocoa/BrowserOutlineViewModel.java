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

import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSDraggingInfo;
import ch.cyberduck.binding.application.NSEvent;
import ch.cyberduck.binding.application.NSOutlineView;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSTableView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.ArrayList;
import java.util.List;

public class BrowserOutlineViewModel extends BrowserTableDataSource implements NSOutlineView.DataSource {
    private static final Logger log = Logger.getLogger(BrowserOutlineViewModel.class);

    public BrowserOutlineViewModel(final BrowserController controller, final Cache<Path> cache) {
        super(controller, cache);
    }

    @Override
    public void render(final NSTableView view, final List<Path> folders) {
        super.render(view, folders);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Reload table view %s for changes files %s", view, folders));
        }
        if(controller.isMounted()) {
            final NSOutlineView outline = (NSOutlineView) view;
            if(folders.isEmpty()) {
                view.reloadData();
            }
            else {
                for(Path folder : folders) {
                    if(folder.equals(controller.workdir())) {
                        outline.reloadData();
                        break;
                    }
                    else {
                        outline.reloadItem_reloadChildren(NSObjectPathReference.get(folder), true);
                    }
                }
            }
        }
        else {
            view.reloadData();
        }
    }

    @Override
    public int indexOf(final NSTableView view, final Path file) {
        return ((NSOutlineView) view).rowForItem(NSObjectPathReference.get(file)).intValue();
    }

    /**
     * @see NSOutlineView.DataSource
     */
    @Override
    public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
        if(log.isTraceEnabled()) {
            log.trace("outlineViewIsItemExpandable:" + item);
        }
        if(null == item) {
            return false;
        }
        final Path lookup = cache.lookup(new NSObjectPathReference(item));
        if(null == lookup) {
            return false;
        }
        return lookup.isDirectory();
    }

    /**
     * @see NSOutlineView.DataSource
     */
    @Override
    public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, final NSObject item) {
        if(log.isTraceEnabled()) {
            log.trace("outlineView_numberOfChildrenOfItem:" + item);
        }
        if(controller.isMounted()) {
            if(null == item) {
                return new NSInteger(this.get(controller.workdir()).size());
            }
            NSEvent event = NSApplication.sharedApplication().currentEvent();
            if(event != null) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Current application event is %d", event.type()));
                }
                if(NSEvent.NSLeftMouseDragged == event.type()) {
                    final int draggingColumn = view.columnAtPoint(view.convertPoint_fromView(event.locationInWindow(), null)).intValue();
                    if(draggingColumn != 0) {
                        log.debug("Returning 0 to #outlineViewNumberOfChildrenOfItem for column:" + draggingColumn);
                        // See ticket #60
                        return new NSInteger(0);
                    }
                    if(!PreferencesFactory.get().getBoolean("browser.view.autoexpand")) {
                        log.debug("Returning 0 to #outlineViewNumberOfChildrenOfItem while dragging because browser.view.autoexpand == false");
                        // See tickets #98 and #633
                        return new NSInteger(0);
                    }
                }
            }
            final Path lookup = cache.lookup(new NSObjectPathReference(item));
            if(null == lookup) {
                return new NSInteger(0);
            }
            return new NSInteger(this.get(lookup).size());
        }
        return new NSInteger(0);
    }

    /**
     * @see NSOutlineView.DataSource
     * Invoked by outlineView, and returns the child item at the specified index. Children
     * of a given parent item are accessed sequentially. If item is null, this method should
     * return the appropriate child item of the root object
     */
    @Override
    public NSObject outlineView_child_ofItem(final NSOutlineView view, final NSInteger index, final NSObject item) {
        if(log.isTraceEnabled()) {
            log.trace("outlineView_child_ofItem:" + item);
        }
        final Path path;
        if(null == item) {
            path = controller.workdir();
        }
        else {
            path = cache.lookup(new NSObjectPathReference(item));
            if(null == path) {
                return null;
            }
        }
        final AttributedList<Path> children = this.get(path);
        if(index.intValue() >= children.size()) {
            log.warn(String.format("Index %s out of bound for %s", index, item));
            return null;
        }
        return NSObjectPathReference.get(children.get(index.intValue()));
    }

    @Override
    public void outlineView_setObjectValue_forTableColumn_byItem(final NSOutlineView view, final NSObject value,
                                                                 final NSTableColumn tableColumn, final NSObject item) {
        super.setObjectValueForItem(cache.lookup(new NSObjectPathReference(item)), value, tableColumn.identifier());
    }

    @Override
    public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView view, final NSTableColumn tableColumn, NSObject item) {
        if(null == item) {
            return null;
        }
        return super.objectValueForItem(cache.lookup(new NSObjectPathReference(item)), tableColumn.identifier());
    }

    @Override
    public NSUInteger outlineView_validateDrop_proposedItem_proposedChildIndex(final NSOutlineView view,
                                                                               final NSDraggingInfo draggingInfo,
                                                                               final NSObject item, final NSInteger row) {
        if(controller.isMounted()) {
            Path destination = null;
            if(null != item) {
                destination = cache.lookup(new NSObjectPathReference(item));
            }
            if(null == destination) {
                // Dragging over empty rows
                view.setDropItem(null, NSOutlineView.NSOutlineViewDropOnItemIndex);
                return super.validateDrop(view, controller.workdir(), row, draggingInfo);
            }
            else {
                // Dragging over file or folder
                final int draggingColumn = view.columnAtPoint(draggingInfo.draggingLocation()).intValue();
                if(-1 == draggingColumn || 0 == draggingColumn) {
                    if(destination.isDirectory()) {
                        // Drop target is directory
                        view.setDropItem(item, NSOutlineView.NSOutlineViewDropOnItemIndex);
                        return super.validateDrop(view, destination, row, draggingInfo);
                    }
                }
                for(Path next : controller.getPasteboard()) {
                    if(destination.equals(next)) {
                        // Do not allow dragging onto myself. Fix #4320
                        return NSDraggingInfo.NSDragOperationNone;
                    }
                }
                view.setDropItem(null, NSOutlineView.NSOutlineViewDropOnItemIndex);
                return super.validateDrop(view, controller.workdir(), row, draggingInfo);
            }
        }
        // Passing to super to look for URLs to mount
        if(draggingInfo.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            view.setDropItem(null, NSOutlineView.NSOutlineViewDropOnItemIndex);
        }
        return super.validateDrop(view, null, row, draggingInfo);
    }

    @Override
    public boolean outlineView_acceptDrop_item_childIndex(final NSOutlineView view, final NSDraggingInfo info,
                                                          final NSObject item, final NSInteger row) {
        Path destination = null;
        if(controller.isMounted()) {
            if(null == item) {
                destination = controller.workdir();
            }
            else {
                destination = cache.lookup(new NSObjectPathReference(item));
                if(null == destination) {
                    return false;
                }
            }
        }
        return super.acceptDrop(view, destination, info);
    }

    @Override
    public NSArray outlineView_namesOfPromisedFilesDroppedAtDestination_forDraggedItems(final NSURL dropDestination, final NSArray items) {
        return this.namesOfPromisedFilesDroppedAtDestination(dropDestination);
    }

    @Override
    public boolean outlineView_writeItems_toPasteboard(final NSOutlineView outlineView, final NSArray items,
                                                       final NSPasteboard pboard) {
        final List<Path> selected = new ArrayList<Path>();
        for(int i = 0; i < items.count().intValue(); i++) {
            selected.add(cache.lookup(new NSObjectPathReference(items.objectAtIndex(new NSUInteger(i)))));
        }
        return super.writeItemsToPasteBoard(outlineView, selected, pboard);
    }
}