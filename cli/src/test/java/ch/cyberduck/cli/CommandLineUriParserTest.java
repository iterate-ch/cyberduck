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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CommandLineUriParserTest {

    @Test
    public void testParse() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Arrays.asList(new FTPTLSProtocol(), new S3Protocol())));
        assertTrue(new Host(new S3Protocol(), "s3.amazonaws.com", 443, "/cyberduck-test/key", new Credentials("AWS456", null))
                .compareTo(new CommandLineUriParser(input, factory).parse("s3://AWS456@cyberduck-test/key")) == 0);
        assertTrue(new Host(new FTPTLSProtocol(), "cyberduck.io", 55, "/folder", new Credentials("anonymous", null))
                .compareTo(new CommandLineUriParser(input, factory).parse("ftps://cyberduck.io:55/folder")) == 0);
    }

    @Test
    public void testProfile() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        final Set<Protocol> list = new HashSet<>(Arrays.asList(
                new SwiftProtocol(),
                new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SwiftProtocol())))
                        .read(new Local("../profiles/Rackspace US.cyberduckprofile"))
        ));
        assertTrue(new Host(new ProtocolFactory(list).find("rackspace"), "identity.api.rackspacecloud.com", 443, "/cdn.cyberduck.ch/", new Credentials("u", null))
                .compareTo(new CommandLineUriParser(input, new ProtocolFactory(list)).parse("rackspace://u@cdn.cyberduck.ch/")) == 0);

    }
}