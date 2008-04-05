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

import com.apple.cocoa.application.NSImageView;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import org.apache.log4j.Logger;

import java.util.Collections;

/**
 * @version $Id$
 */
public class CDFolderController extends CDFileController {
    private static Logger log = Logger.getLogger(CDFolderController.class);

    protected NSImageView iconView; //IBOutlet

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        this.iconView.setImage(CDIconCache.FOLDER_NEW_ICON);
    }

    public CDFolderController(final CDWindowController parent) {
        super(parent);
    }

    protected String getBundleName() {
        return "Folder";
    }

    public void callback(int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createFolder(this.getWorkdir(), filenameField.stringValue());
        }
    }

    protected void createFolder(final Path workdir, final String filename) {
        final CDBrowserController c = (CDBrowserController) parent;
        c.background(new BackgroundAction() {
            final Path folder
                    = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(),
                    filename, Path.DIRECTORY_TYPE);

            public void run() {
                folder.mkdir(false);
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                        folder.writePermissions(new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default")),
                                false);
                    }
                }
                folder.cache().put(folder, new AttributedList());
                folder.getParent().invalidate();
            }

            public void cleanup() {
                if(folder.exists()) {
                    if(filename.charAt(0) == '.') {
                        c.setShowHiddenFiles(true);
                    }
                    c.reloadData(Collections.singletonList(folder));
                }
            }
        });
    }
}
