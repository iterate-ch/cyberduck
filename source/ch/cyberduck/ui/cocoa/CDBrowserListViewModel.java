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

import java.util.Iterator;
import java.util.Observer;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDBrowserListViewModel extends CDTableDataSource {
    private static Logger log = Logger.getLogger(CDBrowserListViewModel.class);

    public CDBrowserListViewModel(CDBrowserController controller) {
        super(controller);
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
        if (controller.isMounted())
            return this.cache(this.controller.workdir()).size();
        return 0;
    }

	public void tableViewWillDisplayCell(NSTableView tableView, Object cell, NSTableColumn tableColumn, int row) {
		if(cell instanceof NSTextFieldCell && !this.controller.isConnected()) {
			((NSTextFieldCell)cell).setTextColor(NSColor.disabledControlTextColor());
		}
	}
	
	public boolean tableViewShouldEditLocation(NSTableView tableview, NSTableColumn tableColumn, int row) {
		return false;
	}
	
//	public void tableViewSetObjectValueForLocation(NSTableView tableview, Object value, NSTableColumn tableColumn, int row) {
//        if (row < this.cache(this.controller.workdir()).size()) {
//			String identifier = (String) tableColumn.identifier();
//          Path p = (Path) this.cache(this.controller.workdir()).get(row);
//			p.rename((String)value);
//		}
//	}
															
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < this.cache(this.controller.workdir()).size()) {
            String identifier = (String) tableColumn.identifier();
            Path p = (Path) this.cache(this.controller.workdir()).get(row);
            if (identifier.equals("TYPE")) {
                NSImage icon;
                if (p.attributes.isSymbolicLink()) {
                    icon = SYMLINK_ICON;
                }
                else if (p.attributes.isDirectory()) {
                    icon = FOLDER_ICON;
                }
                else if (p.attributes.isFile()) {
                    icon = CDIconCache.instance().get(p.getExtension());
                }
                else {
                    icon = NOT_FOUND_ICON;
                }
                icon.setSize(new NSSize(16f, 16f));
                return icon;
            }
            if (identifier.equals("FILENAME")) {
                return new NSAttributedString(p.getName(), CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("TYPEAHEAD")) {
                return p.getName();
            }
            if (identifier.equals("SIZE")) {
                return new NSAttributedString(Status.getSizeAsString(p.attributes.getSize()), CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("MODIFIED")) {
                return new NSGregorianDate((double) p.attributes.getTimestamp().getTime() / 1000,
                        NSDate.DateFor1970);
            }
            if (identifier.equals("OWNER")) {
                return new NSAttributedString(p.attributes.getOwner(), CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("PERMISSIONS")) {
                return new NSAttributedString(p.attributes.getPermission().toString(), CDTableCell.TABLE_CELL_PARAGRAPH_DICTIONARY);
            }
            if (identifier.equals("TOOLTIP")) {
                return p.getAbsolute() + "\n"
                        + Status.getSizeAsString(p.attributes.getSize()) + "\n"
                        + p.attributes.getTimestampAsString();
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

    public int tableViewValidateDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        log.info("tableViewValidateDrop:row:" + row + ",operation:" + operation);
        if (controller.isMounted()) {
            if (info.draggingPasteboard().availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
                if (row != -1 && row < tableView.numberOfRows()) {
                    Path selected = (Path) this.cache(this.controller.workdir()).get(row);
                    if (selected.attributes.isDirectory()) {
                        tableView.setDropRowAndDropOperation(row, NSTableView.DropOn);
                        return NSDraggingInfo.DragOperationCopy;
                    }
                }
                tableView.setDropRowAndDropOperation(-1, NSTableView.DropOn);
                return NSDraggingInfo.DragOperationCopy;
            }
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                if (row != -1 && row < tableView.numberOfRows()) {
                    Path selected = (Path) this.cache(this.controller.workdir()).get(row);
                    if (selected.attributes.isDirectory()) {
                        tableView.setDropRowAndDropOperation(row, NSTableView.DropOn);
                        return NSDraggingInfo.DragOperationMove;
                    }
                }
            }
        }
        return NSDraggingInfo.DragOperationNone;
    }

    public boolean tableViewAcceptDrop(NSTableView tableView, NSDraggingInfo info, int row, int operation) {
        log.debug("tableViewAcceptDrop:row:" + row + ",operation:" + operation);
        NSPasteboard infoPboard = info.draggingPasteboard();
        if (infoPboard.availableTypeFromArray(new NSArray(NSPasteboard.FilenamesPboardType)) != null) {
            NSArray filesList = (NSArray) infoPboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            Queue q = new UploadQueue((Observer) controller);
            Session session = controller.workdir().getSession().copy();
            for (int i = 0; i < filesList.count(); i++) {
                log.debug(filesList.objectAtIndex(i));
                Path p = null;
                if (row != -1) {
                    p = PathFactory.createPath(session,
                            ((Path) this.cache(this.controller.workdir()).get(row)).getAbsolute(),
                            new Local((String) filesList.objectAtIndex(i)));
                }
                else {
                    p = PathFactory.createPath(session,
                            controller.workdir().getAbsolute(),
                            new Local((String) filesList.objectAtIndex(i)));
                }
                q.addRoot(p);
            }
            if (q.numberOfRoots() > 0) {
                CDQueueController.instance().startItem(q);
            }
            return true;
        }
        else if (row != -1 && row < tableView.numberOfRows()) {
            NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
            log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
            if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
                tableView.deselectAll(null);
                NSArray elements = (NSArray) pboard.propertyListForType("QueuePBoardType");// get the data from pasteboard
                for (int i = 0; i < elements.count(); i++) {
                    NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
                    Path selected = (Path) this.cache(this.controller.workdir()).get(row);
                    if (selected.attributes.isDirectory()) {
                        Queue q = Queue.createQueue(dict);
                        for (Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
                            Path p = (Path) iter.next();
							PathFactory.createPath(selected.getSession(), p.getAbsolute()).rename(selected.getAbsolute()+"/"+p.getName());
                        }
                    }
                }
				return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    // Drag methods
    // ----------------------------------------------------------

    /**
     * Invoked by tableView after it has been determined that a drag should begin, but before the drag has been started.
     * The drag image and other drag-related information will be set up and provided by the table view once this call
     * returns with true.
     *
     * @param rows is the list of row numbers that will be participating in the drag.
     * @return To refuse the drag, return false. To start a drag, return true and place the drag data onto pboard
     *         (data, owner, and so on).
     */
    public boolean tableViewWriteRowsToPasteboard(NSTableView tableView, NSArray rows, NSPasteboard pboard) {
        log.debug("tableViewWriteRowsToPasteboard:" + rows);
        if (rows.count() > 0) {
            this.promisedDragPaths = new Path[rows.count()];
            // The fileTypes argument is the list of fileTypes being promised. The array elements can consist of file extensions and HFS types encoded with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory of files, only include the top directory in the array.
            NSMutableArray fileTypes = new NSMutableArray();
            NSMutableArray queueDictionaries = new NSMutableArray();
            Queue q = new DownloadQueue();
            Session session = controller.workdir().getSession().copy();
            for (int i = 0; i < rows.count(); i++) {
                promisedDragPaths[i] = ((Path) this.cache(this.controller.workdir()).get(((Integer) rows.objectAtIndex(i)).intValue())).copy(session);
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
            NSPoint dragPosition = tableView.convertPointFromView(event.locationInWindow(), null);
            NSRect imageRect = new NSRect(new NSPoint(dragPosition.x() - 16, dragPosition.y() - 16), new NSSize(32, 32));
            tableView.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
        }
        // @see http://www.cocoabuilder.com/archive/message/cocoa/2003/5/15/81424
        return true;
    }

    // @see http://www.cocoabuilder.com/archive/message/2004/10/5/118857
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
}