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
public class CDPreferencesWindow extends NSWindow {
    private static Logger log = Logger.getLogger(CDPreferencesWindow.class);

    public CDPreferencesWindow() {
	super();
	log.debug("CDPreferencesWindow");
    }

    public CDPreferencesWindow(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
	log.debug("CDPreferencesWindow");
    }

    public CDPreferencesWindow(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
	log.debug("CDPreferencesWindow");
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
/*	(NSNotificationCenter.defaultCenter()).addObserver(
						    this,
						    new NSSelector("textInputDidEndEditing", new Class[]{NSNotification.class}),
						    NSControl.ControlTextDidEndEditingNotification,
						    filenameField);*/
    }

    public void textInputDidEndEditing(NSNotification sender) {
	log.debug("textInputDidEndEditing");
    }

}