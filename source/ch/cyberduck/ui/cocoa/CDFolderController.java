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
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Path;

/**
 * @version $Id$
 */
public class CDFolderController extends NSObject {
    private static Logger log = Logger.getLogger(CDFolderController.class);

    private static NSMutableArray instances = new NSMutableArray();

    private NSWindow window;

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }

    private NSTextField folderField; //IBOutlet

    public void setFolderField(NSTextField folderField) {
        this.folderField = folderField;
    }

    public NSWindow window() {
        return this.window;
    }

    public CDFolderController() {
        instances.addObject(this);
        if (false == NSApplication.loadNibNamed("Folder", this)) {
            log.fatal("Couldn't load Folder.nib");
        }
    }

    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
    }
	
    public void closeSheet(Object sender) {
        // Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
		if(folderField.stringValue().indexOf('/') == -1) {
			NSApplication.sharedApplication().endSheet(this.window, ((NSButton) sender).tag());
		}
		else {
			NSAlertPanel.beginInformationalAlertSheet(NSBundle.localizedString("Error", "Alert sheet title"), //title
													  NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
													  null, //alternative button
													  null, //other button
													  this.window(), //docWindow
													  null, //modalDelegate
													  null, //didEndSelector
													  null, // dismiss selector
													  null, // context
													  NSBundle.localizedString("Invalid character in folder name.", "") // message
													  );
		}
	}

    public void newfolderSheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
        log.debug("newfolderSheetDidEnd");
		sheet.orderOut(null);
        switch (returncode) {
            case (NSAlertPanel.DefaultReturn):
				Path p = (Path)contextInfo;
				p.setPath(p.getAbsolute(), folderField.stringValue());
				p.mkdir(false);
				p.getParent().list(true);
				break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }
}
