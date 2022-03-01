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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.io.watchservice.FSEventWatchService;
import ch.cyberduck.core.local.FileWatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Using FSEvents API
 */
public class FSEventWatchEditor extends DefaultWatchEditor {
    private static final Logger log = LogManager.getLogger(FSEventWatchEditor.class);

    public FSEventWatchEditor(final ProgressListener listener) {
        super(new FileWatcher(new FSEventWatchService()), listener);
    }
}
