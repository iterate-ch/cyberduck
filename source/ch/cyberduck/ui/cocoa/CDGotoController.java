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
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;

/**
 * @version $Id$
 */
public class CDGotoController extends CDController {
	private static Logger log = Logger.getLogger(CDGotoController.class);

	private static NSMutableArray instances = new NSMutableArray();

	public void awakeFromNib() {
		this.window().setReleasedWhenClosed(true);
	}

	private NSTextField folderField; // IBOutlet

	public void setFolderField(NSTextField folderField) {
		this.folderField = folderField;
	}
	
	public void setCurrentString(String folder) {
		this.folderField.setStringValue(folder);
	}

	public CDGotoController() {
		instances.addObject(this);
		if(false == NSApplication.loadNibNamed("Goto", this)) {
			log.fatal("Couldn't load Goto.nib");
		}
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	public void goButtonClicked(Object sender) {
		if(folderField.stringValue().length() == 0) {
			// folderField.setStringValue(this.file.getName());
		}
		else {
			// Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
			NSApplication.sharedApplication().endSheet(this.window(), ((NSButton)sender).tag());
		}
	}

	public void cancelButtonClicked(Object sender) {
		NSApplication.sharedApplication().endSheet(this.window(), ((NSButton)sender).tag());
	}

	public void gotoSheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
		log.debug("gotoSheetDidEnd");
		sheet.orderOut(null);
		switch(returncode) {
			case (NSAlertPanel.DefaultReturn):
				Path workdir = (Path)contextInfo;
				this.gotoFolder(workdir, this.folderField.stringValue());
				break;
			case (NSAlertPanel.AlternateReturn):
				break;
		}
	}
	
	public Path gotoFolder(Path workdir, String filename) {
		Path go = workdir.copy(workdir.getSession());
		if(filename.charAt(0) != '/') {
			go.setPath(workdir.getAbsolute(), filename);
		}
		else {
			go.setPath(filename);
		}
		go.list();
		return go;
	}
}