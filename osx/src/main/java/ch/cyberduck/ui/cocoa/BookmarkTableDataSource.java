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

import ch.cyberduck.binding.ListDataSource;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSDraggingInfo;
import ch.cyberduck.binding.application.NSDraggingSource;
import ch.cyberduck.binding.application.NSEvent;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSTableColumn;
import ch.cyberduck.binding.application.NSTableView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSIndexSet;
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.binding.foundation.NSMutableDictionary;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.pasteboard.HostPasteboard;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.threading.ScheduledThreadPool;
import ch.cyberduck.core.threading.WindowMainAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.UploadTransfer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BookmarkTableDataSource extends ListDataSource {
    private static final Logger log = Logger.getLogger(BookmarkTableDataSource.class);

    public enum Column {
        icon,
        bookmark,
        status,
    }

    protected final BrowserController controller;

    private HostFilter filter;

    private AbstractHostCollection source = AbstractHostCollection.empty();

    /**
     * Subset of the original source
     */
    private AbstractHostCollection filtered;

    private CollectionListener<Host> listener;

    private final ScheduledThreadPool timerPool = new ScheduledThreadPool();

    private final HostPasteboard pasteboard
            = HostPasteboard.getPasteboard();

    public BookmarkTableDataSource(final BrowserController controller) {
        this.controller = controller;
    }

    public void setSource(final AbstractHostCollection source) {
        this.source.removeListener(listener); //Remove previous listener
        this.source = source;
        this.source.addListener(listener = new CollectionListener<Host>() {
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
        });
        this.setFilter(null);
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
    public void setFilter(HostFilter filter) {
        this.filter = filter;
        this.filtered = null;
    }

    /**
     * @return The filtered collection currently to be displayed within the constraints
     * given by the comparison with the bookmark filter
     * @see HostFilter
     */
    protected AbstractHostCollection getSource() {
        if(null == filter) {
            return source;
        }
        if(null == filtered) {
            filtered = new AbstractHostCollection() {
                private static final long serialVersionUID = -2154002477046004380L;

                @Override
                public String getName() {
                    return source.getName();
                }

                @Override
                public boolean allowsAdd() {
                    return source.allowsAdd();
                }

                @Override
                public boolean allowsDelete() {
                    return source.allowsDelete();
                }

                @Override
                public boolean allowsEdit() {
                    return source.allowsEdit();
                }

                @Override
                public void save() {
                    source.save();
                }

                @Override
                public void load() throws AccessDeniedException {
                    source.load();
                }
            };
            for(final Host bookmark : source) {
                if(filter.accept(bookmark)) {
                    filtered.add(bookmark);
                }
            }
            filtered.addListener(new CollectionListener<Host>() {
                @Override
                public void collectionLoaded() {
                    source.collectionLoaded();
                }

                @Override
                public void collectionItemAdded(final Host item) {
                    source.add(item);
                }

                @Override
                public void collectionItemRemoved(final Host item) {
                    source.remove(item);
                }

                @Override
                public void collectionItemChanged(final Host item) {
                    source.collectionItemChanged(item);
                }
            });
        }
        return filtered;
    }

    @Override
    public NSInteger numberOfRowsInTableView(NSTableView view) {
        return new NSInteger(this.getSource().size());
    }

    @Override
    public NSObject tableView_objectValueForTableColumn_row(final NSTableView view, final NSTableColumn tableColumn,
                                                            final NSInteger row) {
        if(row.intValue() >= this.numberOfRowsInTableView(view).intValue()) {
            return null;
        }
        final String identifier = tableColumn.identifier();
        final Host host = this.getSource().get(row.intValue());
        if(identifier.equals(Column.icon.name())) {
            return IconCacheFactory.<NSImage>get().iconNamed(host.getProtocol().disk(),
                    PreferencesFactory.get().getInteger("bookmark.icon.size"));
        }
        if(identifier.equals(Column.bookmark.name())) {
            final NSMutableDictionary dict = NSMutableDictionary.dictionary();
            dict.setObjectForKey(BookmarkNameProvider.toString(host), "Nickname");
            dict.setObjectForKey(host.getHostname(), "Hostname");
            dict.setObjectForKey(new HostUrlProvider(true, true).get(host), "URL");
            final String comment = this.getSource().getComment(host);
            if(StringUtils.isNotBlank(comment)) {
                dict.setObjectForKey(comment, "Comment");
            }
            return dict;
        }
        if(identifier.equals(Column.status.name())) {
            final SessionPool session = controller.getSession();
            if(session != null) {
                if(host.equals(session.getHost())) {
                    switch(session.getState()) {
                        case open:
                            return IconCacheFactory.<NSImage>get().iconNamed("statusGreen.tiff", 16);
                        case opening:
                        case closing:
                            return IconCacheFactory.<NSImage>get().iconNamed("statusYellow.tiff", 16);
                    }
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
    public NSUInteger tableView_validateDrop_proposedRow_proposedDropOperation(final NSTableView view, final NSDraggingInfo info,
                                                                               final NSInteger row, final NSUInteger operation) {
        NSPasteboard draggingPasteboard = info.draggingPasteboard();
        if(!this.getSource().allowsEdit()) {
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
                if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
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
                if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
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

    /**
     * @param info contains details on this dragging operation.
     * @param row  The proposed location is row and action is operation.
     *             The data source should incorporate the data from the dragging pasteboard at this time.
     * @see NSTableView.DataSource
     * Invoked by view when the mouse button is released over a table view that previously decided to allow a drop.
     */
    @Override
    public boolean tableView_acceptDrop_row_dropOperation(final NSTableView view, final NSDraggingInfo info,
                                                          final NSInteger row, final NSUInteger operation) {
        NSPasteboard draggingPasteboard = info.draggingPasteboard();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Accept drop at row %s", row));
        }
        view.deselectAll(null);
        final AbstractHostCollection source = this.getSource();
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.StringPboardType)) != null) {
            String o = draggingPasteboard.stringForType(NSPasteboard.StringPboardType);
            if(null == o) {
                return false;
            }
            final Host h = HostParser.parse(o);
            source.add(row.intValue(), h);
            view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), false);
            view.scrollRowToVisible(row);
            return true;
        }
        else if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            // We get a drag from another application e.g. Finder.app proposing some files
            final NSObject object = draggingPasteboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(object != null) {
                if(object.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
                    final NSArray elements = Rococoa.cast(object, NSArray.class);
                    // If regular files are dropped, these will be uploaded to the dropped bookmark location
                    final List<TransferItem> uploads = new ArrayList<TransferItem>();
                    Host host = null;
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        final String filename = elements.objectAtIndex(new NSUInteger(i)).toString();
                        final Local local = LocalFactory.get(filename);
                        if(filename.endsWith(".duck")) {
                            // Adding a previously exported bookmark file from the Finder
                            final Host bookmark;
                            try {
                                bookmark = HostReaderFactory.get().read(local);
                                if(null == bookmark) {
                                    continue;
                                }
                                source.add(row.intValue(), bookmark);
                                view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), true);
                                view.scrollRowToVisible(row);
                            }
                            catch(AccessDeniedException e) {
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
                                    new Path(new Path(PathNormalizer.normalize(h.getDefaultPath(), true), EnumSet.of(Path.Type.directory)),
                                            local.getName(), EnumSet.of(Path.Type.file)),
                                    local
                            ));
                        }
                    }
                    if(!uploads.isEmpty()) {
                        // If anything has been added to the queue, then process the queue
                        final Transfer t = new UploadTransfer(host, uploads);
                        TransferControllerFactory.get().start(t);
                    }
                    return true;
                }
            }
        }
        else if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            final NSObject object = draggingPasteboard.propertyListForType(NSPasteboard.URLPboardType);
            if(object != null) {
                if(object.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
                    final NSArray elements = Rococoa.cast(object, NSArray.class);
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        final String url = elements.objectAtIndex(new NSUInteger(i)).toString();
                        if(StringUtils.isNotBlank(url)) {
                            final Host h = HostParser.parse(url);
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
                List<Host> duplicates = new ArrayList<Host>();
                for(Host bookmark : pasteboard) {
                    final Host duplicate = new HostDictionary().deserialize(bookmark.serialize(SerializerFactory.get()));
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
        NSPasteboard.pasteboardWithName(NSPasteboard.DragPboard).declareTypes_owner(null, null);
        pasteboard.clear();
    }

    /**
     * @param local indicates that the candidate destination object (the window or view over which the dragged
     *              image is currently poised) is in the same application as the source, while a NO value indicates that
     *              the destination object is in a different application
     * @return A mask, created by combining the dragging operations listed in the NSDragOperation section of
     * NSDraggingInfo protocol reference using the C bitwise OR operator.If the source does not permit
     * any dragging operations, it should return NSDragOperationNone.
     * @see NSDraggingSource
     */
    @Override
    public NSUInteger draggingSourceOperationMaskForLocal(final boolean local) {
        if(local) {
            return new NSUInteger(NSDraggingInfo.NSDragOperationMove.intValue() | NSDraggingInfo.NSDragOperationCopy.intValue());
        }
        return new NSUInteger(NSDraggingInfo.NSDragOperationCopy.intValue() | NSDraggingInfo.NSDragOperationDelete.intValue());
    }

    /**
     * @param rowIndexes is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place
     * the drag data onto pboard (data, owner, and so on).
     * @see NSTableView.DataSource
     * Invoked by view after it has been determined that a drag should begin, but before the drag has been started.
     * The drag image and other drag-related information will be set up and provided by the table view once this call
     * returns with true.
     */
    @Override
    public boolean tableView_writeRowsWithIndexes_toPasteboard(final NSTableView view, final NSIndexSet rowIndexes,
                                                               final NSPasteboard pboard) {
        for(NSUInteger index = rowIndexes.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = rowIndexes.indexGreaterThanIndex(index)) {
            pasteboard.add(this.getSource().get(index.intValue()));
        }
        NSEvent event = NSApplication.sharedApplication().currentEvent();
        if(event != null) {
            NSPoint dragPosition = view.convertPoint_fromView(event.locationInWindow(), null);
            NSRect imageRect = new NSRect(new NSPoint(dragPosition.x.doubleValue() - 16, dragPosition.y.doubleValue() - 16), new NSSize(32, 32));
            // Writing a promised file of the host as a bookmark file to the clipboard
            view.dragPromisedFilesOfTypes(NSArray.arrayWithObject("duck"), imageRect, this.id(), true, event);
            return true;
        }
        return false;
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     * This method is invoked when the drop has been accepted by the destination and the destination,
     * in the case of another Cocoa application, invokes the NSDraggingInfo method
     * namesOfPromisedFilesDroppedAtDestination.
     * For long operations, you can cache dropDestination and defer the creation of the files until the
     * finishedDraggingImage method to avoid blocking the destination application.
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
                promisedDragNames.addObject(NSString.stringWithString(file.getName()));
            }
            pasteboard.clear();
        }
        return promisedDragNames;
    }
}
