package ch.cyberduck.fs;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.worker.Worker;

import java.util.Objects;

/**
 * @version $Id$
 */
public class FilesystemWorker extends Worker<Void> {

    private final Session<?> session;

    private final Filesystem fs;

    private final String url;

    public FilesystemWorker(final Session<?> session, final Filesystem fs) {
        this.session = session;
        this.fs = fs;
        this.url = new HostUrlProvider(true, true).get(session.getHost());
    }

    @Override
    public Void run() throws BackgroundException {
        fs.mount(session.getFeature(Home.class).find());
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final FilesystemWorker that = (FilesystemWorker) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
