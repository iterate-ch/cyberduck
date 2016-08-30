package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;

import org.junit.Test;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;

import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.transport.TransportException;

import static org.junit.Assert.assertEquals;

public class SFTPExceptionMappingServiceTest {

    @Test
    public void testMapReadFailure() throws Exception {
        assertEquals(SocketException.class,
                new SFTPExceptionMappingService().map(new SocketException("Unexpected end of sftp stream.")).getCause().getClass());
    }

    @Test
    public void testWrapped() throws Exception {
        assertEquals(InteroperabilityException.class,
                new SFTPExceptionMappingService().map(new TransportException(DisconnectReason.UNKNOWN, new SSHException(DisconnectReason.PROTOCOL_ERROR))).getClass());
    }

    @Test
    public void testSocketTimeout() throws Exception {
        assertEquals(ConnectionTimeoutException.class, new SFTPExceptionMappingService()
                .map(new SocketTimeoutException()).getClass());
        assertEquals(ConnectionTimeoutException.class, new SFTPExceptionMappingService()
                .map("message", new SocketTimeoutException()).getClass());
        assertEquals(ConnectionTimeoutException.class, new SFTPExceptionMappingService()
                .map("message", new SocketTimeoutException(), new Path("/f", EnumSet.of(Path.Type.file))).getClass());
    }
}
