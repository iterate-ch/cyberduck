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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class FlatTemporaryFileService extends AbstractTemporaryFileService implements TemporaryFileService {

    /**
     * Maximum length of a single path component
     */
    private static final int NAME_MAX = 255;

    private final Local temp;

    public FlatTemporaryFileService() {
        this(LocalFactory.get(PreferencesFactory.get().getProperty("tmp.dir")));
    }

    public FlatTemporaryFileService(final Local temp) {
        super(temp);
        this.temp = temp;
    }

    /**
     * Create random prefix for filename
     *
     * @param file Filename
     */
    @Override
    public Local create(final Path file) {
        return this.create(this.toFilename(new AlphanumericRandomStringService().random(), file.getName()));
    }

    /**
     * Use given random as prefix for filename
     *
     * @param uid  Prefix
     * @param file Filename
     */
    @Override
    public Local create(final String uid, final Path file) {
        final Local folder = LocalFactory.get(temp, uid);
        return this.create(folder, this.toFilename(String.valueOf(file.attributes().hashCode()),
                StringUtils.isNotBlank(file.attributes().getDisplayname()) ? file.attributes().getDisplayname() : file.getName()));
    }

    /**
     * @param prefix   Prefix
     * @param filename Filename
     * @return Filename with prefix. Replace filename with its hash retaining the extension if the result exceeds the maximum length of a path component
     */
    private String toFilename(final String prefix, final String filename) {
        final String name = String.format("%s-%s", prefix, filename);
        if(name.length() <= NAME_MAX) {
            return name;
        }
        final String hash = String.format("%s-%s", prefix, DigestUtils.md5Hex(filename));
        final String extension = Path.getExtension(filename);
        if(StringUtils.isNotBlank(extension) && hash.length() + extension.length() + 1 <= NAME_MAX) {
            return String.format("%s.%s", hash, extension);
        }
        return hash;
    }

    /**
     * @return temporary file with name in system temporary directory
     */
    @Override
    public Local create(final String name) {
        return this.delete(LocalFactory.get(temp, name));
    }
}
