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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.worker.MountWorker;

import org.apache.log4j.Logger;

import java.util.Objects;

public class FilesystemWorker extends MountWorker {
    private static final Logger log = Logger.getLogger(FilesystemWorker.class);

    private final Filesystem fs;

    private Filesystem.Options options = Filesystem.Options.readwrite;

    public FilesystemWorker(final Filesystem fs) {
        this(fs, PathCache.empty());
    }

    public FilesystemWorker(final Filesystem fs, final Cache<Path> cache) {
        this(fs, cache, new DisabledListProgressListener());
    }

    public FilesystemWorker(final Filesystem fs, final Cache<Path> cache, final ListProgressListener listener) {
        super(fs.getHost(), cache, listener, null, null, null);
        this.fs = fs;
    }

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        final Path workdir = super.run(session);
        fs.mount(workdir, options);
        return workdir;
    }

    public FilesystemWorker withOptions(final Filesystem.Options options) {
        this.options = options;
        return this;
    }

    @Override
    public void cancel() {
        try {
            fs.unmount();
        }
        catch(BackgroundException e) {
            log.warn(e.getMessage());
        }
        finally {
            super.cancel();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof FilesystemWorker)) {
            return false;
        }
        final FilesystemWorker that = (FilesystemWorker) o;
        return Objects.equals(fs, that.fs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fs);
    }
}
