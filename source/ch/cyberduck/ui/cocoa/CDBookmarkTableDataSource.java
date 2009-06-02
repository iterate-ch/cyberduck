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
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.NSPoint;
import org.rococoa.cocoa.NSRect;
import org.rococoa.cocoa.NSSize;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class CDBookmarkTableDataSource extends CDController implements CDListDataSource {
    private static Logger log = Logger.getLogger(CDBookmarkTableDataSource.class);

    public static final String ICON_COLUMN = "ICON";
    public static final String BOOKMARK_COLUMN = "BOOKMARK";
    public static final String STATUS_COLUMN = "STATUS";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    protected CDBrowserController controller;

    public CDBookmarkTableDataSource(CDBrowserController controller, BookmarkCollection source) {
        this.controller = controller;
        this.source = source;
    }

    private BookmarkCollection source;

    public void setSource(final BookmarkCollection source) {
        this.source = source;
        this.setFilter(null);
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
                public boolean allowsAdd() {
                    return source.allowsAdd();
                }

                public boolean allowsDelete() {
                    return source.allowsDelete();
                }

                public boolean allowsEdit() {
                    return source.allowsEdit();
                }
            };
            for(Iterator<Host> i = source.iterator(); i.hasNext();) {
                final Host bookmark = i.next();
                if(filter.accept(bookmark)) {
                    filtered.add(bookmark);
                }
            }
            filtered.addListener(new AbstractCollectionListener<Host>() {
                public void collectionItemAdded(Host item) {
                    source.add(item);
                }

                public void collectionItemRemoved(Host item) {
                    source.remove(item);
                }
            });
        }
        return filtered;
    }

    /**
     * @see NSTableView.DataSource
     */
    public int numberOfRowsInTableView(NSTableView view) {
        return this.getSource().size();
    }

    /**
     * @see NSTableView.DataSource
     */
    public NSObject tableView_objectValueForTableColumn_row(NSTableView view, NSTableColumn tableColumn, int row) {
        final String identifier = tableColumn.identifier();
        final Host host = this.getSource().get(row);
        if(identifier.equals(ICON_COLUMN)) {
            return CDIconCache.instance().iconForName(host.getProtocol().disk(),
                    Preferences.instance().getInteger("bookmark.icon.size"));
        }
        if(identifier.equals(BOOKMARK_COLUMN)) {
            return host.getAsDictionary();
        }
        if(identifier.equals(STATUS_COLUMN)) {
            if(controller.hasSession()) {
                final Session session = controller.getSession();
                if(host.equals(session.getHost())) {
                    if(session.isConnected()) {
                        return NSImage.imageNamed("statusGreen.tiff");
                    }
                    if(session.isOpening()) {
                        return NSImage.imageNamed("statusYellow.tiff");
                    }
                }
            }
            return NSImage.imageWithSize(new NSSize(0, 0));
        }
        if(identifier.equals(TYPEAHEAD_COLUMN)) {
            return NSString.stringWithString(host.getNickname());
        }
        throw new IllegalArgumentException("Unknown identifier: " + identifier);
    }

    /**
     * @see NSTableView.DataSource
     */
    public int tableView_validateDrop_proposedRow_proposedDropOperation(NSTableView view, NSDraggingInfo info, int index, int operation) {
        if(!this.getSource().allowsEdit()) {
            // Do not allow drags for non writable collections
            return NSDraggingInfo.DragOperationNone;
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.StringPboardType)) != null) {
            String o = info.draggingPasteboard().stringForType(NSPasteboard.StringPboardType);
            if(o != null) {
                if(Protocol.isURL(o)) {
                    view.setDropRow_dropOperation(index, NSTableView.NSTableViewDropAbove);
                    return NSDraggingInfo.DragOperationCopy;
                }
            }
            return NSDraggingInfo.DragOperationNone;
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count(); i++) {
                    String file = elements.objectAtIndex(i).toString();
                    if(file.contains(".duck")) {
                        //allow file drags if bookmark file even if list is empty
                        return NSDraggingInfo.DragOperationCopy;
                    }
                }
                if(index > -1 && index < view.numberOfRows()) {
                    //only allow other files if there is at least one bookmark
                    view.setDropRow_dropOperation(index, NSTableView.NSTableViewDropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
            }
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count(); i++) {
                    if(Protocol.isURL(elements.objectAtIndex(i).toString())) {
                        view.setDropRow_dropOperation(index, NSTableView.NSTableViewDropAbove);
                        return NSDraggingInfo.DragOperationCopy;
                    }
                }
            }
            return NSDraggingInfo.DragOperationNone;
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilesPromisePboardType)) != null) {
            if(index > -1 && index < view.numberOfRows()) {
                view.setDropRow_dropOperation(index, NSTableView.NSTableViewDropAbove);
                // We accept any file promise within the bounds
                return NSDraggingInfo.DragOperationMove;
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    /**
     * @param info contains details on this dragging operation.
     * @param row  The proposed location is row and action is operation.
     *             The data source should incorporate the data from the dragging pasteboard at this time.
     * @see NSTableView.DataSource
     *      Invoked by view when the mouse button is released over a table view that previously decided to allow a drop.
     */
    public boolean tableView_acceptDrop_row_dropOperation(NSTableView view, NSDraggingInfo info, int row, int operation) {
        log.debug("tableViewAcceptDrop:" + row);
        final BookmarkCollection source = this.getSource();
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            // We get a drag from another application e.g. Finder.app proposing some files
            NSArray filesList = Rococoa.cast(info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType), NSArray.class);// get the filenames from pasteboard
            // If regular files are dropped, these will be uploaded to the dropped bookmark location
            final List<Path> roots = new Collection<Path>();
            Session session = null;
            for(int i = 0; i < filesList.count(); i++) {
                String filename = filesList.objectAtIndex(i).toString();
                if(filename.endsWith(".duck")) {
                    // Adding a previously exported bookmark file from the Finder
                    if(row < 0) {
                        row = 0;
                    }
                    if(row > view.numberOfRows()) {
                        row = view.numberOfRows();
                    }
                    try {
                        source.add(row, new Host(new Local(filename)));
                        view.selectRow_byExtendingSelection(row, false);
                        view.scrollRowToVisible(row);
                    }
                    catch(IOException e) {
                        log.error(e.getMessage());
                        return false;
                    }
                }
                else {
                    // The bookmark this file has been dropped onto
                    Host h = source.get(row);
                    if(null == session) {
                        session = SessionFactory.createSession(h);
                    }
                    // Upload to the remote host this bookmark points to
                    roots.add(PathFactory.createPath(session, h.getDefaultPath(), new Local(filename)));
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
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count(); i++) {
                    final String url = elements.objectAtIndex(i).toString();
                    if(StringUtils.isNotBlank(url)) {
                        final Host h = Host.parse(url);
                        source.add(row, h);
                        view.selectRow_byExtendingSelection(row, false);
                        view.scrollRowToVisible(row);
                    }
                }
                return true;
            }
            return false;
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.StringPboardType)) != null) {
            String o = info.draggingPasteboard().stringForType(NSPasteboard.StringPboardType);
            if(o != null) {
                final Host h = Host.parse(o);
                source.add(row, h);
                view.selectRow_byExtendingSelection(row, false);
                view.scrollRowToVisible(row);
                return true;
            }
            return false;
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilesPromisePboardType)) != null && promisedDragBookmarks != null) {
            for(Host promisedDragBookmark : promisedDragBookmarks) {
                source.remove(source.indexOf(promisedDragBookmark));
                source.add(row, promisedDragBookmark);
                view.selectRow_byExtendingSelection(row, false);
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
    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
        log.debug("finishedDraggingImage:" + operation);
        if(NSDraggingInfo.DragOperationDelete == operation) {
            controller.deleteBookmarkButtonClicked(null);
        }
        NSPasteboard.pasteboardWithName(NSPasteboard.DragPboard).declareTypes_owner(null, null);
        promisedDragBookmarks = null;
    }

    /**
     * @param local
     * @return
     * @see NSDraggingSource
     */
    public int draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if(local) {
            return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
        }
        return NSDraggingInfo.DragOperationCopy | NSDraggingInfo.DragOperationDelete;
    }

    /**
     * The files dragged from the favorits drawer to the Finder --> bookmark files
     */
    private Host[] promisedDragBookmarks;

    /**
     * @param rows is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place
     *         the drag data onto pboard (data, owner, and so on).
     * @see NSTableView.DataSource
     *      Invoked by view after it has been determined that a drag should begin, but before the drag has been started.
     *      The drag image and other drag-related information will be set up and provided by the table view once this call
     *      returns with true.
     */
    public boolean tableViewWriteRowsToPasteboard(NSTableView view, NSArray rows, NSPasteboard pboard) {
        log.debug("tableViewWriteRowsToPasteboard:" + rows);
        if(rows.count() > 0) {
            this.promisedDragBookmarks = new Host[rows.count()];
            for(int i = 0; i < rows.count(); i++) {
                promisedDragBookmarks[i] =
                        new Host(this.getSource().get(((Number) rows.objectAtIndex(i)).intValue()).getAsDictionary());
            }
            NSEvent event = NSApplication.sharedApplication().currentEvent();
            if(event != null) {
                NSPoint dragPosition = view.convertPoint_fromView(event.locationInWindow(), null);
                NSRect imageRect = new NSRect(new NSPoint(dragPosition.x.intValue() - 16, dragPosition.y.intValue() - 16), new NSSize(32, 32));
                // Writing a promised file of the host as a bookmark file to the clipboard
//                view.dragPromisedFilesOfTypes(NSArray.arrayWithObject("duck"), imageRect, this, true, event);
                return true;
            }
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
    public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        final NSMutableArray promisedDragNames = NSMutableArray.arrayWithCapacity(promisedDragBookmarks.length);
        try {
            for(Host promisedDragBookmark : promisedDragBookmarks) {
                // utf-8 is just a wild guess
                Local file = new Local(URLDecoder.decode(dropDestination.getPath(), "utf-8"),
                        promisedDragBookmark.getNickname() + ".duck");
                promisedDragBookmark.setFile(file);
                promisedDragBookmark.write();
                // Adding the filename that is promised to be created at the dropDestination
                promisedDragNames.addObject(NSString.stringWithString(file.getName()));
            }
        }
        catch(java.io.UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return promisedDragNames;
    }
}