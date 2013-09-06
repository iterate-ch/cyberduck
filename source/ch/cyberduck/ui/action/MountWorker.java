package ch.cyberduck.ui.action;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class MountWorker extends Worker<Path> {

    private Session<?> session;

    private Cache cache;

    private ListProgressListener listener;

    protected MountWorker(final Session<?> session, final Cache cache, final ListProgressListener listener) {
        this.session = session;
        this.cache = cache;
        this.listener = listener;
    }

    /**
     * Mount the default path of the configured host or the home directory as returned by the server
     * when not given.
     */
    @Override
    public Path run() throws BackgroundException {
        try {
            final Path home = new DefaultHomeFinderService(session).find();
            // Retrieve directory listing of default path
            cache.put(home.getReference(), new SessionListWorker(session, cache, home, listener).run());
            return home;
        }
        catch(NotfoundException e) {
            // The default path does not exist or is not readable due to possible permission issues. Fallback
            // to default working directory
            final Path workdir = session.workdir();
            cache.put(workdir.getReference(), new SessionListWorker(session, cache, workdir, listener).run());
            return workdir;
        }
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Mounting {0}", "Status"),
                session.getHost().getHostname());
    }
}
