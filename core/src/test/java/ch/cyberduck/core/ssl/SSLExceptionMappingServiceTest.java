package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import javax.net.ssl.SSLException;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

public class SSLExceptionMappingServiceTest {

    @Test
    public void testMap() throws Exception {
        final BackgroundException f = new SSLExceptionMappingService().map(new SSLException(
                "Connection has been shutdown: javax.net.ssl.SSLException: java.net.SocketException: Broken pipe",
                new SSLException("javax.net.ssl.SSLException: java.net.SocketException: Broken pipe",
                        new SocketException("Broken pipe"))));
        assertEquals("Connection failed", f.getMessage());
        assertEquals("Broken pipe. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", f.getDetail());
    }
}
