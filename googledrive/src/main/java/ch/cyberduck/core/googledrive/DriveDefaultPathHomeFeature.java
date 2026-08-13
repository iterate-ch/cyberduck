package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.shared.AbstractHomeFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

/**
 * Resolve default path of bookmark. Names of files and Shared Drives in Google Drive may contain the path delimiter
 * making the absolute path saved in the bookmark ambiguous. When no file is found for the default path interpreting
 * every delimiter as separator, match the path segments against the file names found on the server.
 */
public class DriveDefaultPathHomeFeature extends AbstractHomeFeature {
    private static final Logger log = LogManager.getLogger(DriveDefaultPathHomeFeature.class);

    private final Host host;
    private final AttributesFinder attributes;

    public DriveDefaultPathHomeFeature(final Host host, final AttributesFinder attributes) {
        this.host = host;
        this.attributes = attributes;
    }

    @Override
    public Path find() throws BackgroundException {
        if(StringUtils.isBlank(host.getDefaultPath())) {
            log.debug("No default path set for bookmark {}", host);
            // No default path configured
            return null;
        }
        final Path home = PathNormalizer.compose(Home.root(), host.getDefaultPath());
        try {
            return home.withAttributes(new DefaultPathAttributes(attributes.find(home)));
        }
        catch(NotfoundException e) {
            log.warn("Failure {} finding default path {}. Match path segments against file names", e, home);
            // Retry interpreting the delimiter in the default path as part of a file name
            final Path resolved = this.resolve(Home.root(),
                    StringUtils.split(PathNormalizer.normalize(host.getDefaultPath()), Path.DELIMITER), 0);
            if(null == resolved) {
                throw e;
            }
            return resolved;
        }
    }

    /**
     * Match segments of the default path against the listing of the directory. A single file name may span multiple
     * segments when containing the path delimiter.
     *
     * @param directory Parent directory of the segment referenced with index
     * @param segments  Path segments of the default path split by delimiter
     * @param index     Index of the next segment to match
     * @return Null if no file matching the remaining segments is found
     */
    protected Path resolve(final Path directory, final String[] segments, final int index) throws BackgroundException {
        for(int last = index; last < segments.length; last++) {
            // Join with subsequent segments to find file name containing the path delimiter
            final String name = StringUtils.join(segments, Path.DELIMITER, index, last + 1);
            final Path file = new Path(directory, name, EnumSet.of(Path.Type.directory));
            final PathAttributes found;
            try {
                found = attributes.find(file);
            }
            catch(NotfoundException e) {
                log.debug("No file {} found in directory {}", name, directory);
                continue;
            }
            file.withAttributes(new DefaultPathAttributes(found));
            if(last == segments.length - 1) {
                return file;
            }
            final Path home = this.resolve(file, segments, last + 1);
            if(null != home) {
                return home;
            }
            log.debug("No match for remaining segments in directory {}", file);
        }
        return null;
    }
}
