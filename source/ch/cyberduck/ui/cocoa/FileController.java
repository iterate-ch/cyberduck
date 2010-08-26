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
import ch.cyberduck.core.Session;
import ch.cyberduck.ui.cocoa.application.NSButton;
import ch.cyberduck.ui.cocoa.application.NSImageView;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.foundation.NSRange;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * @version $Id$
 */
public abstract class FileController extends SheetController {
    protected static Logger log = Logger.getLogger(FileController.class);

    @Outlet
    protected NSImageView iconView;

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
    }

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

    @Override
    protected double getMaxWindowWidth() {
        return 500;
    }

    @Override
    protected double getMaxWindowHeight() {
        return this.window().frame().size.height.doubleValue();
    }

    public FileController(final WindowController parent) {
        super(parent);
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        filenameField.selectText(null);
        this.window().fieldEditor_forObject(true, filenameField).setSelectedRange(NSRange.NSMakeRange(
                new NSUInteger(0), new NSUInteger(FilenameUtils.getBaseName(filenameField.stringValue()).length())
        ));
    }

    /**
     * @return The current working directory or selected folder
     */
    protected Path getWorkdir() {
        if(((BrowserController) parent).getSelectionCount() == 1) {
            final Path selected = ((BrowserController) parent).getSelectedPath();
            return selected.getParent();
        }
        return ((BrowserController) parent).workdir();
    }

    protected Session getSession() {
        return ((BrowserController) parent).getSession();
    }

    @Override
    protected boolean validateInput() {
        if(StringUtils.contains(filenameField.stringValue(), Path.DELIMITER)) {
            return false;
        }
        if(StringUtils.isNotBlank(filenameField.stringValue())) {
            Path file = PathFactory.createPath(this.getSession(), this.getWorkdir().getAbsolute(),
                    filenameField.stringValue(), Path.FILE_TYPE);
            return !file.exists();
        }
        return false;
    }
}
