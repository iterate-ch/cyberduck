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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSNotificationCenter;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Login;
import ch.cyberduck.ui.LoginController;

/**
 * @version $Id$
 */
public class CDLoginController extends CDController implements LoginController {
	private static Logger log = Logger.getLogger(CDLoginController.class);

	private static NSMutableArray instances = new NSMutableArray();

	public void awakeFromNib() {
		this.window().setReleasedWhenClosed(false);
	}
	
	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------

	private NSTextField userField; // IBOutlet

	public void setUserField(NSTextField userField) {
		this.userField = userField;
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("userFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.userField);
	}

	public void userFieldDidChange(Object sender) {
		log.debug("userFieldDidChange");
		if(null == userField.objectValue() || userField.objectValue().equals("")) {
			log.warn("Value of username textfield is null");
		}
	}

	private NSTextField textField; // IBOutlet

	public void setTextField(NSTextField textField) {
		this.textField = textField;
	}

	private NSSecureTextField passField; // IBOutlet

	public void setPassField(NSSecureTextField passField) {
		this.passField = passField;
		NSNotificationCenter.defaultCenter().addObserver(this,
		    new NSSelector("passFieldDidChange", new Class[]{NSNotification.class}),
		    NSControl.ControlTextDidChangeNotification,
		    this.passField);
	}

	public void passFieldDidChange(Object sender) {
		log.debug("passFieldDidChange");
		if(null == passField.objectValue() || passField.objectValue().equals("")) {
			log.warn("Value of password textfield is null");
		}
	}

	private NSButton keychainCheckbox;

	public void setKeychainCheckbox(NSButton keychainCheckbox) {
		this.keychainCheckbox = keychainCheckbox;
		this.keychainCheckbox.setState(NSCell.OffState);
	}

	private CDController windowController;

	public CDLoginController(CDController windowController) {
		instances.addObject(this);
		this.windowController = windowController;
		if(false == NSApplication.loadNibNamed("Login", this)) {
			log.fatal("Couldn't load Login.nib");
		}
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
		NSNotificationCenter.defaultCenter().removeObserver(this);
	}

	private Login login;

	public Login promptUser(final Login login, final String message) {
		this.login = login;
		this.textField.setStringValue(message);
		this.userField.setStringValue(login.getUsername());
		this.windowController.beginSheet(window());
	    this.windowController.waitForSheetEnd();
		return this.login;
	}

	public void closeSheet(NSButton sender) {
		if(null == userField.objectValue() || userField.objectValue().equals("")) {
			log.warn("Value of username textfield is null");
		}
		if(null == passField.objectValue() || passField.objectValue().equals("")) {
			log.warn("Value of password textfield is null");
		}
		switch(sender.tag()) {
			case (NSAlertPanel.DefaultReturn):
				log.info("Updating login credentials...");
				this.login.setTryAgain(true);
				this.login.setUsername((String)userField.objectValue());
				this.login.setPassword((String)passField.objectValue());
				this.login.setUseKeychain(keychainCheckbox.state() == NSCell.OnState);
				break;
			case (NSAlertPanel.AlternateReturn):
				log.info("Cancelling login...");
				this.login.setTryAgain(false);
				break;
		}
		this.windowController.endSheet();
	}
}
