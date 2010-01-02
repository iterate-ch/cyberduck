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
import ch.cyberduck.core.TransferAction;
import ch.cyberduck.core.io.FileWatcher;
import ch.cyberduck.core.io.FileWatcherListener;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.apache.log4j.Logger;

/**
 * An editor listing for file system notifications on a particular folder
 *
 * @version $Id$
 */
public class WatchEditor extends Editor implements FileWatcherListener {
    private static Logger log = Logger.getLogger(WatchEditor.class);

    private FileWatcher monitor;

    /**
     * @param c
     * @param bundleIdentifier
     */
    public WatchEditor(BrowserController c, String bundleIdentifier, Path path) {
        super(c, bundleIdentifier, path);
    }

    @Override
    protected TransferAction getAction() {
        return TransferAction.ACTION_RENAME;
    }

    /**
     * Edit and watch the file for changes
     */
    @Override
    public void edit() {
        NSWorkspace.sharedWorkspace().openFile(edited.getLocal().getAbsolute(),
                NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier));
        monitor = new FileWatcher(edited.getLocal());
        monitor.register();
        monitor.addListener(this);
    }

    @Override
    protected void delete() {
        monitor.removeListener(this);
        super.delete();
    }

    @Override
    protected void setDeferredDelete(boolean deferredDelete) {
        if(!this.isOpen()) {
            this.delete();
        }
        super.setDeferredDelete(deferredDelete);
    }

    public void fileWritten(Local file) {
        log.info("fileWritten:" + file);
        this.save();
    }

    public void fileRenamed(Local file) {
        log.info("fileRenamed:" + file);
    }

    public void fileDeleted(Local file) {
        log.info("fileDeleted:" + file);
        monitor.removeListener(this);
    }
}
