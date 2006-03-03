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

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.UploadQueue;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * @version $Id$
 */
public class CDBookmarkTableDataSource extends Collection {
    private static Logger log = Logger.getLogger(CDBookmarkTableDataSource.class);

    private static final File BOOKMARKS_FILE
            = new File(Preferences.instance().getProperty("application.support.path"), "Favorites.plist");

    static {
	    BOOKMARKS_FILE.getParentFile().mkdir();
    }

    private int hostPboardChangeCount = NSPasteboard.pasteboardWithName("HostPBoard").changeCount();

    private static CDBookmarkTableDataSource instance;

    protected CDBookmarkTableDataSource() {
        this.load();
    }

    public static CDBookmarkTableDataSource instance() {
        if (null == instance) {
            instance = new CDBookmarkTableDataSource();
        }
        return instance;
    }

    private static final NSImage DOCUMENT_ICON;

    static {
        DOCUMENT_ICON = NSImage.imageNamed("bookmark40.tiff");
    }

    /**
     * NSTableView.DataSource
     */
    public int numberOfRowsInTableView(NSTableView view) {
        return this.size();
    }

    /**
     * NSTableView.DataSource
     */
    public Object tableViewObjectValueForLocation(NSTableView view, NSTableColumn tableColumn, int row) {
        if (row < this.size()) {
            String identifier = (String) tableColumn.identifier();
            if (identifier.equals("ICON")) {
                return DOCUMENT_ICON;
            }
            if (identifier.equals("BOOKMARK")) {
                return this.get(row);
            }
            if (identifier.equals("TYPEAHEAD")) {
                return ((Host) this.get(row)).getNickname();
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        log.warn("tableViewObjectValueForLocation:"+row+" == null");
        return null;
    }

    /**
     * NSComboBox.DataSource
     */
    public int numberOfItemsInComboBox(NSComboBox combo) {
        return this.size();
    }

    /**
     * NSComboBox.DataSource
     */
    public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
        if (row < this.size()) {
            return ((Host) this.get(row)).getNickname();
        }
        return null;
    }

    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    public int tableViewValidateDrop(NSTableView view, NSDraggingInfo info, int index, int operation) {
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            Object o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                NSArray elements = (NSArray) o;
                for (int i = 0; i < elements.count(); i++) {
                    String file = (String) elements.objectAtIndex(i);
                    if (file.indexOf(".duck") != -1) {
//allow file drags if bookmark file even if list is empty
                        return NSDraggingInfo.DragOperationCopy;
                    }
                }
                if (index > -1 && index < view.numberOfRows()) {
//only allow other files if there is at least one bookmark
                    view.setDropRowAndDropOperation(index, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
            }
        }
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilesPromisePboardType)) != null) {
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("HostPBoard");
            if (this.hostPboardChangeCount < pboard.changeCount()) {
                if (pboard.availableTypeFromArray(new NSArray("HostPBoardType")) != null) {
                    if (index > -1 && index < view.numberOfRows()) {
                        return NSDraggingInfo.DragOperationMove;
                    }
                }
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    /**
     * Invoked by view when the mouse button is released over a table view that previously decided to allow a drop.
     *
     * @param info  contains details on this dragging operation.
     * @param index The proposed location is row and action is operation.
     *              The data source should
     *              incorporate the data from the dragging pasteboard at this time.
     */
    public boolean tableViewAcceptDrop(NSTableView view, NSDraggingInfo info, int index, int operation) {
        log.debug("tableViewAcceptDrop:" + index);
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            NSArray filesList = (NSArray) info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);// get the data from paste board
            Queue q = new UploadQueue();
            Session session = null;
            for (int i = 0; i < filesList.count(); i++) {
                String filename = (String) filesList.objectAtIndex(i);
                if (filename.endsWith(".duck")) {
                    // Adding a previously exported bookmark file from the Finder
                    if (index < 0) {
                        index = 0;
                    }
                    if (index > view.numberOfRows()) {
                        index = view.numberOfRows();
                    }
                    Host bookmark = this.importBookmark(new File(filename));
                    if (bookmark != null) {
                        //parsing succeeded
                        this.add(index, bookmark);
                        view.reloadData();
                        view.selectRow(index, false);
                    }
                }
                else {
                    //the bookmark this file has been dropped onto
                    Host h = (Host) this.get(index);
                    if (null == session) {
                        session = SessionFactory.createSession(h);
                    }
                    // Drop of a file from the finder > upload to the remote host this bookmark points to
                    q.addRoot(PathFactory.createPath(session, h.getDefaultPath(), new Local(filename)));
                }
            }
            // if anything has been added to the queue then process the queue
            if (q.numberOfRoots() > 0) {
                CDQueueController.instance().startItem(q);
            }
            return true;
        }
        if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilesPromisePboardType)) != null) {
            // we are only interested in our private pasteboard with a description of the host encoded in as a xml.
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("HostPBoard");
            if (this.hostPboardChangeCount < pboard.changeCount()) {
                log.debug("availableTypeFromArray:HostPBoardType: " + pboard.availableTypeFromArray(new NSArray("HostPBoardType")));
                if (pboard.availableTypeFromArray(new NSArray("HostPBoardType")) != null) {
                    Object o = pboard.propertyListForType("HostPBoardType");// get the data from paste board
                    log.debug("tableViewAcceptDrop:" + o);
                    if (o != null) {
                        NSArray elements = (NSArray) o;
                        for (int i = 0; i < elements.count(); i++) {
                            Host h = new Host((NSDictionary) elements.objectAtIndex(i));
                            this.remove(this.indexOf(h));
                            this.add(index, h);
                            view.reloadData();
                            view.selectRow(index, false);
                        }
                        this.hostPboardChangeCount++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------

    // @see http://www.cocoabuilder.com/archive/message/2005/10/5/118857
    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
        log.debug("finishedDraggingImage:" + operation);
        NSPasteboard.pasteboardWithName(NSPasteboard.DragPboard).declareTypes(null, null);
    }

    public int draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if (local)
            return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
        return NSDraggingInfo.DragOperationCopy;
    }

    /**
     * The files dragged from the favorits drawer to the Finder --> bookmark files
     */
    private Host[] promisedDragBookmarks;
    private File[] promisedDragBookmarksFileDestination;

    /**
     * Invoked by view after it has been determined that a drag should begin, but before the drag has been started.
     * The drag image and other drag-related information will be set up and provided by the table view once this call
     * returns with true.
     *
     * @param rows is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
     *         (data, owner, and so on).
     */
    public boolean tableViewWriteRowsToPasteboard(NSTableView view, NSArray rows, NSPasteboard pboard) {
        log.debug("tableViewWriteRowsToPasteboard:" + rows);
        if (rows.count() > 0) {
            this.promisedDragBookmarks = new Host[rows.count()];
            this.promisedDragBookmarksFileDestination = new File[rows.count()];
            NSMutableArray promisedDragBookmarksAsDictionary = new NSMutableArray();
            for (int i = 0; i < rows.count(); i++) {
                promisedDragBookmarks[i] = (Host) this.get(((Integer) rows.objectAtIndex(i)).intValue());
                promisedDragBookmarksAsDictionary.addObject(promisedDragBookmarks[i].getAsDictionary());
            }

            // Writing data for private use for moving bookmarks.
            NSPasteboard hostPboard = NSPasteboard.pasteboardWithName("HostPBoard");
            hostPboard.declareTypes(new NSArray("HostPBoardType"), null);
            if (hostPboard.setPropertyListForType(promisedDragBookmarksAsDictionary, "HostPBoardType")) {
                log.debug("HostPBoardType data sucessfully written to pasteboard");
            }

            NSEvent event = NSApplication.sharedApplication().currentEvent();
            NSPoint dragPosition = view.convertPointFromView(event.locationInWindow(), null);
            NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
            view.dragPromisedFilesOfTypes(new NSArray("duck"), imageRect, this, true, event);
        }
        return true;
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     *         This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     *         Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     *         you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     *         blocking the destination application.
     */
    public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        NSMutableArray promisedDragNames = new NSMutableArray();
        for (int i = 0; i < promisedDragBookmarks.length; i++) {
            try {
                promisedDragBookmarksFileDestination[i] = new File(java.net.URLDecoder.decode(dropDestination.getPath(), "utf-8"),
                        promisedDragBookmarks[i].getNickname() + ".duck");
                this.exportBookmark(promisedDragBookmarks[i], promisedDragBookmarksFileDestination[i]);
                promisedDragNames.addObject(promisedDragBookmarks[i].getNickname() + ".duck");
            }
            catch (java.io.UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }
        }
        return promisedDragNames;
    }

    // ----------------------------------------------------------
    //	Data Manipulation
    // ----------------------------------------------------------

    public boolean add(Object host) {
        super.add(host);
        this.save();
        return true;
    }

    public void add(int row, Object host) {
        super.add(row, host);
        this.save();
    }

    public Object remove(int row) {
        super.remove(row);
        this.save();
        return null;
    }

    public void save() {
        this.save(BOOKMARKS_FILE);
    }

    /**
     * Saves this collection of bookmarks in to a file to the users's application support directory
     * in a plist xml format
     */
    public void save(File f) {
        if (Preferences.instance().getBoolean("favorites.save")) {
            try {
                NSMutableArray list = new NSMutableArray();
                java.util.Iterator i = this.iterator();
                while (i.hasNext()) {
                    Host bookmark = (Host) i.next();
                    list.addObject(bookmark.getAsDictionary());
                }
                NSMutableData collection = new NSMutableData();
                String[] errorString = new String[]{null};
                collection.appendData(NSPropertyListSerialization.dataFromPropertyList(list,
                        NSPropertyListSerialization.PropertyListXMLFormat,
                        errorString));
                if (errorString[0] != null) {
                    log.error("Problem writing bookmark file: " + errorString[0]);
                }

                if (collection.writeToURL(f.toURL(), true)) {
                    if(log.isInfoEnabled())
                        log.info("Bookmarks sucessfully saved to :" + f.toString());
                }
                else {
                    log.error("Error saving Bookmarks to :" + f.toString());
                }
            }
            catch (java.net.MalformedURLException e) {
                log.error(e.getMessage());
            }
        }
    }

    public void load() {
        this.load(BOOKMARKS_FILE);
    }

    /**
     * Deserialize all the bookmarks saved previously in the users's application support directory
     */
    public void load(File f) {
        if (f.exists()) {
            log.info("Found Bookmarks file: " + f.toString());
            NSData plistData = new NSData(f);
            String[] errorString = new String[]{null};
            Object propertyListFromXMLData =
                    NSPropertyListSerialization.propertyListFromData(plistData,
                            NSPropertyListSerialization.PropertyListImmutable,
                            new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                            errorString);
            if (errorString[0] != null) {
                log.error("Problem reading bookmark file: " + errorString[0]);
                return;
            }
            if (propertyListFromXMLData instanceof NSArray) {
                NSArray entries = (NSArray) propertyListFromXMLData;
                java.util.Enumeration i = entries.objectEnumerator();
                Object element;
                while (i.hasMoreElements()) {
                    element = i.nextElement();
                    if (element instanceof NSDictionary) {
                        super.add(new Host((NSDictionary) element));
                    }
                }
            }
        }
    }

    public Host importBookmark(File file) {
        NSData plistData = new NSData(file);
        String[] errorString = new String[]{null};
        Object propertyListFromXMLData =
                NSPropertyListSerialization.propertyListFromData(plistData,
                        NSPropertyListSerialization.PropertyListImmutable,
                        new int[]{NSPropertyListSerialization.PropertyListXMLFormat},
                        errorString);
        if (errorString[0] != null) {
            log.error("Problem reading bookmark file: " + errorString[0]);
            return null;
        }
        if (propertyListFromXMLData instanceof NSDictionary) {
            return new Host((NSDictionary) propertyListFromXMLData);
        }
        log.error("Invalid file format:" + file);
        return null;
    }

    public void exportBookmark(Host bookmark, File file) {
        try {
            log.info("Exporting bookmark " + bookmark + " to " + file);
            NSMutableData collection = new NSMutableData();
            String[] errorString = new String[]{null};
            collection.appendData(NSPropertyListSerialization.dataFromPropertyList(bookmark.getAsDictionary(),
                    NSPropertyListSerialization.PropertyListXMLFormat,
                    errorString));
            if (errorString[0] != null) {
                log.error("Problem writing bookmark file: " + errorString[0]);
            }
            if (collection.writeToURL(file.toURL(), true)) {
                log.info("Bookmarks sucessfully saved in :" + file.toString());
                NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(file.getAbsolutePath());
            }
            else {
                log.error("Error saving Bookmarks in :" + file.toString());
            }
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }
}