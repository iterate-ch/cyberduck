/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

package ch.cyberduck.ui.cocoa;

import java.util.Observer;
import java.util.Observable;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBrowserView extends NSTableView implements Observer, NSDraggingDestination {
    private static Logger log = Logger.getLogger(CDBrowserView.class);

//    public Object transferController;
    
    public CDBrowserView() {
	super();
	log.debug("CDBrowserView");
    }

    public CDBrowserView(NSRect frame) {
	super(frame);
	log.debug("CDBrowserView");
    }

    public CDBrowserView(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDBrowserView");
    }
    
    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
	log.debug("CDBrowserView");
    }
    
    public void awakeFromNib() {
	log.debug("awakeFromNib");
	this.setDelegate(this);
	this.setTarget(this);
	this.setIntercellSpacing(NSSize.ZeroSize);
        this.setDoubleAction(new NSSelector("doubleClickAction", new Class[] {null}));
	//By setting the drop row to -1, the entire table is highlighted instead of just a single row.

	//this.setDropRowAndDropOperation(-1, NSTableView.DropOn);

//	this.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());

	//	this.setIndicatorImage(NSImage.imageNamed("file.tiff"), this.tableColumnWithIdentifier("FILENAME"))

    }

    public void doubleClickAction(NSObject sender) {
	log.debug("doubleClickAction");
        CDBrowserTableDataSource browserTableDataSource = (CDBrowserTableDataSource)this.dataSource();
        Path p = (Path)browserTableDataSource.getEntry(this.clickedRow());
        p.list();
    }
    
    /*
    public void mouseUp(NSEvent event) {
	log.debug(event.toString());
	if(event.clickCount() == 2) { //double click
            int clickedRow = this.clickedRow();
            log.debug(""+clickedRow);
            CDBrowserTableDataSource browserTableDataSource = (CDBrowserTableDataSource)this.dataSource();
            Path p = (Path)browserTableDataSource.getEntry(clickedRow);
            p.list();
	}
    }
     */

    public void update(Observable o, Object arg) {
	log.debug("update");
	if(o instanceof Host) {
	    if(arg instanceof java.util.List) {
		java.util.List files = (java.util.List)arg;
		java.util.Iterator i = files.iterator();
		CDBrowserTableDataSource browserTableDataSource = (CDBrowserTableDataSource)this.dataSource();
		browserTableDataSource.clear();
		while(i.hasNext()) {
		    browserTableDataSource.addEntry(i.next());
		    this.reloadData();
		}
	    }
	}
    }

    public Path getWorkingPath() {
	//@todo
	return null;
    }
    

    // ----------------------------------------------------------
    // Delegate methods
    // ----------------------------------------------------------    

    /**	Informs the delegate that aTableView will display the cell at rowIndex in aTableColumn using aCell.
	* The delegate can modify the display attributes of a cell to alter the appearance of the cell.
	* Because aCell is reused for every row in aTableColumn, the delegate must set the display attributes both when drawing special cells and when drawing normal cells.
	*/
    public void tableViewWillDisplayCell(NSTableView browserTable, Object cell, NSTableColumn tableColumn, int row) {
//	log.debug("tableViewWillDisplayCell:"+row);
	String identifier = (String)tableColumn.identifier();
//	CDBrowserTableDataSource ds = (CDBrowserTableDataSource)browserTable.dataSource();
	if(identifier.equals("TYPE"))
	    tableColumn.setDataCell(new NSImageCell());
//	    tableColumn.dataCell().setImage(new NSImage());

//@todo throws null pointer fo ds ???
//        Path p = (Path)ds.tableViewObjectValueForLocation(browserTable, tableColumn, row);
	//if(identifier.equals("TYPE")) {
	//    browserTable.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());;
//	    if(p.isFile())
//		typeColumn.setDataCell(new NSImageCell(NSWorkspace.sharedWorkspace().iconForFileType(p.getExtension())));
//	    if(p.isDirectory())
//		typeColumn.setDataCell(new NSImageCell(NSImage.imageNamed("folder.tiff")));
	//}
	//else {


/*
	if(cell instanceof NSTextFieldCell) {
	    NSTextFieldCell textCell = (NSTextFieldCell)cell;
	    if ((row % 2) == 0) {
		textCell.setDrawsBackground(true);
		textCell.setBackgroundColor(NSColor.lightGrayColor());
	    }
	}
 */
    }

    /**	Returns true to permit aTableView to select the row at rowIndex, false to deny permission.
	* The delegate can implement this method to disallow selection of particular rows.
	*/
    public  boolean tableViewShouldSelectRow( NSTableView aTableView, int rowIndex) {
	return true;
    }

	
    /**	Returns true to permit aTableView to edit the cell at rowIndex in aTableColumn, false to deny permission.
	*The delegate can implemen this method to disallow editing of specific cells.
	*/
    public boolean tableViewShouldEditLocation( NSTableView view, NSTableColumn tableColumn, int row) {
        String identifier = (String)tableColumn.identifier();
//	if(identifier.equals("FILENAME"))
//	    return true;
	return false;
    }

    public void tableViewSelectionDidChange(NSNotification notification) {
	log.debug("tableViewSelectionDidChange");
	//	NSTableView table = (NSTableView)notification.object(); // Returns the object associated with the receiver. This is often the object that posted this notification
    }

    public void sort(final String columnIdentifier, final boolean ascending) {
	final int higher;
	final int lower;
	if(ascending) {
	    higher = 1;
	    lower = -1;
	}
	else {
	    higher = -1;
	    lower = 1;
	}
	if(columnIdentifier.equals("FILENAME")) {
	    Collections.sort((List)this.dataSource(),
		      new Comparator() {
			  public int compare(Object o1, Object o2) {
			      Path p1 = (Path) o1;
			      Path p2 = (Path) o2;
			      if(ascending) {
				  return p1.getName().compareTo(p2.getName());
			      }
			      else {
				  return -p1.getName().compareTo(p2.getName());
			      }
			  }
		      }
		      );
	}
    }


    // ----------------------------------------------------------
    // NSDraggingDestination interface methods
    // ----------------------------------------------------------

    /**
	* Invoked when a dragged image enters the destination. Specifically, this method is invoked when the mouse
     * pointer enters the destination's bounds rectangle (if it is a view object) or its frame
     * rectangle (if it is a window object).
     */
    public int draggingEntered(NSDraggingInfo sender) {
	log.debug("draggingEntered");
	return NSDraggingInfo.DragOperationCopy;
    }

    public int draggingUpdated(NSDraggingInfo sender) {
	log.debug("draggingUpdated");
	return NSDraggingInfo.DragOperationCopy;
    }

    public void draggingEnded(NSDraggingInfo sender) {
	log.debug("draggingEnded");
	//
    }

    public void draggingExited(NSDraggingInfo sender) {
	log.debug("draggingExited");
	//
    }

    /**
	* Invoked when the image is released, if the most recent draggingEntered or draggingUpdated message
     * returned an acceptable drag-operation value. Returns true if the receiver agrees to perform the drag operation
     * and false if not. Use sender to obtain details about the dragging operation.
     */
    public boolean prepareForDragOperation(NSDraggingInfo sender)  {
	log.debug("prepareForDragOperation");
	NSPasteboard pasteboard = sender.draggingPasteboard();
	if(NSPasteboard.FileContentsPboardType.equals(pasteboard.availableTypeFromArray(new NSArray(NSPasteboard.FileContentsPboardType))))
	    return true;
	return false;
    }

    /**
	* Invoked after the released image has been removed from the screen and the previous prepareForDragOperation message
     * has returned true. The destination should implement this method to do the real work of importing the pasteboard data
     * represented by the image. If the destination accepts the data, it returns true; otherwise it returns false. The default is
     * to return false. Use sender to obtain details about the dragging operation.
     */
    public boolean performDragOperation(NSDraggingInfo sender) {
	log.debug("performDragOperation");
	NSPasteboard pasteboard = sender.draggingPasteboard();
//	NSData data = pasteboard.dataForType(NSPasteboard.FileContentsPboardType);
//	if(data == null) {
//	    // error sheet
//	}
	NSFileWrapper wrapper = pasteboard.readFileWrapper();
	String file = wrapper.filename();
	log.debug("performDragOperation:"+file);
//	transferController.upload(file);

	
//	if(wrapper.isRegularFile())
//	if(wrapper.isDirectory())
	
	return true;
    }

    /**
	* Invoked when the dragging operation is complete and the previous performDragOperation returned true. The destination
     * implements this method to perform any tidying up that it needs to do, such as updating its visual representation
     * now that it has incorporated the dragged data. This message is the last message sent from sender to the destination
     * during a dragging session.
     */
    public void concludeDragOperaton(NSDraggingInfo sender) {
	log.debug("concludeDragOperaton");
	//
    }
}