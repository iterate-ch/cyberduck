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

public class CDMainController {

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSPanel donationSheet; // IBOutlet
    public void setDonationSheet(NSPanel donationSheet) {
	this.donationSheet = donationSheet;
    }

    
        // ----------------------------------------------------------
    // Application delegate methods
    // ----------------------------------------------------------

    public int applicationShouldTerminate(NSObject sender) {
	log.debug("applicationShouldTerminate");
	//@todo             NSArray windows = NSApplication.sharedApplication().windows();

	Preferences.instance().setProperty("uses", Integer.parseInt(Preferences.instance().getProperty("uses"))+1);
        Preferences.instance().save();
	History.instance().save();
	Favorites.instance().save();
        NSApplication.loadNibNamed("Donate", this);
        if(Integer.parseInt(Preferences.instance().getProperty("uses")) > 5 &&
	   Preferences.instance().getProperty("donate").equals("true")) {
	    if(Preferences.instance().getProperty("donate").equals("true")) {
		NSApplication.sharedApplication().beginSheet(
					       donationSheet,//sheet
					       mainWindow, //docwindow
					       this, //delegate
					       new NSSelector(
			   "donationSheetDidEnd",
			   new Class[] { NSWindow.class, int.class, NSWindow.class }
			   ),// did end selector
					       null); //contextInfo
		return NSApplication.TerminateLater;
	    }
	}
	return NSApplication.TerminateNow;
    }

    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
	log.debug("applicationShouldTerminateAfterLastWindowClosed");
	return true;
    }

    
}
