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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.NSImageView;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;

/**
 * @version $Id$
 */
public class CDFolderController extends CDFileController {
    private static Logger log = Logger.getLogger(CDFolderController.class);

    @Outlet
    protected NSImageView iconView;

    public void setIconView(NSImageView iconView) {
        this.iconView = iconView;
        this.iconView.setImage(CDIconCache.iconNamed("newfolder.icns", 128));
    }

    public CDFolderController(final CDWindowController parent) {
        super(parent);
    }

    @Override
    protected String getBundleName() {
        return "Folder";
    }

    public void callback(int returncode) {
        if(returncode == DEFAULT_OPTION) {
            this.createFolder(this.getWorkdir(), filenameField.stringValue());
        }
    }

    protected void createFolder(final Path workdir, final String filename) {
        final CDBrowserController c = (CDBrowserController) parent;
        c.background(new BrowserBackgroundAction(c) {
            final Path folder
                    = PathFactory.createPath(workdir.getSession(), workdir.getAbsolute(),
                    filename, Path.DIRECTORY_TYPE);

            public void run() {
                folder.mkdir(false);
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                        folder.writePermissions(new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default")),
                                false);
                    }
                }
                folder.cache().put(folder, AttributedList.<Path>emptyList());
                folder.getParent().invalidate();
            }

            @Override
            public String getActivity() {
                return MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        folder.getName());
            }

            @Override
            public void cleanup() {
                if(filename.charAt(0) == '.') {
                    c.setShowHiddenFiles(true);
                }
                c.reloadData(Collections.singletonList(folder));
            }
        });
    }
}
