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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class DefaultTemporaryFileService extends AbstractTemporaryFileService implements TemporaryFileService {

    private final Preferences preferences = PreferencesFactory.get();
    private final String delimiter = preferences.getProperty("local.delimiter");

    @Override
    public Local create(final Path file) {
        return this.create(new UUIDRandomStringService().random(), file);
    }

    @Override
    public Local create(final String name) {
        return this.create(LocalFactory.get(preferences.getProperty("tmp.dir"), new UUIDRandomStringService().random()), name);
    }

    /**
     * @return Path with /temporary directory/<uid>/shortened absolute parent path/<region><versionid>/filename
     */
    @Override
    public Local create(final String uid, final Path file) {
        /*
        $1%s: Delimiter
        $2%s: UID
        $3%s: Path
        $4%s: Attributes Hash
         */
        final String pathFormat = "%2$s%1$s%3$s%1$s%4$s";
        final String normalizedPathFormat = pathFormat + "%1$s%5$s";
        final String attributes = String.valueOf(file.attributes().hashCode());
        final int limit = preferences.getInteger("local.temporaryfiles.shortening.threshold") -
                new File(preferences.getProperty("tmp.dir"), String.format(normalizedPathFormat, delimiter, uid, "", attributes, file.getName())).getAbsolutePath().length();
        final Local folder = LocalFactory.get(preferences.getProperty("tmp.dir"), String.format(pathFormat, delimiter, uid,
                this.shorten(file.getParent().getAbsolute(), limit), attributes));
        return this.create(folder, StringUtils.isNotBlank(file.attributes().getDisplayname()) ? file.attributes().getDisplayname() : file.getName());
    }
}
