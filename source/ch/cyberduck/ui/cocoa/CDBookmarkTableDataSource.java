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
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.*;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Iterator;

/**
 * @version $Id$
 */
public class CDBookmarkTableDataSource extends CDController {
    private static Logger log = Logger.getLogger(CDBookmarkTableDataSource.class);

    private static final NSImage DOCUMENT_ICON;
    private static final NSImage DOCUMENT_ICON_SMALL;

    static {
        DOCUMENT_ICON = NSImage.imageNamed("bookmark40.tiff");
        DOCUMENT_ICON.setSize(new NSSize(40, 40));
        DOCUMENT_ICON_SMALL = NSImage.imageNamed("bookmark16.tiff");
        DOCUMENT_ICON_SMALL.setSize(new NSSize(16, 16));
    }

    private static final NSImage GREEN_ICON
            = NSImage.imageNamed("statusGreen.tiff");

    private static final NSImage YELLOW_ICON
            = NSImage.imageNamed("statusYellow.tiff");

    public static final String ICON_COLUMN = "ICON";
    public static final String BOOKMARK_COLUMN = "BOOKMARK";
    public static final String STATUS_COLUMN = "STATUS";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    protected CDBrowserController controller;

    public CDBookmarkTableDataSource(CDBrowserController controller) {
        this.controller = controller;
    }

    private HostFilter filter;

    /**
     * Display only a subset of all bookmarks
     * @param filter
     */
    public void setFilter(HostFilter filter) {
        this.filter = filter;
    }

    /**
     * @param c
     * @see HostFilter
     * @return The filtered collection currently to be displayed within the constraints
     * given by the comparision with the HostFilter
     */
    protected Collection filter(Collection c) {
        if(null == filter) {
            return c;
        }
        Collection filtered = new Collection(c);
        for(Iterator i = filtered.iterator(); i.hasNext();) {
            if(!filter.accept((Host) i.next())) {
                //temporarly remove the bookmark from the collection
                i.remove();
            }
        }
        return filtered;
    }

    /**
     * @see NSTableView.DataSource
     */
    public int numberOfRowsInTableView(NSTableView view) {
        return this.filter(HostCollection.instance()).size();
    }

    /**
     * @see NSTableView.DataSource
     */
    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if(row < this.numberOfRowsInTableView(view)) {
            String identifier = (String) tableColumn.identifier();
            if(identifier.equals(ICON_COLUMN)) {
                if(Preferences.instance().getBoolean("browser.bookmarkDrawer.smallItems")) {
                    return DOCUMENT_ICON_SMALL;
                }
                return DOCUMENT_ICON;
            }
            final Object host = this.filter(HostCollection.instance()).get(row);
            if(identifier.equals(BOOKMARK_COLUMN)) {
                return host;
            }
            if(identifier.equals(STATUS_COLUMN)) {
                if(controller.isMounted()) {
                    if(host.equals(controller.getSession().getHost())) {
                        return controller.isConnected() ? GREEN_ICON : YELLOW_ICON;
                    }
                }
                return null;
            }
            if(identifier.equals(TYPEAHEAD_COLUMN)) {
                return ((Host) host).getNickname();
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        log.warn("tableViewObjectValueForLocation:" + row + " == null");
        return null;
    }

    /**
     * @see NSTableView.DataSource
     */
    public int tableViewValidateDrop(NSTableView view, NSDraggingInfo info, int index, int operation) {
        if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            Object o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                NSArray elements = (NSArray) o;
                for(int i = 0; i < elements.count(); i++) {
                    String file = (String) elements.objectAtIndex(i);
                    if(file.indexOf(".duck") != -1) {
                        //allow file drags if bookmark file even if list is empty
                        return NSDraggingInfo.DragOperationCopy;
                    }
                }
                if(index > -1 && index < view.numberOfRows()) {
                    //only allow other files if there is at least one bookmark
                    view.setDropRowAndDropOperation(index, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
            }
        }
        if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilesPromisePboardType)) != null) {
            if(index > -1 && index < view.numberOfRows()) {
                // We accept any file promise within the bounds
                return NSDraggingInfo.DragOperationMove;
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    /**
     * @see NSTableView.DataSource
     * Invoked by view when the mouse button is released over a table view that previously decided to allow a drop.
     *
     * @param info  contains details on this dragging operation.
     * @param index The proposed location is row and action is operation.
     *              The data source should incorporate the data from the dragging pasteboard at this time.
     */
    public boolean tableViewAcceptDrop(NSTableView view, NSDraggingInfo info, int index, int operation) {
        log.debug("tableViewAcceptDrop:" + index);
        final Collection collection = this.filter(HostCollection.instance());
        if(info.draggingPasteboard().availableTypeFromArray(
                new NSArray(NSPasteboard.FilenamesPboardType)) != null) {

            boolean accepted = false;
            // We get a drag from another application e.g. Finder.app proposing some files
            NSArray filesList = (NSArray) info.draggingPasteboard().propertyListForType(
                    NSPasteboard.FilenamesPboardType);// get the filenames from pasteboard
            // If regular files are dropped, these will be uploaded to the dropped bookmark location
            final List roots = new Collection();
            Session session = null;
            for(int i = 0; i < filesList.count(); i++) {
                String filename = (String) filesList.objectAtIndex(i);
                if(filename.endsWith(".duck")) {
                    // Adding a previously exported bookmark file from the Finder
                    if(index < 0) {
                        index = 0;
                    }
                    if(index > view.numberOfRows()) {
                        index = view.numberOfRows();
                    }
                    Host bookmark = ((CDMainController)NSApplication.sharedApplication().delegate()).importBookmark(
                            new Local(filename));
                    if(bookmark != null) {
                        //parsing succeeded
                        collection.add(index, bookmark);
                        view.reloadData();
                        view.selectRow(index, false);
                        view.scrollRowToVisible(index);
                        accepted = true;
                    }
                }
                else {
                    // The bookmark this file has been dropped onto
                    Host h = (Host) collection.get(index);
                    if(null == session) {
                        session = SessionFactory.createSession(h);
                    }
                    // Upload to the remote host this bookmark points to
                    roots.add(PathFactory.createPath(session, h.getDefaultPath(), new Local(filename)));
                }
            }
            final Transfer q = new UploadTransfer(roots);
            // If anything has been added to the queue, then process the queue
            if(q.numberOfRoots() > 0) {
                CDTransferController.instance().startTransfer(q);
                accepted = true;
            }
            return accepted;
        }
        if(info.draggingPasteboard().availableTypeFromArray(
                new NSArray(NSPasteboard.FilesPromisePboardType)) != null) {
            for(int i = 0; i < promisedDragBookmarks.length; i++) {
                collection.remove(collection.indexOf(promisedDragBookmarks[i]));
                collection.add(index, promisedDragBookmarks[i]);
                view.reloadData();
                view.selectRow(index, false);
                view.scrollRowToVisible(index);
            }
            return true;
        }
        return false;
    }

    /**
     * @see NSDraggingSource
     *
     * @see "http://www.cocoabuilder.com/archive/message/2005/10/5/118857"
     */
    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
        log.debug("finishedDraggingImage:" + operation);
        NSPasteboard.pasteboardWithName(NSPasteboard.DragPboard).declareTypes(null, null);
    }

    /**
     * @see NSDraggingSource
     *
     * @param local
     * @return
     */
    public int draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if(local)
            return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
        return NSDraggingInfo.DragOperationCopy;
    }

    /**
     * The files dragged from the favorits drawer to the Finder --> bookmark files
     */
    private Host[] promisedDragBookmarks;

    /**
     * @see NSTableView.DataSource
     * Invoked by view after it has been determined that a drag should begin, but before the drag has been started.
     * The drag image and other drag-related information will be set up and provided by the table view once this call
     * returns with true.
     *
     * @param rows is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place
     *         the drag data onto pboard (data, owner, and so on).
     */
    public boolean tableViewWriteRowsToPasteboard(NSTableView view, NSArray rows, NSPasteboard pboard) {
        log.debug("tableViewWriteRowsToPasteboard:" + rows);
        if(rows.count() > 0) {
            this.promisedDragBookmarks = new Host[rows.count()];
            for(int i = 0; i < rows.count(); i++) {
                promisedDragBookmarks[i]
                        = (Host) HostCollection.instance().get(((Number) rows.objectAtIndex(i)).intValue());
            }
            NSEvent event = NSApplication.sharedApplication().currentEvent();
            if (event != null) {
                NSPoint dragPosition = view.convertPointFromView(event.locationInWindow(), null);
                NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
                // Writing a promised file of the host as a bookmark file to the clipboard
                view.dragPromisedFilesOfTypes(new NSArray("duck"), imageRect, this, true, event);
                return true;
            }
        }
        return false;
    }

    /**
     * @see NSTableView.DataSource
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     *         This method is invoked when the drop has been accepted by the destination and the destination,
     *         in the case of another Cocoa application, invokes the NSDraggingInfo method
     *         namesOfPromisedFilesDroppedAtDestination.
     *         For long operations, you can cache dropDestination and defer the creation of the files until the
     *         finishedDraggingImage method to avoid blocking the destination application.
     */
    public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        NSMutableArray promisedDragNames = new NSMutableArray();
        for(int i = 0; i < promisedDragBookmarks.length; i++) {
            try {
                // utf-8 is just a wild guess
                Local file = new Local(java.net.URLDecoder.decode(dropDestination.getPath(), "utf-8"),
                        promisedDragBookmarks[i].getNickname() + ".duck");
                ((CDMainController)NSApplication.sharedApplication().delegate()).exportBookmark(
                        promisedDragBookmarks[i],
                        file);
                // Adding the filename that is promised to be created at the dropDestination
                promisedDragNames.addObject(file.getName());
            }
            catch(java.io.UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }
        }
        return promisedDragNames;
    }
}