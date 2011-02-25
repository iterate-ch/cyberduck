package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @version $Id$
 */
public class CreateSymlinkController extends FileController {

    public CreateSymlinkController(final WindowController parent) {
        super(parent, NSAlert.alert(
                Locale.localizedString("Create new symbolic link", "File"),
                StringUtils.EMPTY,
                Locale.localizedString("Create", "File"),
                null,
                Locale.localizedString("Cancel", "File")
        ));
        alert.setIcon(IconCache.aliasIcon(null, 64));
        final Path selected = this.getSelected();
        this.filenameField.setStringValue(FilenameUtils.getBaseName(selected.getName()));
        this.setMessage(MessageFormat.format(Locale.localizedString("Enter the name for the new symbolic link for {0}:", "File"),
                selected.getName()));
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createSymlink(this.getSelected(), filenameField.stringValue(), false);
        }
    }

    protected void createSymlink(final Path selected, final String symlink, final boolean edit) {
        final BrowserController c = (BrowserController) parent;
        final Path link = PathFactory.createPath(this.getSession(),
                this.getWorkdir().getAbsolute(), symlink, Path.FILE_TYPE);
        c.background(new BrowserBackgroundAction(c) {
            public void run() {
                // Symlink pointing to existing file
                link.symlink(selected.getName());
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        symlink);
            }

            @Override
            public void cleanup() {
                if(symlink.charAt(0) == '.') {
                    c.setShowHiddenFiles(true);
                }
                c.reloadData(Collections.singletonList(link));
            }
        });
    }
}