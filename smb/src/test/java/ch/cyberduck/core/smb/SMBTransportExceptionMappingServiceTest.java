package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;

import org.junit.Test;

import java.io.EOFException;
import java.net.SocketException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.common.SMBRuntimeException;

import static org.junit.Assert.assertEquals;

public class SMBTransportExceptionMappingServiceTest {

    @Test
    public void testTimeoutExpired() {
        final TransportException failure = new TransportException(new ExecutionException(new SMBRuntimeException(new TimeoutException("Timeout expired"))));
        final BackgroundException f = new SMBTransportExceptionMappingService().map(failure);
        assertEquals("Connection failed", f.getMessage());
        assertEquals("Timeout expired. The connection attempt timed out. The server may be down, or your network may not be properly configured.", f.getDetail());
        assertEquals(ConnectionTimeoutException.class, f.getClass());
    }

    @Test
    public void testPacketEof() {
        // com.hierynomus.smbj.common.SMBRuntimeException: com.hierynomus.protocol.transport.TransportException: java.io.EOFException: EOF while reading packet
        final TransportException failure = new TransportException(new EOFException("EOF while reading packet"));
        final BackgroundException f = new SMBTransportExceptionMappingService().map(failure);
        assertEquals(ConnectionRefusedException.class, f.getClass());
    }

    @Test
    public void testConnectException() {
        // com.hierynomus.smbj.common.SMBRuntimeException: com.hierynomus.protocol.transport.TransportException: java.io.EOFException: EOF while reading packet
        final TransportException failure = new TransportException(new ExecutionException(new SocketException("Network is unreachable")));
        final BackgroundException f = new SMBTransportExceptionMappingService().map(failure);
        assertEquals(ConnectionRefusedException.class, f.getClass());
        assertEquals("Connection failed", f.getMessage());
        assertEquals("Network is unreachable. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", f.getDetail());
    }
}