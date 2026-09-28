package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class NativeSecurityKeyMiddlewareTest {

    /**
     * Library for keys in the Secure Enclave on macOS
     */
    private static final String SSH_KEYCHAIN = "/usr/lib/ssh-keychain.dylib";

    @Test
    public void testApiVersion() throws Exception {
        assumeTrue(new File(SSH_KEYCHAIN).exists());
        final NativeSecurityKeyMiddleware middleware = new NativeSecurityKeyMiddleware(SSH_KEYCHAIN);
        assertEquals(SecurityKeyMiddleware.SSH_SK_VERSION_MAJOR,
                middleware.version() & SecurityKeyMiddleware.SSH_SK_VERSION_MAJOR_MASK);
    }

    @Test
    public void testLoadFailure() {
        try {
            new NativeSecurityKeyMiddleware("/nonexistent/libsk.dylib");
            fail("Expected failure loading middleware");
        }
        catch(IOException e) {
            // Expected
        }
    }
}
