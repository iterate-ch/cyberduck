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

import java.util.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDBrowserOutlineViewModel extends CDBrowserTableDataSource {

    private static Logger log = Logger.getLogger(CDBrowserOutlineViewModel.class);

    private static final NSImage SYMLINK_ICON = NSImage.imageNamed("symlink.tiff");
    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder16.tiff");
    private static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    private Map content = new HashMap();

    protected List childs(Path path) {
        List childs = super.childs(path);
        //Keep a referencd to all returned items so they don't get released by the java garbage collector when
        //there is still a weak reference from the obj-c runtime
        this.content.put(path, childs);
        return childs;
    }

    public CDBrowserOutlineViewModel(CDBrowserController controller) {
        super(controller);
    }

    public void outlineViewItemDidExpand(NSNotification notification) {
        Path p = (Path) notification.userInfo().allValues().lastObject();
        p.getSession().cache().setExpanded(p.getAbsolute(), true);
    }

    public void outlineViewItemDidCollapse(NSNotification notification) {
        Path p = (Path) notification.userInfo().allValues().lastObject();
        p.getSession().cache().setExpanded(p.getAbsolute(), false);
    }

//    public Object outlineViewItemForPersistentObject(NSOutlineView view, Object o) {
//        if(controller.isMounted()) {
//            if(o instanceof NSDictionary) {
//                NSDictionary dict = (NSDictionary)o;
//                Path p = PathFactory.createPath(this.controller.workdir().getSession(), dict);
//                return p;
//            }
//        }
//        return null;
//    }
//
//    public Object outlineViewPersistentObjectForItem(NSOutlineView view, Object o) {
//        if(o instanceof Path) {
//            Path p = (Path)o;
//            return p.getAsDictionary();
//        }
//        return null;
//    }

    public boolean outlineViewShouldEditTableColumn(NSOutlineView outlineView,
                                                    NSTableColumn tableColumn, Object item) {
        return false;
    }

    public int outlineViewNumberOfChildrenOfItem(NSOutlineView outlineView, Path item) {
        if (controller.isMounted()) {
            if (null == item) {
                item = controller.workdir();
            }
            List childs = this.childs(item);
            if (childs != null) {
                return childs.size();
            }
        }
        return 0;
    }

    public boolean outlineViewIsItemExpandable(NSOutlineView outlineView, Path item) {
        if (null == item) {
            item = controller.workdir();
        }
        return item.attributes.isDirectory();
    }

    public void outlineViewWillDisplayCell(NSOutlineView outlineView, Object cell,
                                           NSTableColumn tableColumn, Path item) {
        String identifier = (String) tableColumn.identifier();
        if (identifier.equals("FILENAME")) {
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
            ((CDOutlineCell) cell).setIcon(icon);
            ((CDOutlineCell) cell).setAttributedStringValue(new NSAttributedString(item.getName(),
                    CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY));
        }
        if (cell instanceof NSTextFieldCell) {
            if (this.controller.isConnected()) {
                ((NSTextFieldCell) cell).setTextColor(NSColor.controlTextColor());
            }
            else {
                ((NSTextFieldCell) cell).setTextColor(NSColor.disabledControlTextColor());
            }
        }
    }

    /**
     * Invoked by outlineView, and returns the child item at the specified index. Children
     * of a given parent item are accessed sequentially. If item is null, this method should
     * return the appropriate child item of the root object
     */
    public Path outlineViewChildOfItem(NSOutlineView outlineView, int index, Path item) {
        if (null == item) {
            item = controller.workdir();
        }
        if (index < this.childs(item).size()) {
            return (Path) this.childs(item).get(index);
        }
        return null;
    }

    public Object outlineViewObjectValueForItem(NSOutlineView outlineView, NSTableColumn tableColumn, Path item) {
        if (null != item) {
            String identifier = (String) tableColumn.identifier();
            if (identifier.equals("FILENAME")) {
                return new NSAttributedString(item.getName(), CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("TYPEAHEAD")) {
                return item.getName();
            }
            if (identifier.equals("SIZE")) {
                return new NSAttributedString(Status.getSizeAsString(item.attributes.getSize()),
                        CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("MODIFIED")) {
                return new NSGregorianDate((double) item.attributes.getTimestamp().getTime() / 1000,
                        NSDate.DateFor1970);
            }
            if (identifier.equals("OWNER")) {
                return new NSAttributedString(item.attributes.getOwner(),
                        CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("PERMISSIONS")) {
                return new NSAttributedString(item.attributes.getPermission().toString(),
                        CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("TOOLTIP")) {
                return item.getAbsolute() + "\n"
                        + Status.getSizeAsString(item.attributes.getSize()) + "\n"
                        + item.attributes.getTimestampAsString();
            }
            throw new IllegalArgumentException("Unknown identifier: " + identifier);
        }
        return null;
    }

    /**
     * The files dragged from the browser to the Finder
     */
    private Path[] promisedDragPaths;

    // ----------------------------------------------------------
    // Drop methods
    // ----------------------------------------------------------

    public int outlineViewValidateDrop(NSOutlineView outlineView, NSDraggingInfo info, Path item, int row) {
        if (controller.isMounted()) {
            if (null == item) {
                outlineView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                return NSDraggingInfo.DragOperationCopy;
            }
            if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                if (item.equals(controller.workdir())) {
                    outlineView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
                if (item.attributes.isDirectory()) {
                    outlineView.setDropRowAndDropOperation(row, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
            }
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                if (item.equals(controller.workdir())) {
                    outlineView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationCopy;
                }
                if (item.attributes.isDirectory()) {
                    outlineView.setDropRowAndDropOperation(row, NSTableView.DropOn);
                    return NSDraggingInfo.DragOperationMove;
                }
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    public boolean outlineViewAcceptDrop(NSOutlineView outlineView, NSDraggingInfo info, Path destination, int row) {
        log.debug("tableViewAcceptDrop:row:" + row);
        if (null == destination) {
            destination = controller.workdir();
        }
        NSPasteboard infoPboard = info.draggingPasteboard();
        if (infoPboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            NSArray filesList = (NSArray) infoPboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            Queue q = new UploadQueue((Observer) controller);
            Session session = controller.workdir().getSession().copy();
            for (int i = 0; i < filesList.count(); i++) {
                log.debug(filesList.objectAtIndex(i));
                Path p = PathFactory.createPath(session,
                        destination.getAbsolute(),
                        new Local((String) filesList.objectAtIndex(i)));
                q.addRoot(p);
            }
            if (q.numberOfRoots() > 0) {
                CDQueueController.instance().startItem(q);
            }
            return true;
        }
        NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
        log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
        if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
            outlineView.deselectAll(null);
            NSArray elements = (NSArray) pboard.propertyListForType("QueuePBoardType");
            for (int i = 0; i < elements.count(); i++) {
                NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                Queue q = Queue.createQueue(dict);
                for (Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                    Path p = PathFactory.createPath(controller.workdir().getSession(), ((Path) iter.next()).getAbsolute());
                    Path tmp = p.copy(p.getSession());
                    p.rename(destination.getAbsolute() + "/" + p.getName());
                }
                destination.invalidate();
                NSRunLoop.currentRunLoop().addTimerForMode(new NSTimer(0.1f, outlineView,
							   new NSSelector("reloadData", new Class[]{}),
							   null,
							   false),
				   NSRunLoop.DefaultRunLoopMode);
            }
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------

    public boolean outlineViewWriteItemsToPasteboard(NSOutlineView outlineView, NSArray items, NSPasteboard pboard) {
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
            NSPoint dragPosition = outlineView.convertPointFromView(event.locationInWindow(), null);
            NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
            outlineView.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
        }
        // @see http://www.cocoabuilder.com/archive/message/cocoa/2003/5/15/81424
        return true;
    }

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
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     *         This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     *         Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     *         you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     *         blocking the destination application.
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
                    q.addRoot(this.promisedDragPaths[i]);
                    promisedDragNames.addObject(this.promisedDragPaths[i].getName());
                }
                catch (java.io.UnsupportedEncodingException e) {
                    log.error(e.getMessage());
                }
            }
            if (q.numberOfRoots() > 0) {
                CDQueueController.instance().startItem(q);
            }
        }
        return promisedDragNames;
    }

    public String outlineViewToolTipForCell(NSOutlineView ov, NSCell cell, NSMutableRect rect, NSTableColumn tc,
                                            Object item, NSPoint mouseLocation) {
        if (item instanceof Path) {
            return ((Path) item).getAbsolute();
        }
        return null;
    }
}