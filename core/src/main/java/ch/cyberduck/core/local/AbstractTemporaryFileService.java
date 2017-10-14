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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractTemporaryFileService implements TemporaryFileService {
    private static final Logger log = Logger.getLogger(AbstractTemporaryFileService.class);

    /**
     * Set of filenames to be deleted on VM exit through a shutdown hook.
     */
    private static final Set<Local> files = new LinkedHashSet<>();

    /**
     * Delete on exit
     *
     * @param file File reference
     */
    protected Local delete(final Local file) {
        files.add(file);
        return file;
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
