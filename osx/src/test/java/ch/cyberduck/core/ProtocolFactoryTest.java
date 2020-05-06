package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertSame;

public class ProtocolFactoryTest {

    @Test
    public void testParse() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Arrays.asList(new FTPProtocol(), new FTPTLSProtocol(), new S3Protocol())));
        final Profile ftp = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/FTP.cyberduckprofile"));
        factory.register(ftp);
        final Profile ftps = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/FTPS.cyberduckprofile"));
        factory.register(ftps);
        final Profile s3 = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        factory.register(s3);
        assertSame(ftp, factory.forName(ftp.getIdentifier()));
        assertSame(ftp, factory.forName(ftp.getIdentifier(), ftp.getProvider()));
        assertSame(ftps, factory.forName(ftps.getIdentifier()));
        assertSame(ftps, factory.forName(ftps.getIdentifier(), ftps.getProvider()));
        assertSame(s3, factory.forName(s3.getIdentifier()));
        assertSame(s3, factory.forName(s3.getIdentifier(), s3.getProvider()));
        assertSame(s3, factory.forName("https"));
        assertSame(s3, factory.forName("https", "iterate GmbH"));
    }


    @Test
    public void testParseOpenStackContext() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singletonList(new SwiftProtocol())));
        final Profile v2 = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Openstack Swift (Keystone 2).cyberduckprofile"));
        factory.register(v2);
        final Profile v3 = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/Openstack Swift (Keystone 3).cyberduckprofile"));
        factory.register(v3);
        // Lookup using hash code
        assertSame(v3, factory.forName(String.valueOf(v3.hashCode())));
        assertSame(v2, factory.forName(String.valueOf(v2.hashCode())));
        // Lookup by name
        assertSame(v3, factory.forName(v3.getIdentifier(), v3.getProvider()));
        assertSame(v2, factory.forName(v2.getIdentifier(), v2.getProvider()));
    }
}
