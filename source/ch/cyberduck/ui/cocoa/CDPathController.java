package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
* @version $Id$
 */
public class CDPathController implements Observer {
    private static Logger log = Logger.getLogger(CDPathController.class);

    private NSPopUpButton combo;
    private List items = new ArrayList();

    public CDPathController(NSPopUpButton combo) {
	log.debug("CDPathController");
	this.combo = combo;
	this.init();
    }

    private void init() {
	//this.removeAllItems();
    }

    public void finalize() throws Throwable {
	super.finalize();
	log.debug("finalize");
    }

    public NSView view() {
	return this.combo;
    }
    
    public void update(Observable o, Object arg) {
	log.debug("update:"+o+","+arg);
	if(o instanceof Session) {
	    if(arg instanceof Path) {
		Path p = (Path)arg;
		this.removeAllItems();
		// current path has index 0
		this.addItem(p);
		// root path has index numberOfItems()-1
		while(!p.isRoot()) {
		    p = p.getParent();
		    this.addItem(p);
		}
	    }
	}
    }

    public int numberOfItems() {
	return items.size();
    }

    public void selectionChanged(Object sender) {
	Path p = (Path)items.get(combo.indexOfSelectedItem());
	p.list();
    }

    public void addItem(Path p) {
	this.items.add(p);
	combo.addItem(p.getAbsolute());
	if(p.isRoot())
	    combo.itemAtIndex(combo.numberOfItems()-1).setImage(NSImage.imageNamed("disk.tiff"));
	else
	    combo.itemAtIndex(combo.numberOfItems()-1).setImage(NSImage.imageNamed("folder.tiff"));
//	combo.sizeToFit();
    }

    public Path getItem(int row) {
	return (Path)items.get(row);
    }

    public void removeAllItems() {
	this.items.clear();
	combo.removeAllItems();
    }
}
