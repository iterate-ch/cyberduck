package ch.cyberduck.core;

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

import ch.cyberduck.core.cryptomator.CryptoAuthenticationException;
import ch.cyberduck.core.exception.BackgroundException;

import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DefaultIOExceptionMappingServiceTest {

    @Test
    public void testRootCause() throws Exception {
        final BackgroundException failure = new DefaultIOExceptionMappingService().map(new IOException("e", new CryptoAuthenticationException("d", new AuthenticationFailedException("f"))));
        assertEquals(CryptoAuthenticationException.class, failure.getClass());
        assertEquals("d.", failure.getDetail());
    }

}