package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;
import org.nuxeo.onedrive.client.OneDriveAPIException;

import java.net.SocketException;

import static org.junit.Assert.assertTrue;

public class GraphExceptionMappingServiceTest {

    @Test
    public void map() {
        assertTrue(new GraphExceptionMappingService(new GraphFileIdProvider(new OneDriveSession(new Host(new OneDriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()))).map(
                new OneDriveAPIException("The OneDrive API responded with too many redirects.")) instanceof InteroperabilityException);
        assertTrue(new GraphExceptionMappingService(new GraphFileIdProvider(new OneDriveSession(new Host(new OneDriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()))).map(
                new OneDriveAPIException("m", 404)) instanceof NotfoundException);
        assertTrue(new GraphExceptionMappingService(new GraphFileIdProvider(new OneDriveSession(new Host(new OneDriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()))).map(
                new OneDriveAPIException("Couldn't connect to the OneDrive API due to a network error.", new SocketException())) instanceof ConnectionRefusedException);
    }
}
