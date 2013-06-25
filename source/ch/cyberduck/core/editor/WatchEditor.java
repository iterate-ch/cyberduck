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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.ui.Controller;

import org.apache.log4j.Logger;

/**
 * An editor listing for file system notifications on a particular folder
 *
 * @version $Id$
 */
public class WatchEditor extends BrowserBackgroundEditor implements FileWatcherListener {
    private static final Logger log = Logger.getLogger(WatchEditor.class);

    private FileWatcher monitor;

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

    @Override
    protected TransferAction getAction() {
        return TransferAction.ACTION_RENAME;
    }

    /**
     * Edit and watch the file for changes
     */
    @Override
    public void edit() {
        if(ApplicationLauncherFactory.get().open(this.getEdited().getLocal(), this.getApplication())) {
            this.watch();
        }
    }

    /**
     * Watch the file for changes
     */
    public void watch() {
        monitor = new FileWatcher(this.getEdited().getLocal());
        monitor.register();
        monitor.addListener(this);
    }

    @Override
    protected void delete() {
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
    }

    @Override
    public void fileDeleted(Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s deleted", file));
        }
        monitor.removeListener(this);
    }

    @Override
    public void fileCreated(Local file) {
        if(log.isInfoEnabled()) {
            log.info(String.format("File %s created", file));
        }
    }
}