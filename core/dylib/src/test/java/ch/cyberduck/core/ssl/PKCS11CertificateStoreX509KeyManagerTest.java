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

import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PKCS11CertificateStoreX509KeyManagerTest {

    @Test
    public void testListBundled() {
        final PKCS11CertificateStoreX509KeyManager manager = new PKCS11CertificateStoreX509KeyManager(CertificateIdentityCallback.noop,
                new Host(new TestProtocol()), new DisabledCertificateStore(), LoginCallback.noop, "opensc-pkcs11.so");
        assertTrue(manager.list().isEmpty());
    }

    @Test
    public void testListInstalled() {
        final PKCS11CertificateStoreX509KeyManager manager = new PKCS11CertificateStoreX509KeyManager(CertificateIdentityCallback.noop,
                new Host(new TestProtocol()), new DisabledCertificateStore(), LoginCallback.noop, "/opt/homebrew/lib/opensc-pkcs11.so");
        assertTrue(manager.list().isEmpty());
    }
}