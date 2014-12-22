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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UriParserTest extends AbstractTestCase {

    @Test
    public void testParse() throws Exception {
        final CommandLineParser parser = new BasicParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        assertTrue(new Host(ProtocolFactory.S3_SSL, "s3.amazonaws.com", 443, "/cyberduck-test/key", new Credentials("AWS456", null))
                .compareTo(new UriParser(input).parse("s3://AWS456@cyberduck-test/key")) == 0);
        assertTrue(new Host(ProtocolFactory.FTP_TLS, "cyberduck.io", 55, "/cyberduck-test/key", new Credentials("anonymous", null))
                .compareTo(new UriParser(input).parse("ftps://cyberduck.io:55/folder")) == 0);
    }

    @Test
    public void testProfile() throws Exception {
        final ProfilePlistReader reader = new ProfilePlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Profile profile = reader.read(
                new Local("profiles/Rackspace US.cyberduckprofile")
        );
        assertNotNull(profile);
        ProtocolFactory.register(profile);

        final CommandLineParser parser = new BasicParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        assertTrue(new Host(profile, "identity.api.rackspacecloud.com", 443, "/cdn.cyberduck.ch/", new Credentials("u", null))
                .compareTo(new UriParser(input).parse("rackspace://u@cdn.cyberduck.ch/")) == 0);

    }
}