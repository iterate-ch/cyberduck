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
import ch.cyberduck.core.Path.FileStatus;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

/**
* @version $Id$
 */
public class CDTransferView extends NSTableView implements Observer {
    private static Logger log = Logger.getLogger(CDTransferView.class);

    private CDTransferTableDataSource model;

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
	this.model = (CDTransferTableDataSource)this.dataSource();
	this.setDelegate(this);
	this.setTarget(this);
        this.setDoubleAction(new NSSelector("doubleClickAction", new Class[] {null}));
	this.setAutoresizesAllColumnsToFit(true);
	if(this.tableColumnWithIdentifier("PROGRESS") != null)
	    this.tableColumnWithIdentifier("PROGRESS").setDataCell(new CDProgressCell());
	if(this.tableColumnWithIdentifier("TYPE") != null)
	    this.tableColumnWithIdentifier("TYPE").setDataCell(new NSImageCell());
    }

    public void doubleClickAction(NSObject sender) {
	log.debug("doubleClickAction");
        Path p = (Path)model.getEntry(this.clickedRow());
	p.download();
    }    

    public void update(Observable o, Object arg) {
	if(o instanceof FileStatus) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.DATA))
                  //  return new JProgressBar(bookmark.status.getProgressModel());
		    this.reloadData();
	    }
	}
	if(o instanceof Path) {
	    if(arg instanceof Message) {
		Message msg = (Message)arg;
		if(msg.getTitle().equals(Message.START)) {
		    model.addEntry(o);
		    this.reloadData();
		}
		if(msg.getTitle().equals(Message.STOP)) {
		    if(Preferences.instance().getProperty("files.removeCompleted").equals("true")) {
			model.removeEntry(o);
			this.reloadData();
		    }
		}
	    }
	}
	if(o instanceof Host) {
	    if(arg instanceof java.util.List) {
		java.util.List files = (java.util.List)arg;
		java.util.Iterator i = files.iterator();
		while(i.hasNext()) {
		    //@todo remove observers if path no longer in listing >memory leak!!!
		    ((Path)i.next()).addObserver(this);
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

    class CDProgressCell extends NSCell {
	NSProgressIndicator progressBar;

	public CDProgressCell() {
	    super();
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
	    log.debug("drawInteriorWithFrameInView");
	    if (null == progressBar) {
		progressBar = new NSProgressIndicator(cellFrame);
		progressBar.setIndeterminate(true);
		progressBar.setControlSize(NSProgressIndicator.SmallControlSize);
		progressBar.setDoubleValue(0.0);
		controlView.addSubview(progressBar);
	    }
	    progressBar.setFrame(cellFrame);
	    progressBar.displayRect(cellFrame);
	}
    }
    
}
