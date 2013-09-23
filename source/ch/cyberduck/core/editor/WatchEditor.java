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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.ui.Controller;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * An editor listing for file system notifications on a particular folder
 *
 * @version $Id$
 */
public class WatchEditor extends BrowserBackgroundEditor implements FileWatcherListener {
    private static final Logger log = Logger.getLogger(WatchEditor.class);

    private FileWatcher monitor = new FileWatcher();

    /**
     * With custom editor for file type.
     *
     * @param c           Browser
     * @param application Editor application
     * @param path        Remote file
     */
    public WatchEditor(final Controller c, final Session session, final Application application, final Path path) {
        super(c, session, application, path);
    }

    /**
     * Edit and watch the file for changes
     */
    @Override
    public void edit() throws IOException {
        final Application application = this.getApplication();
        if(ApplicationLauncherFactory.get().open(edited.getLocal(), application)) {
            this.watch();
        }
        else {
            throw new IOException(String.format("Failed to open application %s", application.getName()));
        }
    }

    /**
     * Watch the file for changes
     */
    public void watch() throws IOException {
        try {
            monitor.register(edited.getLocal()).await();
        }
        catch(InterruptedException e) {
            throw new IOException(String.format("Failure monitoring file %s", edited.getLocal()), e);
        }
        monitor.addListener(this);
    }

    @Override
    protected void delete() {
        monitor.close(edited.getLocal());
        monitor.removeListener(this);
        super.delete();
    }

    @Override
    public void fileWritten(Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s written", file));
        }
        this.save();
    }

    @Override
    public void fileRenamed(Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s renamed", file));
        }
        monitor.close(edited.getLocal());
        monitor.removeListener(this);
    }

    @Override
    public void fileDeleted(Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s deleted", file));
        }
        monitor.close(edited.getLocal());
        monitor.removeListener(this);
    }

    @Override
    public void fileCreated(Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s created", file));
        }
        this.save();
    }
}