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
import org.apache.log4j.Logger;
import ch.cyberduck.core.Login;

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

    private CDBrowserController controller;

    public CDLoginController(CDBrowserController controller, String user, String pass) {
	super(user, pass);
	this.controller = controller;
    }

    public CDLoginController(CDBrowserController controller) {
	super();
	this.controller = controller;
    }
    
    public void closeSheet(Object sender) {
	log.debug("closeSheet");
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(this.window(), ((NSButton)sender).tag());
    }
    
    public NSWindow window() {
	return this.sheet;
    }


    private boolean done;
    private boolean tryAgain;

    public boolean loginFailure(String message) {
	log.info("Authentication failed");
	NSApplication.loadNibNamed("Login", this);
	this.textField.setStringValue(message);
	NSApplication.sharedApplication().beginSheet(
					      this.window(), //sheet
					      controller.window(),
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
		e.printStackTrace();
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
