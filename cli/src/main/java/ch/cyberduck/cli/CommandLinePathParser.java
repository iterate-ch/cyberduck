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

import ch.cyberduck.core.ContainerPathKindDetector;
import ch.cyberduck.core.DelimiterPathKindDetector;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.HostParserException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class CommandLinePathParser {

    private final ProtocolFactory factory;

    public CommandLinePathParser(final CommandLine input) {
        this(input, ProtocolFactory.get());
    }

    public CommandLinePathParser(final CommandLine input, final ProtocolFactory factory) {
        this.factory = factory;
    }

    public Path parse(final String uri) throws HostParserException {
        final Host host = new HostParser(factory).get(uri);
        if(StringUtils.isBlank(host.getDefaultPath())) {
            return new Path(String.valueOf(Path.DELIMITER), EnumSet.of((Path.Type.directory)));
        }
        switch(new ContainerPathKindDetector(host.getProtocol().getFeature(PathContainerService.class)).detect(host.getDefaultPath())) {
            case directory:
                return new Path(PathNormalizer.normalize(host.getDefaultPath()), EnumSet.of(Path.Type.directory,
                        Path.Type.volume));
        }
        return new Path(PathNormalizer.normalize(host.getDefaultPath()), EnumSet.of(
                new DelimiterPathKindDetector().detect(host.getDefaultPath())));
    }
}
