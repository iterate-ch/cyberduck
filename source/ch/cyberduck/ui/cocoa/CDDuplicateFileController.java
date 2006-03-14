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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSPathUtilities;
import com.apple.cocoa.foundation.NSSize;

/**
 * @version $Id$
 */
public class CDDuplicateFileController extends CDFileController {

    private NSImageView iconView;

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        NSImage icon = NSWorkspace.sharedWorkspace().iconForFileType(((CDBrowserController)parent).getSelectedPath().getExtension());
        icon.setScalesWhenResized(true);
        icon.setSize(new NSSize(64f, 64f));
        this.iconView.setImage(icon);
    }

    public CDDuplicateFileController(CDWindowController controller) {
        super(controller);
        if (!NSApplication.loadNibNamed("Duplicate", this)) {
            log.fatal("Couldn't load Duplicate.nib");
        }
    }

    public void setFilenameField(NSTextField field) {
        super.setFilenameField(field);
        this.filenameField.setStringValue(((CDBrowserController)parent).getSelectedPath().getName() + "-Copy");
    }

    public void callback(int returncode) {
        Path workdir = ((CDBrowserController)parent).getSelectedPath().getParent();
        if (returncode == DEFAULT_OPTION) {
            this.duplicateFile(workdir, filenameField.stringValue());
        }
        if (returncode == ALTERNATE_OPTION) {
            Path path = duplicateFile(workdir, filenameField.stringValue());
            if (path != null) {
                Editor editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"));
                editor.open(path);
            }
        }
    }

    protected Path duplicateFile(Path workdir, String filename) {
        Path file = PathFactory.createPath(workdir.getSession(),
                workdir.getAbsolute(),
                new Local(NSPathUtilities.temporaryDirectory(),
                        ((CDBrowserController)parent).getSelectedPath().getName()));
        file.download();
        file.setPath(workdir.getAbsolute(), filename);
        file.upload();
        if(file.exists()) {
            ((CDBrowserController)parent).setShowHiddenFiles(filename.charAt(0) == '.');
            ((CDBrowserController)parent).reloadData(true);
            return file;
        }
        return null;
    }
}