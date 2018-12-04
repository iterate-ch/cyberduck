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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CommandLineUriParserTest {

    @Test
    public void testParse() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Arrays.asList(new FTPTLSProtocol(), new S3Protocol())));
        factory.register(new ProfilePlistReader(factory).read(LocalFactory.get("../profiles/default/FTP.cyberduckprofile")));
        factory.register(new ProfilePlistReader(factory).read(LocalFactory.get("../profiles/default/FTPS.cyberduckprofile")));
        factory.register(new ProfilePlistReader(factory).read(LocalFactory.get("../profiles/default/S3 (HTTPS).cyberduckprofile")));
        assertEquals(0, new Host(new S3Protocol(), "s3.amazonaws.com", 443, "/cyberduck-test/key", new Credentials("AWS456", null))
            .compareTo(new CommandLineUriParser(input, factory).parse("s3://AWS456@cyberduck-test/key")));
        assertEquals(0, new Host(new FTPTLSProtocol(), "cyberduck.io", 55, "/folder", new Credentials("anonymous", null))
            .compareTo(new CommandLineUriParser(input, factory).parse("ftps://cyberduck.io:55/folder")));
    }

    @Test
    public void testProfile() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        final Set<Protocol> list = new HashSet<>(Arrays.asList(
                new SwiftProtocol(),
                new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SwiftProtocol() {
                    @Override
                    public boolean isEnabled() {
                        return true;
                    }
                })))
                        .read(new Local("../profiles/default/Rackspace US.cyberduckprofile"))
        ));
        assertEquals(0, new Host(new ProtocolFactory(list).forName("rackspace"), "identity.api.rackspacecloud.com", 443, "/cdn.cyberduck.ch/", new Credentials("u", null))
            .compareTo(new CommandLineUriParser(input, new ProtocolFactory(list)).parse("rackspace://u@cdn.cyberduck.ch/")));

    }

    @Test
    public void testDefaultWebDAVForHttpScheme() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});

        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Arrays.asList(
                new AzureProtocol(),
                new DAVSSLProtocol()
        )));
        factory.register(new ProfilePlistReader(factory).read(LocalFactory.get("../profiles/default/Azure.cyberduckprofile")));
        factory.register(new ProfilePlistReader(factory).read(LocalFactory.get("../profiles/default/DAVS.cyberduckprofile")));
        assertEquals(0, new Host(new DAVSSLProtocol(), "ftp.gnu.org", 443, "/gnu/wget/wget-1.19.1.tar.gz", new Credentials("anonymous", null))
            .compareTo(new CommandLineUriParser(input, factory).parse("https://ftp.gnu.org/gnu/wget/wget-1.19.1.tar.gz")));
    }

    @Test
    public void testCustomProvider() throws Exception {
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singletonList(new SwiftProtocol())));
        final Profile rackspace = new ProfilePlistReader(factory).read(LocalFactory.get("../profiles/default/Rackspace US.cyberduckprofile"));
        factory.register(rackspace);
        final Profile generic = new ProfilePlistReader(factory).read(LocalFactory.get("../profiles/default/Swift.cyberduckprofile"));
        factory.register(generic);
        assertEquals(0, new Host(rackspace, "identity.api.rackspacecloud.com", 443, "/container")
            .compareTo(new CommandLineUriParser(input, factory).parse("rackspace://container/")));
        assertEquals(0, new Host(generic, "OS_AUTH_URL", 443, "/container")
            .compareTo(new CommandLineUriParser(input, factory).parse("swift://container/")));
    }

    @Test
    public void testSpecificProfile() throws Exception {
        final TestProtocol provider1 = new TestProtocol(Scheme.https) {
            @Override
            public String getIdentifier() {
                return "swift";
            }

            @Override
            public boolean isBundled() {
                return true;
            }

            @Override
            public String getProvider() {
                return "iterate GmbH";
            }
        };
        final TestProtocol provider2 = new TestProtocol(Scheme.https) {
            @Override
            public String getIdentifier() {
                return "swift";
            }

            @Override
            public boolean isBundled() {
                return true;
            }

            @Override
            public String getProvider() {
                return "iterate GmbH";
            }

            @Override
            public String getDefaultHostname() {
                return "identity.api.rackspacecloud.com";
            }

            @Override
            public String[] getSchemes() {
                return new String[]{"rackspace"};
            }
        };
        final ProtocolFactory f_sort1 = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(provider1, provider2)));
        final ProtocolFactory f_sort2 = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(provider2, provider1)));
        final CommandLineParser parser = new PosixParser();
        final CommandLine input = parser.parse(new Options(), new String[]{});
        assertEquals(provider1, new CommandLineUriParser(input, f_sort1).parse("swift://cyberduck-test/key").getProtocol());
        assertEquals(provider2, new CommandLineUriParser(input, f_sort1).parse("rackspace://cyberduck-test/key").getProtocol());
        assertEquals(provider1, new CommandLineUriParser(input, f_sort2).parse("swift://cyberduck-test/key").getProtocol());
        assertEquals(provider2, new CommandLineUriParser(input, f_sort2).parse("rackspace://cyberduck-test/key").getProtocol());
    }
}
