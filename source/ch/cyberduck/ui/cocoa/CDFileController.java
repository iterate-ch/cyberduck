package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
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

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDFileController extends CDController {
	protected static Logger log = Logger.getLogger(CDFileController.class);

	private static NSMutableArray instances = new NSMutableArray();

	protected NSTextField filenameField; //IBOutlet

	public void setFilenameField(NSTextField filenameField) {
		this.filenameField = filenameField;
	}

	public void awakeFromNib() {
		this.window().setReleasedWhenClosed(true);
	}

	public CDFileController() {
		instances.addObject(this);
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	public void createButtonClicked(NSButton sender) {
		// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
		if(filenameField.stringValue().indexOf('/') != -1) {
			NSAlertPanel.beginInformationalAlertSheet(NSBundle.localizedString("Error", "Alert sheet title"), //title
			    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
			    null, //alternative button
			    null, //other button
			    this.window(), //docWindow
			    null, //modalDelegate
			    null, //didEndSelector
			    null, // dismiss selector
			    null, // context
			    NSBundle.localizedString("Invalid character in filename.", "") // message
			);
		}
		else if(filenameField.stringValue().length() == 0) {
			//
		}
		else {
			this.endSheet(this.window(), sender.tag());
		}
	}

	public void cancelButtonClicked(NSButton sender) {
		this.endSheet(this.window(), sender.tag());
	}

	public void editButtonClicked(NSButton sender) {
		this.createButtonClicked(sender);
	}
}
