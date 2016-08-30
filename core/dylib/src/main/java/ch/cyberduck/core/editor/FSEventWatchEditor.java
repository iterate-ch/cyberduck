package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.io.watchservice.FSEventWatchService;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * An editor listing for file system notifications on a particular folder

 */
public class FSEventWatchEditor extends AbstractEditor {
    private static final Logger log = Logger.getLogger(FSEventWatchEditor.class);

    private FileWatcher monitor
            = new FileWatcher(new FSEventWatchService());

    /**
     * With custom editor for file type.
     *
     * @param application Editor application
     * @param file        Remote file
     */
    public FSEventWatchEditor(final Application application, final Session session,
                              final Path file, final ProgressListener listener) {
        super(application, session, file, listener);
    }

    public void watch(final Local local, final FileWatcherListener listener) throws IOException {
        try {
            monitor.register(local, listener).await();
        }
        catch(InterruptedException e) {
            throw new IOException(String.format("Failure monitoring file %s", local), e);
        }
    }

    @Override
    public void delete() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Close monitor %s", monitor));
        }
        monitor.close();
        super.delete();
    }
}