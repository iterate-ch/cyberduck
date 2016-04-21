package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FTPWorkdirServiceTest {

    @Test
    public void testDefaultPath() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final String name = UUID.randomUUID().toString();
        host.setDefaultPath(name);
        final FTPSession session = new FTPSession(host);
        assertEquals("/" + name, new FTPWorkdirService(session).find().getAbsolute());
    }

}