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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.io.FileWatcher;
import ch.cyberduck.core.io.FileWatcherListener;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

import java.io.IOException;

/**
 * @version $Id$
 */
public class WatchEditor extends Editor implements FileWatcherListener {
    private static Logger log = Logger.getLogger(WatchEditor.class);

    private FileWatcher monitor;

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
    @Override
    public void edit() {
        this.watch();
        if(null == bundleIdentifier) {
            NSWorkspace.sharedWorkspace().openFile(edited.getLocal().getAbsolute());
        }
        else {
            NSWorkspace.sharedWorkspace().openFile(edited.getLocal().getAbsolute(),
                    NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier));
        }
    }

    private void watch() {
        monitor = new FileWatcher(edited.getLocal());
        try {
            monitor.watch(this);
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    private void unwatch() {
        try {
            monitor.unwatch();
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected void delete() {
        this.unwatch();
        super.delete();
    }

    @Override
    public boolean isOpen() {
        if(null == bundleIdentifier) {
            final String fullpath = edited.getLocal().getDefaultEditor();
            final NSEnumerator apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
            NSObject next;
            while(((next = apps.nextObject()) != null)) {
                NSDictionary app = Rococoa.cast(next, NSDictionary.class);
                if(fullpath.equals(app.objectForKey("NSApplicationPath").toString())) {
                    return true;
                }
            }
            return false;
        }
        return super.isOpen();
    }

    @Override
    protected void setDeferredDelete(boolean deferredDelete) {
        if(!this.isOpen()) {
            this.delete();
        }
        super.setDeferredDelete(deferredDelete);
    }

    public void fileWritten(Local file) {
        log.debug("fileWritten:" + file);
        this.save();
    }

    public void fileRenamed(Local file) {
        log.debug("fileRenamed:" + file);
    }

    public void fileDeleted(Local file) {
        log.debug("fileDeleted:" + file);
        if(file.exists()) {
            this.save();
            this.watch();
        }
    }
}
