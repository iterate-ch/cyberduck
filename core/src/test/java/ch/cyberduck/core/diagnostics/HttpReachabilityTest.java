package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.CertificateTrustCallback;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;

import org.junit.Test;

import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpReachabilityTest {

    @Test
    public void testIsReachable() {
        final Reachability r = new HttpReachability();
        assertTrue(r.isReachable(
                new Host(new TestProtocol(Scheme.http), "test.cyberduck.io")
        ));
        assertTrue(r.isReachable(
                new Host(new TestProtocol(Scheme.http), "test.cyberduck.io")
        ));
        assertTrue(r.isReachable(
                new Host(new TestProtocol(Scheme.https), "test.cyberduck.io")
        ));
    }

    @Test
    public void testIsReachableCertFailure() {
        final Reachability r = new HttpReachability(new DisabledProxyFinder(), new DisabledCertificateStore() {
            @Override
            public boolean verify(final CertificateTrustCallback prompt, final String hostname, final List<X509Certificate> certificates) {
                return false;
            }
        });
        assertTrue(r.isReachable(new Host(new TestProtocol(Scheme.https), "test.cyberduck.io")));
    }

    @Test
    public void testIsReachableProxyUnknownHost() {
        final Reachability r = new HttpReachability(new DisabledProxyFinder() {
            @Override
            public Proxy find(final String target) {
                return new Proxy(Proxy.Type.HTTP, "unknown.cyberduck.io", 9999);
            }
        }, new DisabledCertificateStore());
        assertFalse(r.isReachable(new Host(new TestProtocol(Scheme.http), "test.cyberduck.io")));
        assertFalse(r.isReachable(new Host(new TestProtocol(Scheme.https), "test.cyberduck.io")));
    }

    @Test
    public void testIsReachableProxyNotListening() {
        final Reachability r = new HttpReachability(new DisabledProxyFinder() {
            @Override
            public Proxy find(final String target) {
                return new Proxy(Proxy.Type.HTTP, "test.cyberduck.io", 9999);
            }
        }, new DisabledCertificateStore());
        assertFalse(r.isReachable(new Host(new TestProtocol(Scheme.http), "test.cyberduck.io")));
        assertFalse(r.isReachable(new Host(new TestProtocol(Scheme.https), "test.cyberduck.io")));
    }

    @Test
    public void testIsReachableNotFound() {
        final Reachability r = new HttpReachability();
        final Host host = new Host(new TestProtocol(Scheme.http), "test.cyberduck.io");
        host.setDefaultPath("/notfound");
        assertTrue(r.isReachable(host));
    }

    @Test
    public void testNotReachableSubdomain() {
        final Reachability r = new HttpReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.http), "a.cyberduck.io")
        ));
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.https), "a.cyberduck.io")
        ));
    }

    @Test
    public void testNotReachableWrongHostname() {
        final Reachability r = new HttpReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.http), "cyberduck.io.f")
        ));
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.https), "cyberduck.io.f")
        ));
    }
}