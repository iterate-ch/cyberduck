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
import java.util.List;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.ObserverList;

/**
* @version $Id$
 */
public class CDTransferView extends NSTableView implements Observer {
    private static Logger log = Logger.getLogger(CDTransferView.class);

    private CDTransferTableDataSource model = new CDTransferTableDataSource();

    public CDTransferView() {
	super();
    }

    public CDTransferView(NSRect frame) {
	super(frame);
    }

    public CDTransferView(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");

	this.setDataSource(model);

	ObserverList.instance().registerObserver((Observer)this);

	this.setDelegate(this);
	this.setTarget(this);
        this.setDoubleAction(new NSSelector("doubleClickAction", new Class[] {null}));
	this.setAutoresizesAllColumnsToFit(true);
	if(this.tableColumnWithIdentifier("PROGRESS") != null)
	    this.tableColumnWithIdentifier("PROGRESS").setDataCell(new CDProgressCell());
	if(this.tableColumnWithIdentifier("TYPE") != null)
	    this.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
	if(this.tableColumnWithIdentifier("BUTTON") != null)
	    this.tableColumnWithIdentifier("BUTTON").setDataCell(new CDButtonCell());	
    }

    public void doubleClickAction(NSObject sender) {
	log.debug("doubleClickAction");
//        Path p = (Path)model.getEntry(this.clickedRow());
//	p.download();
    }

    public void keyUp(NSEvent event) {
	log.debug("keyUp:"+event.toString());
	short key = event.keyCode();
	if(event.keyCode() == 51) { //@todo
	    Path p = (Path)model.getEntry(this.selectedRow());
	    if(p.status.isStopped()) {
		p.deleteObservers();
		model.removeEntry(p);
	    }
	}
    }

//    public void reloadData() {
//	super.reloadData();
//	this.setNeedsDisplay(true);
  //  }

    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Path.FileStatus) {
//	    log.debug("instanceof Status:"+o.toString());
	    if(arg instanceof Message) {
//		log.debug("instanceof Message:"+arg.toString());
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.DATA))
		    //  return new JProgressBar(bookmark.status.getProgressModel());
		    this.reloadData(); //@todo inefficient?
		this.setNeedsDisplay(true);
	    }
	}
	if(o instanceof Path) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.START)) {
		    model.addEntry(o);
		    this.reloadData();
		    this.setNeedsDisplay(true);
		}
		if(msg.getTitle().equals(Message.STOP)) {
		    if(Preferences.instance().getProperty("files.removeCompleted").equals("true")) {
			model.removeEntry(o);
			this.reloadData();
			this.setNeedsDisplay(true);
		    }
		}
	    }
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
    public boolean tableViewShouldEditLocation(NSTableView view, NSTableColumn tableColumn, int row) {
	return false;
    }

    public void tableViewSelectionDidChange(NSNotification notification) {
	log.debug("tableViewSelectionDidChange");
	//
    }

    class CDButtonCell extends NSButtonCell {
	public CDButtonCell() {
	    super(NSImage.imageNamed("stop.tiff"));
	    this.setTarget(this);
	    this.setAction(new NSSelector("selectionChanged", new Class[]{null}));
//	    this.setDrawsBackground(false);
	    log.debug("CDButtonCell");
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
	    super.drawInteriorWithFrameInView(cellFrame, controlView);
	    log.debug("drawInteriorWithFrameInView");
	}

	public void selectionChanged(NSObject sender) {
	    log.debug("selectionChanged");
//	    if(e.type() == NSEvent.LeftMouseDown) {
//		log.debug("NSEvent.LeftMouseDown");
//	    }
	}
    }

    class CDProgressCell extends NSCell {
	NSProgressIndicator progressBar;

	public CDProgressCell() {
	    super();
	    log.debug("CDProgressCell");
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
	    log.debug("drawInteriorWithFrameInView");
//	    if (null == progressBar) {
		progressBar = new NSProgressIndicator(cellFrame);
		progressBar.setIndeterminate(true);
		progressBar.setControlSize(NSProgressIndicator.SmallControlSize);
//		progressBar.setDoubleValue(0.0);
//	    }
	    progressBar.setFrame(cellFrame);
	    progressBar.displayRect(cellFrame);
//	    controlView.addSubview(progressBar);
	}
    }
}