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
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSMutableArray;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.foundation.NSURL;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.*;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class CDBrowserTableDataSource extends CDController implements NSDraggingSource {
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

    private static final NSAttributedString UNKNOWN_STRING = NSAttributedString.attributedStringWithAttributes(
            Locale.localizedString("Unknown"),
            CDTableCellAttributes.browserFontLeftAlignment());

    /**
     * Container for all paths currently being listed in the background
     */
    protected final List<Path> isLoadingListingInBackground
            = new Collection<Path>();

    protected CDBrowserController controller;

    public CDBrowserTableDataSource(CDBrowserController controller) {
        this.controller = controller;
    }

    @Override
    protected void invalidate() {
        tableViewCache.clear();
        super.invalidate();
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

                    @Override
                    public String getActivity() {
                        return MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                                path.getName());
                    }

                    public void cleanup() {
                        synchronized(isLoadingListingInBackground) {
                            isLoadingListingInBackground.remove(path);
                            if(isLoadingListingInBackground.isEmpty()) {
                                if(controller.isConnected()) {
                                    tableViewCache.clear();
                                    controller.reloadData(true);
                                }
                            }
                        }
                    }
                });
            }
            return path.cache().get(path, controller.getComparator(), controller.getFileFilter());
        }
    }

    public int indexOf(NSView tableView, Path p) {
        return this.childs(controller.workdir()).indexOf(p);
    }

    public boolean contains(NSView tableView, Path p) {
        return this.childs(controller.workdir()).contains(p);
    }

    public void setObjectValueForItem(final Path item, final NSObject value, final String identifier) {
        log.debug("setObjectValueForItem:" + item);
        if(identifier.equals(FILENAME_COLUMN)) {
            if(StringUtils.isNotBlank(value.toString()) && !item.getName().equals(value.toString())) {
                final Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                        item.getParent().getAbsolute(), value.toString(), item.attributes.getType());
                controller.renamePath(item, renamed);
            }
        }
    }

    protected NSImage iconForPath(final Path item) {
        return CDIconCache.instance().iconForPath(item, 16);
    }

    /**
     * Second cache because it is expensive to create proxy instances
     */
    private AttributeCache<Path> tableViewCache = new AttributeCache<Path>(
            Preferences.instance().getInteger("browser.model.cache.size")
    );

    protected NSObject objectValueForItem(Path item, String identifier) {
        if(log.isDebugEnabled()) {
            log.debug("objectValueForItem:"+item.getAbsolute());
        }
        final NSObject cached = tableViewCache.get(item, identifier);
        if(null == cached) {
            if(identifier.equals(ICON_COLUMN)) {
                return tableViewCache.put(item, identifier, this.iconForPath(item));
            }
            if(identifier.equals(FILENAME_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(item.getName(),
                        CDTableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(SIZE_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(Status.getSizeAsString(item.attributes.getSize()),
                        CDTableCellAttributes.browserFontRightAlignment()));
            }
            if(identifier.equals(MODIFIED_COLUMN)) {
                if(item.attributes.getModificationDate() != -1) {
                    return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(CDDateFormatter.getShortFormat(item.attributes.getModificationDate()),
                            CDTableCellAttributes.browserFontLeftAlignment()));
                }
                return tableViewCache.put(item, identifier, UNKNOWN_STRING);
            }
            if(identifier.equals(OWNER_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(item.attributes.getOwner(),
                        CDTableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(GROUP_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(item.attributes.getGroup(),
                        CDTableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(PERMISSIONS_COLUMN)) {
                Permission permission = item.attributes.getPermission();
                if(null == permission) {
                    return tableViewCache.put(item, identifier, UNKNOWN_STRING);
                }
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(permission.toString(),
                        CDTableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(KIND_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(item.kind(),
                        CDTableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(TYPEAHEAD_COLUMN)) {
                return tableViewCache.put(item, identifier, NSString.stringWithString(item.getName()));
            }
            if(identifier.equals(LOCAL_COLUMN)) {
                return tableViewCache.put(item, identifier, NSString.stringWithString(item.getLocal().getAbsolute()));
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        return cached;
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
    public NSUInteger draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if(local) {
            return new NSUInteger(NSDraggingInfo.NSDragOperationMove.intValue() | NSDraggingInfo.NSDragOperationCopy.intValue());
        }
        return new NSUInteger(NSDraggingInfo.NSDragOperationCopy.intValue() | NSDraggingInfo.NSDragOperationDelete.intValue());
    }

    public boolean acceptDrop(NSTableView view, final Path destination, NSDraggingInfo draggingInfo) {
        log.debug("acceptDrop:" + destination);
        if(controller.isMounted()) {
            if(draggingInfo.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                NSObject o = draggingInfo.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
                // A file drag has been received by another application; upload to the dragged directory
                if(o != null) {
                    final NSArray elements = Rococoa.cast(o, NSArray.class);
                    final Session session = controller.getTransferSession();
                    final List<Path> roots = new Collection<Path>();
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        Path p = PathFactory.createPath(session,
                                destination.getAbsolute(),
                                LocalFactory.createLocal(elements.objectAtIndex(new NSUInteger(i)).toString()));
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
        if(draggingInfo.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = draggingInfo.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                final NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count().intValue(); i++) {
                    if(Protocol.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                        controller.mount(Host.parse(elements.objectAtIndex(new NSUInteger(i)).toString()));
                        return true;
                    }
                }
                return false;
            }
        }
        if(controller.isMounted()) {
            final PathPasteboard<NSDictionary> pasteboard = PathPasteboard.getPasteboard(controller.getSession().getHost());
            if(!pasteboard.isEmpty()) {
                // A file dragged within the browser has been received
                if((draggingInfo.draggingSourceOperationMask().intValue() & NSDraggingInfo.NSDragOperationMove.intValue())
                        == NSDraggingInfo.NSDragOperationMove.intValue()) {
                    // The file should be renamed
                    final Map<Path, Path> files = new HashMap<Path, Path>();
                    for(Path next : pasteboard.getFiles(controller.getSession())) {
                        Path original = PathFactory.createPath(controller.workdir().getSession(),
                                next.getAbsolute(), next.attributes.getType());
                        Path renamed = PathFactory.createPath(controller.workdir().getSession(),
                                destination.getAbsolute(), original.getName(), next.attributes.getType());
                        files.put(original, renamed);
                    }
                    pasteboard.clear();
                    controller.renamePaths(files);
                    return true;
                }
                if(draggingInfo.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue()) {
                    // The file should be duplicated
                    final Map<Path, Path> files = new HashMap<Path, Path>();
                    for(Path next : pasteboard.getFiles(controller.getSession())) {
                        final Path copy = PathFactory.createPath(controller.getSession(), next.getAsDictionary());
                        copy.setPath(destination.getAbsolute(), next.getName());
                        files.put(next, copy);
                    }
                    pasteboard.clear();
                    controller.duplicatePaths(files, false);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public NSUInteger validateDrop(NSTableView view, Path destination, NSInteger row, NSDraggingInfo info) {
        if(controller.isMounted()) {
            if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                if(destination.attributes.isDirectory()) {
                    this.setDropRowAndDropOperation(view, destination, row);
                    return NSDraggingInfo.NSDragOperationCopy;
                }
                return NSDraggingInfo.NSDragOperationNone;
            }
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count().intValue(); i++) {
                    if(Protocol.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                        // Passing a value of –1 for row, and NSTableViewDropOn as the operation causes the
                        // entire table view to be highlighted rather than a specific row.
                        view.setDropRow(new NSInteger(-1), NSTableView.NSTableViewDropOn);
                        return NSDraggingInfo.NSDragOperationCopy;
                    }
                }
            }
            return NSDraggingInfo.NSDragOperationNone;
        }
        if(controller.isMounted()) {
            if(PathPasteboard.getPasteboard(controller.getSession().getHost()).isEmpty()) {
                return NSDraggingInfo.NSDragOperationNone;
            }
            for(Path next : PathPasteboard.getPasteboard(controller.getSession().getHost()).getFiles(controller.getSession())) {
                if(destination.equals(next)) {
                    // Do not allow dragging onto myself
                    return NSDraggingInfo.NSDragOperationNone;
                }
                if(next.attributes.isDirectory() && destination.isChild(next)) {
                    // Do not allow dragging a directory into its own containing items
                    return NSDraggingInfo.NSDragOperationNone;
                }
                if(next.getParent().equals(destination)) {
                    // Moving to the same destination makes no sense
                    return NSDraggingInfo.NSDragOperationNone;
                }
            }
            log.debug("Operation Mask:" + info.draggingSourceOperationMask().intValue());
            if(destination.attributes.isDirectory()) {
                this.setDropRowAndDropOperation(view, destination, row);
                if(info.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue()) {
                    return NSDraggingInfo.NSDragOperationCopy;
                }
                if(destination.isRenameSupported()) {
                    return NSDraggingInfo.NSDragOperationMove;
                }
            }
        }
        return NSDraggingInfo.NSDragOperationNone;
    }

    private void setDropRowAndDropOperation(NSTableView view, Path destination, NSInteger row) {
        if(destination.equals(controller.workdir())) {
            log.debug("setDropRowAndDropOperation:-1");
            // Passing a value of –1 for row, and NSTableViewDropOn as the operation causes the
            // entire table view to be highlighted rather than a specific row.
            view.setDropRow(new NSInteger(-1), NSTableView.NSTableViewDropOn);
        }
        else if(destination.attributes.isDirectory()) {
            log.debug("setDropRowAndDropOperation:" + row.intValue());
            view.setDropRow(row, NSTableView.NSTableViewDropOn);
        }
    }

    public boolean writeItemsToPasteBoard(NSTableView view, NSArray items, NSPasteboard pboard) {
        log.debug("writeItemsToPasteBoard");
        if(controller.isMounted()) {
            if(items.count().intValue() > 0) {
                // The fileTypes argument is the list of fileTypes being promised.
                // The array elements can consist of file extensions and HFS types encoded
                // with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory
                // of files, only include the top directory in the array.
                final NSMutableArray fileTypes = NSMutableArray.array();
                final PathPasteboard<NSDictionary> pasteboard = PathPasteboard.getPasteboard(controller.getSession().getHost());
                for(int i = 0; i < items.count().intValue(); i++) {
                    final Path path = controller.lookup(new OutlinePathReference(items.objectAtIndex(new NSUInteger(i))));
                    if(null == path) {
                        continue;
                    }
                    if(path.attributes.isFile()) {
                        if(StringUtils.isNotEmpty(path.getExtension())) {
                            fileTypes.addObject(NSString.stringWithString(path.getExtension()));
                        }
                        else {
                            fileTypes.addObject(NSString.stringWithString(NSFileManager.NSFileTypeRegular));
                        }
                    }
                    else if(path.attributes.isDirectory()) {
                        fileTypes.addObject(NSString.stringWithString("'fldr'")); //NSFileTypeForHFSTypeCode('fldr')
                    }
                    else {
                        fileTypes.addObject(NSString.stringWithString(NSFileManager.NSFileTypeUnknown));
                    }
                    // Writing data for private use when the item gets dragged to the transfer queue.
                    pasteboard.add(path.<NSDictionary>getAsDictionary());
                }
                NSEvent event = NSApplication.sharedApplication().currentEvent();
                if(event != null) {
                    NSPoint dragPosition = view.convertPoint_fromView(event.locationInWindow(), null);
                    NSRect imageRect = new NSRect(new NSPoint(dragPosition.x.doubleValue() - 16, dragPosition.y.doubleValue() - 16), new NSSize(32, 32));
                    view.dragPromisedFilesOfTypes(fileTypes, imageRect, this.id(), true, event);
                    // @see http://www.cocoabuilder.com/archive/message/cocoa/2003/5/15/81424
                    return true;
                }
            }
        }
        return false;
    }

    public void draggedImage_beganAt(NSImage image, NSPoint point) {
        log.trace("draggedImage_beganAt:" + point);
    }

    /**
     * See http://www.cocoabuilder.com/archive/message/2005/10/5/118857
     */
    public void draggedImage_endedAt_operation(NSImage image, NSPoint point, NSUInteger operation) {
        log.trace("draggedImage_endedAt_operation:" + operation);
        final PathPasteboard<NSDictionary> pasteboard = PathPasteboard.getPasteboard(controller.getSession().getHost());
        if(NSDraggingInfo.NSDragOperationDelete.intValue() == operation.intValue()) {
            final List<Path> files = pasteboard.getFiles(controller.getSession());
            controller.deletePaths(files);
        }
        pasteboard.clear();
    }

    public void draggedImage_movedTo(NSImage image, NSPoint point) {
        log.trace("draggedImage_movedTo:" + point);
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
        NSMutableArray promisedDragNames = NSMutableArray.array();
        if(null != dropDestination) {
            final PathPasteboard<NSDictionary> pasteboard = PathPasteboard.getPasteboard(controller.getSession().getHost());
            final List<Path> promisedPaths = pasteboard.getFiles(controller.getTransferSession());
            for(Path p: promisedPaths) {
                p.setLocal(LocalFactory.createLocal(dropDestination.path(), p.getName()));
                // Add to returned path names
                promisedDragNames.addObject(NSString.stringWithString(p.getLocal().getName()));
            }
            if(promisedPaths.size() == 1) {
                if(promisedPaths.get(0).attributes.isFile()) {
                    promisedPaths.get(0).getLocal().touch();
                }
                if(promisedPaths.get(0).attributes.isDirectory()) {
                    promisedPaths.get(0).getLocal().mkdir();
                }
            }
            final Transfer q = new DownloadTransfer(promisedPaths);
            if(q.numberOfRoots() > 0) {
                controller.transfer(q);
            }
            pasteboard.clear();
        }
        // Filenames
        return promisedDragNames;
    }
}