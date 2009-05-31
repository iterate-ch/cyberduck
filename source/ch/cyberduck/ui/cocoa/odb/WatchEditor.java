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

import ch.cyberduck.core.Path;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

/**
 * @version $Id:$
 */
public class WatchEditor extends Editor {
    private static Logger log = Logger.getLogger(WatchEditor.class);

    /**
     * @param c
     */
    public WatchEditor(CDBrowserController c, Path path) {
        this(c, null, path);
    }

    /**
     * @param c
     * @param bundleIdentifier
     */
    public WatchEditor(CDBrowserController c, String bundleIdentifier, Path path) {
        super(c, bundleIdentifier, path);
    }

    /**
     * Edit and watch the file for changes
     */
    public void edit() {
//        edited.getLocal().watch(new AbstractFileWatcherListener() {
//            public void fileWritten(Local file) {
//                log.debug("fileWritten:" + file);
//                save();
//                if(!isOpen()) {
//                    delete();
//                }
//            }
//        });
//        if(null == bundleIdentifier) {
//            NSWorkspace.sharedWorkspace().openFile(edited.getLocal().getAbsolute());
//        }
//        else {
//            NSWorkspace.sharedWorkspace().openFile(
//                    edited.getLocal().getAbsolute(),
//                    NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier)
//            );
//        }
    }

    public boolean isOpen() {
//        if(null == bundleIdentifier) {
//            final String fullpath = NSWorkspace.sharedWorkspace().applicationForFile(edited.getLocal().getAbsolute());
//            final NSEnumerator apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
//            NSObject next;
//            while(((next = apps.nextObject()) != null)) {
//                NSDictionary app = Rococoa.cast(next, NSDictionary.class);
//                if(fullpath.equals(app.objectForKey("NSApplicationPath").toString())) {
//                    return true;
//                }
//            }
//            return false;
//        }
        return super.isOpen();
    }
}
