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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.odb.Editor;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSPathUtilities;

import java.util.Collections;

/**
 * @version $Id$
 */
public class CDCreateFileController extends CDFileController {

    public CDCreateFileController(final CDWindowController parent) {
        super(parent);
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("File", this)) {
                log.fatal("Couldn't load File.nib");
            }
        }
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createFile(this.getWorkdir(), filenameField.stringValue(), false);
        }
        if(returncode == ALTERNATE_OPTION) {
            this.createFile(this.getWorkdir(), filenameField.stringValue(), true);
        }
    }

    protected void createFile(final Path workdir, final String filename, final boolean edit) {
        final CDBrowserController c = (CDBrowserController)parent;
        c.background(new BackgroundAction() {
            final Path file = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(),
                    new Local(NSPathUtilities.temporaryDirectory(), filename));

            public void run() {
                int no = 0;
                int index = filename.lastIndexOf(".");
                while(file.getLocal().exists()) {
                    no++;
                    String proposal;
                    if(index != -1) {
                        proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
                    }
                    else {
                        proposal = filename + "-" + no;
                    }
                    file.setLocal(new Local(NSPathUtilities.temporaryDirectory(), proposal));
                }
                file.getLocal().createNewFile();
                Permission d = new Permission(Preferences.instance().getInteger("queue.upload.permissions.file.default"));
                if(!Permission.EMPTY.equals(d)) {
                    file.getLocal().setPermission(d);
                }
                file.upload();
                file.getLocal().delete();
                if(file.exists()) {
                    if(edit) {
                        Editor editor = new Editor(c);
                        editor.open(file);
                    }
                }
            }

            public void cleanup() {
                if(file.exists()) {
                    if(filename.charAt(0) == '.') {
                        c.setShowHiddenFiles(true);
                    }
                    c.reloadData(Collections.singletonList(file));
                }
            }
        });
    }
}