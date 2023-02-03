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
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.azure.AzureProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
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
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CommandLineUriParserTest {

    @Test
    public void testParse() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        final ProtocolFactory factory = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(new FTPTLSProtocol(), new S3Protocol())));
        factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/FTP.cyberduckprofile")));
        factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/FTPS.cyberduckprofile")));
        factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile")));
        assertEquals(0, new Host(new S3Protocol(), "s3.amazonaws.com", 443, "/cyberduck-test", new Credentials("AWS456", null))
                .compareTo(new CommandLineUriParser(input, factory).parse("s3:AWS456@cyberduck-test/key")));
        assertEquals(0, new Host(new S3Protocol(), "s3.amazonaws.com", 443, "/cyberduck-test", new Credentials("AWS456", null))
                .compareTo(new CommandLineUriParser(input, factory).parse("s3://AWS456@/cyberduck-test/key")));
        assertEquals(0, new Host(new FTPTLSProtocol(), "cyberduck.io", 55, "/folder", new Credentials("anonymous", null))
                .compareTo(new CommandLineUriParser(input, factory).parse("ftps://cyberduck.io:55/folder/")));
    }

    @Test
    public void testProfile() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        final ProtocolFactory factory = new ProtocolFactory(new LinkedHashSet<>(Collections.singleton(new SwiftProtocol())));
        factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Rackspace US.cyberduckprofile")));
        assertEquals(0, new Host(factory.forName("rackspace"), "identity.api.rackspacecloud.com", 443, "/cdn.cyberduck.ch", new Credentials("u", null))
                .compareTo(new CommandLineUriParser(input, factory).parse("rackspace://u@cdn.cyberduck.ch/")));

    }

    @Test
    public void testDefaultWebDAVForHttpScheme() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        final ProtocolFactory factory = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(
            new AzureProtocol(),
            new DAVSSLProtocol()
        )));
        factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Azure.cyberduckprofile")));
        factory.register(new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/DAVS.cyberduckprofile")));
        assertEquals(0, new Host(new DAVSSLProtocol(), "ftp.gnu.org", 443, "/gnu/wget", new Credentials("anonymous", null))
                .compareTo(new CommandLineUriParser(input, factory).parse("https://ftp.gnu.org/gnu/wget/wget-1.19.1.tar.gz")));
    }

    @Test
    public void testCustomProvider() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        final SwiftProtocol protocol = new SwiftProtocol();
        final ProtocolFactory factory = new ProtocolFactory(new LinkedHashSet<>(Collections.singletonList(protocol)));
        final Profile rackspace = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Rackspace US.cyberduckprofile"));
        assertSame(protocol, rackspace.getProtocol());
        factory.register(rackspace);
        final Profile generic = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Openstack Swift (Keystone 2).cyberduckprofile"));
        assertSame(protocol, generic.getProtocol());
        factory.register(generic);
        assertSame(rackspace, factory.forName("swift", "rackspace"));
        assertSame(generic, factory.forName("swift", "openstack-keystone2"));

        assertEquals(rackspace, new CommandLineUriParser(input, factory).parse("rackspace://container//").getProtocol());
        assertEquals(0, new Host(rackspace, "identity.api.rackspacecloud.com", 443, "/container")
            .compareTo(new CommandLineUriParser(input, factory).parse("rackspace://container/")));

        assertEquals(generic, new CommandLineUriParser(input, factory).parse("openstack-keystone2://auth.cloud.ovh.net/container/").getProtocol());
        assertEquals(0, new Host(generic, "auth.cloud.ovh.net", 443, "/container")
            .compareTo(new CommandLineUriParser(input, factory).parse("openstack-keystone2://auth.cloud.ovh.net/container/")));
    }
}
