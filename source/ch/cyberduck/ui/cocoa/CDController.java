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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDController {
	protected static Logger log = Logger.getLogger(CDController.class);

	private NSWindow window; // IBOutlet

	public void setWindow(NSWindow window) {
		this.window = window;
		this.window.setDelegate(this);
	}

	public NSWindow window() {
		return this.window;
	}

	public abstract void awakeFromNib();

	public boolean windowShouldClose(NSWindow sender) {
		return true;
	}	
	
	public abstract void windowWillClose(NSNotification notification);

	public void endSheet() {
		log.debug("endSheet");
		if(this.hasSheet()) {
			NSApplication.sharedApplication().endSheet(this.window().attachedSheet());
		}
	}

	public synchronized void beginSheet(NSWindow sheet) {
		this.beginSheet(sheet, false);
	}

	public synchronized void beginSheet(NSWindow sheet, boolean force) {
		if(force) this.endSheet();
		log.debug("beginSheet");
		try {
			this.window().makeKeyAndOrderFront(null);
			synchronized(this) {
				while(this.hasSheet()) {
					log.debug("Sleeping...");
					this.wait();
				}
				NSApplication.sharedApplication().beginSheet(sheet, //sheet
				    this.window(),
				    this, //modalDelegate
				    new NSSelector("sheetDidEnd",
				        new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
				    null); //contextInfo
			}
			this.window().makeKeyAndOrderFront(null);
		}
		catch(InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	public void sheetDidEnd(NSWindow window, int returncode, Object contextInfo) {
		window.orderOut(null);
		synchronized(this) {
			this.notify();
		}
	}

	public boolean hasSheet() {
		return this.window().attachedSheet() != null;
	}
}