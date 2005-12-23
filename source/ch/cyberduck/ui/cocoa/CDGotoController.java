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

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSComboBox;
import com.apple.cocoa.application.NSPanel;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class CDGotoController extends CDSheetController {
    private static Logger log = Logger.getLogger(CDGotoController.class);

    private NSComboBox folderCombobox; // IBOutlet
    private Object folderComboDataSource;

    public void setFolderCombobox(NSComboBox folderCombobox) {
        this.folderCombobox = folderCombobox;
        this.folderCombobox.setCompletes(true);
        this.folderCombobox.setUsesDataSource(true);
        this.folderCombobox.setDataSource(this.folderComboDataSource = new Object() {
            private List directories = new ArrayList();

            {
                for (Iterator i = ((CDBrowserController)parent).workdir().list(false).iterator(); i.hasNext();) {
                    Path p = (Path) i.next();
                    if (p.attributes.isDirectory()) {
                        directories.add(p.getName());
                    }
                }
            }

            public int numberOfItemsInComboBox(NSComboBox combo) {
                return directories.size();
            }

            public Object comboBoxObjectValueForItemAtIndex(NSComboBox combo, int row) {
                if (row < this.numberOfItemsInComboBox(combo)) {
                    return directories.get(row);
                }
                return null;
            }
        });
        this.folderCombobox.setStringValue(((CDBrowserController)this.parent).workdir().getAbsolute());
    }

    public CDGotoController(CDWindowController parent) {
        super(parent);
        if (!NSApplication.loadNibNamed("Goto", this)) {
            log.fatal("Couldn't load Goto.nib");
        }
    }

    public void goButtonClicked(NSButton sender) {
        if (folderCombobox.stringValue().length() == 0) {
            // folderCombobox.setStringValue(this.file.getName());
        }
        else {
            // Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
            this.endSheet(sender.tag());
        }
    }

    public void cancelButtonClicked(NSButton sender) {
        this.endSheet(sender.tag());
    }

    public void dismissedSheet(int returncode, Object contextInfo) {
        if (returncode == NSAlertPanel.DefaultReturn) {
            Path workdir = (Path) contextInfo;
            this.gotoFolder(workdir, this.folderCombobox.stringValue());
        }
    }

    protected void gotoFolder(Path workdir, String filename) {
        Path dir = workdir.copy(workdir.getSession());
        if (filename.charAt(0) != '/') {
            dir.setPath(workdir.getAbsolute(), filename);
        }
        else {
            dir.setPath(filename);
        }
        ((CDBrowserController)this.parent).setWorkdir(dir);
    }
}