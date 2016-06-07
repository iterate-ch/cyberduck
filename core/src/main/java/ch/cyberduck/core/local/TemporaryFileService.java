package ch.cyberduck.core.local;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultPathReference;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.File;
import java.util.UUID;

public class TemporaryFileService {

    private final String delimiter
            = PreferencesFactory.get().getProperty("local.delimiter");

    public Local create(final Path file) {
        return this.create(UUID.randomUUID().toString(), file);
    }

    /**
     * @return Path with /temporary directory/<uid>/shortened absolute parent path/<region><versionid>/filename
     */
    public Local create(final String uid, final Path file) {
        final Local folder = LocalFactory.get(
                new File(PreferencesFactory.get().getProperty("tmp.dir"),
                        uid + delimiter + this.shorten(file.getParent().getAbsolute())
                                + delimiter + new DefaultPathReference(file).attributes()).getAbsolutePath());
        return LocalFactory.get(folder, PathNormalizer.name(file.getAbsolute()));
    }

    protected String shorten(final String path) {
        return path;
    }
}
