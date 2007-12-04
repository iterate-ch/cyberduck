package ch.cyberduck.ui.cocoa.odb;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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
import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.io.AbstractFileWatcherListener;
import ch.cyberduck.ui.cocoa.CDBrowserController;

import org.apache.log4j.Logger;

import java.util.Enumeration;

/**
 * @version $Id:$
 */
public class WatchEditor extends Editor {
    private static Logger log = Logger.getLogger(WatchEditor.class);

    /**
     * @param c
     */
    public WatchEditor(CDBrowserController c) {
        this(c, null);
    }

    /**
     * @param c
     * @param bundleIdentifier
     */
    public WatchEditor(CDBrowserController c, String bundleIdentifier) {
        super(c, bundleIdentifier);
    }

    /**
     * Edit and watch the file for changes
     */
    public void edit() {
        edited.getLocal().watch(new AbstractFileWatcherListener() {
            public void fileWritten(Local file) {
                log.debug("fileWritten:" + file);
                save();
                if(!isOpen()) {
                    delete();
                }
            }
        });
        if(null == bundleIdentifier) {
            NSWorkspace.sharedWorkspace().openFile(edited.getLocal().getAbsolute());
        }
        else {
            NSWorkspace.sharedWorkspace().openFile(
                    edited.getLocal().getAbsolute(),
                    NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier)
            );
        }
    }

    public boolean isOpen() {
        if(null == bundleIdentifier) {
            final String fullpath = NSWorkspace.sharedWorkspace().applicationForFile(edited.getLocal().getAbsolute());

            final Enumeration apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
            while(apps.hasMoreElements()) {
                NSDictionary app = (NSDictionary) apps.nextElement();
                if(fullpath.equals(app.objectForKey("NSApplicationPath").toString())) {
                    return true;
                }
            }
            return false;
        }
        return super.isOpen();
    }
}
