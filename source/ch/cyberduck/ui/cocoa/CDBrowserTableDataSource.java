/*
 *  ch.cyberduck.ui.cocoa.CDListingTableDataSource.java
 *  Cyberduck
 *
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import com.sshtools.j2ssh.sftp.SftpFile;
//import java.util.Observer;
//import java.util.Observable;

import java.util.List;
import java.util.ArrayList;

//import ch.cyberduck.core.Bookmark;
//import ch.cyberduck.core.Path;

import org.apache.log4j.Logger;

public class CDBrowserTableDataSource extends NSObject {//implements Observer {//implements NSTableView.DataSource {
    private static Logger log = Logger.getLogger(CDBrowserTableDataSource.class);

    private List data;

    public CDBrowserTableDataSource() {
	super();
	this.setEntries(new ArrayList());
	log.debug("CDBrowserTableDataSource");
    }

    public void mouseUp(NSEvent event) {
	if(event.clickCount() == 2) {
	    // Double click
	    log.info("I got double click!");
	}
    }

    public void awakeFromNib() {
	// test
//	this.addEntry(new Path("/Users/dkocher/Desktop"));
        //ObserverList.instance().registerObserver((Observer)this);	
    }

    public int numberOfRowsInTableView(NSTableView tableView) {
	return data.size();
    }

    //getValue()
    public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
//	log.debug("CDListingTableDataSource.tableViewObjectValueForLocation("+row+")");
        String identifier = (String)tableColumn.identifier();
	//todo generalisieren mit cyberduck.Path
        SftpFile p = (SftpFile)data.get(row);
	if(identifier.equals("TYPE")) {
	    if(p.isFile())
		return NSImage.imageNamed("file.tiff");
	    if(p.isDirectory())
		return NSImage.imageNamed("folder.tiff");
	}
	if(identifier.equals("FILENAME"))
	    return p.getFilename();
	if(identifier.equals("SIZE"))
	    return p.getAttributes().getSize()+"";
	if(identifier.equals("MODIFIED"))
	    return p.getAttributes().getModifiedTime()+"";
	if(identifier.equals("OWNER"))
	    return p.getAttributes().getUID()+"";
	if(identifier.equals("PERMISSION"))
	    return p.getAttributes().getPermissions()+"";
	throw new IllegalArgumentException("Unknown identifier: "+identifier);
    }

    //setValue()
    public void tableViewSetObjectValueForLocation(NSTableView tableView, Object object, NSTableColumn tableColumn, int row) {
	log.debug("tableViewSetObjectValueForLocation() not implemented.");
    }
    
    //	Returns true to permit aTableView to edit the cell at rowIndex in aTableColumn, false to deny permission. The delegate can implemen this method to disallow editing of specific cells.
    public boolean tableViewShouldEditLocation( NSTableView view, NSTableColumn tableColumn, int row) {
        String identifier = (String)tableColumn.identifier();
	if(identifier.equals("FILENAME"))
	    return true;
	return false;
    }

    /* Delegate method of CDConnectedTableDataSource*/
    public void tableViewSelectionDidChange(NSNotification notification) {
	NSTableView table = (NSTableView)notification.object(); // Returns the object associated with the receiver. This is often the object that posted this notification
	//table.selectedRow();
    }
    
    public void clear() {
	this.data.clear();
    }

    public void addEntry(Object entry, int row) {
	this.data.add(row, entry);
    }

    public void addEntry(Object entry) {
	log.debug("CDListingTableDataSource.addEntry("+entry+")");
	this.data.add(entry);
    }
    
    public Object getEntry(int row) {
	log.debug("CDListingTableDataSource.getEntry("+row+")");
	return this.data.get(row);
    }

    public List getEntries() {
	return this.data;
    }

    public void setEntries(List data) {
	this.data = data;
    }

    /*
    public void update(Observable o, Object arg) {
	log.debug("CDListingTableDataSource:update");
	if(o instanceof Bookmark) {
	    if(arg.equals(Bookmark.LIST)) {
		Bookmark bookmark = (Bookmark)o;
                java.util.Iterator iterator = bookmark.getListing().iterator();
                int i = 0;
                Path p = null;
		this.files.removeAllObjects();
                while(iterator.hasNext()) {
                    p = (Path)iterator.next();
                    if(p.isVisible()) {
                        this.files.insertObjectAtIndex(p, i);
                        i++;
                    }
                }
		//update path select combobox
                //Path cwd = selected.getCurrentPath();
		//int depth = cwd.getPathDepth();
		//for(int i = 0; i <= depth; i++) {
		//    this.files.addObject(cwd.getPathFragment(i));
		//}
	    }
	    else
		throw new IllegalArgumentException("Unknown observable argument: "+arg);
	}
	else
	    throw new IllegalArgumentException("Unknown observable: "+o);
    }
     */
}
