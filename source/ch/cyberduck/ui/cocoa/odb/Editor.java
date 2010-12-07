package ch.cyberduck.ui.cocoa.odb;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.editor.AbstractEditor;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.threading.BrowserBackgroundAction;

import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

/**
 * @version $Id$
 */
public abstract class Editor extends AbstractEditor {
    private static Logger log = Logger.getLogger(Editor.class);

    private BrowserController controller;

    /**
     * The editor application
     */
    protected String bundleIdentifier;

    /**
     * @param controller
     * @param bundleIdentifier
     */
    public Editor(BrowserController controller, String bundleIdentifier, Path path) {
        super(path);
        this.controller = controller;
        this.bundleIdentifier = bundleIdentifier;
    }

    /**
     * Open the file in the parent directory
     */
    public void open(final BackgroundAction download) {
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
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
     * @return True if the editor application is running
     */
    public boolean isOpen() {
        final NSEnumerator apps = NSWorkspace.sharedWorkspace().launchedApplications().objectEnumerator();
        NSObject next;
        while(((next = apps.nextObject()) != null)) {
            NSDictionary app = Rococoa.cast(next, NSDictionary.class);
            final NSObject identifier = app.objectForKey("NSApplicationBundleIdentifier");
            if(identifier.toString().equals(bundleIdentifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Upload the edited file to the server
     */
    @Override
    protected void save(final BackgroundAction upload) {
        log.debug("save");
        controller.background(new BrowserBackgroundAction(controller) {
            public void run() {
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