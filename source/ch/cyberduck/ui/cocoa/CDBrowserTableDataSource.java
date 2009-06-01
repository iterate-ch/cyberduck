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
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.cocoa.NSPoint;
import org.rococoa.cocoa.NSSize;
import org.rococoa.cocoa.NSRect;
import org.rococoa.Rococoa;

import java.text.MessageFormat;
import java.util.*;

/**
 * @version $Id$
 */
public abstract class CDBrowserTableDataSource extends CDController {
    protected static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    public static final String ICON_COLUMN = "ICON";
    public static final String FILENAME_COLUMN = "FILENAME";
    public static final String SIZE_COLUMN = "SIZE";
    public static final String MODIFIED_COLUMN = "MODIFIED";
    public static final String OWNER_COLUMN = "OWNER";
    public static final String GROUP_COLUMN = "GROUP";
    public static final String PERMISSIONS_COLUMN = "PERMISSIONS";
    public static final String KIND_COLUMN = "KIND";
    // virtual column to implement keyboard selection
    protected static final String TYPEAHEAD_COLUMN = "TYPEAHEAD";
    // virtual column to implement quick look
    protected static final String LOCAL_COLUMN = "LOCAL";

    /**
     * Container for all paths currently being listed in the background
     */
    protected final List<Path> isLoadingListingInBackground
            = new Collection<Path>();

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
    protected AttributedList<Path> childs(final Path path) {
        synchronized(isLoadingListingInBackground) {
            // Check first if it hasn't been already requested so we don't spawn
            // a multitude of unecessary threads
            if(!isLoadingListingInBackground.contains(path)) {
                if(path.isCached()) {
                    return path.cache().get(path, controller.getComparator(), controller.getFileFilter());
                }
                isLoadingListingInBackground.add(path);
                // Reloading a workdir that is not cached yet would cause the interface to freeze;
                // Delay until path is cached in the background
                controller.background(new BrowserBackgroundAction(controller) {
                    public void run() {
                        path.childs();
                    }

                    public String getActivity() {
                        return MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                                path.getName());
                    }

                    public void cleanup() {
                        synchronized(isLoadingListingInBackground) {
                            isLoadingListingInBackground.remove(path);
                            if(path.isCached() && isLoadingListingInBackground.isEmpty()) {
                                if(controller.isConnected()) {
                                    controller.reloadData(true);
                                }
                            }
                        }
                    }
                });
            }
            log.warn("No cached listing for " + path.getName());
            return new AttributedList<Path>();
        }
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
            if(!item.getName().equals(value) && StringUtils.isNotBlank(value.toString())) {
                final Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                        item.getParent().getAbsolute(), value.toString(), item.attributes.getType());
                controller.renamePath(item, renamed);
            }
        }
    }

    protected NSImage iconForPath(final Path item) {
        return CDIconCache.instance().iconForPath(item, 16);
    }

    private static final NSAttributedString UNKNOWN_STRING = NSAttributedString.create(
            Locale.localizedString("Unknown", ""),
            CDTableCellAttributes.browserFontLeftAlignment());

    protected NSObject objectValueForItem(Path item, String identifier) {
        if(null != item) {
            if(identifier.equals(ICON_COLUMN)) {
                return this.iconForPath(item);
            }
            if(identifier.equals(FILENAME_COLUMN)) {
                return NSAttributedString.create(item.getName(),
                        CDTableCellAttributes.browserFontLeftAlignment());
            }
            if(identifier.equals(SIZE_COLUMN)) {
                return NSAttributedString.create(Status.getSizeAsString(item.attributes.getSize()),
                        CDTableCellAttributes.browserFontRightAlignment());
            }
            if(identifier.equals(MODIFIED_COLUMN)) {
                if(item.attributes.getModificationDate() != -1) {
                    return NSAttributedString.create(CDDateFormatter.getShortFormat(item.attributes.getModificationDate()),
                            CDTableCellAttributes.browserFontLeftAlignment());
                }
                return UNKNOWN_STRING;
            }
            if(identifier.equals(OWNER_COLUMN)) {
                return NSAttributedString.create(item.attributes.getOwner(),
                        CDTableCellAttributes.browserFontLeftAlignment());
            }
            if(identifier.equals(GROUP_COLUMN)) {
                return NSAttributedString.create(item.attributes.getGroup(),
                        CDTableCellAttributes.browserFontLeftAlignment());
            }
            if(identifier.equals(PERMISSIONS_COLUMN)) {
                Permission permission = item.attributes.getPermission();
                if(null == permission) {
                    return UNKNOWN_STRING;
                }
                return NSAttributedString.create(permission.toString(),
                        CDTableCellAttributes.browserFontLeftAlignment());
            }
            if(identifier.equals(KIND_COLUMN)) {
                return NSAttributedString.create(item.kind(),
                        CDTableCellAttributes.browserFontLeftAlignment());
            }
            if(identifier.equals(TYPEAHEAD_COLUMN)) {
                return NSString.stringWithString(item.getName());
            }
            if(identifier.equals(LOCAL_COLUMN)) {
                return NSString.stringWithString(item.getLocal().getAbsolute());
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
        return NSDraggingInfo.DragOperationCopy | NSDraggingInfo.DragOperationDelete;
    }

    public boolean acceptDrop(NSTableView view, final Path destination, NSDraggingInfo info) {
        log.debug("acceptDrop:" + destination);
        if(controller.isMounted()) {
            if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
                // A file drag has been received by another application; upload to the dragged directory
                if(o != null) {
                    NSArray elements = (NSArray) o;
                    final Session session = controller.getTransferSession();
                    final List<Path> roots = new Collection<Path>();
                    for(int i = 0; i < elements.count(); i++) {
                        Path p = PathFactory.createPath(session,
                                destination.getAbsolute(),
                                new Local(elements.objectAtIndex(i).toString()));
                        roots.add(p);
                    }
                    final Transfer q = new UploadTransfer(roots);
                    if(q.numberOfRoots() > 0) {
                        controller.transfer(q, destination);
                    }
                    return true;
                }
                return false;
            }
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = (NSArray) o;
                for(int i = 0; i < elements.count(); i++) {
                    if(Protocol.isURL(elements.objectAtIndex(i).toString())) {
                        controller.mount(Host.parse(elements.objectAtIndex(i).toString()));
                        return true;
                    }
                }
                return false;
            }
        }
        if(controller.isMounted()) {
            final NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
            if(pboard.availableTypeFromArray(NSArray.arrayWithObject(CDPasteboards.TransferPasteboardType)) != null) {
                NSObject o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);
                if(o != null) {
                    // A file dragged within the browser has been received
                    final NSArray elements = (NSArray) o;
                    if((info.draggingSourceOperationMask() & NSDraggingInfo.DragOperationMove)
                            == NSDraggingInfo.DragOperationMove) {
                        // The file should be renamed
                        final Map<Path, Path> files = new HashMap<Path, Path>();
                        for(int i = 0; i < elements.count(); i++) {
                            NSDictionary dict = Rococoa.cast(elements.objectAtIndex(i), NSDictionary.class);
                            Transfer q = TransferFactory.create(dict);
                            for(Path next : q.getRoots()) {
                                Path original = PathFactory.createPath(controller.workdir().getSession(),
                                        next.getAbsolute(), next.attributes.getType());
                                Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                                        destination.getAbsolute(), original.getName(), next.attributes.getType());
                                files.put(original, renamed);
                            }
                        }
                        controller.renamePaths(files);
                        return true;
                    }
                    if(info.draggingSourceOperationMask() == NSDraggingInfo.DragOperationCopy) {
                        // The file should be duplicated
                        final Map<Path, Path> files = new HashMap<Path, Path>();
                        for(int i = 0; i < elements.count(); i++) {
                            NSDictionary dict = Rococoa.cast(elements.objectAtIndex(i), NSDictionary.class);
                            Transfer q = TransferFactory.create(dict, controller.getSession());
                            for(final Path source : q.getRoots()) {
                                final Path copy = PathFactory.createPath(controller.getSession(), source.getAsDictionary());
                                copy.setPath(destination.getAbsolute(), source.getName());
                                files.put(source, copy);
                            }
                        }
                        controller.duplicatePaths(files, false);
                        return true;
                    }
                    pboard.setPropertyList_forType(null, CDPasteboards.TransferPasteboardType);
                    return false;
                }
                return false;
            }
        }
        return false;
    }

    public int validateDrop(NSTableView view, Path destination, int row, NSDraggingInfo info) {
        if(controller.isMounted()) {
            if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                if(destination.attributes.isDirectory()) {
                    this.setDropRowAndDropOperation(view, destination, row);
                    return NSDraggingInfo.DragOperationCopy;
                }
                return NSDraggingInfo.DragOperationNone;
            }
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count(); i++) {
                    if(Protocol.isURL(elements.objectAtIndex(i).toString())) {
                        // Passing a value of –1 for row, and NSTableViewDropOn as the operation causes the
                        // entire table view to be highlighted rather than a specific row.
                        view.setDropRow_dropOperation(-1, NSTableView.NSTableViewDropOn);
                        return NSDraggingInfo.DragOperationCopy;
                    }
                }
            }
            return NSDraggingInfo.DragOperationNone;
        }
        if(controller.isMounted()) {
            final NSPasteboard pboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
            if(pboard.availableTypeFromArray(NSArray.arrayWithObject(CDPasteboards.TransferPasteboardType)) != null) {
                NSObject o = pboard.propertyListForType(CDPasteboards.TransferPasteboardType);
                if(o != null) {
                    NSArray elements = Rococoa.cast(o, NSArray.class);
                    for(int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = Rococoa.cast(elements.objectAtIndex(i), NSDictionary.class);
                        Transfer q = TransferFactory.create(dict);
                        for(Path next : q.getRoots()) {
                            if(!next.getSession().equals(this.controller.getSession())) {
                                // Don't allow dragging between two browser windows if not connected
                                // to the same server using the same protocol
                                return NSDraggingInfo.DragOperationNone;
                            }
                            if(destination.equals(next)) {
                                // Do not allow dragging onto myself
                                return NSDraggingInfo.DragOperationNone;
                            }
                            if(next.attributes.isDirectory() && destination.isChild(next)) {
                                // Do not allow dragging a directory into its own containing items
                                return NSDraggingInfo.DragOperationNone;
                            }
                            if(next.getParent().equals(destination)) {
                                // Moving to the same destination makes no sense
                                return NSDraggingInfo.DragOperationNone;
                            }
                        }
                    }
                    log.debug("Operation Mask:" + info.draggingSourceOperationMask());
                    if(destination.attributes.isDirectory()) {
                        this.setDropRowAndDropOperation(view, destination, row);
                        if(info.draggingSourceOperationMask() == NSDraggingInfo.DragOperationCopy) {
                            return NSDraggingInfo.DragOperationCopy;
                        }
                        if(destination.isRenameSupported()) {
                            return NSDraggingInfo.DragOperationMove;
                        }
                    }
                }
                return NSDraggingInfo.DragOperationNone;
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    private void setDropRowAndDropOperation(NSTableView view, Path destination, int row) {
        if(destination.equals(controller.workdir())) {
            log.debug("setDropRowAndDropOperation:-1");
            // Passing a value of –1 for row, and NSTableViewDropOn as the operation causes the
            // entire table view to be highlighted rather than a specific row.
            view.setDropRow_dropOperation(-1, NSTableView.NSTableViewDropOn);
        }
        else if(destination.attributes.isDirectory()) {
            log.debug("setDropRowAndDropOperation:" + row);
            view.setDropRow_dropOperation(row, NSTableView.NSTableViewDropOn);
        }
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
                NSMutableArray fileTypes = NSMutableArray.arrayWithCapacity(items.count());
                final List<Path> roots = new Collection<Path>();
                final Session session = controller.getTransferSession();
                for(int i = 0; i < items.count(); i++) {
                    final Path path = controller.lookup(items.objectAtIndex(i).toString());
                    promisedDragPaths[i] = PathFactory.createPath(session, path.getAsDictionary());
                    if(promisedDragPaths[i].attributes.isFile()) {
                        if(StringUtils.isNotEmpty(promisedDragPaths[i].getExtension())) {
                            fileTypes.addObject(NSString.stringWithString(promisedDragPaths[i].getExtension()));
                        }
                        else {
                            fileTypes.addObject(NSFileManager.NSFileTypeRegular.get().value);
                        }
                    }
                    else if(promisedDragPaths[i].attributes.isDirectory()) {
                        fileTypes.addObject(NSFileManager.NSFileTypeDirectory.get().value);
                    }
                    else {
                        fileTypes.addObject(NSFileManager.NSFileTypeUnknown.get().value);
                    }
                    roots.add(promisedDragPaths[i]);
                }
                final Transfer q = new DownloadTransfer(roots);

                // Writing data for private use when the item gets dragged to the transfer queue.
                NSPasteboard transferPasteboard = NSPasteboard.pasteboardWithName(CDPasteboards.TransferPasteboard);
                transferPasteboard.declareTypes_owner(NSArray.arrayWithObject(CDPasteboards.TransferPasteboardType), null);
                if(transferPasteboard.setPropertyList_forType(NSArray.arrayWithObject(q.getAsDictionary()), CDPasteboards.TransferPasteboardType)) {
                    log.debug("TransferPasteboardType data sucessfully written to pasteboard");
                }
                NSEvent event = NSApplication.sharedApplication().currentEvent();
                if(event != null) {
                    NSPoint dragPosition = view.convertPoint_fromView(event.locationInWindow(), null);
                    NSRect imageRect = new NSRect(new NSPoint(dragPosition.x.intValue() - 16, dragPosition.y.intValue() - 16), new NSSize(32, 32));
//                    view.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
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
        if(NSDraggingInfo.DragOperationDelete == operation) {
            for(Path promisedDragPath : promisedDragPaths) {
                controller.deletePaths(Arrays.asList(promisedDragPaths));
            }
        }
        this.promisedDragPaths = null;
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     *         This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     *         Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     *         you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     *         blocking the destination application.
     */
    public NSArray namesOfPromisedFilesDroppedAtDestination(final NSURL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        NSMutableArray promisedDragNames = NSMutableArray.arrayWithCapacity(promisedDragPaths.length);
        if(null != dropDestination) {
//                final String d = java.net.URLDecoder.decode(dropDestination.getFile().replaceAll("\\+", "%2B"), "UTF-8");
            for(Path promisedDragPath : promisedDragPaths) {
                promisedDragPath.setLocal(new Local(dropDestination.path(), promisedDragPath.getName()));
                promisedDragNames.addObject(NSString.stringWithString(promisedDragPath.getName()));
            }
        }
        if(promisedDragPaths.length == 1) {
            if(promisedDragPaths[0].attributes.isFile()) {
                promisedDragPaths[0].getLocal().touch();
            }
            if(promisedDragPaths[0].attributes.isDirectory()) {
                promisedDragPaths[0].getLocal().mkdir();
            }
        }
        final List<Path> roots = new Collection<Path>();
        roots.addAll(Arrays.asList(promisedDragPaths));
        final Transfer q = new DownloadTransfer(roots);
        if(q.numberOfRoots() > 0) {
            controller.transfer(q);
        }
        return promisedDragNames;
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + this.toString());
        super.finalize();
    }
}