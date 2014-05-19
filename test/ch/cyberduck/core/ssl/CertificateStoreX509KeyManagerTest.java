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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DisabledCertificateStore;

import org.apache.http.auth.BasicUserPrincipal;
import org.junit.Test;

import java.net.Socket;
import java.security.Principal;

import static org.junit.Assert.assertNull;

/**
 * @version $Id:$
 */
public class CertificateStoreX509KeyManagerTest extends AbstractTestCase {

    @Test
    public void testChooseClientAlias() throws Exception {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "h";
            }
        }, new DisabledCertificateStore()).init();
        assertNull(m.chooseClientAlias(new String[]{"issuer"}, new Principal[]{new BasicUserPrincipal("user")}, new Socket("localhost", 443)));
    }

    @Test
    public void testGetCertificateChain() throws Exception {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "h";
            }
        }, new DisabledCertificateStore()).init();
        m.getCertificateChain("a");
    }

    @Test
    public void testGetPrivateKey() throws Exception {
        final X509KeyManager m = new CertificateStoreX509KeyManager(new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return "h";
            }
        }, new DisabledCertificateStore()).init();
        assertNull(m.getPrivateKey("unknown-alias"));
    }
}
