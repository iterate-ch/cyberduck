package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;

import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.security.Key;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Arrays;

import static org.junit.Assert.*;

public class KeychainX509KeyManagerTest {

    private static KeyStore keychain;

    @BeforeClass
    public static void initKeychain() throws Exception {
        keychain = KeyStore.getInstance("KeychainStore", "Apple");
        keychain.load(null, null);
        KeyStore kspkcs12 = KeyStore.getInstance("pkcs12");
        kspkcs12.load(KeychainX509KeyManagerTest.class.getResourceAsStream("/test.p12"), "test".toCharArray());
        Key key = kspkcs12.getKey("test", "test".toCharArray());
        Certificate[] chain = kspkcs12.getCertificateChain("test");
        keychain.setKeyEntry("myClient", key, "null".toCharArray(), chain);
        keychain.store(null, null);
    }

    @AfterClass
    public static void removeCertificate() throws Exception {
        keychain.deleteEntry("myClient");
        keychain.store(null, null);
    }

    @Test
    public void testGetAliasesForIssuerDN() {
        final CertificateStoreX509KeyManager m = new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(),
                new Host(new TestProtocol()), new DisabledCertificateStore(), new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() {
                return keychain;
            }
        }).init();
        final String[] aliases = m.getClientAliases("RSA", new Principal[]{
                new X500Principal("CN=iterate GmbH - Test")
        });
        assertNotNull(aliases);
        assertFalse(Arrays.asList(aliases).isEmpty());
    }

    @Test
    @Ignore
    public void testLoadPrivateKeyFromKeychain() {
        final CertificateStoreX509KeyManager m = new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(),
                new Host(new TestProtocol()), new DisabledCertificateStore(), new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() {
                return keychain;
            }
        }).init();
        assertTrue(m.list().contains("myclient"));
        assertNotNull(m.getPrivateKey("myClient"));
    }
}
