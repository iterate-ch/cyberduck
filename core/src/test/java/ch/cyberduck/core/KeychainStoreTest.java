package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;

import org.junit.Ignore;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.security.KeyStore;
import java.security.Principal;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Ignore
public class KeychainStoreTest {

    @Test
    public void testGetAliasesForIssuerDN() throws Exception {
        final CertificateStoreX509KeyManager m = new CertificateStoreX509KeyManager(new Host(new TestProtocol()), new DisabledCertificateStore(),
                KeyStore.getInstance("KeychainStore", "Apple")).init();
        final String[] aliases = m.getClientAliases("RSA", new Principal[]{
                new X500Principal("C=US, O=Apple Inc., OU=Apple Certification Authority, CN=Developer ID Certification Authority")
        });
        assertNotNull(aliases);
        assertFalse(Arrays.asList(aliases).isEmpty());
    }
}