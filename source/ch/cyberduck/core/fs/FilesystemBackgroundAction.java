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

import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.DisabledAlertCallback;
import ch.cyberduck.core.threading.SessionBackgroundAction;

import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class FilesystemBackgroundAction<T> extends SessionBackgroundAction<T> {

    private Session<?> session;

    public FilesystemBackgroundAction(final Session session) {
        super(new DisabledAlertCallback(), new DisabledProgressListener(), new DisabledTranscriptListener(), new DisabledLoginController(),
                new DefaultHostKeyController());
        this.session = session;
    }

    @Override
    public List<Session<?>> getSessions() {
        return Collections.<Session<?>>singletonList(session);
    }

    public T run() throws BackgroundException {
        throw new BackgroundException("Not supported");
    }

    @Override
    public void cleanup() {
        //
    }

    @Override
    public String getActivity() {
        return Locale.localizedString("Filesystem");
    }
}
