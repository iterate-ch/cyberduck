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

import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
* @version $Id$
*/
public class CDConnectionController {
    private static Logger log = Logger.getLogger(CDConnectionController.class);

    private Host host;
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
        
    //public CDHostView hostView; // IBOutlet
  
    public CDConnectionController(Host host) {
	super();
	log.debug("CDConnectionController");
	this.host = host;
    }

    public void recycle() {
	log.debug("recycle");
	host.recycle();
    }

    public void disconnect() {
	log.debug("disconnect");
	host.closeSession();
	host.deleteObservers();
    }    

    public void connect() {
	log.debug("connect");

	//connection initiated from menu item "recent connections"
//	if(sender instanceof NSMenuItem) {
//	    log.debug("New connection from \"Recent Connections\"");
//	    NSMenuItem item = (NSMenuItem)sender;
//	    Host h = History.instance().get(item.title());
//	    h.openSession();
//	    return;
//	}

	host.openSession();
    }
}
