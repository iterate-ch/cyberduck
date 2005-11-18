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
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSNotification;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class CDGotoController extends CDWindowController {
    private static Logger log = Logger.getLogger(CDGotoController.class);

    private static NSMutableArray instances = new NSMutableArray();

    public void awakeFromNib() {
        super.awakeFromNib();

        this.window().setReleasedWhenClosed(true);
        this.folderCombobox.setStringValue(controller.workdir().getAbsolute());
    }

    private NSComboBox folderCombobox; // IBOutlet
    private Object folderComboDataSource;

    public void setFolderCombobox(NSComboBox folderCombobox) {
        this.folderCombobox = folderCombobox;
        this.folderCombobox.setCompletes(true);
        this.folderCombobox.setUsesDataSource(true);
        this.folderCombobox.setDataSource(this.folderComboDataSource = new Object() {
            private List directories = new ArrayList();

            {
                for (Iterator i = controller.workdir().list(false, controller.getEncoding(),
                        controller.getComparator(), controller.getFileFilter()).iterator(); i.hasNext();) {
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
    }

    private CDBrowserController controller;

    public CDGotoController(CDBrowserController controller) {
        instances.addObject(this);
        this.controller = controller;
        if (!NSApplication.loadNibNamed("Goto", this)) {
            log.fatal("Couldn't load Goto.nib");
        }
    }

    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
    }

    public void goButtonClicked(Object sender) {
        if (folderCombobox.stringValue().length() == 0) {
            // folderCombobox.setStringValue(this.file.getName());
        }
        else {
            // Ends a document modal session by specifying the sheet window, sheet. Also passes along a returnCode to the delegate.
            this.endSheet(this.window(), ((NSButton) sender).tag());
        }
    }

    public void cancelButtonClicked(Object sender) {
        this.endSheet(this.window(), ((NSButton) sender).tag());
    }

    public void sheetDidEnd(NSPanel sheet, int returncode, Object contextInfo) {
        sheet.orderOut(null);
        switch (returncode) {
            case (NSAlertPanel.DefaultReturn):
                Path workdir = (Path) contextInfo;
                this.gotoFolder(workdir, this.folderCombobox.stringValue());
                break;
            case (NSAlertPanel.AlternateReturn):
                break;
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
        controller.setWorkdir(dir);
        controller.reloadData();
    }
}