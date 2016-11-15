package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class DefaultHomeFinderService implements Home {

    protected final Path DEFAULT_HOME = new Path(String.valueOf(Path.DELIMITER),
            EnumSet.of(Path.Type.volume, Path.Type.directory));

    private final Session<?> session;

    public DefaultHomeFinderService(final Session session) {
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final Host host = session.getHost();
        if(host.getWorkdir() != null) {
            return host.getWorkdir();
        }
        else {
            final String path = host.getDefaultPath();
            if(StringUtils.isNotBlank(path)) {
                return this.find(DEFAULT_HOME, path);
            }
            else {
                // No default path configured
                return DEFAULT_HOME;
            }
        }
    }

    @Override
    public Path find(final Path root, final String path) {
        if(path.startsWith(String.valueOf(Path.DELIMITER))) {
            // Mount absolute path
            final String normalized = PathNormalizer.normalize(path);
            return new Path(normalized,
                    normalized.equals(String.valueOf(Path.DELIMITER)) ? EnumSet.of(Path.Type.volume, Path.Type.directory) : EnumSet.of(Path.Type.directory));
        }
        else {
            if(path.startsWith(Path.HOME)) {
                // Relative path to the home directory
                return new Path(root, PathNormalizer.normalize(StringUtils.removeStart(
                        StringUtils.removeStart(path, Path.HOME), String.valueOf(Path.DELIMITER)), false), EnumSet.of(Path.Type.directory));
            }
            else {
                // Relative path
                return new Path(root, PathNormalizer.normalize(path, false), EnumSet.of(Path.Type.directory));
            }
        }
    }
}
