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

import ch.cyberduck.binding.Proxy;
import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.io.watchservice.FSEventWatchService;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.FileWatcher;
import ch.cyberduck.core.local.FileWatcherListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Using FSEvents API and WorkspaceDidTerminateApplicationNotification
 */
public class FSEventWatchEditor extends DefaultWatchEditor {
    private static final Logger log = LogManager.getLogger(FSEventWatchEditor.class);

    private final NSWorkspace workspace = NSWorkspace.sharedWorkspace();

    /**
     * Registered callbacks
     */
    private final Map<Application, Set<ApplicationQuitCallback>> registered = new HashMap<>();

    private final Proxy terminate = new Proxy() {
        public void terminated(final NSNotification notification) {
            log.debug("Received notification {} from workspace", notification.userInfo());
            if(notification.userInfo().objectForKey("NSApplicationBundleIdentifier") == null) {
                log.warn("Missing NSApplicationBundleIdentifier in notification dictionary");
                return;
            }
            final Application application = new Application(notification.userInfo().objectForKey(
                    "NSApplicationBundleIdentifier").toString());
            // Do cleanup if application matches
            for(ApplicationQuitCallback callback : registered.getOrDefault(application, Collections.emptySet())) {
                log.info("Run quit callback {} for application {}", callback, application);
                callback.callback();
            }
        }
    };

    public FSEventWatchEditor(final Host host, final Path file, final ProgressListener listener) {
        super(host, file, listener, new FileWatcher(new FSEventWatchService()));
        workspace.notificationCenter().addObserver(terminate.id(),
                Foundation.selector("terminated:"),
                NSWorkspace.WorkspaceDidTerminateApplicationNotification,
                null);
    }

    @Override
    protected void watch(final Application application, final Local temporary, final FileWatcherListener listener, final ApplicationQuitCallback quit) throws IOException {
        log.info("Register application {} for terminate callback {}", application, quit);
        final Set<ApplicationQuitCallback> callbacks = registered.getOrDefault(application, new HashSet<>());
        callbacks.add(quit);
        registered.putIfAbsent(application, callbacks);
        super.watch(application, temporary, listener, quit);
    }

    @Override
    public void close() {
        log.warn("Remove observer {}", terminate);
        workspace.notificationCenter().removeObserver(terminate.id());
        super.close();
    }
}
