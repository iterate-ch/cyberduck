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

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.application.NSComboBox;
import ch.cyberduck.ui.cocoa.application.NSImageView;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.NSObject;

import java.util.Comparator;

/**
 * @version $Id$
 */
public class CDGotoController extends CDSheetController {
    private static Logger log = Logger.getLogger(CDGotoController.class);

    protected NSImageView iconView; //IBOutlet

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        this.iconView.setImage(CDIconCache.FOLDER_ICON);
    }

    private NSComboBox folderCombobox; // IBOutlet
    private CDController folderComboboxModel;

    public void setFolderCombobox(NSComboBox folderCombobox) {
        this.folderCombobox = folderCombobox;
        this.folderCombobox.setCompletes(true);
        this.folderCombobox.setUsesDataSource(true);
        this.folderCombobox.setDataSource((this.folderComboboxModel = new CDController() {
            final CDBrowserController c = (CDBrowserController) parent;
            private final Comparator<Path> comparator = new NullComparator<Path>();
            private final PathFilter<Path> filter = new PathFilter<Path>() {
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
                return c.workdir().childs(comparator, filter).size();
            }

            /**
             * @see NSComboBox.DataSource
             */
            public NSObject comboBox_objectValueForItemAtIndex(final NSComboBox sender, final int row) {
                return NSString.stringWithString(c.workdir().childs(comparator, filter).get(row).getAbsolute());
            }
        }).id());
        this.folderCombobox.setStringValue(((CDBrowserController) this.parent).workdir().getAbsolute());
    }

    public CDGotoController(final CDWindowController parent) {
        super(parent);
    }

    public String getBundleName() {
        return "Goto";
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.gotoFolder(((CDBrowserController) parent).workdir(), folderCombobox.stringValue());
        }
    }

    protected boolean validateInput() {
        return StringUtils.isNotBlank(folderCombobox.stringValue());
    }

    protected void gotoFolder(final Path workdir, final String filename) {
        final CDBrowserController c = (CDBrowserController) parent;
        final Path dir = PathFactory.createPath(workdir.getSession(), workdir.getAsDictionary());
        if(filename.charAt(0) != '/') {
            dir.setPath(workdir.getAbsolute(), filename);
        }
        else {
            dir.setPath(filename);
        }
        c.setWorkdir(dir);
        if(workdir.getParent().equals(dir)) {
            c.setSelectedPath(workdir);
        }
    }
}