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
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultTemporaryFileService implements TemporaryFileService {
    private static final Logger log = Logger.getLogger(DefaultTemporaryFileService.class);

    /**
     * Set of filenames to be deleted on VM exit through a shutdown hook.
     */
    private static final Set<Local> files = new LinkedHashSet<>();

    private final String delimiter
            = PreferencesFactory.get().getProperty("local.delimiter");

    @Override
    public Local create(final Path file) {
        return this.create(new UUIDRandomStringService().random(), file);
    }

    @Override
    public Local create(final String name) {
        return this.create(new UUIDRandomStringService().random(), name);
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
        $4%s: Region
         */
        final String pathFormat = "%2$s%1$s%3$s%1$s%4$s";
        final String normalizedPathFormat = pathFormat + "%1$s%5$s";

        final String attributes = new DefaultPathReference(file).attributes();
        final String normalizedFileName = PathNormalizer.name(file.getAbsolute());

        final File shortenTestPath = new File(PreferencesFactory.get().getProperty("tmp.dir"), String.format(normalizedPathFormat, delimiter, uid, "", attributes, normalizedFileName));
        final int shortenLength = PreferencesFactory.get().getInteger("local.temporaryfiles.shortening.threshold") - shortenTestPath.getAbsolutePath().length();
        if (shortenLength < 0) {
            // should throw Exception or warn user that this operation might result in CD crash
        }

        final String shortenedPath = this.shorten(file.getParent().getAbsolute(), shortenLength);
        final String folder = String.format(pathFormat, delimiter, uid, shortenedPath, attributes);
        return this.create(folder, normalizedFileName);
    }

    private Local create(final String folder, final String name) {
        final Local file = LocalFactory.get(new File(PreferencesFactory.get().getProperty("tmp.dir"), folder).getAbsolutePath(), name);
        this.delete(file.getParent());
        this.delete(file);
        return file;
    }

    /**
     * Delete on exit
     *
     * @param file File reference
     */
    protected void delete(final Local file) {
        files.add(file);
    }

    protected String shorten(final String path, int limit) {
        if(path.length() > limit) {
            return DigestUtils.md5Hex(path);
        }
        return path;
    }

    @Override
    public void shutdown() {
        final List<Local> list = new ArrayList<>(files);
        Collections.reverse(list);
        for(Local f : list) {
            try {
                f.delete();
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Failure deleting file %s in shutdown hook. %s", f, e.getMessage()));
            }
        }
    }
}
