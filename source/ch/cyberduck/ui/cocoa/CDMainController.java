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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.History;
import ch.cyberduck.core.Favorites;
import org.apache.log4j.Logger;

public class CDMainController {
    private static Logger log = Logger.getLogger(CDMainController.class);

    public void awakeFromNib() {
	CDBrowserController controller = new CDBrowserController();
	controller.window().makeKeyAndOrderFront(null);
	controller.connectButtonPressed(this);
    }

    private NSArray references = new NSArray();
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSPanel donationSheet; // IBOutlet
    public void setDonationSheet(NSPanel donationSheet) {
	this.donationSheet = donationSheet;
    }


    public void donateMenuPressed(Object sender) {
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("donate.url")));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }


    public void donationSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.debug("donationSheetDidEnd");
	sheet.orderOut(null);
	switch(returncode) {
	    case(NSAlertPanel.DefaultReturn):
		try {
		    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("donate.url")));
		}
		catch(java.net.MalformedURLException e) {
		    e.printStackTrace();
		}
	    case(NSAlertPanel.AlternateReturn):
		//
	}
        NSApplication.sharedApplication().replyToApplicationShouldTerminate(true);
    }

    public void closeDonationSheet(Object sender) {
	log.debug("closeDonationSheet");
	NSApplication.sharedApplication().endSheet(donationSheet, NSAlertPanel.AlternateReturn);
    }


    public void preferencesMenuPressed(Object sender) {
	CDPreferencesController controller = CDPreferencesController.instance();
	controller.window().makeKeyAndOrderFront(null);
    }

    public void newDownloadMenuPressed(Object sender) {
	/*
	CDDownloadController controller = new CDDownloadController();
	controller.window().setMenu(null);
	controller.window().center();
	controller.window().makeKeyAndOrderFront(null);
	 */
	
    }

    public void newBrowserMenuPressed(Object sender) {
	CDBrowserController controller = new CDBrowserController();
	this.references = references.arrayByAddingObject(controller);
//	controller.window().setMenu(null);
//	controller.window().center();
	controller.window().makeKeyAndOrderFront(null);
    }

    
    // ----------------------------------------------------------
    // Application delegate methods
    // ----------------------------------------------------------

    public void applicationDidFinishLaunching (NSNotification notification) {
        // To get service requests to go to the controller...
//        NSApplication.sharedApplication().setServicesProvider(this);
    }


//    public boolean applicationOpenFile (NSApplication app, String filename) {
	//
  //  }


    public int applicationShouldTerminate (NSApplication app) {
	log.debug("applicationShouldTerminate");
//	NSArray windows = app.windows();
//	java.util.Enumeration i = windows.objectEnumerator();
//	while(i.hasMoreElements()) {
//	    ((NSWindow)i.nextElement()).performClose(this);
//	}
	Preferences.instance().setProperty("uses", Integer.parseInt(Preferences.instance().getProperty("uses"))+1);
        Preferences.instance().save();
	History.instance().save();
	Favorites.instance().save();
	
//        if(Integer.parseInt(Preferences.instance().getProperty("uses")) > 5 && Preferences.instance().getProperty("donate").equals("true")) {
	if(Preferences.instance().getProperty("donate").equals("true")) {
	    if (false == NSApplication.loadNibNamed("Donate", this)) {
		log.error("Couldn't load Donate.nib");
		return NSApplication.TerminateNow;
	    }
	    //@todo don't use sheet
	    NSApplication.sharedApplication().beginSheet(
						  donationSheet,//sheet
						  null, //docwindow
						  this, //delegate
						  new NSSelector(
		       "donationSheetDidEnd",
		       new Class[] { NSWindow.class, int.class, NSWindow.class }
		       ),// did end selector
						  null); //contextInfo
	    return NSApplication.TerminateLater;
	}
	return NSApplication.TerminateNow;
}

public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
	return false;
    }
}
