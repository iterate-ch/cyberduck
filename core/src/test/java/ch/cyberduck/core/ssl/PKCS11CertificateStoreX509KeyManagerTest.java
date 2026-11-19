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

import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.TestProtocol;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.security.Principal;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PKCS11CertificateStoreX509KeyManagerTest {

    /**
     * No PKCS11 library configured such as by default on Windows
     */
    @Test
    public void testNoLibrary() {
        final PKCS11CertificateStoreX509KeyManager manager = new PKCS11CertificateStoreX509KeyManager(
                new Host(new TestProtocol()), new DisabledCertificateStore(), LoginCallback.noop, StringUtils.EMPTY);
        assertTrue(manager.list().isEmpty());
        assertNull(manager.getClientAliases(new String[]{"RSA"}, new Principal[]{new X500Principal("CN=Test CA")}));
        assertNull(manager.getPrivateKey("alias"));
    }
}
