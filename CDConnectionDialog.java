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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDConnectionDialog extends NSWindow {

    private static Logger log = Logger.getLogger(CDConnectionDialog.class);

    public NSPopUpButton protocolPopup;    
    public NSTextField hostNameField;
    public NSTextField pathField;
    public NSTextField portField;
    public NSTextField usernameField;
    public NSTextField urlLabel;
    
    public CDConnectionDialog() {
	super();
	log.debug("CDConnectionDialog");
    }

    public CDConnectionDialog(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
	log.debug("CDConnectionDialog");
    }

    public CDConnectionDialog(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
	log.debug("CDConnectionDialog");
    }

    public void awakeFromNib() {
	log.debug("CDConnectionDialog:awakeFromNib");
	this.textInputDidChange(null);
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
    }

    private static final int SFTP_TAG = 1;
    private static final int FTP_TAG = 2;
    
    public void protocolSelectionChanged(NSObject sender) {
	NSMenuItem selectedItem = protocolPopup.selectedItem();
	log.debug("protocol selection changed");
	if(selectedItem.tag() == SFTP_TAG)
	    portField.setIntValue(22); //TODO: use constant
	if(selectedItem.tag() == FTP_TAG)
	    portField.setIntValue(21);
    }

    public void textInputDidChange(NSNotification sender) {
	urlLabel.setStringValue(usernameField.stringValue()+"@"+hostNameField.stringValue()+":"+portField.stringValue()+"/"+pathField.stringValue());
    }

    public void finalize() {
	(NSNotificationCenter.defaultCenter()).removeObserver(this);
    }
}
