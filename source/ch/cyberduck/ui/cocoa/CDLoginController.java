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

import ch.cyberduck.core.Login;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDLoginController extends Login {
    private static Logger log = Logger.getLogger(CDLoginController.class);

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
    
    private NSTextField userField;
    public void setUserField(NSTextField userField) {
	this.userField = userField;
    }

    private NSTextField textField;
    public void setTextField(NSTextField textField) {
	this.textField = textField;
    }
    
    private NSSecureTextField passField;
    public void setPassField(NSSecureTextField passField) {
	this.passField = passField;
    }

    private NSWindow sheet;
    public void setSheet(NSWindow sheet) {
	this.sheet = sheet;
    }

    private NSWindow parentWindow;

    private static NSMutableArray allDocuments = new NSMutableArray();

    public CDLoginController(NSWindow parentWindow, String user, String pass) {
	super(user, pass);
	allDocuments.addObject(this);
	this.parentWindow = parentWindow;
    }

    public CDLoginController(NSWindow parentWindow) {
	super();
	allDocuments.addObject(this);
	this.parentWindow = parentWindow;
    }

    public void finalize() throws Throwable {
	log.debug("finalize");
	super.finalize();
    }    
    
    public void closeSheet(Object sender) {
	log.debug("closeSheet");
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(this.window(), ((NSButton)sender).tag());
    }
    
    public NSWindow window() {
	return this.sheet;
    }

    public void windowWillClose(NSNotification notification) {
	this.window().setDelegate(null);
	allDocuments.removeObject(this);
    }

    private boolean done;
    private boolean tryAgain;

    public boolean loginFailure(String message) {
	log.info("Authentication failed");
	NSApplication.loadNibNamed("Login", this);
	this.textField.setStringValue(message);
	NSApplication.sharedApplication().beginSheet(
					      this.window(), //sheet
					      parentWindow,
					      this, //modalDelegate
					      new NSSelector(
			  "loginSheetDidEnd",
			  new Class[] { NSWindow.class, int.class, NSWindow.class }
			  ),// did end selector
					      null); //contextInfo
	this.window().makeKeyAndOrderFront(null);
	while(!done) {
	    try {
		Thread.sleep(500); //milliseconds
	    }
	    catch(InterruptedException e) {
		log.error(e.getMessage());
	    }
	}
	return tryAgain;
    }

    /**
	* Selector method from
     * @see loginFailure
     */
    public void loginSheetDidEnd(NSWindow sheet, int returncode, NSWindow main) {
	log.info("loginSheetDidEnd");
	this.window().orderOut(null);
	switch(returncode) {
	    case(NSAlertPanel.DefaultReturn):
		tryAgain = true;
		this.setUsername(userField.stringValue());
		this.setPassword(passField.stringValue());
		break;
	    case(NSAlertPanel.AlternateReturn):
		tryAgain = false;
		break;
	}
	done = true;
    }
}
