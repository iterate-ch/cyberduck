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
import ch.cyberduck.core.NullComparator;
import ch.cyberduck.core.PathFilter;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSComboBox;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Comparator;

/**
 * @version $Id$
 */
public class CDGotoController extends CDSheetController{
    private static Logger log = Logger.getLogger(CDGotoController.class);

    private NSComboBox folderCombobox; // IBOutlet
    private NSObject folderComboboxModel;

    public void setFolderCombobox(NSComboBox folderCombobox) {
        this.folderCombobox = folderCombobox;
        this.folderCombobox.setCompletes(true);
        this.folderCombobox.setUsesDataSource(true);
        this.folderCombobox.setDataSource(this.folderComboboxModel = new NSObject()/*NSComboBox.DataSource*/ {
            final CDBrowserController c = (CDBrowserController)parent;
            private final Comparator comparator = new NullComparator();
            private final PathFilter filter = new PathFilter() {
                public boolean accept(Path p) {
                    return p.attributes.isDirectory();
                }
            };

            /**
             * @see NSComboBox.DataSource
             */
            public int numberOfItemsInComboBox(NSComboBox combo) {
                if(!c.isMounted()) {
                    return 0;
                }
                return c.workdir().list(comparator, filter).size();
            }

            /**
             * @see NSComboBox.DataSource
             */
            public Object comboBoxObjectValueForItemAtIndex(final NSComboBox sender, final int row) {
                final List childs = c.workdir().list(comparator, filter);
                if(row < childs.size()) {
                    return ((Path)childs.get(row)).getAbsolute();
                }
                return null;
            }
        });
        this.folderCombobox.setStringValue(((CDBrowserController)this.parent).workdir().getAbsolute());
    }

    public CDGotoController(final CDWindowController parent) {
        super(parent);
        synchronized(NSApplication.sharedApplication()) {
            if (!NSApplication.loadNibNamed("Goto", this)) {
                log.fatal("Couldn't load Goto.nib");
            }
        }
    }

    public void callback(final int returncode) {
        if (returncode == DEFAULT_OPTION) {
            this.gotoFolder(((CDBrowserController)parent).workdir(), folderCombobox.stringValue());
        }
    }

    protected boolean validateInput() {
        return folderCombobox.stringValue().length() != 0;
    }

    protected void gotoFolder(final Path workdir, final String filename) {
        final CDBrowserController c = (CDBrowserController)parent;
        c.background(new BackgroundAction() {
            final Path dir = (Path)workdir.clone();

            public void run() {
                if (filename.charAt(0) != '/') {
                    dir.setPath(workdir.getAbsolute(), filename);
                }
                else {
                    dir.setPath(filename);
                }
                c.setWorkdir(dir);
            }

            public void cleanup() {
                if(workdir.getParent().equals(dir)) {
                    c.setSelectedPath(workdir);
                }
            }
        });
    }
}