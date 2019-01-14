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

import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class ProtocolFactoryTest {

    @Test
    public void testParse() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Arrays.asList(new FTPTLSProtocol(), new S3Protocol())));
        final Profile ftp = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/FTP.cyberduckprofile"));
        factory.register(ftp);
        final Profile ftps = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/FTPS.cyberduckprofile"));
        factory.register(ftps);
        final Profile s3 = new ProfilePlistReader(factory).read(this.getClass().getResourceAsStream("/S3 (HTTPS).cyberduckprofile"));
        factory.register(s3);
        assertEquals(ftp, factory.forName("ftp"));
        assertEquals(ftp, factory.forName("ftp", "iterate GmbH"));
        assertEquals(ftps, factory.forName("ftps"));
        assertEquals(ftps, factory.forName("ftps", "iterate GmbH"));
        assertEquals(s3, factory.forName("s3"));
        assertEquals(s3, factory.forName("s3", "iterate GmbH"));
        assertEquals(s3, factory.forName("https"));
        assertEquals(s3, factory.forName("https", "iterate GmbH"));
    }
}
