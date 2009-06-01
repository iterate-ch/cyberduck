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
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSImageView;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public class CDDuplicateFileController extends CDFileController {

    private NSImageView iconView;

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        NSImage icon = NSWorkspace.sharedWorkspace().iconForFileType(((CDBrowserController) parent).getSelectedPath().getExtension());
        this.iconView.setImage(CDIconCache.instance().convert(icon, 64));
    }

    public CDDuplicateFileController(final CDWindowController parent) {
        super(parent);
    }

    protected String getBundleName() {
        return "Duplicate";
    }

    public void setFilenameField(NSTextField field) {
        super.setFilenameField(field);
        final Path selected = ((CDBrowserController) parent).getSelectedPath();
        StringBuffer proposal = new StringBuffer();
        proposal.append(FilenameUtils.getBaseName(selected.getName()));
        proposal.append(" (" + CDDateFormatter.getShortFormat(System.currentTimeMillis()).replace('/', ':') + ")");
        if(StringUtils.isNotEmpty(selected.getExtension())) {
            proposal.append("." + selected.getExtension());
        }
        this.filenameField.setStringValue(proposal.toString());
    }

    public void callback(final int returncode) {
        final Path selected = ((CDBrowserController) parent).getSelectedPath();
        if(returncode == DEFAULT_OPTION) {
            this.duplicateFile(selected, filenameField.stringValue(), false);
        }
        if(returncode == ALTERNATE_OPTION) {
            this.duplicateFile(selected, filenameField.stringValue(), true);
        }
    }

    protected Path getWorkdir() {
        return (Path) ((CDBrowserController) parent).getSelectedPath().getParent();
    }

    private void duplicateFile(final Path selected, final String filename, final boolean edit) {
        final Path duplicate = PathFactory.createPath(selected.getSession(), selected.getAsDictionary());
        duplicate.setPath(duplicate.getParent().getAbsolute(), filename);
        ((CDBrowserController) parent).duplicatePath(selected, duplicate, edit);
    }
}