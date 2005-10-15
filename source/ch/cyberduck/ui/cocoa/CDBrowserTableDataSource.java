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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DownloadQueue;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.UploadQueue;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSDraggingInfo;
import com.apple.cocoa.application.NSEvent;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSPasteboard;
import com.apple.cocoa.application.NSTableView;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSDate;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSGregorianDate;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSPathUtilities;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSRunLoop;
import com.apple.cocoa.foundation.NSSelector;
import com.apple.cocoa.foundation.NSSize;
import com.apple.cocoa.foundation.NSTimer;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Observer;

/**
 * @version $Id$
 */
public abstract class CDBrowserTableDataSource {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    protected static final NSImage SYMLINK_ICON = NSImage.imageNamed("symlink.tiff");
    protected static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    protected static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    protected static final String TYPE_COLUMN = "TYPE";
    protected static final String FILENAME_COLUMN = "FILENAME";
    protected static final String SIZE_COLUMN = "SIZE";
    protected static final String MODIFIED_COLUMN = "MODIFIED";
    protected static final String OWNER_COLUMN = "OWNER";
    protected static final String PERMISSIONS_COLUMN = "PERMISSIONS";

    protected AttributedList childs(Path path) {
        return path.list(false, controller.getEncoding(), false,
                controller.getComparator(), controller.getFileFilter());
    }

    protected CDBrowserController controller;

    public CDBrowserTableDataSource(CDBrowserController controller) {
        this.controller = controller;
    }

    public int indexOf(NSView tableView, Path p) {
        return this.childs(controller.workdir()).indexOf(p);
    }

    public boolean contains(Path p) {
        return this.childs(controller.workdir()).contains(p);
    }


    public Object objectValueForItem(Path item, String identifier) {
        if (null != item) {
            if (identifier.equals(TYPE_COLUMN)) {
                NSImage icon;
                if (item.attributes.isSymbolicLink()) {
                    icon = SYMLINK_ICON;
                }
                else if (item.attributes.isDirectory()) {
                    icon = FOLDER_ICON;
                }
                else if (item.attributes.isFile()) {
                    icon = CDIconCache.instance().get(item.getExtension());
                }
                else {
                    icon = NOT_FOUND_ICON;
                }
                icon.setSize(new NSSize(16f, 16f));
                return icon;
            }
            if (identifier.equals(FILENAME_COLUMN)) {
                return new NSAttributedString(item.getName(), CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("TYPEAHEAD")) {
                return item.getName();
            }
            if (identifier.equals(SIZE_COLUMN)) {
                return new NSAttributedString(Status.getSizeAsString(item.attributes.getSize()),
                        CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals(MODIFIED_COLUMN)) {
                return new NSGregorianDate((double) item.attributes.getTimestamp().getTime() / 1000,
                        NSDate.DateFor1970);
            }
            if (identifier.equals(OWNER_COLUMN)) {
                return new NSAttributedString(item.attributes.getOwner(),
                        CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals(PERMISSIONS_COLUMN)) {
                return new NSAttributedString(item.attributes.getPermission().toString(),
                        CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        return null;
    }

    public void setObjectValueForItem(Path item, Object value, String identifier) {
        log.debug("setObjectValueForItem:"+item);
//        if (identifier.equals(FILENAME_COLUMN)) {
//            if(!item.getName().equals(value)) {
//                controller.renamePath(item, controller.workdir()+Path.DELIMITER+value.toString());
//            }
//        }
    }

    // ----------------------------------------------------------
    //	NSDraggingSource
    // ----------------------------------------------------------

    public boolean ignoreModifierKeysWhileDragging() {
        return false;
    }

    public int draggingSourceOperationMaskForLocal(boolean local) {
        log.debug("draggingSourceOperationMaskForLocal:" + local);
        if (local)
            return NSDraggingInfo.DragOperationMove | NSDraggingInfo.DragOperationCopy;
        return NSDraggingInfo.DragOperationCopy;
    }

    public boolean acceptDrop(NSTableView view, Path destination, NSDraggingInfo info) {
        log.debug("acceptDrop:"+destination);
        NSPasteboard infoPboard = info.draggingPasteboard();
        if (infoPboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            NSArray filesList = (NSArray) infoPboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            Queue q = new UploadQueue((Observer) controller);
            Session session = controller.workdir().getSession().copy();
            for (int i = 0; i < filesList.count(); i++) {
                Path p = PathFactory.createPath(session,
                        destination.getAbsolute(),
                        new Local((String) filesList.objectAtIndex(i)));
                q.addRoot(p);
            }
            if (q.numberOfRoots() > 0) {
                CDQueueController.instance().startItem(q);
            }
            destination.invalidate();
            return true;
        }
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
        if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            view.deselectAll(null);
            NSArray elements = (NSArray) pboard.propertyListForType("QueuePBoardType");
            for (int i = 0; i < elements.count(); i++) {
                NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                Queue q = Queue.createQueue(dict);
                for (Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                    Path p = PathFactory.createPath(controller.workdir().getSession(), ((Path) iter.next()).getAbsolute());
                    p.rename(destination.getAbsolute() +Path.DELIMITER+ p.getName());
                }
                destination.invalidate();
                NSRunLoop.currentRunLoop().addTimerForMode(new NSTimer(0.1f, controller,
                               new NSSelector("reloadData", new Class[]{}),
                               null,
                               false),
                   NSRunLoop.DefaultRunLoopMode);
            }
            return true;
        }
        return false;

    }

    public int validateDrop(NSTableView view, Path destination, int row, NSDraggingInfo info) {
        if (controller.isMounted()) {
            if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                if (null == destination) {
                    view.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
                if (destination.equals(controller.workdir())) {
                    view.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
                if (destination.attributes.isDirectory()) {
                    view.setDropRowAndDropOperation(row, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
            }
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                if (null == destination) {
                    view.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationMove;
                }
                if (destination.equals(controller.workdir())) {
                    view.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationMove;
                }
                if (destination.attributes.isDirectory()) {
                    view.setDropRowAndDropOperation(row, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationMove;
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
        if (controller.isMounted()) {
            if (items.count() > 0) {
                this.promisedDragPaths = new Path[items.count()];
                // The fileTypes argument is the list of fileTypes being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
                NSMutableArray fileTypes = new NSMutableArray();
                Queue q = new DownloadQueue();
                Session session = controller.workdir().getSession().copy();
                for (int i = 0; i < items.count(); i++) {
                    promisedDragPaths[i] = ((Path) items.objectAtIndex(i)).copy(session);
                    if (promisedDragPaths[i].attributes.isFile()) {
                        if (promisedDragPaths[i].getExtension() != null) {
                            fileTypes.addObject(promisedDragPaths[i].getExtension());
                        }
                        else {
                            fileTypes.addObject(NSPathUtilities.FileTypeRegular);
                        }
                    }
                    else if (promisedDragPaths[i].attributes.isDirectory()) {
                        fileTypes.addObject("'fldr'");
                    }
                    else {
                        fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
                    }
                    q.addRoot(promisedDragPaths[i]);
                }

                // Writing data for private use when the item gets dragged to the transfer queue.
                NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
                if (queuePboard.setPropertyListForType(new NSArray(q.getAsDictionary()), "QueuePBoardType")) {
                    log.debug("QueuePBoardType data sucessfully written to pasteboard");
                }

                NSEvent event = NSApplication.sharedApplication().currentEvent();
                NSPoint dragPosition = view.convertPointFromView(event.locationInWindow(), null);
                NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
                view.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
                // @see http://www.cocoabuilder.com/archive/message/cocoa/2003/5/15/81424
                return true;
            }
        }
        return false;
    }

    // @see http://www.cocoabuilder.com/archive/message/2005/10/5/118857
    public void finishedDraggingImage(NSImage image, NSPoint point, int operation) {
        log.debug("finishedDraggingImage:" + operation);
        NSPasteboard.pasteboardWithName(NSPasteboard.DragPboard).declareTypes(null, null);
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     * This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     * Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     * you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     * blocking the destination application.
     */
    public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        NSMutableArray promisedDragNames = new NSMutableArray();
        if (null != dropDestination) {
            Queue q = new DownloadQueue();
            for (int i = 0; i < this.promisedDragPaths.length; i++) {
                try {
                    this.promisedDragPaths[i].setLocal(new Local(java.net.URLDecoder.decode(dropDestination.getPath(), "UTF-8"),
                            this.promisedDragPaths[i].getName()));
//                    this.promisedDragPaths[i].getLocal().createNewFile();
                    q.addRoot(this.promisedDragPaths[i]);
                    promisedDragNames.addObject(this.promisedDragPaths[i].getName());
                }
                catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage());
                }
//                catch(IOException e) {
//                    log.error(e.getMessage());
//                }
            }
            if (q.numberOfRoots() > 0) {
                CDQueueController.instance().startItem(q);
            }
        }
        return promisedDragNames;
    }
}