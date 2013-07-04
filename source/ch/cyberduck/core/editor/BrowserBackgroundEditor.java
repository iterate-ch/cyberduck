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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class BrowserBackgroundEditor extends AbstractEditor {
    private static final Logger log = Logger.getLogger(BrowserBackgroundEditor.class);

    private Controller controller;

    /**
     * @param controller  Browser
     * @param application Editor
     * @param path        Remote file
     */
    public BrowserBackgroundEditor(final Controller controller, final Session session,
                                   final Application application, final Path path) {
        super(application, session, path);
        this.controller = controller;
    }

    /**
     * Open the file in the parent directory
     */
    @Override
    public void open(final BackgroundAction<Void> download) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open %s in %s", edited.getLocal().getAbsolute(), this.getApplication()));
        }
        controller.background(new BrowserBackgroundAction((BrowserController) controller) {
            @Override
            public void run() throws BackgroundException {
                download.run();
            }

            @Override
            public String getActivity() {
                return download.getActivity();
            }

            @Override
            public void cleanup() {
                download.cleanup();
            }
        });
    }

    /**
     * Upload the edited file to the server
     */
    @Override
    public void save(final BackgroundAction<Void> upload) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Save changes from %s for %s", this.getApplication().getIdentifier(), edited.getLocal().getAbsolute()));
        }
        controller.background(new BrowserBackgroundAction((BrowserController) controller) {
            @Override
            public void run() throws BackgroundException {
                upload.run();
            }

            @Override
            public String getActivity() {
                return upload.getActivity();
            }

            @Override
            public void cleanup() {
                upload.cleanup();
            }
        });
    }
}
