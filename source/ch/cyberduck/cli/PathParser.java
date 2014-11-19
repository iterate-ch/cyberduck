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

import ch.cyberduck.core.DefaultPathKindDetector;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

/**
 * @version $Id$
 */
public class PathParser {

    private final DefaultPathKindDetector detector
            = new DefaultPathKindDetector();

    private final CommandLine input;

    public PathParser(final CommandLine input) {
        this.input = input;
    }

    public Path parse(final String uri) {
        final Path remote;
        final Host host = HostParser.parse(uri);
        switch(host.getProtocol().getType()) {
            case s3:
            case googlestorage:
            case swift:
            case azure:
                if(StringUtils.isBlank(host.getProtocol().getDefaultHostname())) {
                    remote = new Path(host.getDefaultPath(), EnumSet.of(detector.detect(host.getDefaultPath())));
                }
                else {
                    final String container = host.getHostname();
                    final String key = host.getDefaultPath();
                    remote = new Path(new Path(container, EnumSet.of(Path.Type.volume, Path.Type.directory)),
                            key, EnumSet.of(detector.detect(host.getDefaultPath())));
                }
                break;
            default:
                remote = new Path(host.getDefaultPath(), EnumSet.of(detector.detect(host.getDefaultPath())));
        }
        return remote;
    }
}
