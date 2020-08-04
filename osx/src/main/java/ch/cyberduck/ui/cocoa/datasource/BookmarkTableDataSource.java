package ch.cyberduck.ui.cocoa.datasource;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.OutlineDataSource;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSDraggingInfo;
import ch.cyberduck.binding.application.NSDraggingSource;
import ch.cyberduck.binding.application.NSEvent;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSOutlineView;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSTableView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.binding.foundation.NSMutableDictionary;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.pasteboard.HostPasteboard;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.serializer.impl.jna.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.jna.PlistSerializer;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.threading.WindowMainAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.ui.browser.BookmarkColumn;
import ch.cyberduck.ui.cocoa.controller.BrowserController;
import ch.cyberduck.ui.cocoa.controller.TransferControllerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BookmarkTableDataSource extends OutlineDataSource {
    private static final Logger log = LogManager.getLogger(BookmarkTableDataSource.class);

    private final Preferences preferences = PreferencesFactory.get();
    private final HostPasteboard pasteboard = HostPasteboard.getPasteboard();

    private final BrowserController controller;
    private final CollectionListener<Host> listener = new BookmarkReloadListener();
    private final ScheduledThreadPool timerPool = new ScheduledThreadPool();

    private AbstractHostCollection source = AbstractHostCollection.empty();
    private Map<String, List<Host>> groups = Collections.emptyMap();

    public BookmarkTableDataSource(final BrowserController controller) {
        this.controller = controller;
    }

    public void setSource(final AbstractHostCollection source) {
        this.source.removeListener(listener);
        this.source = source;
        this.source.addListener(listener);
        this.groups = source.groups(HostFilter.NONE);
    }

    @Override
    public void invalidate() {
        timerPool.shutdown();
        source.removeListener(listener);
        super.invalidate();
    }

    /**
     * Display only a subset of all bookmarks
     *
     * @param filter Filter for bookmarks
     */
    public void setFilter(final HostFilter filter) {
        this.groups = source.groups(filter);
    }

    /**
     * @return The filtered collection currently to be displayed within the constraints given by the comparison with the
     * bookmark filter
     * @see HostFilter
     */
    public AbstractHostCollection getSource() {
        return source;
    }

    @Override
    public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
        return item.isKindOfClass(NSString.CLASS);
    }

    @Override
    public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, final NSObject item) {
        if(null == item) {
            return new NSInteger(groups.size());
        }
        return new NSInteger(groups.get(item.toString()).size());
    }

    @Override
    public NSObject outlineView_child_ofItem(final NSOutlineView outlineView, final NSInteger index, final NSObject item) {
        if(null == item) {
            final String label = new ArrayList<>(groups.keySet()).get(index.intValue());
            return NSString.stringWithString(label);
        }
        return groups.get(item.toString()).get(index.intValue()).serialize(new PlistSerializer());
    }

    @Override
    public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView view, final NSTableColumn column, final NSObject item) {
        if(null == item) {
            return null;
        }
        if(null == column) {
            // Group row)
            if(StringUtils.isBlank(item.toString())) {
                // Default group of bookmarks with no label assigned
                return NSString.stringWithString(LocaleFactory.localizedString("Default"));
            }
            return item;
        }
        final NSMutableDictionary dict = Rococoa.cast(item, NSMutableDictionary.class);
        final Host host = new HostDictionary(new DeserializerFactory(PlistDeserializer.class)).deserialize(dict);
        final String identifier = column.identifier();
        if(identifier.equals(BookmarkColumn.icon.name())) {
            return IconCacheFactory.<NSImage>get().iconNamed(host.getProtocol().disk(), preferences.getInteger("bookmark.icon.size"));
        }
        if(identifier.equals(BookmarkColumn.bookmark.name())) {
            if(null == dict.objectForKey("Nickname")) {
                dict.setObjectForKey(BookmarkNameProvider.toString(host), "Nickname");
            }
            return item;
        }
        if(identifier.equals(BookmarkColumn.status.name())) {
            final SessionPool session = controller.getSession();
            if(host.equals(session.getHost())) {
                switch(session.getState()) {
                    case open:
                        return IconCacheFactory.<NSImage>get().iconNamed("NSStatusAvailable", 16);
                    case opening:
                    case closing:
                        return IconCacheFactory.<NSImage>get().iconNamed("NSStatusPartiallyAvailable", 16);
                }
            }
            return null;
        }
        throw new IllegalArgumentException(String.format("Unknown identifier %s", identifier));
    }

    /**
     * Sets whether the use of modifier keys should have an effect on the type of operation performed.
     *
     * @return Always false
     * @see NSDraggingSource
     */
    @Override
    public boolean ignoreModifierKeysWhileDragging() {
        // If this method is not implemented or returns false, the user can tailor the drag operation by
        // holding down a modifier key during the drag.
        return false;
    }

    @Override
    public NSUInteger outlineView_validateDrop_proposedItem_proposedChildIndex(final NSOutlineView view, final NSDraggingInfo info, final NSObject destination, final NSInteger row) {
        NSPasteboard draggingPasteboard = info.draggingPasteboard();
        if(!source.allowsEdit()) {
            // Do not allow drags for non writable collections
            return NSDraggingInfo.NSDragOperationNone;
        }
        else if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.StringPboardType)) != null) {
            String o = draggingPasteboard.stringForType(NSPasteboard.StringPboardType);
            if(o != null) {
                if(Scheme.isURL(o)) {
                    view.setDropRow(row, NSTableView.NSTableViewDropAbove);
                    return NSDraggingInfo.NSDragOperationCopy;
                }
            }
            return NSDraggingInfo.NSDragOperationNone;
        }
        else if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            final NSObject o = draggingPasteboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                if(o.isKindOfClass(NSArray.CLASS)) {
                    final NSArray elements = Rococoa.cast(o, NSArray.class);
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        String file = elements.objectAtIndex(new NSUInteger(i)).toString();
                        if(file.endsWith(".duck")) {
                            // Allow drag if at least one file is a serialized bookmark
                            view.setDropRow(row, NSTableView.NSTableViewDropAbove);
                            return NSDraggingInfo.NSDragOperationCopy;
                        }
                    }
                    //only allow other files if there is at least one bookmark
                    view.setDropRow(row, NSTableView.NSTableViewDropOn);
                    return NSDraggingInfo.NSDragOperationCopy;
                }
            }
            return NSDraggingInfo.NSDragOperationNone;
        }
        else if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            final NSObject o = draggingPasteboard.propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                if(o.isKindOfClass(NSArray.CLASS)) {
                    final NSArray elements = Rococoa.cast(o, NSArray.class);
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        if(Scheme.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                            view.setDropRow(row, NSTableView.NSTableViewDropAbove);
                            return NSDraggingInfo.NSDragOperationCopy;
                        }
                    }
                }
            }
            return NSDraggingInfo.NSDragOperationNone;
        }
        else if(!pasteboard.isEmpty()) {
            view.setDropRow(row, NSTableView.NSTableViewDropAbove);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Drag operation mask is %d", info.draggingSourceOperationMask().intValue()));
            }
            // We accept any file promise within the bounds
            if(info.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue()) {
                return NSDraggingInfo.NSDragOperationCopy;
            }
            return NSDraggingInfo.NSDragOperationMove;
        }
        return NSDraggingInfo.NSDragOperationNone;
    }

    @Override
    public boolean outlineView_acceptDrop_item_childIndex(final NSOutlineView view, final NSDraggingInfo info, final NSObject item, final NSInteger row) {
        final NSPasteboard draggingPasteboard = info.draggingPasteboard();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Accept drop at row %s", row));
        }
        view.deselectAll(null);
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.StringPboardType)) != null) {
            String o = draggingPasteboard.stringForType(NSPasteboard.StringPboardType);
            if(null == o) {
                return false;
            }
            final Host h;
            try {
                h = HostParser.parse(o);
            }
            catch(HostParserException e) {
                return false;
            }
            source.add(row.intValue(), h);
            view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), false);
            view.scrollRowToVisible(row);
            return true;
        }
        else if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            // We get a drag from another application e.g. Finder.app proposing some files
            final NSObject object = draggingPasteboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(object != null) {
                if(object.isKindOfClass(NSArray.CLASS)) {
                    final NSArray elements = Rococoa.cast(object, NSArray.class);
                    // If regular files are dropped, these will be uploaded to the dropped bookmark location
                    final List<TransferItem> uploads = new ArrayList<>();
                    Host host = null;
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        final String filename = elements.objectAtIndex(new NSUInteger(i)).toString();
                        final Local f = LocalFactory.get(filename);
                        if(filename.endsWith(".duck")) {
                            // Adding a previously exported bookmark file from the Finder
                            try {
                                source.add(row.intValue(), HostReaderFactory.get().read(f));
                                view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), true);
                                view.scrollRowToVisible(row);
                            }
                            catch(AccessDeniedException e) {
                                log.error(String.format("Failure reading bookmark from %s. %s", f, e.getMessage()));
                                continue;
                            }
                        }
                        else {
                            // The bookmark this file has been dropped onto
                            final Host h = source.get(row.intValue());
                            if(null == host) {
                                host = h;
                            }
                            // Upload to the remote host this bookmark points to
                            uploads.add(new TransferItem(
                                new Path(new Path(PathNormalizer.normalize(h.getDefaultPath()), EnumSet.of(Path.Type.directory)),
                                    f.getName(), EnumSet.of(Path.Type.file)), f)
                            );
                        }
                    }
                    if(!uploads.isEmpty()) {
                        // If anything has been added to the queue, then process the queue
                        final Transfer t = new UploadTransfer(host, uploads);
                        TransferControllerFactory.get().start(t, new TransferOptions());
                    }
                    return true;
                }
            }
        }
        else if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            final NSObject object = draggingPasteboard.propertyListForType(NSPasteboard.URLPboardType);
            if(object != null) {
                if(object.isKindOfClass(NSArray.CLASS)) {
                    final NSArray elements = Rococoa.cast(object, NSArray.class);
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        final String url = elements.objectAtIndex(new NSUInteger(i)).toString();
                        if(StringUtils.isNotBlank(url)) {
                            final Host h;
                            try {
                                h = HostParser.parse(url);
                            }
                            catch(HostParserException e) {
                                log.warn(e);
                                continue;
                            }
                            source.add(row.intValue(), h);
                            view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), true);
                            view.scrollRowToVisible(row);
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        else if(!pasteboard.isEmpty()) {
            if(info.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue()) {
                List<Host> duplicates = new ArrayList<>();
                for(Host bookmark : pasteboard) {
                    final Host duplicate = new HostDictionary<>().deserialize(bookmark.serialize(SerializerFactory.get()));
                    // Make sure a new UUID is assigned for duplicate
                    duplicate.setUuid(null);
                    source.add(row.intValue(), duplicate);
                    duplicates.add(duplicate);
                }
                for(Host bookmark : duplicates) {
                    int index = source.indexOf(bookmark);
                    view.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(index)), true);
                    view.scrollRowToVisible(new NSInteger(index));
                }
            }
            else {
                int insert = row.intValue();
                for(Host bookmark : pasteboard) {
                    int previous = source.indexOf(bookmark);
                    if(previous == insert) {
                        // No need to move
                        continue;
                    }
                    source.remove(previous);
                    int moved;
                    if(previous < insert) {
                        moved = insert - 1;
                    }
                    else {
                        moved = insert;
                    }
                    source.add(moved, bookmark);
                }
                for(Host bookmark : pasteboard) {
                    int index = source.indexOf(bookmark);
                    view.selectRowIndexes(NSIndexSet.indexSetWithIndex(new NSInteger(index)), true);
                    view.scrollRowToVisible(new NSInteger(index));
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @see NSDraggingSource
     * @see "http://www.cocoabuilder.com/archive/message/2005/10/5/118857"
     */
    @Override
    public void draggedImage_endedAt_operation(final NSImage image, final NSPoint point, final NSUInteger operation) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Drop finished with operation %s", operation));
        }
        if(NSDraggingInfo.NSDragOperationDelete.intValue() == operation.intValue()) {
            controller.deleteBookmarkButtonClicked(null);
        }
        pasteboard.clear();
    }

    /**
     * @param local indicates that the candidate destination object (the window or view over which the dragged image is
     *              currently poised) is in the same application as the source, while a NO value indicates that the
     *              destination object is in a different application
     * @return A mask, created by combining the dragging operations listed in the NSDragOperation section of
     * NSDraggingInfo protocol reference using the C bitwise OR operator.If the source does not permit any dragging
     * operations, it should return NSDragOperationNone.
     * @see NSDraggingSource
     */
    @Override
    public NSUInteger draggingSourceOperationMaskForLocal(final boolean local) {
        if(local) {
            return new NSUInteger(NSDraggingInfo.NSDragOperationMove.intValue() | NSDraggingInfo.NSDragOperationCopy.intValue());
        }
        return new NSUInteger(NSDraggingInfo.NSDragOperationCopy.intValue() | NSDraggingInfo.NSDragOperationDelete.intValue());
    }

    @Override
    public boolean outlineView_writeItems_toPasteboard(final NSOutlineView view, final NSArray items, final NSPasteboard pboard) {
        for(int i = 0; i < items.count().intValue(); i++) {
            if(items.objectAtIndex(new NSUInteger(i)).isKindOfClass(NSDictionary.CLASS)) {
                final NSDictionary dict = Rococoa.cast(items.objectAtIndex(new NSUInteger(i)), NSDictionary.class);
                pasteboard.add(new HostDictionary(new DeserializerFactory(PlistDeserializer.class)).deserialize(dict));
            }
        }
        NSEvent event = NSApplication.sharedApplication().currentEvent();
        if(event != null) {
            NSPoint dragPosition = view.convertPoint_fromView(event.locationInWindow(), null);
            NSRect imageRect = new NSRect(new NSPoint(dragPosition.x.doubleValue() - 16, dragPosition.y.doubleValue() - 16), new NSSize(32, 32));
            // Writing a promised file of the host as a bookmark file to the clipboard
            if(!view.dragPromisedFilesOfTypes(NSArray.arrayWithObject("duck"), imageRect, this.id(), true, event)) {
                log.warn(String.format("Failure for drag promise operation of %s", event));
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination. This
     * method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     * Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long
     * operations, you can cache dropDestination and defer the creation of the files until the finishedDraggingImage
     * method to avoid blocking the destination application.
     * @see NSTableView.DataSource
     */
    @Override
    public NSArray namesOfPromisedFilesDroppedAtDestination(final NSURL dropDestination) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Query promised files dropped dat destination %s", dropDestination.path()));
        }
        final NSMutableArray promisedDragNames = NSMutableArray.array();
        if(null != dropDestination) {
            for(Host bookmark : pasteboard) {
                final Local file = LocalFactory.get(dropDestination.path(),
                    String.format("%s.duck", StringUtils.replace(BookmarkNameProvider.toString(bookmark), "/", ":")));
                try {
                    HostWriterFactory.get().write(bookmark, file);
                }
                catch(AccessDeniedException e) {
                    log.warn(e.getMessage());
                }
                // Adding the filename that is promised to be created at the dropDestination
                promisedDragNames.addObject(file.getName());
            }
            pasteboard.clear();
        }
        return promisedDragNames;
    }

    private final class BookmarkReloadListener implements CollectionListener<Host> {
        private ScheduledFuture<?> delayed = null;

        @Override
        public void collectionLoaded() {
            controller.invoke(new WindowMainAction(controller) {
                @Override
                public void run() {
                    controller.reloadBookmarks();
                }
            });
        }

        @Override
        public void collectionItemAdded(final Host item) {
            controller.invoke(new WindowMainAction(controller) {
                @Override
                public void run() {
                    controller.reloadBookmarks();
                }
            });
        }

        @Override
        public void collectionItemRemoved(final Host item) {
            controller.invoke(new WindowMainAction(controller) {
                @Override
                public void run() {
                    controller.reloadBookmarks();
                }
            });
        }

        @Override
        public void collectionItemChanged(final Host item) {
            if(null != delayed) {
                delayed.cancel(false);
            }
            // Delay to 1 second. When typing changes we don't have to save every iteration.
            delayed = timerPool.schedule(new Runnable() {
                public void run() {
                    controller.invoke(new WindowMainAction(controller) {
                        @Override
                        public void run() {
                            controller.reloadBookmarks();
                        }
                    });
                }
            }, 1L, TimeUnit.SECONDS);
        }
    }
}
