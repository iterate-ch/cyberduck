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
import ch.cyberduck.ui.Controller;
import ch.cyberduck.ui.cocoa.ProxyController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;

import java.io.IOException;

/**
 * An editor listing for file system notifications on a particular folder
 *
 * @version $Id$
 */
public class FSEventWatchEditor extends BrowserBackgroundEditor {
    private static final Logger log = Logger.getLogger(FSEventWatchEditor.class);

    private FileWatcher monitor = new FileWatcher();

    /**
     * With custom editor for file type.
     *
     * @param controller  Browser
     * @param application Editor application
     * @param file        Remote file
     */
    public FSEventWatchEditor(final Controller controller, final Session session,
                              final Application application, final Path file) {
        super(controller, session, application, file);
    }

    private ProxyController terminate = new ProxyController() {
        public void terminated(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received notification %s from workspace", notification.userInfo()));
            }
            if(notification.userInfo().objectForKey("NSApplicationBundleIdentifier") == null) {
                log.warn("Missing NSApplicationBundleIdentifier in notification dictionary");
            }
            // Do cleanup if application matches current editor
            if(FSEventWatchEditor.this.getApplication()
                    .equals(new Application(notification.userInfo().objectForKey("NSApplicationBundleIdentifier").toString()))) {
                FSEventWatchEditor.this.delete();
                NSWorkspace.sharedWorkspace().notificationCenter().removeObserver(terminate.id());
            }
        }
    };

    /**
     * Edit and watch the file for changes
     */
    @Override
    public void edit() throws IOException {
        final Application application = this.getApplication();
        if(ApplicationLauncherFactory.get().open(local, application)) {
            NSWorkspace.sharedWorkspace().notificationCenter().addObserver(terminate.id(),
                    Foundation.selector("terminated:"),
                    NSWorkspace.WorkspaceDidTerminateApplicationNotification,
                    null);
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
        this.watch(new DefaultEditorListener(this));
    }

    public void watch(final FileWatcherListener listener) throws IOException {
        try {
            monitor.register(local).await();
        }
        catch(InterruptedException e) {
            throw new IOException(String.format("Failure monitoring file %s", local), e);
        }
        monitor.addListener(listener);
    }

    @Override
    public void delete() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Close monitor %s for %s", monitor, local));
        }
        monitor.close(local);
        super.delete();
    }
}