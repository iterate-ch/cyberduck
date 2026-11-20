package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.UnsupportedException;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PKCS11KeyStoreTest {

    /**
     * Library for smartcards in Keychain (CryptoTokenKit) on macOS. Returns CKR_FUNCTION_NOT_SUPPORTED from
     * C_GetFunctionList for any caller other than ssh, ssh-keygen and ssh-pkcs11-helper which crashes the runtime
     * when loaded.
     */
    @Test
    public void testLibraryBlacklisted() {
        final ConcurrentException failure = assertThrows(ConcurrentException.class, () -> PKCS11KeyStore.build(
                "/usr/lib/ssh-keychain.dylib", new Host(new TestProtocol()), new DisabledLoginCallback()).get());
        assertTrue(failure.getCause() instanceof UnsupportedException);
    }
}
