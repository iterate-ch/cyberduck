package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class FlatTemporaryFileService extends AbstractTemporaryFileService implements TemporaryFileService {
    /**
     * Create random prefix for filename
     *
     * @param file Filename
     */
    @Override
    public Local create(final Path file) {
        return this.create(String.format("%s-%s", new AlphanumericRandomStringService().random(), file.getName()));
    }

    /**
     * Use given random as prefix for filename
     *
     * @param uid  Prefix
     * @param file Filename
     */
    @Override
    public Local create(final String uid, final Path file) {
        final Local folder = LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir"), uid);
        return this.create(folder, String.format("%d-%s", file.attributes().hashCode(), file.getName()));
    }

    /**
     * @return temporary file with name in system temporary directory
     */
    @Override
    public Local create(final String name) {
        return this.delete(LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir"), name));
    }
}
