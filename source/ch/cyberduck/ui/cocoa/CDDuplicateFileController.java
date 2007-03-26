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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSImageView;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSSize;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class CDDuplicateFileController extends CDFileController {

    private NSImageView iconView;

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        NSImage icon = NSWorkspace.sharedWorkspace().iconForFileType(((CDBrowserController)parent).getSelectedPath().getExtension());
        icon.setSize(new NSSize(64f, 64f));
        this.iconView.setImage(icon);
    }

    public CDDuplicateFileController(final CDWindowController parent) {
        super(parent);
        synchronized(NSApplication.sharedApplication()) {
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
        proposal.append(" (" + CDDateFormatter.getShortFormat(System.currentTimeMillis(),
                selected.getHost().getTimezone()).replace('/', ':') + ")");
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
        return (Path)((CDBrowserController)parent).getSelectedPath().getParent();
    }

    private void duplicateFile(final Path selected, final String filename, final boolean edit) {
        final Path duplicate = (Path)selected.clone();
        duplicate.setPath(duplicate.getParent().getAbsolute(), filename);
        ((CDBrowserController)parent).duplicatePath(selected, duplicate, edit);
    }
}