package ch.cyberduck.ui.cocoa.odb;

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

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDController;
import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;

import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.io.File;

/**
 * @version $Id$
 */
public abstract class Editor extends CDController {
    private static Logger log = Logger.getLogger(Editor.class);

    /**
     *
     */
    private Local TEMPORARY_DIRECTORY
            = new Local(Preferences.instance().getProperty("editor.tmp.directory"));

    private CDBrowserController controller;

    /**
     * The edited path
     */
    protected Path edited;

    /**
     * The editor application
     */
    protected String bundleIdentifier;

    /**
     * @param controller
     * @param bundleIdentifier
     */
    public Editor(CDBrowserController controller, String bundleIdentifier) {
        this.controller = controller;
        this.bundleIdentifier = bundleIdentifier;
    }

    /**
     * 
     * @param path
     */
    public void open(Path path) {
        edited = PathFactory.createPath(path.getSession(), path.getAsDictionary());

        Local folder = new Local(new File(TEMPORARY_DIRECTORY.getAbsolute(),
                edited.getParent().getAbsolute()));
        folder.mkdir(true);

        String filename = edited.getName();
        String proposal = filename;
        int no = 0;
        int index = filename.lastIndexOf(".");
        do {
            edited.getLocal().setPath(folder.getAbsolute(), proposal);
            no++;
            if(index != -1 && index != 0) {
                proposal = filename.substring(0, index) + "-" + no + filename.substring(index);
            } else {
                proposal = filename + "-" + no;
            }
        }
        while(edited.getLocal().exists());

        controller.background(new BackgroundAction() {
            public void run() {
                edited.download();
            }

            public void cleanup() {
                if(edited.getStatus().isComplete()) {
                    edited.getSession().message(NSBundle.localizedString("Download complete", "Growl", "Growl Notification"));
                    final Permission permissions = edited.getLocal().attributes.getPermission();
                    if(null != permissions) {
                        permissions.getOwnerPermissions()[Permission.READ] = true;
                        permissions.getOwnerPermissions()[Permission.WRITE] = true;
                        edited.getLocal().writePermissions(permissions, false);
                    }
                    // Important, should always be run on the main thread; otherwise applescript crashes
                    Editor.this.edit();
                }
            }
        });
    }

    /**
     * @return True if the editor is open
     */
    public boolean isOpen() {
        final Enumeration apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
        while(apps.hasMoreElements()) {
            NSDictionary app = (NSDictionary) apps.nextElement();
            final Object identifier = app.objectForKey("NSApplicationBundleIdentifier");
            if(identifier.equals(bundleIdentifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    protected abstract void edit();

    /**
     *
     */
    protected void delete() {
        log.debug("delete");
        edited.getLocal().delete();
        for(AbstractPath parent = edited.getLocal().getParent(); !parent.equals(TEMPORARY_DIRECTORY); parent = parent.getParent())
        {
            if(parent.isEmpty()) {
                parent.delete();
            }
        }
        this.invalidate();
    }

    /**
     * The file has been closed in the editor while the upload was in progress
     */
    protected boolean deferredDelete;

    /**
     * Upload the edited file to the server
     */
    protected void save() {
        log.debug("save");
        controller.background(new BackgroundAction() {
            public void run() {
                edited.upload();
            }

            public void cleanup() {
                if(edited.getStatus().isComplete()) {
                    Growl.instance().notify("Upload complete", edited.getName());
                    if(deferredDelete) {
                        delete();
                    }
                    controller.reloadData(true);
                }
            }
        });
    }
}