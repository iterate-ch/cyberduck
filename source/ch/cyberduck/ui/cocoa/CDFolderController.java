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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDFolderController extends CDSheetController
{
    private static Logger log = Logger.getLogger(CDFolderController.class);

    private NSTextField folderField; //IBOutlet

    public void setFolderField(NSTextField folderField) {
        this.folderField = folderField;
    }

    public CDFolderController(CDWindowController parent) {
        super(parent);
        synchronized(parent) {
            if (!NSApplication.loadNibNamed("Folder", this)) {
                log.fatal("Couldn't load Folder.nib");
            }
        }
    }

    protected boolean validateInput() {
        if (folderField.stringValue().indexOf('/') != -1) {
            this.alert(NSAlertPanel.informationalAlertPanel(
                    NSBundle.localizedString("Error", "Alert sheet title"),
                    NSBundle.localizedString("Invalid character in folder name.", ""), // message
                    NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                    null, //alternative button
                    null //other button
            ));
            return false;
        }
        return folderField.stringValue().length() != 0;
    }

    public void callback(int returncode) {
        if (returncode == DEFAULT_OPTION) {
            this.createFolder(((CDBrowserController)parent).workdir(), folderField.stringValue());
        }
    }

    public void createFolder(Path workdir, String filename) {
        Path folder = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(), filename);
        folder.mkdir(false);
        if(folder.exists()) {
            ((CDBrowserController)parent).setShowHiddenFiles(filename.charAt(0) == '.');
            ((CDBrowserController)parent).reloadData(false);
            ((CDBrowserController)parent).setSelectedPath(folder);
        }
    }
}
