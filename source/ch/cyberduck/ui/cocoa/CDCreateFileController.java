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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.odb.Editor;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSPathUtilities;

/**
 * @version $Id$
 */
public class CDCreateFileController extends CDFileController {

    public CDCreateFileController(CDWindowController parent) {
        super(parent);
        synchronized(parent) {
            if (!NSApplication.loadNibNamed("File", this)) {
                log.fatal("Couldn't load File.nib");
            }
        }
    }

    public void callback(int returncode) {
        Path workdir = null;
        if(((CDBrowserController)parent).getSelectionCount() == 1) {
            workdir = ((CDBrowserController)parent).getSelectedPath().getParent();
        }
        else {
            workdir = ((CDBrowserController) parent).workdir();
        }
        if (returncode == DEFAULT_OPTION) {
            this.createFile(workdir, filenameField.stringValue());
        }
        if (returncode == ALTERNATE_OPTION) {
            Path path = createFile(workdir, filenameField.stringValue());
            if (path != null) {
                Editor editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"));
                editor.open(path);
            }
        }
    }

    protected Path createFile(Path workdir, String filename) {
        Path file = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(),
                new Local(NSPathUtilities.temporaryDirectory(), filename));
        if (!file.getRemote().exists()) {
            String proposal;
            int no = 0;
            int index = filename.lastIndexOf(".");
            while (file.getLocal().exists()) {
                no++;
                if (index != -1) {
                    proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
                }
                else {
                    proposal = filename + "-" + no;
                }
                file.setLocal(new Local(NSPathUtilities.temporaryDirectory(), proposal));
            }
            file.getLocal().createNewFile();
            file.upload();
            file.getLocal().delete();
        }
        if (file.exists()) {
            if(filename.charAt(0) == '.') {
                ((CDBrowserController) parent).setShowHiddenFiles(true);
            }
            ((CDBrowserController) parent).reloadData(false);
            ((CDBrowserController) parent).setSelectedPath(file);
            return file;
        }
        return null;
    }
}