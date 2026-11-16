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
import ch.cyberduck.core.TestProtocol;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.net.Socket;
import java.security.Principal;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class DelegatingCertificateStoreX509KeyManagerTest {

    /**
     * Store with no certificate selected saving empty selection
     */
    private static CertificateStoreX509KeyManager none(final Host bookmark, final AtomicBoolean invoked) {
        return new DefaultCertificateStoreX509KeyManager(CertificateIdentityCallback.noop, bookmark, new DisabledCertificateStore()) {
            @Override
            public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
                invoked.set(true);
                if(null == bookmark.getCredentials().getCertificate()) {
                    bookmark.getCredentials().setCertificate(StringUtils.EMPTY);
                }
                return null;
            }
        };
    }

    @Test
    public void testChooseClientAliasFromNextStore() {
        final Host bookmark = new Host(new TestProtocol());
        final AtomicBoolean first = new AtomicBoolean();
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(bookmark, none(bookmark, first),
                new DefaultCertificateStoreX509KeyManager(CertificateIdentityCallback.noop, bookmark, new DisabledCertificateStore()) {
                    @Override
                    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
                        // Empty selection from previous store must not prevent selection
                        assertNull(bookmark.getCredentials().getCertificate());
                        bookmark.getCredentials().setCertificate("token");
                        return "token";
                    }
                });
        assertEquals("token", m.chooseClientAlias(new String[]{"RSA"}, new Principal[]{}, null));
        assertTrue(first.get());
        assertEquals("token", bookmark.getCredentials().getCertificate());
    }

    @Test
    public void testChooseClientAliasNoSelection() {
        final Host bookmark = new Host(new TestProtocol());
        final AtomicBoolean first = new AtomicBoolean();
        final AtomicBoolean second = new AtomicBoolean();
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(bookmark, none(bookmark, first), none(bookmark, second));
        assertNull(m.chooseClientAlias(new String[]{"RSA"}, new Principal[]{}, null));
        assertTrue(first.get());
        assertTrue(second.get());
        // Empty selection saved
        assertEquals(StringUtils.EMPTY, bookmark.getCredentials().getCertificate());
    }

    @Test
    public void testChooseClientAliasSavedEmptySelection() {
        final Host bookmark = new Host(new TestProtocol());
        bookmark.getCredentials().setCertificate(StringUtils.EMPTY);
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(bookmark, none(bookmark, new AtomicBoolean()), none(bookmark, new AtomicBoolean()));
        assertNull(m.chooseClientAlias(new String[]{"RSA"}, new Principal[]{}, null));
        assertEquals(StringUtils.EMPTY, bookmark.getCredentials().getCertificate());
    }
}
