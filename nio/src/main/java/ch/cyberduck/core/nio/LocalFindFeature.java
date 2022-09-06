package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;

public class LocalFindFeature implements Find {
    private static final Logger log = LogManager.getLogger(LocalFindFeature.class);

    private final LocalSession session;

    public LocalFindFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        final boolean exists = Files.exists(session.toPath(file), LinkOption.NOFOLLOW_LINKS);
        if(exists) {
            if(Files.isSymbolicLink(session.toPath(file))) {
                return true;
            }
            if(!file.isRoot()) {
                try {
                    if(!StringUtils.equals(session.toPath(file).toFile().getCanonicalFile().getName(), file.getName())) {
                        return false;
                    }
                }
                catch(IOException e) {
                    log.warn(String.format("Failure obtaining canonical file reference for %s", file));
                }
            }
        }
        return exists;
    }
}
