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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Favorites;

/**
* @version $Id$
 */
public class CDFavoritesView extends NSTableView implements Observer {
    private static Logger log = Logger.getLogger(CDFavoritesView.class);

    private CDFavoritesTableDataSource model = new CDFavoritesTableDataSource();

    public CDFavoritesView() {
	super();
    }

    public CDFavoritesView(NSRect frame) {
	super(frame);
    }

    protected CDFavoritesView(NSCoder decoder, long token) {
	super(decoder, token);
    }

    public Object dataSource() {
	log.debug("dataSource");
	return this.model;
    }

    protected void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }

    public void awakeFromNib() {
	this.addTableColumn(new CDFavoriteTableColumn());
	this.addTableColumn(new CDButtonTableColumn());
	this.setDataSource(model);
	this.setDelegate(this);
	this.setAutoresizesAllColumnsToFit(true);

	Favorites.instance().addObserver(this);
    }

    public void finalize() {
	this.setDelegate(null);
    }

    public void update(Observable o, Object arg) {
	if(o instanceof Favorites) {
	    if(arg instanceof Host) {
		Host h = (Host)arg;
		model.addEntry(h);
		this.reloadData();
	    }
	}
    }
    
    public void reloadData() {
	super.reloadData();
	this.setNeedsDisplay(true);
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

    class CDFavoriteTableColumn extends NSTableColumn {
	public CDFavoriteTableColumn() {
	    super();
	    log.debug("CDFavoriteTableColumn");
	}

	public Object identifier() {
	    return "HOSTNAME";
	}
    }

    class CDButtonTableColumn extends NSTableColumn {
	public CDButtonTableColumn() {
	    super();
	    log.debug("NSTableColumn");
	    this.setMinWidth(30);
	    this.setMaxWidth(30);
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
	    return new CDButtonCell(NSImage.imageNamed("reload.tiff"));
	}


	class CDButtonCell extends NSButtonCell {
	    public CDButtonCell(NSImage img) {
		super();
		this.setImage(img);
		this.setControlTint(NSButtonCell.ClearControlTint);
		this.setBordered(false);
		this.setTarget(this);
		this.setAction(new NSSelector("cellClicked", new Class[]{null}));
		log.debug("CDImageCell");
	    }

	    public void cellClicked(Object sender) {
		log.debug("cellClicked");
		Host host = (Host)model.getEntry(selectedRow());
		host.openSession();
	    }
	}
    }    
}
