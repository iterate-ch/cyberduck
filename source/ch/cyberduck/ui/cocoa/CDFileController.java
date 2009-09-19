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
import ch.cyberduck.ui.cocoa.application.NSButton;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDFileController extends CDSheetController {
    protected static Logger log = Logger.getLogger(CDFileController.class);

    @Outlet
    protected NSTextField filenameField;

    public void setFilenameField(NSTextField filenameField) {
        this.filenameField = filenameField;
    }

    @Outlet
    private NSButton editButton;

    public void setEditButton(NSButton editButton) {
        this.editButton = editButton;
        this.editButton.setEnabled(EditorFactory.defaultEditor() != null);
    }

    public CDFileController(final CDWindowController parent) {
        super(parent);
    }

    /**
     * @return The current working directory or selected folder
     */
    protected Path getWorkdir() {
        if(((CDBrowserController) parent).getSelectionCount() == 1) {
            final Path selected = ((CDBrowserController) parent).getSelectedPath();
            if(selected.attributes.isDirectory()) {
                return selected;
            }
            return selected.getParent();
        }
        return ((CDBrowserController) parent).workdir();
    }

    @Override
    protected boolean validateInput() {
        if(filenameField.stringValue().indexOf('/') != -1) {
            return false;
        }
        if(StringUtils.isNotBlank(filenameField.stringValue())) {
            Path file = PathFactory.createPath(this.getWorkdir().getSession(), this.getWorkdir().getAbsolute(),
                    filenameField.stringValue(), Path.FILE_TYPE);
            return !file.exists();
        }
        return false;
    }
}
