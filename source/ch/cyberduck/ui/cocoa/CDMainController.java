/*
 *  ch.cyberduck.ui.cocoa.CDMainController.java
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

import org.apache.log4j.Logger;

public class CDMainController extends NSObject {

    private static Logger log = Logger.getLogger(CDMainController.class);

    public CDMainController() {
	super();
	org.apache.log4j.BasicConfigurator.configure();
	log.debug("CDMainController");
    }

    public void awakeFromNib() {

    }

/*
    public int applicationShouldTerminate(NSObject sender) {
        return NSApplication.TerminateNow;
    }
*/    
    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
	return true;
    }
    
    public void donate(NSObject sender) {
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL("http://www.cyberduck.ch/donate/"));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }
}
