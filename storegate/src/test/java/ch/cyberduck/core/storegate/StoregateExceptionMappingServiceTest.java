package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;

import org.junit.Test;

import javax.net.ssl.SSLException;
import javax.ws.rs.ProcessingException;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

public class StoregateExceptionMappingServiceTest {

    @Test
    public void testDisconnect() {
        final StoregateExceptionMappingService service = new StoregateExceptionMappingService(new StoregateIdProvider(
                new StoregateSession(new Host(new StoregateProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager())
        ));
        assertEquals(ConnectionRefusedException.class, service.map(
                new ApiException(new ProcessingException(new SSLException(new SocketException("Operation timed out (Read failed)"))))).getClass());
    }
}
