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

import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.editor.Editor;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;
import ch.cyberduck.ui.resources.IconCacheFactory;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @version $Id$
 */
public class CreateFileController extends FileController {

    public CreateFileController(final WindowController parent) {
        super(parent, NSAlert.alert(
                Locale.localizedString("Create new file", "File"),
                Locale.localizedString("Enter the name for the new file:", "File"),
                Locale.localizedString("Create", "File"),
                EditorFactory.instance().getDefaultEditor() != null ? Locale.localizedString("Edit", "File") : null,
                Locale.localizedString("Cancel", "File")
        ));
        alert.setIcon(IconCacheFactory.<NSImage>get().documentIcon(null, 64));
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createFile(this.getWorkdir(), inputField.stringValue(), false);
        }
        else if(returncode == ALTERNATE_OPTION) {
            this.createFile(this.getWorkdir(), inputField.stringValue(), true);
        }
    }

    protected void createFile(final Path workdir, final String filename, final boolean edit) {
        final BrowserController c = (BrowserController) parent;
        c.background(new BrowserBackgroundAction(c) {
            final Path file = new Path(workdir,
                    filename, Path.FILE_TYPE);

            @Override
            public void run() throws BackgroundException {
                final Session<?> session = c.getSession();
                final Touch feature = session.getFeature(Touch.class, new DisabledLoginController());
                feature.touch(file);
                if(edit) {
                    Editor editor = EditorFactory.instance().create(c, session, file);
                    editor.open();
                }
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        file.getName());
            }

            @Override
            public void cleanup() {
                super.cleanup();
                if(filename.charAt(0) == '.') {
                    c.setShowHiddenFiles(true);
                }
                c.reloadData(Collections.singletonList(file), Collections.singletonList(file));
            }
        });
    }
}