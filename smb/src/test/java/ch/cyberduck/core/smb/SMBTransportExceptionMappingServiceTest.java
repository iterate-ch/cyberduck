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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.common.SMBRuntimeException;

import static org.junit.Assert.assertEquals;

public class SMBTransportExceptionMappingServiceTest {

    @Test
    public void map() {
        {
            final BackgroundException f = new SMBTransportExceptionMappingService().map(
                    new TransportException(new ExecutionException(new SMBRuntimeException(new TimeoutException("Timeout expired")))));
            assertEquals("Connection failed", f.getMessage());
            assertEquals(ConnectionTimeoutException.class, f.getClass());
        }
        {
            final BackgroundException f = new SMBTransportExceptionMappingService().map(new TransportException(new ExecutionException(new SMBRuntimeException(new TimeoutException("Timeout expired")))));
            assertEquals("Timeout expired. The connection attempt timed out. The server may be down, or your network may not be properly configured.",
                    f.getDetail());
            assertEquals(ConnectionTimeoutException.class, f.getClass());
        }
        // com.hierynomus.smbj.common.SMBRuntimeException: com.hierynomus.protocol.transport.TransportException: java.io.EOFException: EOF while reading packet
        {
            final BackgroundException f = new SMBTransportExceptionMappingService().map(new TransportException(new EOFException("EOF while reading packet")));
            assertEquals(ConnectionRefusedException.class, f.getClass());
        }
    }
}