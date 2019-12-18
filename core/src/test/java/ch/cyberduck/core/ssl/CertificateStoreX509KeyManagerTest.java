package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateIdentityCallback;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.http.auth.BasicUserPrincipal;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.net.Socket;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class CertificateStoreX509KeyManagerTest {

    @Test
    public void testChooseClientAliasNotfound() throws Exception {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), new Host(new TestProtocol()), new DisabledCertificateStore()).init();
        assertNull(m.chooseClientAlias(new String[]{"RSA", "DSA"},
                new Principal[]{new BasicUserPrincipal("user")}, new Socket("test.cyberduck.ch", 443)));
    }

    @Test
    public void testChooseClientAliasStartcom() throws Exception {
        final AtomicBoolean choose = new AtomicBoolean();
        final X509KeyManager m = new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), new Host(new TestProtocol(), "test2.cyberduck.ch"), new DisabledCertificateStore() {
            @Override
            public X509Certificate choose(final CertificateIdentityCallback prompt, final String[] keyTypes, final Principal[] issuers, final Host bookmark) throws ConnectionCanceledException {
                for(Principal issuer : issuers) {
                    assertEquals("CN=StartCom Class 2 Primary Intermediate Client CA", issuer.getName());
                }
                choose.set(true);
                throw new ConnectionCanceledException();
            }
        }
        ).init();
        assertNull(m.chooseClientAlias(new String[]{"RSA", "DSA"},
                new Principal[]{new X500Principal("CN=StartCom Class 2 Primary Intermediate Client CA")},
                new Socket("test.cyberduck.ch", 443)));
        assertTrue(choose.get());
    }

    @Test
    public void testGetCertificateChain() {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), new Host(new TestProtocol()), new DisabledCertificateStore()).init();
        m.getCertificateChain("a");
    }

    @Test
    public void testGetPrivateKey() {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), new Host(new TestProtocol()), new DisabledCertificateStore()).init();
        assertNull(m.getPrivateKey("unknown-alias"));
    }

    @Test
    public void testPrincipalNotFound() {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), new Host(new TestProtocol()), new DisabledCertificateStore()).init();
        assertNull(m.getClientAliases("RSA", new Principal[]{
                new X500Principal("CN=g")
        }));
    }

    @Test
    public void testClientAliasesNoIssuer() {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), new Host(new TestProtocol()), new DisabledCertificateStore()).init();
        assertNull(m.getClientAliases("RSA", new Principal[]{}));
        assertNull(m.getClientAliases("RSA", null));
    }

    @Test
    public void testList() {
        assertTrue(new CertificateStoreX509KeyManager(new DisabledCertificateIdentityCallback(), new Host(new TestProtocol()), new DisabledCertificateStore()).init().list().isEmpty());
    }
}
