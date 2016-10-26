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

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.serializer.impl.dd.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommandLinePathParserTest {

    @Test
    public void testParse() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Arrays.asList(new FTPTLSProtocol(), new S3Protocol())));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)),
                new CommandLinePathParser(input, factory).parse("ftps://u@test.cyberduck.ch/"));
        assertEquals(new Path("/d", EnumSet.of(Path.Type.directory)),
                new CommandLinePathParser(input, factory).parse("ftps://u@test.cyberduck.ch/d/"));
        assertEquals(new Path("/d", EnumSet.of(Path.Type.file)),
                new CommandLinePathParser(input, factory).parse("ftps://u@test.cyberduck.ch/d"));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)),
                new CommandLinePathParser(input, factory).parse("ftps://u@test.cyberduck.ch/"));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)),
                new CommandLinePathParser(input, factory).parse("ftps://u@test.cyberduck.ch"));

        assertEquals(new Path("/test.cyberduck.ch", EnumSet.of(Path.Type.directory)),
                new CommandLinePathParser(input, factory).parse("s3://u@test.cyberduck.ch/"));
        assertEquals(new Path("/test.cyberduck.ch/d", EnumSet.of(Path.Type.directory)),
                new CommandLinePathParser(input, factory).parse("s3://u@test.cyberduck.ch/d/"));
        assertEquals(new Path("/test.cyberduck.ch/d", EnumSet.of(Path.Type.file)),
                new CommandLinePathParser(input, factory).parse("s3://u@test.cyberduck.ch/d"));
    }

    @Test
    public void testParseProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singletonList(new SwiftProtocol())));
        final ProfilePlistReader reader = new ProfilePlistReader(factory, new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                new Local("../profiles/Rackspace US.cyberduckprofile")
        );
        assertNotNull(profile);

        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        assertEquals(new Path("/cdn.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new CommandLinePathParser(input, new ProtocolFactory(new HashSet<>(Arrays.asList(new SwiftProtocol(), profile)))).parse("rackspace://u@cdn.cyberduck.ch/"));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new CommandLinePathParser(input, new ProtocolFactory(new HashSet<>(Arrays.asList(new SwiftProtocol(), profile)))).parse("rackspace:///"));
    }
}