package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.irods.jargon.core.exception.AuthenticationException;
import org.irods.jargon.core.exception.CatNoAccessException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IRODSExceptionMappingServiceTest {

    @Test
    public void testMap() throws Exception {
        assertTrue(new IRODSExceptionMappingService().map(new CatNoAccessException("no access")) instanceof AccessDeniedException);
        assertTrue(new IRODSExceptionMappingService().map(new FileNotFoundException("no file")) instanceof NotfoundException);
        assertTrue(new IRODSExceptionMappingService().map(new AuthenticationException("no user")) instanceof LoginFailureException);
    }
}
