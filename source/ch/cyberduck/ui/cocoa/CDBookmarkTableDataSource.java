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

import ch.cyberduck.core.*;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.serializer.HostReaderFactory;
import ch.cyberduck.core.serializer.HostWriterFactory;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.foundation.NSMutableArray;
import ch.cyberduck.ui.cocoa.foundation.NSMutableDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.foundation.NSURL;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.*;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @version $Id$
 */
public class CDBookmarkTableDataSource extends CDListDataSource {
    private static Logger log = Logger.getLogger(CDBookmarkTableDataSource.class);

    public static final String ICON_COLUMN = "ICON";
    public static final String BOOKMARK_COLUMN = "BOOKMARK";
    public static final String STATUS_COLUMN = "STATUS";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    protected CDBrowserController controller;

    public CDBookmarkTableDataSource(CDBrowserController controller, BookmarkCollection source) {
        this.controller = controller;
        this.setSource(source);
    }

    private BookmarkCollection source = BookmarkCollection.empty();

    private CollectionListener<Host> listener;

    public void setSource(final BookmarkCollection source) {
        this.source.removeListener(listener); //Remove previous listener
        this.source = source;
        this.source.addListener(listener = new CollectionListener<Host>() {
            private Timer delayed = null;

            public void collectionItemAdded(Host item) {
                cache.clear();
                controller.reloadBookmarks();
            }

            public void collectionItemRemoved(Host item) {
                cache.clear();
                controller.reloadBookmarks();
            }

            public void collectionItemChanged(Host item) {
                cache.clear();
                controller.reloadBookmarks();
                if(null != delayed) {
                    delayed.cancel();
                }
                delayed = new Timer();
                delayed.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        controller.invoke(new DefaultMainAction() {
                            public void run() {
                                source.save();
                            }
                        });
                    }
                }, 5000); // Delay to 5 seconds. When typing changes we don't have to save every iteration.
            }
        });
        this.setFilter(null);
    }

    @Override
    protected void invalidate() {
        cache.clear();
        source.removeListener(listener);
        super.invalidate();
    }

    private HostFilter filter;

    /**
     * Display only a subset of all bookmarks
     *
     * @param filter
     */
    public void setFilter(HostFilter filter) {
        this.filter = filter;
        this.filtered = null;
    }

    /**
     * Subset of the original source
     */
    private BookmarkCollection filtered;

    /**
     * @return The filtered collection currently to be displayed within the constraints
     *         given by the comparision with the HostFilter
     * @see HostFilter
     */
    protected BookmarkCollection getSource() {
        if(null == filter) {
            return source;
        }
        if(null == filtered) {
            filtered = new BookmarkCollection() {
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
                public void load() {
                    source.load();
                }
            };
            for(final Host bookmark : source) {
                if(filter.accept(bookmark)) {
                    filtered.add(bookmark);
                }
            }
            filtered.addListener(new CollectionListener<Host>() {
                public void collectionItemAdded(Host item) {
                    source.add(item);
                }

                public void collectionItemRemoved(Host item) {
                    source.remove(item);
                }

                public void collectionItemChanged(Host item) {
                    source.collectionItemChanged(item);
                }
            });
        }
        return filtered;
    }

    public NSInteger numberOfRowsInTableView(NSTableView view) {
        return new NSInteger(this.getSource().size());
    }

    private static final NSImage STATUS_GREEN = CDIconCache.iconNamed("statusGreen.tiff");
    private static final NSImage STATUS_YELLOW = CDIconCache.iconNamed("statusYellow.tiff");

    /**
     * Second cache because it is expensive to create proxy instances
     */
    private AttributeCache<Host> cache = new AttributeCache<Host>(
            Preferences.instance().getInteger("bookmark.model.cache.size")
    );

    public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, NSInteger row) {
        final String identifier = tableColumn.identifier();
        final Host host = this.getSource().get(row.intValue());
        final NSObject cached = cache.get(host, identifier);
        if(null == cached) {
            if(identifier.equals(ICON_COLUMN)) {
                return CDIconCache.iconNamed(host.getProtocol().disk(),
                        Preferences.instance().getInteger("bookmark.icon.size"));
            }
            if(identifier.equals(BOOKMARK_COLUMN)) {
                NSMutableDictionary dict = NSMutableDictionary.dictionaryWithDictionary(host.<NSDictionary>getAsDictionary());
                dict.setObjectForKey(host.toURL() + Path.normalize(host.getDefaultPath()), "URL");
                if(StringUtils.isNotBlank(host.getComment())) {
                    dict.setObjectForKey(StringUtils.remove(StringUtils.remove(host.getComment(), CharUtils.LF), CharUtils.CR), "Comment");
                }
                return cache.put(host, identifier, dict);
            }
            if(identifier.equals(STATUS_COLUMN)) {
                if(controller.hasSession()) {
                    final Session session = controller.getSession();
                    if(host.equals(session.getHost())) {
                        if(session.isConnected()) {
                            return STATUS_GREEN;
                        }
                        if(session.isOpening()) {
                            return STATUS_YELLOW;
                        }
                    }
                }
                return null;
            }
            if(identifier.equals(TYPEAHEAD_COLUMN)) {
                return cache.put(host, identifier, NSString.stringWithString(host.getNickname()));
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        return cached;
    }

    @Override
    public NSUInteger tableView_validateDrop_proposedRow_proposedDropOperation(NSTableView view, NSDraggingInfo draggingInfo, NSInteger index, NSUInteger operation) {
        NSPasteboard draggingPasteboard = draggingInfo.draggingPasteboard();
        if(!this.getSource().allowsEdit()) {
            // Do not allow drags for non writable collections
            return NSDraggingInfo.NSDragOperationNone;
        }
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.StringPboardType)) != null) {
            String o = draggingPasteboard.stringForType(NSPasteboard.StringPboardType);
            if(o != null) {
                if(Protocol.isURL(o)) {
                    view.setDropRow(index, NSTableView.NSTableViewDropAbove);
                    return NSDraggingInfo.NSDragOperationCopy;
                }
            }
            return NSDraggingInfo.NSDragOperationNone;
        }
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            NSObject o = draggingPasteboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count().intValue(); i++) {
                    String file = elements.objectAtIndex(new NSUInteger(i)).toString();
                    if(file.contains(".duck")) {
                        //allow file drags if bookmark file even if list is empty
                        return NSDraggingInfo.NSDragOperationCopy;
                    }
                }
                if(index.intValue() > -1 && index.intValue() < view.numberOfRows().intValue()) {
                    //only allow other files if there is at least one bookmark
                    view.setDropRow(index, NSTableView.NSTableViewDropOn);
                    return NSDraggingInfo.NSDragOperationCopy;
                }
            }
        }
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = draggingPasteboard.propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count().intValue(); i++) {
                    if(Protocol.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                        view.setDropRow(index, NSTableView.NSTableViewDropAbove);
                        return NSDraggingInfo.NSDragOperationCopy;
                    }
                }
            }
            return NSDraggingInfo.NSDragOperationNone;
        }
        if(!HostPasteboard.getPasteboard().isEmpty()) {
            if(index.intValue() > -1 && index.intValue() < view.numberOfRows().intValue()) {
                view.setDropRow(index, NSTableView.NSTableViewDropAbove);
                // We accept any file promise within the bounds
                return NSDraggingInfo.NSDragOperationMove;
            }
        }
        return NSDraggingInfo.NSDragOperationNone;
    }

    /**
     * @param draggingInfo contains details on this dragging operation.
     * @param row          The proposed location is row and action is operation.
     *                     The data source should incorporate the data from the dragging pasteboard at this time.
     * @see NSTableView.DataSource
     *      Invoked by view when the mouse button is released over a table view that previously decided to allow a drop.
     */
    @Override
    public boolean tableView_acceptDrop_row_dropOperation(NSTableView view, NSDraggingInfo draggingInfo, NSInteger row, NSUInteger operation) {
        NSPasteboard draggingPasteboard = draggingInfo.draggingPasteboard();
        log.debug("tableViewAcceptDrop:" + row);
        final BookmarkCollection source = this.getSource();
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            // We get a drag from another application e.g. Finder.app proposing some files
            NSArray filesList = Rococoa.cast(draggingPasteboard.propertyListForType(NSPasteboard.FilenamesPboardType), NSArray.class);// get the filenames from pasteboard
            // If regular files are dropped, these will be uploaded to the dropped bookmark location
            final List<Path> roots = new Collection<Path>();
            Session session = null;
            for(int i = 0; i < filesList.count().intValue(); i++) {
                String filename = filesList.objectAtIndex(new NSUInteger(i)).toString();
                if(filename.endsWith(".duck")) {
                    // Adding a previously exported bookmark file from the Finder
                    if(row.intValue() < 0) {
                        row = new NSInteger(0);
                    }
                    if(row.intValue() > view.numberOfRows().intValue()) {
                        row = new NSInteger(view.numberOfRows().intValue());
                    }
                    source.add(row.intValue(), HostReaderFactory.instance().read(LocalFactory.createLocal(filename)));
                    view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), false);
                    view.scrollRowToVisible(row);
                }
                else {
                    // The bookmark this file has been dropped onto
                    Host h = source.get(row.intValue());
                    if(null == session) {
                        session = SessionFactory.createSession(h);
                    }
                    // Upload to the remote host this bookmark points to
                    roots.add(PathFactory.createPath(session, h.getDefaultPath(), LocalFactory.createLocal(filename)));
                }
            }
            if(!roots.isEmpty()) {
                final Transfer q = new UploadTransfer(roots);
                // If anything has been added to the queue, then process the queue
                if(q.numberOfRoots() > 0) {
                    CDTransferController.instance().startTransfer(q);
                }
            }
            return true;
        }
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = draggingPasteboard.propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count().intValue(); i++) {
                    final String url = elements.objectAtIndex(new NSUInteger(i)).toString();
                    if(StringUtils.isNotBlank(url)) {
                        final Host h = Host.parse(url);
                        source.add(row.intValue(), h);
                        view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), false);
                        view.scrollRowToVisible(row);
                    }
                }
                return true;
            }
            return false;
        }
        if(draggingPasteboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.StringPboardType)) != null) {
            String o = draggingPasteboard.stringForType(NSPasteboard.StringPboardType);
            if(o != null) {
                final Host h = Host.parse(o);
                source.add(row.intValue(), h);
                view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), false);
                view.scrollRowToVisible(row);
                return true;
            }
            return false;
        }
        if(!HostPasteboard.getPasteboard().isEmpty()) {
            for(Host promisedDragBookmark : HostPasteboard.getPasteboard()) {
                source.remove(source.indexOf(promisedDragBookmark));
                source.add(row.intValue(), promisedDragBookmark);
                view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), false);
                view.scrollRowToVisible(row);
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
    public void draggedImage_endedAt_operation(NSImage image, NSPoint point, NSUInteger operation) {
        if(NSDraggingInfo.NSDragOperationDelete.intValue() == operation.intValue()) {
            controller.deleteBookmarkButtonClicked(null);
        }
        NSPasteboard.pasteboardWithName(NSPasteboard.DragPboard).declareTypes_owner(null, null);
        HostPasteboard.getPasteboard().clear();
    }

    /**
     * @param local
     * @return
     * @see NSDraggingSource
     */
    @Override
    public NSUInteger draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if(local) {
            return new NSUInteger(NSDraggingInfo.NSDragOperationMove.intValue() | NSDraggingInfo.NSDragOperationCopy.intValue());
        }
        return new NSUInteger(NSDraggingInfo.NSDragOperationCopy.intValue() | NSDraggingInfo.NSDragOperationDelete.intValue());
    }

    /**
     * @param rowIndexes is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place
     *         the drag data onto pboard (data, owner, and so on).
     * @see NSTableView.DataSource
     *      Invoked by view after it has been determined that a drag should begin, but before the drag has been started.
     *      The drag image and other drag-related information will be set up and provided by the table view once this call
     *      returns with true.
     */
    @Override
    public boolean tableView_writeRowsWithIndexes_toPasteboard(NSTableView view, NSIndexSet rowIndexes, NSPasteboard pboard) {
        for(NSUInteger index = rowIndexes.firstIndex(); !index.equals(NSIndexSet.NSNotFound); index = rowIndexes.indexGreaterThanIndex(index)) {
            HostPasteboard.getPasteboard().add(this.getSource().get(index.intValue()));
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
     *         This method is invoked when the drop has been accepted by the destination and the destination,
     *         in the case of another Cocoa application, invokes the NSDraggingInfo method
     *         namesOfPromisedFilesDroppedAtDestination.
     *         For long operations, you can cache dropDestination and defer the creation of the files until the
     *         finishedDraggingImage method to avoid blocking the destination application.
     * @see NSTableView.DataSource
     */
    @Override
    public NSArray namesOfPromisedFilesDroppedAtDestination(final NSURL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        final NSMutableArray promisedDragNames = NSMutableArray.array();
        if(null != dropDestination) {
            final HostPasteboard pasteboard = HostPasteboard.getPasteboard();
            for(Host promisedDragBookmark : pasteboard) {
                final Local file = LocalFactory.createLocal(dropDestination.path(), promisedDragBookmark.getNickname() + ".duck");
                HostWriterFactory.instance().write(promisedDragBookmark, file);
                // Adding the filename that is promised to be created at the dropDestination
                promisedDragNames.addObject(NSString.stringWithString(file.getName()));
            }
            pasteboard.clear();
        }
        return promisedDragNames;
    }

//    @Override
//    public NSArray tableView_namesOfPromisedFilesDroppedAtDestination_forDraggedRowsWithIndexes(NSTableView view,
//                                                                                                final NSURL dropDestination,
//                                                                                                NSIndexSet rowIndexes) {
//        final NSMutableArray promisedDragNames = NSMutableArray.arrayWithCapacity(rowIndexes.count().intValue().intValue());
//        for(NSUInteger index = rowIndexes.firstIndex(); index.longValue() != NSIndexSet.NSNotFound; index = rowIndexes.indexGreaterThanIndex(index)) {
//            if(index.intValue() == -1) {
//                break;
//            }
//            final Host host = new Host(this.getSource().get(index.intValue()).getAsDictionary());
//            Local file = LocalFactory.createLocalLocal(dropDestination.path(), host.getNickname() + ".duck");
//            host.setFile(file);
//            try {
//                host.write();
//                // Adding the filename that is promised to be created at the dropDestination
//                promisedDragNames.addObject(NSString.stringWithString(file.getName()));
//            }
//            catch(IOException e) {
//                log.error(e.getMessage());
//            }
//        }
//        return promisedDragNames;
//    }
}