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
import java.util.Observer;
import java.util.Observable;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.History;

public class CDRecentMenuItem extends NSMenuItem implements Observer {

    private NSMenu submenu;
    
    public CDRecentMenuItem() {
	super();
    }

    public CDRecentMenuItem(String itemName, NSSelector action, String charCode) {
	super(itemName, action, charCode);
    }

    protected CDRecentMenuItem(NSCoder decoder, long token) {
	super(decoder, token);
    }

    protected void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }


    public void awakeFromNib() {
	History.instance().addObserver(this);
	this.setSubmenu(submenu = new NSMenu());
	History.instance();
    }

    public void update(Observable o, Object arg) {
	if(o instanceof History) {
	    if(arg instanceof Host) {
		Host h = (Host)arg;
		// Adds a new item with title aString, action aSelector, and key equivalent keyEquiv to the end of the receiver. Returns the new menu item. If you do not want the menu item to have a key equivalent, keyEquiv should be an empty string and not null.
  //	    recentConnectionsMenu.addItem(h.getName(), new NSSelector("connect", new Class[] {null}), "");
		submenu.addItem(h.getName(), null, "");
	    }
	}
    }
}
