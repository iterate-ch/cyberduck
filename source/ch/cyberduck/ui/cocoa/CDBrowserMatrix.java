package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 Whitney Young. All rights reserved.
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

import ch.cyberduck.core.*;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBrowserMatrix extends NSMatrix {
	private static Logger log = Logger.getLogger(CDBrowserMatrix.class);

	public CDBrowserMatrix() {
		super();
        // receive drag events from types
//        this.registerForDraggedTypes(new NSArray(new Object[]{
//            "QueuePboardType",
//            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
//            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
//																   ));
	}

	public CDBrowserMatrix(NSRect frameRect) {
		super(frameRect);
        // receive drag events from types
//        this.registerForDraggedTypes(new NSArray(new Object[]{
//            "QueuePboardType",
//            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
//            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
//																   ));
	}
	
	public CDBrowserMatrix(NSRect frameRect, int mode, NSCell cell, int numRows, int numColumns) {
		super(frameRect, mode, cell, numRows, numColumns);
        // receive drag events from types
//        this.registerForDraggedTypes(new NSArray(new Object[]{
//            "QueuePboardType",
//            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
//            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
//																   ));
	}

	public CDBrowserMatrix(NSRect frameRect, int mode, Class clazz, int numRows, int numColumns) {
		super(frameRect, mode, clazz, numRows, numColumns);
        // receive drag events from types
//        this.registerForDraggedTypes(new NSArray(new Object[]{
//            "QueuePboardType",
//            NSPasteboard.FilenamesPboardType, //accept files dragged from the Finder for uploading
//            NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as QueuePboardType
//																   ));
	}
	
	public NSSize cellSize() {
		return new NSSize(super.cellSize().width(), CDBrowserCell.HEIGHT);
	}
	
	public NSSize intercellSpacing() {
		return NSSize.ZeroSize;
	}
	
	// Dropping files onto the browser

	public int draggingEntered (NSDraggingInfo info) {
		log.debug("draggingEntered");
		return this.draggingEnteredOrUpdated(info);
	}
	
	public int draggingUpdated(NSDraggingInfo info) {
		log.debug("draggingUpdated");
		return this.draggingEnteredOrUpdated(info);
	}
		
	private int draggingEnteredOrUpdated(NSDraggingInfo info) {
		this.deselectAllCells();
		NSEvent event = NSApplication.sharedApplication().currentEvent();
		NSPoint location = this.convertPointFromView(this.window().mouseLocationOutsideOfEventStream(), 
													 null);
		int row = this.rowForPoint(location); int col = this.columnForPoint(location);
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
//			else {
//				NSPasteboard pboard = NSPasteboard.pasteboardWithName("QueuePBoard");
//				log.debug("availableTypeFromArray:QueuePBoardType: " + pboard.availableTypeFromArray(new NSArray("QueuePBoardType")));
//				if (pboard.availableTypeFromArray(new NSArray("QueuePBoardType")) != null) {
//					NSArray elements = (NSArray) pboard.propertyListForType("QueuePBoardType");
//					for (int i = 0; i < elements.count(); i++) {
//						NSDictionary dict = (NSDictionary) elements.objectAtIndex(i);
//						if (selected.attributes.isDirectory()) {
//							Queue q = Queue.createQueue(dict);
//							for (Iterator iter = q.getRoots().iterator(); iter.hasNext();) {
//								Path p = (Path) iter.next();
//								p.rename(selected.getAbsolute() + "/" + p.getName());
//							}
//							return true;
//						}
//					}
//				}
//			}
		}
		return false;
	}	
	
	// Dragging files outside of browser

	private Path promisedFile = null;
	
	public void mouseDown(NSEvent event) {
		if(event.clickCount() == 1) {
			NSEvent next = NSApplication.sharedApplication().nextEventMatchingMask(NSEvent.LeftMouseUpMask | NSEvent.LeftMouseDraggedMask, 
																				   (NSDate)NSDate.distantFuture(), //why does this declare Object?
																				   NSApplication.EventTrackingRunLoopMode, 
																				   false); //remove event from queue
			if(next.type() == 6) { //NSEvent.LeftMouseDraggedMask
				NSPoint location = this.convertPointFromView(this.window().mouseLocationOutsideOfEventStream(), 
															 null);
				int row = this.rowForPoint(location); int col = this.columnForPoint(location);
				if(this.cellAtLocation(row, col) != null) {
					CDBrowserCell cell = (CDBrowserCell)this.cellAtLocation(row, col);
					this.promisedFile = cell.getPath(); 

					NSMutableArray fileTypes = new NSMutableArray();
//					Queue q = new DownloadQueue();
					if (this.promisedFile.attributes.isFile()) {
						if (this.promisedFile.getExtension() != null) {
							fileTypes.addObject(this.promisedFile.getExtension());
						}
						else {
							fileTypes.addObject(NSPathUtilities.FileTypeRegular);
						}
					}
					else if (this.promisedFile.attributes.isDirectory()) {
						fileTypes.addObject("'fldr'");
					}
					else {
						fileTypes.addObject(NSPathUtilities.FileTypeUnknown);
					}
//					q.addRoot(this.promisedFile);
					
					// Writing data for private use when the item gets dragged to the transfer queue.
//					NSPasteboard queuePboard = NSPasteboard.pasteboardWithName("QueuePBoard");
//					queuePboard.declareTypes(new NSArray("QueuePBoardType"), null);
//					if (queuePboard.setPropertyListForType(new NSArray(q.getAsDictionary()), "QueuePBoardType")) {
//						log.debug("QueuePBoardType data sucessfully written to pasteboard");
//					}
					
					NSRect imageRect = new NSRect(new NSPoint(location.x() - 16, location.y() - 16), 
												  new NSSize(32, 32));
					this.dragPromisedFilesOfTypes(fileTypes, imageRect, this, true, event);
					return;
				}
			}
		}
		super.mouseDown(event);
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
	
	public NSArray namesOfPromisedFilesDroppedAtDestination(java.net.URL dropDestination) {
		log.debug("namesOfPromisedFilesDroppedAtDestination:" + dropDestination);
		NSMutableArray promisedDragNames = new NSMutableArray();
		if (null != dropDestination) {
			Queue q = new DownloadQueue();
			try {
				this.promisedFile.setLocal(new Local(java.net.URLDecoder.decode(dropDestination.getPath(), "UTF-8"),
											 this.promisedFile.getName()));
				q.addRoot(this.promisedFile);
				promisedDragNames.addObject(this.promisedFile.getName());
			}
			catch (java.io.UnsupportedEncodingException e) {
				log.error(e.getMessage());
			}
			if (q.numberOfRoots() > 0) {
				CDQueueController.instance().startItem(q);
			}
		}
		return promisedDragNames;
	}
}