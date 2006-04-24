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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.PathFactory;
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

    public CDDuplicateFileController(CDWindowController parent) {
        super(parent);
        synchronized(parent) {
            if (!NSApplication.loadNibNamed("Duplicate", this)) {
                log.fatal("Couldn't load Duplicate.nib");
            }
        }
    }

    public void setFilenameField(NSTextField field) {
        super.setFilenameField(field);
        this.filenameField.setStringValue(((CDBrowserController)parent).getSelectedPath().getName() + "-Copy");
    }

    public void callback(int returncode) {
        Path selected = ((CDBrowserController)parent).getSelectedPath();
        if (returncode == DEFAULT_OPTION) {
            this.duplicateFile(selected, filenameField.stringValue());
        }
        if (returncode == ALTERNATE_OPTION) {
            Path path = this.duplicateFile(selected, filenameField.stringValue());
            if (path != null) {
                Editor editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"));
                editor.open(path);
            }
        }
    }

    protected Path getWorkdir() {
        return ((CDBrowserController)parent).getSelectedPath().getParent();
    }

    private Path duplicateFile(Path selected, String filename) {
        Path duplicate = (Path)selected.clone(selected.getSession());
        duplicate.setLocal(new Local(NSPathUtilities.temporaryDirectory(),
                selected.getName()));
        duplicate.download();
        duplicate.setPath(selected.getParent().getAbsolute(), filename);
        duplicate.upload();
        duplicate.setLocal(null);
        if(duplicate.exists()) {
            if(filename.charAt(0) == '.') {
                ((CDBrowserController)parent).setShowHiddenFiles(true);
            }
            ((CDBrowserController)parent).reloadData(false);
            ((CDBrowserController)parent).setSelectedPath(duplicate);
            return duplicate;
        }
        return null;
    }
}