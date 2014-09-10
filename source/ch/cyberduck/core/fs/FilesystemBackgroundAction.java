package ch.cyberduck.core.fs;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.DisabledAlertCallback;
import ch.cyberduck.core.threading.SessionBackgroundAction;

/**
 * @version $Id$
 */
public abstract class FilesystemBackgroundAction<T> extends SessionBackgroundAction<T> {

    private Cache<Path> cache;

    public FilesystemBackgroundAction(final Session session, final Cache<Path> cache) {
        super(session, cache, new DisabledAlertCallback(), new DisabledProgressListener(), new DisabledTranscriptListener(), new DisabledLoginController(),
                new DisabledHostKeyCallback());
        this.cache = cache;
    }

    @Override
    public void cleanup() {
        //
    }

    @Override
    public String getActivity() {
        return LocaleFactory.localizedString("Filesystem");
    }
}
