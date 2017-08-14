package ch.cyberduck.core.manta;

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

import ch.cyberduck.core.exception.LoginFailureException;

import org.junit.Test;

import java.security.KeyException;

import static org.junit.Assert.assertTrue;

public class MantaExceptionMappingServiceTest {

    @Test
    public void map() throws Exception {
        assertTrue(new MantaExceptionMappingService().map(new KeyException("Private Key Authentication is required"))
                instanceof LoginFailureException);
    }
}
