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
public class CDLoginSheet extends NSPanel {
    private static Logger log = Logger.getLogger(CDLoginSheet.class);

    public NSTextField userField;
    public NSSecureTextField passField;

    public CDLoginSheet() {
	super();
    }

    public CDLoginSheet(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
    }

    public CDLoginSheet(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
    }
    
    public void awakeFromNib() {
	//
    }

    public void closeSheet(NSObject sender) {
	log.debug("closeSheet");
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSButton button = (NSButton)sender;
	log.debug("DefaultReturn:"+NSAlertPanel.DefaultReturn);
	log.debug("AlternateReturn:"+NSAlertPanel.AlternateReturn);
	if(button.title().equals("Login")) { //@todo replace with id
	    NSApplication.sharedApplication().endSheet(this, NSAlertPanel.DefaultReturn);
	}
	else
	    NSApplication.sharedApplication().endSheet(this, NSAlertPanel.AlternateReturn);
    }

    public String getUser() {
	log.debug("getUser");
	return userField.stringValue();
    }

    public String getPass() {
	log.debug("getPass");
	return passField.stringValue();
    }
}
