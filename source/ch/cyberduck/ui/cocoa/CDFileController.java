package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Preferences;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.application.NSWindow;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDFileController extends CDSheetController {
    protected static Logger log = Logger.getLogger(CDFileController.class);

    protected NSTextField filenameField; //IBOutlet

    public void setFilenameField(NSTextField filenameField) {
        this.filenameField = filenameField;
    }

    private NSButton editButton; //IBOutlet

    public void setEditButton(NSButton editButton) {
        this.editButton = editButton;
        this.editButton.setEnabled(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(
                Preferences.instance().getProperty("editor.bundleIdentifier")) != null);
    }

    public CDFileController(CDWindowController parent) {
        super(parent);
    }

    protected boolean validateInput() {
        if (filenameField.stringValue().indexOf('/') != -1) {
            this.alert(NSAlertPanel.informationalAlertPanel(
                    NSBundle.localizedString("Error", "Alert sheet title"),
                    NSBundle.localizedString("Invalid character in filename.", ""), // message
                    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                    null, //alternative button
                    null //other button
            ));
            return false;
        }
        return filenameField.stringValue().length() != 0;
    }
}
