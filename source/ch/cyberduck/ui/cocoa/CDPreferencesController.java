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
import ch.cyberduck.core.Preferences;

/**
* @version $Id$
 */
public class CDPreferencesController {
    private static Logger log = Logger.getLogger(CDPreferencesController.class);

    private static CDPreferencesController instance;
    
    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSTextField anonymousField;
    public void setAnonymousField(NSTextField anonymousField) {
	this.anonymousField = anonymousField;
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

    private NSPopUpButton transfermodeCombo;
    public void setTransfermodeCombo(NSPopUpButton transfermodeCombo) {
	this.transfermodeCombo = transfermodeCombo;
    }

    private NSPopUpButton connectmodeCombo;
    public void setConnectmodeCombo(NSPopUpButton connectmodeCombo) {
	this.connectmodeCombo = connectmodeCombo;
    }
    
    private NSPopUpButton protocolCombo;
    public void setProtocolCombo(NSPopUpButton protocolCombo) {
	this.protocolCombo = protocolCombo;
    }

    private NSWindow window;
    public void setWindow(NSWindow window) {
	this.window = window;
    }


    public static CDPreferencesController instance() {
	if(null == instance) {
	    instance = new CDPreferencesController();
	}
        if (false == NSApplication.loadNibNamed("Preferences", instance)) {
            log.error("Couldn't load Preferences.nib");
        }
	instance.init();
	return instance;
    }
    
    private CDPreferencesController() {
//
    }

    private void init() {
	//setting values
	anonymousField.setStringValue(Preferences.instance().getProperty("ftp.anonymous.pass"));
	downloadPathField.setStringValue(Preferences.instance().getProperty("download.path"));
	
	transfermodeCombo.selectItemWithTitle(Preferences.instance().getProperty("ftp.transfermode"));
	connectmodeCombo.selectItemWithTitle(Preferences.instance().getProperty("ftp.connectmode"));
	protocolCombo.selectItemWithTitle(Preferences.instance().getProperty("connection.protocol.default"));


	
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  anonymousField);
	NSNotificationCenter.defaultCenter().addObserver(
						  this,
						  new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						  NSControl.ControlTextDidChangeNotification,
						  downloadPathField);
	
    }

    public void finalize() throws Throwable {
	super.finalize();
	NSNotificationCenter.defaultCenter().removeObserver(this);
    }


    public void textInputDidChange(NSNotification sender) {

    }
    
    public NSWindow window() {
	return this.window;
    }
}
