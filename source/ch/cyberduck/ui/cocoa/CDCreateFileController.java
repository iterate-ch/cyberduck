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

import com.apple.cocoa.application.NSAlertPanel;
import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSPanel;
import com.apple.cocoa.foundation.NSPathUtilities;

/**
 * @version $Id$
 */
public class CDCreateFileController extends CDFileController {

    public CDCreateFileController(CDWindowController parent) {
        super(parent);
        if (!NSApplication.loadNibNamed("File", this)) {
            log.fatal("Couldn't load File.nib");
        }
    }

    public void dismissedSheet(int returncode, Object contextInfo) {
        Path workdir = (Path) contextInfo;
        if (returncode == NSAlertPanel.DefaultReturn) {
            this.create(workdir, filenameField.stringValue());
        }
        if (returncode == NSAlertPanel.OtherReturn) {
            Path path = this.create(workdir, filenameField.stringValue());
            if (path != null) {
                Editor editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"));
                editor.open(path);
            }
        }
    }

    protected Path create(Path workdir, String filename) {
        Path file = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(), new Local(NSPathUtilities.temporaryDirectory(), filename));
        if (!file.getRemote().exists()) {
            try {
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
            catch (java.io.IOException e) {
                log.error(e.getMessage());
            }
        }
        if(file.exists()) {
            ((CDBrowserController)parent).setShowHiddenFiles(filename.charAt(0) == '.');
            ((CDBrowserController)parent).reloadData(true);
            ((CDBrowserController)parent).setSelectedPath(file);
            return file;
        }
        return null;
    }
}