package ch.cyberduck.core.io;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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
import ch.cyberduck.core.LocalFactory;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class FileWatcher implements FileMonitor.FileListener {
    private static Logger log = Logger.getLogger(FileWatcher.class);

    private FileMonitor monitor = FileMonitor.getInstance();

    private static FileWatcher instance = null;

    private FileWatcher() {
        monitor.addFileListener(this);
    }

    private static final Object lock = new Object();

    public static FileWatcher instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new FileWatcher();
            }
            return instance;
        }
    }

    private Map<File, FileWatcherListener> listeners = new HashMap<File, FileWatcherListener>();

    public void watch(final Local file, final FileWatcherListener listener) throws IOException {
        final File f = new File(file.getAbsolute());
        monitor.addWatch(f);
        listeners.put(f, listener);
    }

    public void unwatch(final Local file) throws IOException {
        final File f = new File(file.getAbsolute());
        monitor.removeWatch(f);
        listeners.remove(f);
    }

    public void fileChanged(FileMonitor.FileEvent e) {
        final FileWatcherListener listener = listeners.get(e.getFile());
        if(null == listener) {
            log.error("No listener for " + e);
            return;
        }
        if(e.getType() == FileMonitor.FILE_MODIFIED || e.getType() == FileMonitor.FILE_SIZE_CHANGED) {
            listener.fileWritten(LocalFactory.createLocal(e.getFile()));
        }
        if(e.getType() == FileMonitor.FILE_DELETED) {
            listener.fileDeleted(LocalFactory.createLocal(e.getFile()));
        }
        if(e.getType() == FileMonitor.FILE_RENAMED) {
            listener.fileRenamed(LocalFactory.createLocal(e.getFile()));
        }
    }
}
