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

import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSTextField;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public abstract class CDFileController extends CDSheetController {
    protected static Logger log = Logger.getLogger(CDFileController.class);

    protected NSTextField filenameField; //IBOutlet

    public void setFilenameField(NSTextField filenameField) {
        this.filenameField = filenameField;
    }

    private NSButton editButton; //IBOutlet

    public void setEditButton(NSButton editButton) {
        this.editButton = editButton;
        this.editButton.setEnabled(EditorFactory.defaultEditor() != null);
    }

    public CDFileController(final CDWindowController parent) {
        super(parent);
    }

    protected Path getWorkdir() {
        Path workdir;
        if(((CDBrowserController)parent).getSelectionCount() == 1) {
            workdir = (Path)((CDBrowserController)parent).getSelectedPath().getParent();
        }
        else {
            workdir = ((CDBrowserController) parent).workdir();
        }
        return workdir;
    }

    protected boolean validateInput() {
        if (filenameField.stringValue().indexOf('/') != -1) {
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
