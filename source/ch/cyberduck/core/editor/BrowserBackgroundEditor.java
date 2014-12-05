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
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.ApplicationLauncher;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.action.Worker;
import ch.cyberduck.ui.threading.WorkerBackgroundAction;

/**
 * @version $Id$
 */
public abstract class BrowserBackgroundEditor extends AbstractEditor {

    private Controller controller;

    private Session session;

    /**
     * @param controller  Browser
     * @param application Editor
     * @param path        Remote file
     */
    public BrowserBackgroundEditor(final Controller controller,
                                   final Session session,
                                   final Application application,
                                   final Path path) {

        this(controller, session,
                ApplicationLauncherFactory.get(),
                ApplicationFinderFactory.get(),
                application, path);
    }

    /**
     * @param controller  Browser
     * @param application Editor
     * @param path        Remote file
     */
    public BrowserBackgroundEditor(final Controller controller,
                                   final Session session,
                                   final ApplicationLauncher launcher,
                                   final ApplicationFinder finder,
                                   final Application application,
                                   final Path path) {
        super(launcher, finder, application, session, path, controller);
        this.controller = controller;
        this.session = session;
    }

    /**
     * Open the file in the parent directory
     */
    @Override
    public void open(final Worker<Transfer> download) {
        controller.background(new WorkerBackgroundAction<Transfer>(controller, session, download));
    }

    /**
     * Upload the edited file to the server
     */
    @Override
    public void save(final Worker<Transfer> upload) {
        controller.background(new WorkerBackgroundAction<Transfer>(controller, session, upload));
    }
}
