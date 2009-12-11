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
import ch.cyberduck.ui.cocoa.application.NSImageView;
import ch.cyberduck.ui.cocoa.application.NSTextField;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public class DuplicateFileController extends FileController {

    public DuplicateFileController(final WindowController parent) {
        super(parent);
    }

    @Override
    public void setIconView(NSImageView iconView) {
        iconView.setImage(
                IconCache.instance().iconForExtension(((BrowserController) parent).getSelectedPath().getExtension(), 64)
        );
        super.setIconView(iconView);
    }

    @Override
    protected String getBundleName() {
        return "Duplicate";
    }

    @Override
    public void setFilenameField(NSTextField field) {
        super.setFilenameField(field);
        final Path selected = ((BrowserController) parent).getSelectedPath();
        StringBuffer proposal = new StringBuffer();
        proposal.append(FilenameUtils.getBaseName(selected.getName()));
        proposal.append(" (").append(DateFormatter.getShortFormat(System.currentTimeMillis()).replace('/', ':')).append(")");
        if(StringUtils.isNotEmpty(selected.getExtension())) {
            proposal.append(".").append(selected.getExtension());
        }
        this.filenameField.setStringValue(proposal.toString());
    }

    public void callback(final int returncode) {
        final Path selected = ((BrowserController) parent).getSelectedPath();
        if(returncode == DEFAULT_OPTION) {
            this.duplicateFile(selected, filenameField.stringValue(), false);
        }
        if(returncode == OTHER_OPTION) {
            this.duplicateFile(selected, filenameField.stringValue(), true);
        }
    }

    @Override
    protected Path getWorkdir() {
        return ((BrowserController) parent).getSelectedPath().getParent();
    }

    private void duplicateFile(final Path selected, final String filename, final boolean edit) {
        final Path duplicate = PathFactory.createPath(selected.getSession(), selected.getAsDictionary());
        duplicate.setPath(duplicate.getParent().getAbsolute(), filename);
        ((BrowserController) parent).duplicatePath(selected, duplicate, edit);
    }
}