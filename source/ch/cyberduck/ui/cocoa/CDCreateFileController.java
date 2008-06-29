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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSPathUtilities;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.ui.cocoa.odb.Editor;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @version $Id$
 */
public class CDCreateFileController extends CDFileController {

    public CDCreateFileController(final CDWindowController parent) {
        super(parent);
    }

    protected String getBundleName() {
        return "File";
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createFile(this.getWorkdir(), filenameField.stringValue(), false);
        }
        if(returncode == ALTERNATE_OPTION) {
            this.createFile(this.getWorkdir(), filenameField.stringValue(), true);
        }
    }

    protected void createFile(final Path workdir, final String filename, final boolean edit) {
        final CDBrowserController c = (CDBrowserController)parent;
        c.background(new BrowserBackgroundAction(c) {
            final Path file = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(),
                    new Local(NSPathUtilities.temporaryDirectory(), filename));

            public void run() {
                int no = 0;
                int index = filename.lastIndexOf(".");
                while(file.getLocal().exists()) {
                    no++;
                    String proposal;
                    if(index != -1) {
                        proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
                    }
                    else {
                        proposal = filename + "-" + no;
                    }
                    file.setLocal(new Local(NSPathUtilities.temporaryDirectory(), proposal));
                }
                file.getLocal().touch();
                file.upload();
                file.getLocal().delete(false);
                if(file.exists()) {
                    if(edit) {
                        Editor editor = EditorFactory.createEditor(c, file.getLocal(), file);
                        editor.open();
                    }
                }
            }

            public String getActivity() {
                return MessageFormat.format(NSBundle.localizedString("Uploading {0}", "Status", ""),
                        file.getName());
            }

            public void cleanup() {
                if(filename.charAt(0) == '.') {
                    c.setShowHiddenFiles(true);
                }
                c.reloadData(Collections.singletonList(file));
            }
        });
    }
}