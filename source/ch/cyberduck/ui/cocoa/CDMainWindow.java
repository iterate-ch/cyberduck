/*
 *  ch.cyberduck.ui.cocoa.CDMainWindow.java
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

//import java.util.Observer;
//import java.util.Observable;

//import ch.cyberduck.core.Status;
//import ch.cyberduck.ui.ObserverList;

import org.apache.log4j.Logger;

public class CDMainWindow extends NSWindow {//implements Observer {

    private static Logger log = Logger.getLogger(CDMainWindow.class);

    public CDMainWindow() {
	super();
	log.debug("CDMainWindow");
//	ObserverList.instance().registerObserver((Observer)this);
    }

    public CDMainWindow(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
	log.debug("CDMainWindow");
//	ObserverList.instance().registerObserver((Observer)this);
    }

    public CDMainWindow(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
	log.debug("CDMainWindow");
//	ObserverList.instance().registerObserver((Observer)this);
    }

    /**
      * @return The string that appears in the title bar of the receiver.
      */
    /*
    public String title() {
	return "Connected to <host>";
    }
     */
	

    public void awakeFromNib() {
	log.debug("CDMainWindow:awakeFromNib");

/*
	NSArray columns = connectedView.tableColumns();
	NSTableColumn c = columns.objectAtIndex(columns.count()-1);
	c.setDataCell(new CDServerItemView());
	    */
	this.setDelegate(this);


    }

    // ----------------------------------------------------------
    // Window delegate methods
    // ----------------------------------------------------------

    public boolean windowShouldClose(NSWindow sender) {
	NSAlertPanel.beginAlertSheet(
			      "Really quit Cyberduck now?", //title
			      "Quit",// defaultbutton
			      "Cancel",//alternative button
			      null,//other button
			      sender,//window
			      this, //delegate
			      new NSSelector
			      (
	  "quitSheetDidEnd",
	  new Class[]
	  {
	      NSWindow.class, int.class, NSWindow.class
	  }
	  ),// end selector
			      null, // dismiss selector
			      sender, // context
			      "All connections to remote servers will be closed." // message
			      );
	return false;
    }

    // ----------------------------------------------------------
    // IB action methods
    // ----------------------------------------------------------

    public void quitSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	// if multi window app only close the one window with main.close()
	if(returncode == NSAlertPanel.DefaultReturn)
	    NSApplication.sharedApplication().terminate(this);
    }
}
