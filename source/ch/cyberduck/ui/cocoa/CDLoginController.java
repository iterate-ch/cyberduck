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
public class CDLoginController extends LoginController {
    private static Logger log = Logger.getLogger(CDLoginController.class);

    private static NSMutableArray instances = new NSMutableArray();

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
        if (null == userField.objectValue() || userField.objectValue().equals("")) {
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
        if (null == passField.objectValue() || passField.objectValue().equals("")) {
            log.warn("Value of password textfield is null");
        }
    }

    private NSButton keychainCheckbox;

    public void setKeychainCheckbox(NSButton keychainCheckbox) {
        this.keychainCheckbox = keychainCheckbox;
        this.keychainCheckbox.setState(NSCell.OffState);
//        this.keychainCheckbox.setState(Preferences.instance().getProperty("connection.login.useKeychain").equals("true") ? NSCell.OnState : NSCell.OffState);
    }

    private NSWindow window; // IBOutlet

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }

	private Controller windowController;

    public CDLoginController(Controller windowController) {
        instances.addObject(this);
        this.windowController = windowController;
        if (false == NSApplication.loadNibNamed("Login", this)) {
            log.fatal("Couldn't load Login.nib");
        }
    }

    public NSWindow window() {
        return this.window;
    }

    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
        NSNotificationCenter.defaultCenter().removeObserver(this);
    }

//    private boolean done = false;
    private boolean tryAgain = false;

    public synchronized boolean promptUser(final Login l, final String message) {
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("----------  Waiting for attached sheet to be closed first...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
//		if(null == windowController.window() || null == windowController.window().delegate()) {
//			log.error("Parent window or its delegate is null; cannot begin sheet!");
//			return false;
//		}
//        this.done = false;
		textField.setStringValue(message);
		userField.setStringValue(l.getUsername());
		NSApplication.sharedApplication().beginSheet(window, //sheet
													 windowController.window(),
													 CDLoginController.this, //modalDelegate
													 new NSSelector("loginSheetDidEnd",
																	new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
													 l); //contextInfo
		window().makeKeyAndOrderFront(null);
        while (windowController.window().attachedSheet() != null) {
            try {
                log.debug("Sleeping...");
                Thread.sleep(1000); //milliseconds
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        return this.tryAgain;
    }

    public void closeSheet(NSButton sender) {
        log.debug("closeSheet");
        if (null == userField.objectValue() || userField.objectValue().equals("")) {
            log.warn("Value of username textfield is null");
        }
        if (null == passField.objectValue() || passField.objectValue().equals("")) {
            log.warn("Value of password textfield is null");
        }
        // Ends a document modal session by specifying the sheet window, sheet.
        // Also passes along a returnCode to the delegate.
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
    }

    /**
     * Selector method from
     *
     * @see #promptUser
     * @see #closeSheet
     */
    public void loginSheetDidEnd(NSWindow sheet, int returncode, Object context) {
        log.debug("loginSheetDidEnd");
        this.window.orderOut(null);
        switch (returncode) {
            case (NSAlertPanel.DefaultReturn):
                this.tryAgain = true;
                ((Login)context).setUsername((String)userField.objectValue());
                ((Login)context).setPassword((String)passField.objectValue());
                ((Login)context).setUseKeychain(keychainCheckbox.state() == NSCell.OnState);
                break;
            case (NSAlertPanel.AlternateReturn):
                this.tryAgain = false;
                break;
        }
//        this.done = true;
    }
}
