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

import org.apache.log4j.Logger;

import java.util.Observer;
import java.util.Observable;

import java.util.List;
import java.util.ArrayList;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

/**
* @version $Id$
 */
public class CDPathPopUpButton extends NSPopUpButton implements Observer {
    private static Logger log = Logger.getLogger(CDPathPopUpButton.class);

    private List items = new ArrayList();
    
    public void update(Observable o, Object arg) {
	//	log.debug("update:"+arg);
	if(o instanceof Host) {
	    if(arg instanceof Path) {
		log.debug("update:"+arg);
		Path p = (Path)arg;
		this.removeAllItems();
		this.addItem(p);
		while(!p.isRoot()) {
		    p = p.getParent();
		    this.addItem(p);
		}
	    }
	}
    }

    public void awakeFromNib() {
	this.setTarget(this);
	this.setAction(new NSSelector("selectionChanged", new Class[]{null}));
	this.removeAllItems();
    }

    public void selectionChanged(NSObject sender) {
	Path p = (Path)items.get(this.indexOfSelectedItem());
	p.list();
    }

    public void addItem(Path p) {
	this.items.add(p);
	super.addItem(p.getAbsolute());
    }

    public CDPathPopUpButton() {
	super();
	log.debug("CDPathPopUpButton");
    }

    public CDPathPopUpButton(NSRect rect) {
	super(rect);
	log.debug("CDPathPopUpButton");
    }

    public CDPathPopUpButton( NSRect rect, boolean flag) {
	super(rect, flag);
	log.debug("CDPathPopUpButton");
    }	

    public CDPathPopUpButton(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDPathPopUpButton:decoder");
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
//	encoder.encodeObject(items);
	log.debug("encodeWithCoder");
    }    
}
