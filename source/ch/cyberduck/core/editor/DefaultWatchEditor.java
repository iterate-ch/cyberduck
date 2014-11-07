package ch.cyberduck.core.editor;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.io.watchservice.NIOEventWatchService;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.ui.Controller;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public class DefaultWatchEditor extends BrowserBackgroundEditor {
    private static final Logger log = Logger.getLogger(DefaultWatchEditor.class);

    private FileWatcher monitor
            = new FileWatcher(new NIOEventWatchService());

    public DefaultWatchEditor(final Controller controller,
                              final Session session,
                              final Application application,
                              final Path path) {
        super(controller, session, application, path);
    }

    public DefaultWatchEditor(final Controller controller,
                              final Session session,
                              final ApplicationLauncher launcher,
                              final Application application,
                              final Path path) {
        super(controller, session, launcher, application, path);
    }

    public void watch(final Local local) throws IOException {
        try {
            monitor.register(local, new DefaultEditorListener(this)).await();
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

