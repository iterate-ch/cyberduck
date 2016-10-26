package ch.cyberduck.core.local;

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
import ch.cyberduck.binding.foundation.NSDistributedNotificationCenter;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.Local;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;

import java.util.HashMap;
import java.util.Map;

public final class WorkspaceApplicationLauncher implements ApplicationLauncher {
    private static final Logger log = Logger.getLogger(WorkspaceApplicationLauncher.class);

    private final NSWorkspace workspace
            = NSWorkspace.sharedWorkspace();

    private final Map<Application, ApplicationQuitCallback> registered
            = new HashMap<Application, ApplicationQuitCallback>();

    public void register(final Application application, final ApplicationQuitCallback callback) {
        workspace.notificationCenter().addObserver(terminate.id(),
                Foundation.selector("terminated:"),
                NSWorkspace.WorkspaceDidTerminateApplicationNotification,
                null);
        if(log.isInfoEnabled()) {
            log.info(String.format("Register application %s for callback %s", application, callback));
        }
        registered.put(application, callback);
    }

    private final Proxy terminate = new Proxy() {
        public void terminated(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received notification %s from workspace", notification.userInfo()));
            }
            if(notification.userInfo().objectForKey("NSApplicationBundleIdentifier") == null) {
                log.warn("Missing NSApplicationBundleIdentifier in notification dictionary");
                return;
            }
            final Application application = new Application(notification.userInfo().objectForKey(
                    "NSApplicationBundleIdentifier").toString());
            if(registered.containsKey(application)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Run quit callback for application %s", application));
                }
                // Do cleanup if application matches
                registered.get(application).callback();
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        try {
            log.warn(String.format("Callback for %s is finalized", terminate));
            workspace.notificationCenter().removeObserver(terminate.id());
        }
        finally {
            super.finalize();
        }
    }

    @Override
    public boolean open(final Local file) {
        synchronized(NSWorkspace.class) {
            if(!workspace.openFile(file.getAbsolute())) {
                log.warn(String.format("Error opening file %s", file));
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean open(final Local file, final Application application, final ApplicationQuitCallback callback) {
        synchronized(NSWorkspace.class) {
            final String path = workspace.absolutePathForAppBundleWithIdentifier(application.getIdentifier());
            if(StringUtils.isNotBlank(path)) {
                if(workspace.openFile(file.getAbsolute(), path)) {
                    this.register(application, callback);
                    return true;
                }
            }
            log.warn(String.format("Error opening file %s with application %s", file, application));
            return false;
        }
    }

    @Override
    public boolean open(final Application application, final String args) {
        throw new UnsupportedOperationException();
    }

    /**
     * Post a download finished notification to the distributed notification center. Will cause the
     * download folder to bounce just once.
     */
    @Override
    public void bounce(final Local file) {
        synchronized(NSWorkspace.class) {
            NSDistributedNotificationCenter.defaultCenter().postNotification(
                    NSNotification.notificationWithName("com.apple.DownloadFileFinished", file.getAbsolute())
            );
        }
    }
}
