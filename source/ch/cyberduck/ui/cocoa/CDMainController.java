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
import org.apache.log4j.Level;

public class CDMainController {
    private static Logger log = Logger.getLogger(CDMainController.class);

    static {
	org.apache.log4j.BasicConfigurator.configure();
	Logger log = Logger.getRootLogger();
	log.setLevel(Level.OFF);
//	log.setLevel(Level.WARN);
//	log.setLevel(Level.DEBUG);
//	log.setLevel(Level.INFO);
    }

    public void awakeFromNib() {
	CDBrowserController controller = new CDBrowserController();
	controller.window().makeKeyAndOrderFront(null);
	controller.connectButtonClicked(this);
    }

    private NSArray references = new NSArray();
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSPanel donationSheet; // IBOutlet
    public void setDonationSheet(NSPanel donationSheet) {
	this.donationSheet = donationSheet;
    }

    public void helpMenuClicked(Object sender) {
	NSWorkspace.sharedWorkspace().openFile("Help.rtfd", "TextEdit");
    }

    public void licenseMenuClicked(Object sender) {
	NSWorkspace.sharedWorkspace().openFile("License.rtf", "TextEdit");
    }

    public void updateMenuClicked(Object sender) {

    }
    
    public void websiteMenuClicked(Object sender) {
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("website.url")));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }
    
    public void donateMenuClicked(Object sender) {
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("donate.url")));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }

    public void feedbackMenuClicked(Object sender) {
	try {
	    NSWorkspace.sharedWorkspace().openURL(new java.net.URL(Preferences.instance().getProperty("mail")+"?subject=Cyberduck Feedback"));
	}
	catch(java.net.MalformedURLException e) {
	    e.printStackTrace();
	}
    }

    public void neverShowDonationSheetAgain(NSButton sender) {
	switch(sender.state()) {
	    case NSCell.OnState:
		Preferences.instance().setProperty("donate", "false");
		return;
	    case NSCell.OffState:
		Preferences.instance().setProperty("donate", "true");
		return;
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

    public void closeDonationSheet(NSButton sender) {
	log.debug("closeDonationSheet");
	/*
	donationSheet.close();
	switch(sender.tag()) {
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
	 */
	NSApplication.sharedApplication().endSheet(donationSheet, sender.tag());
    }


    public void preferencesMenuClicked(Object sender) {
	CDPreferencesController controller = CDPreferencesController.instance();
	controller.window().makeKeyAndOrderFront(null);
    }

    public void newDownloadMenuClicked(Object sender) {
	CDDownloadSheet controller = new CDDownloadSheet();
	controller.window().makeKeyAndOrderFront(null);
    }

    public void newBrowserMenuClicked(Object sender) {
	CDBrowserController controller = new CDBrowserController();
	this.references = references.arrayByAddingObject(controller);
	controller.window().makeKeyAndOrderFront(null);
    }

    
    // ----------------------------------------------------------
    // Application delegate methods
    // ----------------------------------------------------------

//    public void applicationDidFinishLaunching (NSNotification notification) {
        // To get service requests to go to the controller...
//        NSApplication.sharedApplication().setServicesProvider(this);
  //  }


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

	/*
	if(Integer.parseInt(Preferences.instance().getProperty("uses")) > 5 && Preferences.instance().getProperty("donate").equals("true")) {
	    if (false == NSApplication.loadNibNamed("Donate", this)) {
		log.error("Couldn't load Donate.nib");
		return NSApplication.TerminateNow;
	    }
//	    app.runModalForWindow(donationSheet);
	    NSApplication.sharedApplication().beginSheet(
						  donationSheet,//sheet
						  null, //docwindow
						  this, //modal delegate
						  new NSSelector(
		       "donationSheetDidEnd",
		       new Class[] { NSWindow.class, int.class, NSWindow.class }
		       ),// did end selector
						  null); //contextInfo
	    return NSApplication.TerminateLater;
	}
	 */
	return NSApplication.TerminateNow;
    }

    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
	return false;
    }
}
