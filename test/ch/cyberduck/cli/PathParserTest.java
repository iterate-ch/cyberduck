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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PathParserTest extends AbstractTestCase {

    @Test
    public void testParse() throws Exception {
        final CommandLineParser parser = new BasicParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)),
                new PathParser(input).parse("ftps://u@test.cyberduck.ch/"));
        assertEquals(new Path("/d", EnumSet.of(Path.Type.directory)),
                new PathParser(input).parse("ftps://u@test.cyberduck.ch/d/"));
        assertEquals(new Path("/d", EnumSet.of(Path.Type.file)),
                new PathParser(input).parse("ftps://u@test.cyberduck.ch/d"));
    }

    @Test
    public void testParseProfile() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                new Local("profiles/Rackspace US.cyberduckprofile")
        );
        assertNotNull(profile);
        ProtocolFactory.register(profile);

        final CommandLineParser parser = new BasicParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        assertEquals(new Path("/cdn.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new PathParser(input).parse("rackspace://u@cdn.cyberduck.ch/"));
    }

}