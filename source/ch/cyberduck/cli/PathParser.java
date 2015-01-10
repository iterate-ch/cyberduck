package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DelimiterPathKindDetector;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathKindDetector;
import ch.cyberduck.core.PathNormalizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

/**
 * @version $Id$
 */
public class PathParser {

    private final PathKindDetector detector
            = new DelimiterPathKindDetector();

    private final CommandLine input;

    public PathParser(final CommandLine input) {
        this.input = input;
    }

    public Path parse(final String uri) {
        final Host host = HostParser.parse(uri);
        switch(host.getProtocol().getType()) {
            case s3:
            case googlestorage:
            case swift:
                if(StringUtils.isBlank(host.getProtocol().getDefaultHostname())) {
                    return new Path(host.getDefaultPath(), EnumSet.of(detector.detect(host.getDefaultPath())));
                }
                else {
                    final Path container;
                    if(StringUtils.isBlank(host.getHostname())) {
                        container = new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory));
                    }
                    else {
                        container = new Path(host.getHostname(), EnumSet.of(Path.Type.volume, Path.Type.directory));
                    }
                    final String key = host.getDefaultPath();
                    if(String.valueOf(Path.DELIMITER).equals(PathNormalizer.normalize(key))) {
                        return container;
                    }
                    return new Path(container, key, EnumSet.of(detector.detect(key)));
                }
            default:
                return new Path(host.getDefaultPath(), EnumSet.of(detector.detect(host.getDefaultPath())));
        }
    }
}
