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
import ch.cyberduck.ui.cocoa.odb.Editor;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSPathUtilities;
import com.apple.cocoa.foundation.NSSize;

import java.util.Calendar;

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

    public CDDuplicateFileController(final CDWindowController parent) {
        super(parent);
        synchronized(parent) {
            if (!NSApplication.loadNibNamed("Duplicate", this)) {
                log.fatal("Couldn't load Duplicate.nib");
            }
        }
    }

    public void setFilenameField(NSTextField field) {
        super.setFilenameField(field);
        Path selected = ((CDBrowserController)parent).getSelectedPath();
        StringBuffer proposal = new StringBuffer();
        if(null == selected.getExtension()) {
            proposal.append(selected.getName());
        }
        else {
            proposal.append(selected.getName().substring(0, selected.getName().lastIndexOf(".")));
        }
        proposal.append(" (" + CDDateFormatter.getShortFormat(Calendar.getInstance().getTime().getTime()) + ")");
        if(null != selected.getExtension()) {
            proposal.append("."+selected.getExtension());
        }
        this.filenameField.setStringValue(proposal.toString());
    }

    public void callback(final int returncode) {
        Path selected = ((CDBrowserController)parent).getSelectedPath();
        if (returncode == DEFAULT_OPTION) {
            this.duplicateFile(selected, filenameField.stringValue(), false);
        }
        if (returncode == ALTERNATE_OPTION) {
            this.duplicateFile(selected, filenameField.stringValue(), true);
        }
    }

    protected Path getWorkdir() {
        return ((CDBrowserController)parent).getSelectedPath().getParent();
    }

    private void duplicateFile(final Path selected, final String filename, final boolean edit) {
        final CDBrowserController c = (CDBrowserController)parent;
        c.background(new Runnable() {
            public void run() {
                final Path file = (Path)selected.clone();
                file.setLocal(new Local(NSPathUtilities.temporaryDirectory(),
                        selected.getName()));
                file.download();
                file.setPath(selected.getParent().getAbsolute(), filename);
                file.upload();
                file.setLocal(null);
                if(file.exists()) {
                    if(edit) {
                        Editor editor = new Editor(Preferences.instance().getProperty("editor.bundleIdentifier"), c);
                        editor.open(file);
                    }
                    c.invoke(new Runnable() {
                        public void run() {
                            if(filename.charAt(0) == '.') {
                                c.setShowHiddenFiles(true);
                            }
                            c.reloadData(false);
                            c.setSelectedPath(file);
                        }
                    });
                }
            }
        });
    }
}