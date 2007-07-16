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
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import com.apple.cocoa.application.NSApplication;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDFolderController extends CDFileController {
    private static Logger log = Logger.getLogger(CDFolderController.class);

    public CDFolderController(final CDWindowController parent) {
        super(parent);
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Folder", this)) {
                log.fatal("Couldn't load Folder.nib");
            }
        }
    }

    public void callback(int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createFolder(this.getWorkdir(), filenameField.stringValue());
        }
    }

    protected void createFolder(final Path workdir, final String filename) {
        final CDBrowserController c = (CDBrowserController)parent;
        c.background(new BackgroundAction() {
            final Path folder
                    = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(), filename);

            public void run() {
                folder.mkdir(false);
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                        folder.writePermissions(new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default")),
                                false);
                    }
                }
            }

            public void cleanup() {
                if(folder.exists()) {
                    if(filename.charAt(0) == '.') {
                        c.setShowHiddenFiles(true);
                    }
                    c.reloadData(false);
                    c.setSelectedPath(folder);
                }
            }
        });
    }
}
