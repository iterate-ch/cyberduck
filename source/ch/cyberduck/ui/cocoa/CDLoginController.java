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

/**
* @version $Id$
 */
public class CDLoginController {
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
    
    public void closeSheet(NSObject sender) {
	log.debug("closeSheet");
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(this, ((NSButton)sender).tag());
    }
    
    public void setExplanation(String text) {
	this.textField.setStringValue(text);
    }

    public String user() {
	return userField.stringValue();
    }

    public String pass() {
	return passField.stringValue();
    }

    public NSWindow window() {
	return this.sheet();
    }
}
