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
import ch.cyberduck.ui.DateFormatterFactory;
import ch.cyberduck.ui.PathPasteboard;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.foundation.NSMutableArray;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSString;
import ch.cyberduck.ui.cocoa.foundation.NSURL;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;
import ch.cyberduck.ui.cocoa.odb.WatchEditor;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class BrowserTableDataSource extends ProxyController implements NSDraggingSource {
    protected static Logger log = Logger.getLogger(BrowserTableDataSource.class);

    public static final String ICON_COLUMN = "ICON";
    public static final String FILENAME_COLUMN = "FILENAME";
    public static final String SIZE_COLUMN = "SIZE";
    public static final String MODIFIED_COLUMN = "MODIFIED";
    public static final String OWNER_COLUMN = "OWNER";
    public static final String GROUP_COLUMN = "GROUP";
    public static final String PERMISSIONS_COLUMN = "PERMISSIONS";
    public static final String KIND_COLUMN = "KIND";
    public static final String EXTENSION_COLUMN = "EXTENSION";
    // virtual column to implement quick look
    protected static final String LOCAL_COLUMN = "LOCAL";

    /**
     * Container for all paths currently being listed in the background
     */
    protected final List<Path> isLoadingListingInBackground
            = new Collection<Path>();

    protected BrowserController controller;

    public BrowserTableDataSource(BrowserController controller) {
        this.controller = controller;
    }

    /**
     * Clear the view cache
     */
    protected void clear() {
        tableViewCache.clear();
    }

    @Override
    protected void invalidate() {
        this.clear();
        super.invalidate();
    }

    /**
     * Must be efficient; called very frequently by the table view
     *
     * @param path The directory to fetch the children from
     * @return The cached or newly fetched file listing of the directory
     * @pre Call from the main thread
     */
    protected AttributedList<Path> children(final Path path) {
        synchronized(isLoadingListingInBackground) {
            // Check first if it hasn't been already requested so we don't spawn
            // a multitude of unecessary threads
            if(!isLoadingListingInBackground.contains(path)) {
                if(path.isCached()) {
                    return path.cache().get(path.getReference(), controller.getComparator(), controller.getFileFilter());
                }
                isLoadingListingInBackground.add(path);
                // Reloading a workdir that is not cached yet would cause the interface to freeze;
                // Delay until path is cached in the background
                controller.background(new BrowserBackgroundAction(controller) {
                    public void run() {
                        path.children();
                    }

                    @Override
                    public String getActivity() {
                        return MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                                path.getName());
                    }

                    @Override
                    public void cleanup() {
                        synchronized(isLoadingListingInBackground) {
                            isLoadingListingInBackground.remove(path);
                            if(isLoadingListingInBackground.isEmpty()) {
                                tableViewCache.clear();
                                controller.reloadData(true);
                            }
                        }
                        super.cleanup();
                    }
                });
            }
            return path.cache().get(path.getReference(), controller.getComparator(), controller.getFileFilter());
        }
    }

    public int indexOf(NSTableView view, PathReference reference) {
        return this.children(controller.workdir()).indexOf(reference);
    }

    protected void setObjectValueForItem(final Path item, final NSObject value, final String identifier) {
        if(log.isTraceEnabled()) {
            log.trace("setObjectValueForItem:" + item.getAbsolute());
        }
        if(identifier.equals(FILENAME_COLUMN)) {
            if(StringUtils.isNotBlank(value.toString()) && !item.getName().equals(value.toString())) {
                final Path renamed = PathFactory.createPath(controller.getSession(),
                        item.getParent().getAbsolute(), value.toString(), item.attributes().getType());
                controller.renamePath(item, renamed);
            }
        }
    }

    protected NSImage iconForPath(final Path item) {
        return IconCache.instance().iconForPath(item, 16);
    }

    /**
     * Second cache because it is expensive to create proxy instances
     */
    private AttributeCache<Path> tableViewCache = new AttributeCache<Path>(
            Preferences.instance().getInteger("browser.model.cache.size")
    );

    protected NSObject objectValueForItem(Path item, String identifier) {
        if(null == item) {
            return null;
        }
        if(log.isTraceEnabled()) {
            log.trace("objectValueForItem:" + item.getAbsolute());
        }
        final NSObject cached = tableViewCache.get(item, identifier);
        if(null == cached) {
            if(identifier.equals(ICON_COLUMN)) {
                return tableViewCache.put(item, identifier, this.iconForPath(item));
            }
            if(identifier.equals(FILENAME_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        item.getDisplayName(),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(SIZE_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        Status.getSizeAsString(item.attributes().getSize()),
                        TableCellAttributes.browserFontRightAlignment()));
            }
            if(identifier.equals(MODIFIED_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        DateFormatterFactory.instance().getShortFormat(item.attributes().getModificationDate()),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(OWNER_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        item.attributes().getOwner(),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(GROUP_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        item.attributes().getGroup(),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(PERMISSIONS_COLUMN)) {
                Permission permission = item.attributes().getPermission();
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        permission.toString(),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(KIND_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        item.kind(),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(EXTENSION_COLUMN)) {
                return tableViewCache.put(item, identifier, NSAttributedString.attributedStringWithAttributes(
                        item.attributes().isFile() ? StringUtils.isNotBlank(item.getExtension()) ? item.getExtension() : Locale.localizedString("None") : Locale.localizedString("None"),
                        TableCellAttributes.browserFontLeftAlignment()));
            }
            if(identifier.equals(LOCAL_COLUMN)) {
                return tableViewCache.put(item, identifier, NSString.stringWithString(
                        item.getLocal().getAbsolute()));
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
        // If this method is not implemented or returns false, the user can tailor the drag operation by
        // holding down a modifier key during the drag.
        return false;
    }

    /**
     * @param local indicates that the candidate destination object (the window or view over which the dragged
     *              image is currently poised) is in the same application as the source, while a NO value indicates that
     *              the destination object is in a different application
     * @return A mask, created by combining the dragging operations listed in the NSDragOperation section of
     *         NSDraggingInfo protocol reference using the C bitwise OR operator.If the source does not permit
     *         any dragging operations, it should return NSDragOperationNone.
     * @see NSDraggingSource
     */
    public NSUInteger draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if(local) {
            // Move or copy within the browser
            return new NSUInteger(NSDraggingInfo.NSDragOperationMove.intValue() | NSDraggingInfo.NSDragOperationCopy.intValue());
        }
        // Copy to a thirdparty application or drag to trash to delete
        return new NSUInteger(NSDraggingInfo.NSDragOperationCopy.intValue() | NSDraggingInfo.NSDragOperationDelete.intValue());
    }

    /**
     * @param view
     * @param destination A directory or null to mount an URL
     * @param info
     * @return
     */
    public boolean acceptDrop(NSTableView view, final Path destination, NSDraggingInfo info) {
        log.debug("acceptDrop:" + destination);
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            // Mount .webloc URLs dragged to browser window
            if(o != null) {
                final NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count().intValue(); i++) {
                    if(ProtocolFactory.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                        controller.mount(Host.parse(elements.objectAtIndex(new NSUInteger(i)).toString()));
                        return true;
                    }
                }
            }
        }
        if(controller.isMounted()) {
            if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
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
            List<PathPasteboard> pasteboards = PathPasteboard.allPasteboards();
            for(PathPasteboard pasteboard : pasteboards) {
                // A file dragged within the browser has been received
                if(pasteboard.isEmpty()) {
                    continue;
                }
                // Explicit copy requested by user.
                boolean duplicate = info.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue();
                if(!pasteboard.getSession().equals(controller.getSession())) {
                    // Drag to browser windows with different session
                    duplicate = true;
                }
                if(duplicate) {
                    // The file should be duplicated
                    final Map<Path, Path> files = new HashMap<Path, Path>();
                    for(Path next : pasteboard) {
                        final Path copy = PathFactory.createPath(controller.getSession(),
                                destination.getAbsolute(), next.getName(), next.attributes().getType());
                        files.put(next, copy);
                    }
                    controller.duplicatePaths(files, false);
                }
                else {
                    // The file should be renamed
                    final Map<Path, Path> files = new HashMap<Path, Path>();
                    for(Path next : pasteboard.copy(controller.getSession())) {
                        Path renamed = PathFactory.createPath(controller.getSession(),
                                destination.getAbsolute(), next.getName(), next.attributes().getType());
                        files.put(next, renamed);
                    }
                    controller.renamePaths(files);
                }
                pasteboard.clear();
            }
            return true;
        }
        return false;
    }

    /**
     * @param view
     * @param destination A directory or null to mount an URL
     * @param row
     * @param info
     * @return
     */
    public NSUInteger validateDrop(NSTableView view, Path destination, NSInteger row, NSDraggingInfo info) {
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            // Dragging URLs to mount new session
            NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                NSArray elements = Rococoa.cast(o, NSArray.class);
                for(int i = 0; i < elements.count().intValue(); i++) {
                    // Validate if .webloc URLs dragged to browser window have a known protocol
                    if(ProtocolFactory.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                        // Passing a value of –1 for row, and NSTableViewDropOn as the operation causes the
                        // entire table view to be highlighted rather than a specific row.
                        view.setDropRow(new NSInteger(-1), NSTableView.NSTableViewDropOn);
                        return NSDraggingInfo.NSDragOperationCopy;
                    }
                    else {
                        log.warn("Protocol not supported for URL:" + elements.objectAtIndex(new NSUInteger(i)).toString());
                    }
                }
            }
            log.warn("URL dragging pasteboard is empty.");
        }
        if(controller.isMounted()) {
            if(null == destination) {
                log.warn("Dragging destination is null.");
                return NSDraggingInfo.NSDragOperationNone;
            }
            if(!controller.getSession().isCreateFileSupported(destination)) {
                // Target file system does not support creating files. Creating files is not supported
                // for example in root of cloud storage accounts.
                return NSDraggingInfo.NSDragOperationNone;
            }
            // Files dragged form other application
            if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                this.setDropRowAndDropOperation(view, destination, row);
                return NSDraggingInfo.NSDragOperationCopy;
            }
            // Files dragged from browser
            for(Path next : PathPasteboard.getPasteboard(controller.getSession())) {
                if(destination.equals(next)) {
                    // Do not allow dragging onto myself
                    return NSDraggingInfo.NSDragOperationNone;
                }
                if(next.attributes().isDirectory() && destination.isChild(next)) {
                    // Do not allow dragging a directory into its own containing items
                    return NSDraggingInfo.NSDragOperationNone;
                }
                if(next.attributes().isFile() && next.getParent().equals(destination)) {
                    // Moving a file to the same destination makes no sense
                    return NSDraggingInfo.NSDragOperationNone;
                }
            }
            log.debug("Operation Mask:" + info.draggingSourceOperationMask().intValue());
            this.setDropRowAndDropOperation(view, destination, row);
            List<PathPasteboard> pasteboards = PathPasteboard.allPasteboards();
            for(PathPasteboard pasteboard : pasteboards) {
                if(pasteboard.isEmpty()) {
                    continue;
                }
                if(pasteboard.getSession().equals(controller.getSession())) {
                    if(info.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue()) {
                        // Explicit copy requested if drag operation is already NSDragOperationCopy. User is pressing the option key.
                        return NSDraggingInfo.NSDragOperationCopy;
                    }
                    if(!controller.getSession().isRenameSupported(destination)) {
                        // Rename is not supported by the target file system
                        return NSDraggingInfo.NSDragOperationNone;
                    }
                    // Defaulting to move for same session
                    return NSDraggingInfo.NSDragOperationMove;
                }
                else {
                    // If copying between sessions is supported
                    return NSDraggingInfo.NSDragOperationCopy;
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
        else if(destination.attributes().isDirectory()) {
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
                final PathPasteboard pasteboard = PathPasteboard.getPasteboard(controller.getSession());
                for(int i = 0; i < items.count().intValue(); i++) {
                    final Path path = controller.lookup(new OutlinePathReference(items.objectAtIndex(new NSUInteger(i))));
                    if(null == path) {
                        continue;
                    }
                    if(path.attributes().isFile()) {
                        if(StringUtils.isNotEmpty(path.getExtension())) {
                            fileTypes.addObject(NSString.stringWithString(path.getExtension()));
                        }
                        else {
                            fileTypes.addObject(NSString.stringWithString(NSFileManager.NSFileTypeRegular));
                        }
                    }
                    else if(path.attributes().isDirectory()) {
                        fileTypes.addObject(NSString.stringWithString("'fldr'")); //NSFileTypeForHFSTypeCode('fldr')
                    }
                    else {
                        fileTypes.addObject(NSString.stringWithString(NSFileManager.NSFileTypeUnknown));
                    }
                    // Writing data for private use when the item gets dragged to the transfer queue.
                    pasteboard.add(path);
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
        final PathPasteboard pasteboard = PathPasteboard.getPasteboard(controller.getSession());
        if(NSDraggingInfo.NSDragOperationDelete.intValue() == operation.intValue()) {
            controller.deletePaths(pasteboard);
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
    public NSArray namesOfPromisedFilesDroppedAtDestination(final NSURL url) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + url);
        NSMutableArray promisedDragNames = NSMutableArray.array();
        if(null != url) {
            final Local destination = LocalFactory.createLocal(url.path());
            final PathPasteboard pasteboard = PathPasteboard.getPasteboard(controller.getSession());
            for(Path p : pasteboard) {
                p.setLocal(LocalFactory.createLocal(destination, p.getName()));
                // Add to returned path names
                promisedDragNames.addObject(NSString.stringWithString(p.getLocal().getName()));
            }
            if(pasteboard.size() == 1) {
                if(pasteboard.get(0).attributes().isFile()) {
                    pasteboard.get(0).getLocal().touch();
                }
                if(pasteboard.get(0).attributes().isDirectory()) {
                    pasteboard.get(0).getLocal().mkdir();
                }
            }
            final boolean dock = destination.equals(LocalFactory.createLocal("~/Library/Caches/TemporaryItems"));
            DownloadTransfer transfer = new DownloadTransfer(pasteboard.copy(controller.getTransferSession())) {
                @Override
                protected void fireDidTransferPath(Path path) {
                    if(dock) {
                        WatchEditor editor = new WatchEditor(controller, path);
                        editor.watch();
                    }
                    super.fireDidTransferPath(path);
                }
            };
            if(dock) {
                // Drag to application icon in dock.
                controller.transfer(transfer, new TransferPrompt() {
                    public TransferAction prompt() {
                        return TransferAction.ACTION_OVERWRITE;
                    }
                });
            }
            else {
                controller.transfer(transfer);
            }
            pasteboard.clear();
        }
        // Filenames
        return promisedDragNames;
    }
}