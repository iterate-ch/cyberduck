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

import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;
import org.nuxeo.onedrive.client.OneDriveAPIException;

import java.net.SocketException;

import static org.junit.Assert.assertTrue;

public class OneDriveExceptionMappingServiceTest {

    @Test
    public void map() throws Exception {
        assertTrue(new OneDriveExceptionMappingService().map(
                new OneDriveAPIException("The OneDrive API responded with too many redirects.")) instanceof InteroperabilityException);
        assertTrue(new OneDriveExceptionMappingService().map(
                new OneDriveAPIException("m", 404)) instanceof NotfoundException);
        assertTrue(new OneDriveExceptionMappingService().map(
                new OneDriveAPIException("Couldn't connect to the OneDrive API due to a network error.", new SocketException())) instanceof ConnectionRefusedException);
    }
}