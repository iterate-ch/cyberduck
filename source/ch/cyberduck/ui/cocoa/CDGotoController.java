package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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
public class CDGotoController extends NSObject {
    private static Logger log = Logger.getLogger(CDGotoController.class);

	private static NSMutableArray instances = new NSMutableArray();

    private NSWindow window; // IBOutlet

    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }

    private NSTextField folderField; // IBOutlet

    public void setFolderField(NSTextField folderField) {
        this.folderField = folderField;
        this.folderField.setStringValue(current.getAbsolute());
    }

    public NSWindow window() {
        return this.window;
    }

    private Path current;

    public CDGotoController(Path current) {
        this.current = current;
        instances.addObject(this);
        if (false == NSApplication.loadNibNamed("Goto", this)) {
            log.fatal("Couldn't load Goto.nib");
        }
    }

    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
    }

    public void closeSheet(Object sender) {
        // Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
        NSApplication.sharedApplication().endSheet(this.window, ((NSButton) sender).tag());
    }

    public void gotoSheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
        log.debug("gotoSheetDidEnd");
        sheet.orderOut(null);
        switch (returncode) {
            case (NSAlertPanel.DefaultReturn):
                Path current = (Path) contextInfo;
                Path go = current.copy(current.getSession());
                String name = this.folderField.stringValue();
                if (name.charAt(0) != '/') {
                    go.setPath(current.getAbsolute(), name);
                }
                else {
                    go.setPath(name);
                }
                go.list();
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
        }
    }

}
