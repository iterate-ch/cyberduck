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
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSDraggingInfo;
import com.apple.cocoa.application.NSDraggingSource;
import com.apple.cocoa.application.NSEvent;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class CDBrowserTableDataSource extends NSObject {
    protected static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    protected static final NSImage FOLDER_NOACCESS_ICON = NSImage.imageNamed("folder_noaccess.tiff");
    protected static final NSImage FOLDER_WRITEONLY_ICON = NSImage.imageNamed("folder_writeonly.tiff");

    static {
        FOLDER_NOACCESS_ICON.setSize(new NSSize(16f, 16f));
        FOLDER_WRITEONLY_ICON.setSize(new NSSize(16f, 16f));
    }

    public static final String ICON_COLUMN = "ICON";
    public static final String FILENAME_COLUMN = "FILENAME";
    public static final String SIZE_COLUMN = "SIZE";
    public static final String MODIFIED_COLUMN = "MODIFIED";
    public static final String OWNER_COLUMN = "OWNER";
    public static final String PERMISSIONS_COLUMN = "PERMISSIONS";
    public static final String KIND_COLUMN = "KIND";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";

    /**
     * Container for all paths currently being listed in the background
     */
    private final List isLoadingListingInBackground = new Collection();

    protected CDBrowserController controller;

    public CDBrowserTableDataSource(CDBrowserController controller) {
        this.controller = controller;
    }

    /**
     * Must be efficient; called very frequently by the table view
     *
     * @param path The directory to fetch the childs from
     * @return The cached or newly fetched file listing of the directory
     * @pre Call from the main thread
     */
    protected AttributedList childs(final Path path) {
        // Check first if it hasn't been already requested so we don't spawn
        // a multitude of unecessary threads
        synchronized(isLoadingListingInBackground) {
            if(!isLoadingListingInBackground.contains(path)) {
                if(!path.isCached() || path.cache().get(path).attributes().isDirty()) {
                    isLoadingListingInBackground.add(path);
                    // Reloading a workdir that is not cached yet would cause the interface to freeze;
                    // Delay until path is cached in the background
                    
                    controller.background(new BackgroundAction() {
                        public void run() {
                            log.debug("childs#run");
                            path.childs();
                        }

                        public void cleanup() {
                            log.debug("childs#cleanup");
                            synchronized(isLoadingListingInBackground) {
                                isLoadingListingInBackground.remove(path);
                                if(path.isCached() && isLoadingListingInBackground.isEmpty()) {
                                    controller.reloadData(true);
                                }
                            }
                        }
                    });
                }
                else {
                    return path.childs(controller.getComparator(), controller.getFileFilter());
                }
            }
        }
        log.warn("No cached listing for " + path.getName());
        return AttributedList.EMPTY_LIST;
    }

    public int indexOf(NSView tableView, Path p) {
        return this.childs(controller.workdir()).indexOf(p);
    }

    public boolean contains(NSView tableView, Path p) {
        return this.childs(controller.workdir()).contains(p);
    }

    public void setObjectValueForItem(final Path item, final Object value, final String identifier) {
        log.debug("setObjectValueForItem:" + item);
        if(identifier.equals(FILENAME_COLUMN)) {
            if(!item.getName().equals(value) && !value.equals("")) {
                final Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                        item.getParent().getAbsolute(), value.toString());
                controller.renamePath(item, renamed);
            }
        }
    }

    protected NSImage iconForPath(final Path item) {
        NSImage icon = null;
        if(item.attributes.isDirectory()) {
            if(Preferences.instance().getBoolean("browser.markInaccessibleFolders")) {
                if(!item.attributes.isExecutable()
                        || (item.isCached() && !this.childs(item).attributes().isReadable())) {
                    icon = FOLDER_NOACCESS_ICON;
                }
                else if(!item.attributes.isReadable()) {
                    if(item.attributes.isWritable()) {
                        icon = FOLDER_WRITEONLY_ICON;
                    }
                }
            }
        }
        if(null == icon) {
            icon = CDIconCache.instance().iconForPath(item);
        }
        return icon;
    }

    private static final NSAttributedString UNKNOWN_STRING = new NSAttributedString(
            NSBundle.localizedString("Unknown", ""),
            CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);

    protected Object objectValueForItem(Path item, String identifier) {
        if(null != item) {
            if(identifier.equals(ICON_COLUMN)) {
                return this.iconForPath(item);
            }
            if(identifier.equals(FILENAME_COLUMN)) {
                return new NSAttributedString(item.getName(),
                        CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT);
            }
            if(identifier.equals(TYPEAHEAD_COLUMN)) {
                return item.getName();
            }
            if(identifier.equals(SIZE_COLUMN)) {
                return new NSAttributedString(Status.getSizeAsString(item.attributes.getSize()),
                        CDTableCell.PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT);
            }
            if(identifier.equals(MODIFIED_COLUMN)) {
                if(item.attributes.getModificationDate() != -1) {
                    return new NSAttributedString(CDDateFormatter.getShortFormat(item.attributes.getModificationDate(),
                            item.getHost().getTimezone()),
                            CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT);
                }
                return UNKNOWN_STRING;
            }
            if(identifier.equals(OWNER_COLUMN)) {
                return new NSAttributedString(item.attributes.getOwner(),
                        CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT);
            }
            if(identifier.equals(PERMISSIONS_COLUMN)) {
                Permission permission = item.attributes.getPermission();
                if(null == permission) {
                    return UNKNOWN_STRING;
                }
                return new NSAttributedString(permission.toString(),
                        CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT);
            }
            if(identifier.equals(KIND_COLUMN)) {
                return new NSAttributedString(item.kind(),
                        CDTableCell.PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT);
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        log.warn("objectValueForItem:" + item + "," + identifier);
        return null;
    }

    /**
     * Sets whether the use of modifier keys should have an effect on the type of operation performed.
     *
     * @return
     * @see NSDraggingSource
     */
    public boolean ignoreModifierKeysWhileDragging() {
        // If this method is not implemented or returns false,
        // the user can tailor the drag operation by holding down a modifier key during the drag.
        return false;
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
        return NSDraggingInfo.DragOperationCopy;
    }

    public boolean acceptDrop(NSTableView view, final Path destination, NSDraggingInfo info) {
        log.debug("acceptDrop:" + destination);
        if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            Object o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
            // A file drag has been received by another application; upload to the dragged directory
            if(o != null) {
                NSArray elements = (NSArray) o;
                final Session session = controller.getTransferSession();
                final List roots = new Collection();
                for(int i = 0; i < elements.count(); i++) {
                    Path p = PathFactory.createPath(session,
                            destination.getAbsolute(),
                            new Local((String) elements.objectAtIndex(i)));
                    roots.add(p);
                }
                final Transfer q = new UploadTransfer(roots);
                if(q.numberOfRoots() > 0) {
                    controller.transfer(q, destination);
                }
            }
            return true;
        }
        if(NSPasteboard.pasteboardWithName("QueuePBoard").availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            Object o = NSPasteboard.pasteboardWithName("QueuePBoard").propertyListForType("QueuePBoardType");
            if(o != null) {
                // A file dragged within the browser has been received
                final NSArray elements = (NSArray) o;
                if((info.draggingSourceOperationMask() & NSDraggingInfo.DragOperationMove)
                        == NSDraggingInfo.DragOperationMove) {
                    // The file should be renamed
                    final Map files = new HashMap();
                    for(int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        Transfer q = TransferFactory.create(dict);
                        for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                            Path original = PathFactory.createPath(controller.workdir().getSession(),
                                    ((Path) iter.next()).getAbsolute());
                            Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                                    destination.getAbsolute(), original.getName());
                            files.put(original, renamed);
                        }
                    }
                    controller.renamePaths(files);
                }
                if(info.draggingSourceOperationMask() == NSDraggingInfo.DragOperationCopy) {
                    // The file should be duplicated
                    final Map files = new HashMap();
                    for(int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        Transfer q = TransferFactory.create(dict, controller.getSession());
                        for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                            final Path source = (Path) iter.next();
                            final Path copy = (Path) source.clone();
                            copy.setPath(destination.getAbsolute(), source.getName());
                            files.put(source, copy);
                        }
                    }
                    controller.duplicatePaths(files, false);
                }
                NSPasteboard.pasteboardWithName("QueuePBoard").setPropertyListForType(null, "QueuePBoardType");
                return true;
            }
        }
        return false;

    }

    public int validateDrop(NSTableView view, Path destination, int row, NSDraggingInfo info) {
        if(controller.isMounted()) {
            if(info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                if(destination.equals(controller.workdir())) {
                    view.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
                if(destination.attributes.isDirectory()) {
                    view.setDropRowAndDropOperation(row, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
            }
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            if(pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                Object o = pboard.propertyListForType("QueuePBoardType");
                if(o != null) {
                    NSArray elements = (NSArray) o;
                    for(int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        Transfer q = TransferFactory.create(dict);
                        for(Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                            Path item = (Path) iter.next();
                            if(!item.getSession().equals(this.controller.getSession())) {
                                // Don't allow dragging between two browser windows if not connected
                                // to the same server using the same protocol
                                return NSDraggingInfo.DragOperationNone;
                            }
                            if(destination.equals(item)) {
                                // Do not allow dragging onto myself
                                return NSDraggingInfo.DragOperationNone;
                            }
                            if(item.attributes.isDirectory() && destination.isChild(item)) {
                                // Do not allow dragging a directory into its own containing items
                                return NSDraggingInfo.DragOperationNone;
                            }
                            if(item.getParent().equals(destination)) {
                                // Moving to the same destination makes no sense
                                return NSDraggingInfo.DragOperationNone;
                            }
                        }
                    }
                    log.debug("Operation Mask:" + info.draggingSourceOperationMask());
                    if(destination.equals(controller.workdir())) {
                        view.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                        if(info.draggingSourceOperationMask() == NSDraggingInfo.DragOperationCopy) {
                            return NSDraggingInfo.DragOperationCopy;
                        }
                        return NSDraggingInfo.DragOperationMove;
                    }
                    if(destination.attributes.isDirectory()) {
                        view.setDropRowAndDropOperation(row, NSTableView.DropOn);
                        if(info.draggingSourceOperationMask() == NSDraggingInfo.DragOperationCopy) {
                            return NSDraggingInfo.DragOperationCopy;
                        }
                        return NSDraggingInfo.DragOperationMove;
                    }
                }
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    /**
     * The files dragged from the browser to the Finder
     */
    private Path[] promisedDragPaths;

    public boolean writeItemsToPasteBoard(NSTableView view, NSArray items, NSPasteboard pboard) {
        if(controller.isMounted()) {
            if(items.count() > 0) {
                this.promisedDragPaths = new Path[items.count()];
                // The fileTypes argument is the list of fileTypes being promised.
                // The array elements can consist of file extensions and HFS types encoded
                // with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory
                // of files, only include the top directory in the array.
                NSMutableArray fileTypes = new NSMutableArray();
                final List roots = new Collection();
                final Session session = controller.getTransferSession();
                for(int i = 0; i < items.count(); i++) {
                    promisedDragPaths[i] = (Path) ((Path) items.objectAtIndex(i)).clone(session);
                    if(promisedDragPaths[i].attributes.isFile()) {
                        if(promisedDragPaths[i].getExtension() != null) {
                            fileTypes.addObject(promisedDragPaths[i].getExtension());
                        }
                        else {
                            fileTypes.addObject(NSPathUtilities.FileTypeRegular);
                        }
                    }
                    else if(promisedDragPaths[i].attributes.isDirectory()) {
                        fileTypes.addObject("'fldr'");
                    }
                    else {
                        fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
                    }
                    roots.add(promisedDragPaths[i]);
                }
                final Transfer q = new DownloadTransfer(roots);

                // Writing data for private use when the item gets dragged to the transfer queue.
                NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
                if(queuePboard.setPropertyListForType(new NSArray(q.getAsDictionary()), "QueuePBoardType")) {
                    log.debug("QueuePBoardType data sucessfully written to pasteboard");
                }
                NSEvent event = NSApplication.sharedApplication().currentEvent();
                if(event != null) {
                    NSPoint dragPosition = view.convertPointFromView(event.locationInWindow(), null);
                    NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
                    view.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
                    // @see http://www.cocoabuilder.com/archive/message/cocoa/2003/5/15/81424
                    return true;
                }
            }
        }
        return false;
    }

    //see http://www.cocoabuilder.com/archive/message/2005/10/5/118857
    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
        log.debug("finishedDraggingImage:" + operation);
        this.promisedDragPaths = null;
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     *         This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     *         Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     *         you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     *         blocking the destination application.
     */
    public NSArray namesOfPromisedFilesDroppedAtDestination(final URL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        NSMutableArray promisedDragNames = new NSMutableArray();
        try {
            if(null != dropDestination) {
                final String d = java.net.URLDecoder.decode(dropDestination.getFile().replaceAll("\\+", "%2B"), "UTF-8");
                for(int i = 0; i < this.promisedDragPaths.length; i++) {
                    this.promisedDragPaths[i].setLocal(new Local(d,
                            this.promisedDragPaths[i].getName()));
                    promisedDragNames.addObject(this.promisedDragPaths[i].getName());
                }
                if(d.indexOf(NSPathUtilities.stringByExpandingTildeInPath("~/.Trash")) != -1) {
                    for(int i = 0; i < promisedDragPaths.length; i++) {
                        controller.deletePaths(Arrays.asList(promisedDragPaths));
                    }
                    promisedDragNames.removeAllObjects();
                    return promisedDragNames;
                }
            }
            if(this.promisedDragPaths.length == 1) {
                if(this.promisedDragPaths[0].attributes.isFile()) {
                    this.promisedDragPaths[0].getLocal().createNewFile();
                }
                if(this.promisedDragPaths[0].attributes.isDirectory()) {
                    this.promisedDragPaths[0].getLocal().mkdir();
                }
            }
            final List roots = new Collection();
            for(int i = 0; i < promisedDragPaths.length; i++) {
                roots.add(promisedDragPaths[i]);
            }
            final Transfer q = new DownloadTransfer(roots);
            if(q.numberOfRoots() > 0) {
                controller.transfer(q);
            }
        }
        catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        return promisedDragNames;
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + this.toString());
        super.finalize();
    }
}