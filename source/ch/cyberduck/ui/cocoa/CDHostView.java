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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import java.util.Observer;
import java.util.Observable;
import ch.cyberduck.ui.ObserverList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Message;

/**
* @version $Id$
 */
public class CDHostView extends NSTableView implements Observer {

    private static Logger log = Logger.getLogger(CDHostView.class);

    private CDHostTableDataSource model = new CDHostTableDataSource();

    public CDHostView() {
	super();
    }

    public CDHostView(NSRect frame) {
	super(frame);
    }

    protected CDHostView(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public Object dataSource() {
	log.debug("dataSource");
	return this.model;
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");

	this.addTableColumn(new CDStatusTableColumn());
	this.addTableColumn(new CDButtonTableColumn());
	this.setDataSource(model);
	

	ObserverList.instance().registerObserver((Observer)this);

	this.setDelegate(this);
	this.setAutoresizesAllColumnsToFit(true);
	this.model.addEntry(new Host("ftp", "hostname", 12, null));


//	if(this.tableColumnWithIdentifier("STATUS") != null)
//	    this.tableColumnWithIdentifier("STATUS").setDataCell(new NSImageCell());
//	if(this.tableColumnWithIdentifier("BUTTON") != null)
//	    this.tableColumnWithIdentifier("BUTTON").setDataCell(new CDButtonCell());
//	this.tableColumnWithIdentifier("HOST").setDataCell(new CDHostCell());
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
	return false;
    }

    public void tableViewSelectionDidChange(NSNotification notification) {
	log.debug("tableViewSelectionDidChange");
	int row = this.selectedRow();
	if(row != -1) {
	    Host h = (Host)model.getEntry(this.selectedRow());
	    h.callObservers(new Message(Message.SELECTION));
	}
    }

    public void reloadData() {
	super.reloadData();
	this.setNeedsDisplay(true);
    }    
    
    // ----------------------------------------------------------
    // Observer interface
    // ----------------------------------------------------------

    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Host) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.OPEN)) {
		    model.addEntry(o);
		    this.reloadData();
		    this.selectRow(model.indexOf(o), false);
		}
		if(msg.getTitle().equals(Message.CLOSE)) {
		    model.removeEntry(o);
		    this.reloadData();
		}
	    }
	}
    }

    class CDStatusTableColumn extends NSTableColumn {
	public CDStatusTableColumn() {
	    super();
	    log.debug("NSTableColumn");
	}

	public Object identifier() {
	    return "STATUS";
	}
	
	/**
	* Returns the NSCell object used by the NSTableView to draw values for the receiver. NSTableView
	 * always calls this method. By default, this method just calls dataCell. Subclassers can override if
	 * they need to potentially use different cells for different rows. Subclasses should expect this method to be
	 * invoked with row equal to -1 in cases where no actual row is involved but the table view needs to get
	 * some generic cell info.
	 */
	public NSCell dataCellForRow(int row) {
	    log.debug("dataCellForRow");
	    //	    return new CDImageCell(NSImage.imageNamed("reload.tiff"));
	    Host h = (Host)model.getEntry(row);
	    if(h.hasValidSession()) {
		return new NSImageCell(NSImage.imageNamed("blipBlue.tiff"));
	    }
	    return new NSImageCell(NSImage.imageNamed("blipGray.tiff"));
	}
    }
	
	
    class CDButtonTableColumn extends NSTableColumn {
	public CDButtonTableColumn() {
	    super();
	    log.debug("NSTableColumn");
	}

	public Object identifier() {
	    return "BUTTON";
	}

	/**
	    * Returns the NSCell object used by the NSTableView to draw values for the receiver. NSTableView
	 * always calls this method. By default, this method just calls dataCell. Subclassers can override if
	 * they need to potentially use different cells for different rows. Subclasses should expect this method to be
	 * invoked with row equal to -1 in cases where no actual row is involved but the table view needs to get
	 * some generic cell info.
	 */
	public NSCell dataCellForRow(int row) {
	    log.debug("dataCellForRow");
//	    return new CDImageCell(NSImage.imageNamed("reload.tiff"));
	    Host h = (Host)model.getEntry(row);
	    if(h.hasValidSession()) {
		return new CDButtonCell(NSImage.imageNamed("stop.tiff"));
	    }
	    return new CDButtonCell(NSImage.imageNamed("reload.tiff"));
	}


	class CDButtonCell extends NSButtonCell {
	    public CDButtonCell(NSImage img) {
		super();
		this.setImage(img);
		//	    this.setTransparent(true);
  //	    this.setHighlightsBy(NSButtonCell.NoCellMask);
  //	    this.setGradientType(NSButtonCell.NSGradientNone);
		this.setControlTint(NSButtonCell.ClearControlTint);
		this.setBordered(false);
		this.setTarget(this);
		this.setAction(new NSSelector("cellClicked", new Class[]{null}));
		//	    this.setDrawsBackground(false);
		log.debug("CDImageCell");
	    }

	    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);
		//	    NSPoint iconPoint = new NSPoint(cellFrame.origin().x(), cellFrame.origin().y());
  //	    this.image().setSize(new NSSize(32, 32));
  //	    this.image().compositeToPoint(iconPoint, NSImage.CompositeSourceOver);
	    }

	    public void cellClicked(NSObject sender) {
		log.debug("cellClicked");
		Host host = (Host)model.getEntry(selectedRow());
		host.closeSession();
		host.deleteObservers();
	    }
	}
    }
}



    

    /*
    class CDButtonCell extends NSButtonCell {
	public CDButtonCell() {
	    super(NSImage.imageNamed("stop.tiff"));//@todo image dependant on state
	    this.setTarget(this);
	    this.setAction(new NSSelector("selectionChanged", new Class[]{null}));
	    //	    this.setDrawsBackground(false);
	    log.debug("CDButtonCell");
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
	    super.drawInteriorWithFrameInView(cellFrame, controlView);
	}

	public void selectionChanged(NSObject sender) {
	    log.debug("selectionChanged");
	    Host host = (Host)model.getEntry(CDHostView.this.selectedRow());
	    host.closeSession();
	    host.deleteObservers();
	}
    }
     */
    
    // ----------------------------------------------------------
    // Cell class
    // ----------------------------------------------------------
    /*
    class CDHostCell extends NSCell {//NSButtonCell
//	private int MARGIN_X = 0;
	private CDHostView hostView;
	
	public CDHostCell() {
	    super();
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {

	    if(null == hostView) {
		hostView = new CDHostView(cellFrame);
		controlView.addSubview(hostView);
	    }
	    hostView.setFrame(cellFrame);
	    hostView.displayRect(cellFrame);

//	    log.debug("drawInteriorWithFrameInView");
//	    String hostName = "@replace This is a host name";
//
//	    NSImage iconImage = NSImage.imageNamed("server.tiff");
//	    log.debug(iconImage.toString());
//	    NSPoint iconPoint = new NSPoint(cellFrame.origin().x() + MARGIN_X, cellFrame.origin().y());
//	    NSSize iconSize = new NSSize(32, 32);//NSSize.ZeroSize;
//
//	    iconImage.setSize(iconSize);
//	    iconImage.compositeToPoint(iconPoint, NSImage.CompositeSourceOver);




//	    NSRect pathRect;
//	    float w = cellFrame.size().width() - (pathRect.origin().x() - cellFrame.origin().x());
//	    float h = cellFrame.size().height();
//	    float x = iconSize.width() + MARGIN_X;
//	    float y = cellFrame.origin().y();
//
//	    pathRect = new NSRect(x, y, w, h);
//		hostname.drawInRect(pathRect, null);
	}
  }
	     */
