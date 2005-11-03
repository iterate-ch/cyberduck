package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 Whitney Young. All rights reserved.
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

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

import java.util.Iterator;

/**
* @version $Id$
 */
public class CDBrowserMatrix extends NSMatrix {
    private static Logger log = Logger.getLogger(CDBrowserMatrix.class);

    public CDBrowserMatrix() {
        this.init();
    }

    public CDBrowserMatrix(NSRect frameRect) {
        super(frameRect);
        this.init();
    }

    public CDBrowserMatrix(NSRect frameRect, int mode, NSCell cell, int numRows, int numColumns) {
        super(frameRect, mode, cell, numRows, numColumns);
        this.init();
    }

    public CDBrowserMatrix(NSRect frameRect, int mode, Class clazz, int numRows, int numColumns) {
        super(frameRect, mode, clazz, numRows, numColumns);
        this.init();
    }

    private void init() {
        this.registerForDraggedTypes(new NSArray(new Object[]{
            "QueuePboardType",
            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
        ));
        this.setCellSize(new NSSize(super.cellSize().width(), CDBrowserCell.HEIGHT));
    }

    public NSSize cellSize() {
        return new NSSize(super.cellSize().width(), CDBrowserCell.HEIGHT);
    }

    public NSSize intercellSpacing() {
        return NSSize.ZeroSize;
    }

    public int draggingEntered (NSDraggingInfo info) {
        return this.draggingEnteredOrUpdated(info);
    }

    public int draggingUpdated(NSDraggingInfo info) {
        return this.draggingEnteredOrUpdated(info);
    }

    private int draggingEnteredOrUpdated(NSDraggingInfo info) {
        this.deselectAllCells();
        NSPoint location = this.convertPointFromView(this.window().mouseLocationOutsideOfEventStream(),
                                                     null);
        int row = this.rowForPoint(location);
        int col = this.columnForPoint(location);
        if(this.cellAtLocation(row, col) != null) {
            CDBrowserCell cell = (CDBrowserCell)this.cellAtLocation(row, col);
            Path selected = cell.getPath();
            if(selected.attributes.isDirectory()) {
                this.selectCellAtLocation(row, col);
                return NSDraggingInfo.DragOperationCopy;
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    public void draggingEnded(NSDraggingInfo info) {
        this.deselectAllCells();
    }

    public void draggingExited(NSDraggingInfo info) {
        this.deselectAllCells();
    }

    public boolean prepareForDragOperation(NSDraggingInfo info) {
        return true;
    }

    public boolean performDragOperation(NSDraggingInfo info) {
        log.debug("performDragOperation");
        NSPoint location = this.convertPointFromView(this.window().mouseLocationOutsideOfEventStream(),
                                                     null);
        int row = this.rowForPoint(location); int col = this.columnForPoint(location);
        if(this.cellAtLocation(row, col) != null) {
            CDBrowserCell cell = (CDBrowserCell)this.cellAtLocation(row, col);
            Path selected = cell.getPath();
            NSPasteboard infoPboard = info.draggingPasteboard();
            if (infoPboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                NSArray filesList = (NSArray) infoPboard.propertyListForType(NSPasteboard.FilenamesPboardType);
                Queue q = new UploadQueue(); //todo set browser as observer
                Session session = selected.getSession().copy();
                for (int i = 0; i < filesList.count(); i++) {
                    log.debug("Filename:"+filesList.objectAtIndex(i));
                    Path p = PathFactory.createPath(session,
                                                    selected.getAbsolute(),
                                                    new Local((String) filesList.objectAtIndex(i)));
                    q.addRoot(p);
                }
                if (q.numberOfRoots() > 0) {
                    CDQueueController.instance().startItem(q);
                    return true;
                }
            }
            else {
                NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
                log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
                if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                    NSArray elements = (NSArray) pboard.propertyListForType("QueuePBoardType");// get the data from pasteboard
                    for (int i = 0; i < elements.count(); i++) {
                        NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                        if (selected.attributes.isDirectory()) {
                            Queue q = Queue.createQueue(dict);
                            for (Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                                Path p = (Path) iter.next();
//                                controller.renamePath(selected.getAbsolute()+Path.DELIMITER+p.getName());
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The files dragged from the browser to the Finder
     */
    private Path[] promisedDragPaths;

    public void mouseDown(NSEvent event) {
        if(event.clickCount() == 1) {
            NSEvent next = NSApplication.sharedApplication().nextEventMatchingMask(NSEvent.LeftMouseUpMask | NSEvent.LeftMouseDraggedMask,
                                                                                   (NSDate)NSDate.distantFuture(), //why does this declare Object?
                                                                                   NSApplication.EventTrackingRunLoopMode,
                                                                                   false); //remove event from queue
            if(next.type() == NSEvent.LeftMouseDragged) {
                NSPoint location = this.convertPointFromView(event.locationInWindow(), null);
                int row = this.rowForPoint(location);
                int col = this.columnForPoint(location);
                NSMutableArray fileTypes = new NSMutableArray();
                if(this.cellAtLocation(row, col) != null) {
                    if(this.selectedCells() != null) {
                        Queue q = new DownloadQueue();
                        Session session = null;
                        NSArray selectedCells = this.selectedCells();
                        java.util.Enumeration iterator = selectedCells.objectEnumerator();
                        this.promisedDragPaths = new Path[selectedCells.count()];
                        for (int i = 0; iterator.hasMoreElements(); i++) {
                            Path p = ((CDBrowserCell)iterator.nextElement()).getPath();
                            if(null == session) {
                                session = p.getSession().copy();
                            }
                            promisedDragPaths[i] = p.copy(session);
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

                        NSRect imageRect = new NSRect(new NSPoint(location.x() - 16, location.y() - 16),
                                new NSSize(32, 32));
                        this.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
                        return;
                    }
                }
            }
        }
        super.mouseDown(event);
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

    public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
        log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
        NSMutableArray promisedDragNames = new NSMutableArray();
        if (null != dropDestination) {
            Queue q = new DownloadQueue();
            for (int i = 0; i < this.promisedDragPaths.length; i++) {
                try {
                    this.promisedDragPaths[i].setLocal(new Local(java.net.URLDecoder.decode(dropDestination.getPath(), "UTF-8"),
                            this.promisedDragPaths[i].getName()));
                    //this.promisedDragPaths[i].getLocal().createNewFile();
                    q.addRoot(this.promisedDragPaths[i]);
                    promisedDragNames.addObject(this.promisedDragPaths[i].getName());
                }
                catch (java.io.UnsupportedEncodingException e) {
                    log.error(e.getMessage());
                }
//					catch(java.io.IOException e) {
//						log.error(e.getMessage());
//					}
            }
            if (q.numberOfRoots() > 0) {
                CDQueueController.instance().startItem(q);
            }
        }
        return promisedDragNames;
    }

    public String toolTip(NSCell cell) {
        if(cell instanceof CDBrowserCell) {
            Path p = ((CDBrowserCell)cell).getPath();
            return p.getAbsolute() + "\n"
                    + Status.getSizeAsString(p.attributes.getSize()) + "\n"
                    + p.attributes.getTimestampAsString();
        }
        return null;
    }
}