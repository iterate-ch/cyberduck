/*
 *  ch.cyberduck.ui.cocoa.CDConnectionDialog.java
 *  Cyberduck
 *
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

package ch.cyberduck.ui.cocoa;

import ch.cyberduck.core.Session;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDConnectionSheet extends NSWindow {

    private static Logger log = Logger.getLogger(CDConnectionSheet.class);

    public NSPopUpButton protocolPopup;    
    public NSTextField hostNameField;
    public NSTextField pathField;
    public NSTextField portField;
    public NSTextField usernameField;
    public NSTextField urlLabel;
    
    public CDConnectionSheet() {
	super();
	log.debug("CDConnectionSheet");
    }

    public CDConnectionSheet(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
	log.debug("CDConnectionSheet");
    }

    public CDConnectionSheet(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
	log.debug("CDConnectionSheet");
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
//	this.urlLabel.setStringValue("");
	// Notify the textInputDidChange() method if the user types.
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    hostNameField);
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    pathField);
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    portField);
	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidChange", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidChangeNotification,
						    usernameField);

	this.textInputDidChange(null);
	this.portField.setIntValue(protocolPopup.selectedItem().tag());
	this.pathField.setStringValue("~");
    }

    public void closeSheet(NSObject sender) {
	// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
	NSApplication.sharedApplication().endSheet(this, NSAlertPanel.AlternateReturn);
    }
    
    public void protocolSelectionChanged(NSObject sender) {
	log.debug("protocolSelectionChanged");
	NSMenuItem selectedItem = protocolPopup.selectedItem();
	if(selectedItem.tag() == Session.SSH_PORT)
	    portField.setIntValue(Session.SSH_PORT);
	if(selectedItem.tag() == Session.FTP_PORT)
	    portField.setIntValue(Session.FTP_PORT);
	if(selectedItem.tag() == Session.HTTP_PORT)
	    portField.setIntValue(Session.HTTP_PORT);
	//@todo HTTPS
    }

    public void textInputDidChange(NSNotification sender) {
	log.debug("textInputDidChange");
	urlLabel.setStringValue(usernameField.stringValue()+"@"+hostNameField.stringValue()+":"+portField.stringValue()+"/"+pathField.stringValue());
    }

    public void finalize() {
	(NSNotificationCenter.defaultCenter()).removeObserver(this);
    }
}
