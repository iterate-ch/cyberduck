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
public class CDPreferencesController {
    private static Logger log = Logger.getLogger(CDPreferencesController.class);

    private static CDPreferencesController instance;
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSTextField usernameField;
    public void setUsernameField(NSTextField usernameField) {
	this.usernameField = usernameField;
    }

    private NSTextField passField;
    public void setPassField(NSTextField passField) {
	this.passField = passField;
    }

    private NSTextField downloadPathField;
    public void setDownloadPathField(NSTextField downloadPathField) {
	this.downloadPathField = downloadPathField;
    }

    private NSButton downloadPathButton;
    public void setDownloadPathButton(NSButton downloadPathButton) {
	this.downloadPathButton = downloadPathButton;
    }

    private NSButton showHiddenCheckbox;
    public void setShowHiddenCheckbox(NSButton showHiddenCheckbox) {
	this.showHiddenCheckbox = showHiddenCheckbox;
    }

    private NSPopUpButton transfermodeCombobox;
    public void setTransfermodeCombobox(NSPopUpButton transfermodeCombobox) {
	this.transfermodeCombobox = transfermodeCombobox;
    }

    private NSPopUpButton protocolCombobox;
    public void setProtocolCombobox(NSPopUpButton protocolCombobox) {
	this.protocolCombobox = protocolCombobox;
    }

    private NSWindow window;
    public void setWindow(NSWindow window) {
	this.window = window;
    }

    private CDPreferencesController() {
        if (false == NSApplication.loadNibNamed("Preferences", this)) {
            log.error("Couldn't load Preferences.nib");
            return;
        }
	this.init();
    }

    private void init() {
	usernameField.setStringValue(Preferences.instance().getProperty("connection.login.anonymous.name"));
	passField.setStringValue(Preferences.instance().getProperty("connection.login.anonymous.pass"));
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  usernameField);
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  passField);
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  downloadPathField);
	
    }

    public static CDPreferencesController instance() {
	if(null == instance) {
	    instance = new CDPreferencesController();
	}
	return instance;
    }

    public NSWindow window() {
	return this.window;
    }
}
