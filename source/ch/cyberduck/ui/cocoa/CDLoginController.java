package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Login;
import ch.cyberduck.ui.LoginController;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDLoginController implements LoginController {
	private static Logger log = Logger.getLogger(CDLoginController.class);

	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------

	private NSTextField userField; // IBOutlet

	public void setUserField(NSTextField userField) {
		this.userField = userField;
	}

	private NSTextField textField; // IBOutlet

	public void setTextField(NSTextField textField) {
		this.textField = textField;
	}

	private NSSecureTextField passField; // IBOutlet

	public void setPassField(NSSecureTextField passField) {
		this.passField = passField;
	}

	private NSButton keychainCheckbox;

	public void setKeychainCheckbox(NSButton keychainCheckbox) {
		this.keychainCheckbox = keychainCheckbox;
		this.keychainCheckbox.setState(NSCell.OffState);
		//this.keychainCheckbox.setState(Preferences.instance().getProperty("connection.login.useKeychain").equals("true") ? NSCell.OnState : NSCell.OffState);
	}

	private NSWindow sheet; // IBOutlet

	public void setSheet(NSWindow sheet) {
		this.sheet = sheet;
		this.sheet.setDelegate(this);
	}

	private NSWindow parentWindow;

	private static NSMutableArray instances = new NSMutableArray();

	public CDLoginController(NSWindow parentWindow) {
		this.parentWindow = parentWindow;
		instances.addObject(this);
		if (false == NSApplication.loadNibNamed("Login", this)) {
			log.fatal("Couldn't load Login.nib");
		}
	}

	public NSWindow window() {
		return this.sheet;
	}

	public void windowWillClose(NSNotification notification) {
		this.window().setDelegate(null);
		instances.removeObject(this);
	}

	private boolean done;
	private boolean tryAgain;

	/**
	 * @return True if the user has choosen to try again with new credentials
	 */
	public boolean loginFailure(Login login, String message) {
		log.debug("Authentication failed:" + login.toString());
		this.done = false;
		this.textField.setStringValue(message);
		this.userField.setStringValue(login.getUsername());
		NSApplication.sharedApplication().beginSheet(
		    this.window(), //sheet
		    parentWindow,
		    this, //modalDelegate
		    new NSSelector(
		        "loginSheetDidEnd",
		        new Class[]{NSWindow.class, int.class, Object.class}
		    ), // did end selector
		    login); //contextInfo
		this.window().makeKeyAndOrderFront(null);
		while (!done) {
			try {
				log.debug("Sleeping...");
				Thread.sleep(1000); //milliseconds
			}
			catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		return tryAgain;
	}

	public void closeSheet(NSButton sender) {
		log.debug("closeSheet");
		// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
		this.userField.abortEditing();
		this.passField.abortEditing();
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	/**
	 * Selector method from
	 * @see #loginFailure
	 */
	public void loginSheetDidEnd(NSWindow sheet, int returncode, Object context) {
		this.window().orderOut(null);
		Login login = (Login) context;
		log.debug("loginSheetDidEnd:" + login.toString());
		switch (returncode) {
			case (NSAlertPanel.DefaultReturn):
				this.tryAgain = true;
				login.setUsername(userField.stringValue());
				login.setPassword(passField.stringValue());
				if (keychainCheckbox.state() == NSCell.OnState) {
					login.addPasswordToKeychain();
				}
				break;
			case (NSAlertPanel.AlternateReturn):
				this.tryAgain = false;
				break;
		}
		this.done = true;
	}
}
