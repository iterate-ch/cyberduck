package ch.cyberduck.ui.cocoa;

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

import java.util.Observer;
import java.util.Observable;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Message;
import ch.cyberduck.ui.ObserverList;
import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDBrowserView extends NSTableView implements Observer {//, NSDraggingDestination {
    private static Logger log = Logger.getLogger(CDBrowserView.class);

    private static final float STRIPE_RED = (float)(237.0/255.0);
    private static final float STRIPE_GREEN = (float)(243.0/255.0);
    private static final float STRIPE_BLUE = (float)(254.0/255.0);
    
    private NSColor sStripeColor = null;

    private CDBrowserTableDataSource model = new CDBrowserTableDataSource();
    
    public CDBrowserView() {
	super();
	log.debug("CDBrowserView");
    }

    public CDBrowserView(NSRect frame) {
	super(frame);
	log.debug("CDBrowserView");
    }

    protected CDBrowserView(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDBrowserView:"+decoder+","+token);
    }
    
    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
	log.debug("encodeWithCoder");
    }


    public Object dataSource() {
	log.debug("dataSource");
	return this.model;
    }
    
    /**
	* Gets called multiple times if this class is references from multiple nib files
     */
    public void awakeFromNib() {
	log.debug("awakeFromNib");

	this.setDataSource(model);
	
	ObserverList.instance().registerObserver(this);
	// Registering for File Drops
	this.registerForDraggedTypes(new NSArray(new Object[] {NSPasteboard.FileContentsPboardType}));

//	if(null == this.model) 
//	    this.model = (CDBrowserTableDataSource)this.dataSource();
//	log.debug("CDBrowserTableDataSource:"+model);
	this.setDelegate(this);
	this.setTarget(this);
	this.setDrawsGrid(false);
	this.setAutoresizesAllColumnsToFit(true);

	//	this.setIntercellSpacing(NSSize.ZeroSize);
        this.setDoubleAction(new NSSelector("doubleClickAction", new Class[] {null}));
	//By setting the drop row to -1, the entire table is highlighted instead of just a single row.
	//this.setDropRowAndDropOperation(-1, NSTableView.DropOn);
	if(this.tableColumnWithIdentifier("TYPE") != null)
	    this.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
	//this.setIndicatorImage(NSImage.imageNamed("indicator.tiff"), this.tableColumnWithIdentifier("FILENAME"));
//	this.setIndicatorImage(this._defaultTableHeaderSortImage(), this.tableColumnWithIdentifier("FILENAME"));
    }

    public void doubleClickAction(NSObject sender) {
	log.debug("doubleClickAction");
        Path p = (Path)model.getEntry(this.clickedRow());
	if(p.isFile())
	    p.download();
	if(p.isDirectory())
	    p.list();
    }
    
    public void tableViewSelectionDidChange(NSNotification notification) {
	log.debug("tableViewSelectionDidChange");
	int row = this.selectedRow();
	if(row != -1) {
	    ((Path)model.getEntry(row)).callObservers(new Message(Message.SELECTION));
	}
    }

    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Host) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		// A new session has been opened.
		if(msg.getTitle().equals(Message.OPEN)) {
//	@todo?	    model.clear();
//		    this.reloadData();
		}
		// The host's session has been closed.
		if(msg.getTitle().equals(Message.CLOSE)) {
		    model.clear();
		    this.reloadData();
		}
		if(msg.getTitle().equals(Message.SELECTION)) {
		    model.clear();
		    this.reloadData();
		}
	    }
	    if(arg instanceof Path) {
		List cache = ((Path)arg).cache();
		java.util.Iterator i = cache.iterator();
		model.clear();
		while(i.hasNext()) {
		    model.addEntry(i.next());
		    this.reloadData();
		}
	    }
	}
    }

    public void reloadData() {
	super.reloadData();
//	this.setNeedsDisplay(true);
	this.display();
    }
    
    // ----------------------------------------------------------
    // Drawing methods
    // ----------------------------------------------------------

    public void highlightSelectionInClipRect(NSRect rect) {
	this.drawStripesInRect(rect);
	super.highlightSelectionInClipRect(rect);
    }

    /**
	* This method does the actual blue stripe drawing, filling in every other row of the table
     * with a blue background so you can follow the rows easier with your eyes.
     */
    public void drawStripesInRect(NSRect clipRect) {
	int fullRowHeight = (int)(this.rowHeight() + this.intercellSpacing().height());
	int clipBottom = (int)clipRect.maxY();
	int firstStripe = (int)(clipRect.origin().y() / fullRowHeight);
	if (firstStripe % 2 == 0)
	    firstStripe++;
	// set up first rect
	NSRect stripeRect = new NSRect(new NSPoint(clipRect.origin().x(), firstStripe * fullRowHeight), new NSSize(clipRect.size().width(), fullRowHeight));
	// set the color
	if (sStripeColor == null) {
	    sStripeColor = NSColor.colorWithCalibratedRGB(STRIPE_RED, STRIPE_GREEN, STRIPE_BLUE, 1.0f);
	}
	sStripeColor.set();
	// and draw the stripes
	while (stripeRect.origin().y() < clipBottom) {
	    NSBezierPath.fillRect(stripeRect);
	    stripeRect = new NSRect(stripeRect.x(), (float)(stripeRect.y()+fullRowHeight*2.0), stripeRect.width(), stripeRect.height());
	}
    }

    // ----------------------------------------------------------
    // Delegate methods
    // ----------------------------------------------------------    

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

    // ----------------------------------------------------------
    // Overwritten NSResponder methods
    // ----------------------------------------------------------

    /**
	* Informs the receiver that the user has released a key event specified by theEvent. NSResponder's
     * implementation simply passes this message to the next responder.
     */
//    public void keyUp(NSEvent event) {
//	log.debug("keyUp:"+event.toString());
//	int row = this.selectedRow();
//	if(row!=-1) {
//	    Path p = (Path)model.getEntry(row);
//	    //@todo enter key	if(event.keyCode() == //36 76
//	    if(event.modifierFlags() == NSEvent.CommandKeyMask) {
//		if(event.keyCode() == NSEvent.UpArrowFunctionKey)
//		    p.getParent().getParent().list(); //@todo use current directory. what if no file in current folder????
//		if(event.keyCode() == NSEvent.DownArrowFunctionKey) {
//		    if(p.isDirectory())
//			p.list();
//		    if(p.isFile())
//			p.download();
//		}
//	    }
//	}
//  }

    //    public void viewDidEndLiveResize() {
//	super.viewDidEndLiveResize();
//	this.setNeedsDisplay(true);
  //  }
    
/*
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
 */


    // ----------------------------------------------------------
    // NSDraggingDestination interface methods -OBSOLETE already implemented in superclass
    // ----------------------------------------------------------

    /**
	* Invoked when a dragged image enters the destination. Specifically, this method is invoked when the mouse
     * pointer enters the destination's bounds rectangle (if it is a view object) or its frame
     * rectangle (if it is a window object).
     */
    /*
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
     */

    /**
	* Invoked when the image is released, if the most recent draggingEntered or draggingUpdated message
     * returned an acceptable drag-operation value. Returns true if the receiver agrees to perform the drag operation
     * and false if not. Use sender to obtain details about the dragging operation.
     */
    /*
    public boolean prepareForDragOperation(NSDraggingInfo sender)  {
	log.debug("prepareForDragOperation");
	NSPasteboard pasteboard = sender.draggingPasteboard();
	if(NSPasteboard.FileContentsPboardType.equals(pasteboard.availableTypeFromArray(new NSArray(NSPasteboard.FileContentsPboardType))))
	    return true;
	return false;
    }
     */

    /**
	* Invoked after the released image has been removed from the screen and the previous prepareForDragOperation message
     * has returned true. The destination should implement this method to do the real work of importing the pasteboard data
     * represented by the image. If the destination accepts the data, it returns true; otherwise it returns false. The default is
     * to return false. Use sender to obtain details about the dragging operation.
     */
    /*
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
     */

    /**
	* Invoked when the dragging operation is complete and the previous performDragOperation returned true. The destination
     * implements this method to perform any tidying up that it needs to do, such as updating its visual representation
     * now that it has incorporated the dragged data. This message is the last message sent from sender to the destination
     * during a dragging session.
     */
    /*
    public void concludeDragOperaton(NSDraggingInfo sender) {
	log.debug("concludeDragOperaton");
	//
    }
     */
}