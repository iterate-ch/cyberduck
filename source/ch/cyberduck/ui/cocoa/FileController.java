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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.foundation.NSRange;

import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSUInteger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class FileController extends AlertController {
    protected static Logger log = Logger.getLogger(FileController.class);

    /**
     *
     */
    protected NSTextField filenameField
            = NSTextField.textfieldWithFrame(new NSRect(0, 22));

    public FileController(final WindowController parent, NSAlert alert) {
        super(parent, alert);
    }

    @Override
    public void beginSheet() {
        this.setAccessoryView(filenameField);
        alert.setShowsHelp(true);
        super.beginSheet();
        filenameField.selectText(null);
        this.window().fieldEditor_forObject(true, filenameField).setSelectedRange(NSRange.NSMakeRange(
                new NSUInteger(0), new NSUInteger(FilenameUtils.getBaseName(filenameField.stringValue()).length())
        ));
    }

    @Override
    protected void focus() {
        // Focus accessory view.
        filenameField.selectText(null);
        this.window().makeFirstResponder(filenameField);
    }

    /**
     * @return The current working directory or selected folder
     */
    protected Path getWorkdir() {
        if(((BrowserController) parent).getSelectionCount() == 1) {
            final Path selected = ((BrowserController) parent).getSelectedPath();
            if(null != selected) {
                return selected.getParent();
            }
        }
        return ((BrowserController) parent).workdir();
    }

    protected Path getSelected() {
        return ((BrowserController) parent).getSelectedPath();
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

    @Override
    protected void help() {
        StringBuilder site = new StringBuilder(Preferences.instance().getProperty("website.help"));
        site.append("/howto/browser");
        openUrl(site.toString());
    }
}
