package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.ProxyLoginFailureException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSessionTest extends AbstractSDSTest {

    @Test
    public void testLoginUserPassword() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.dracoon.com", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(new SDSListService(session, new SDSNodeIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
    }

    @Test(expected = InteroperabilityException.class)
    public void testLoginNotfound() throws Exception {
        final Host host = new Host(new SDSProtocol(), "heroes.dracoon.team", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        fail();
    }

    @Test
    public void testLoginRefreshToken() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.dracoon.com", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        session.retryHandler.setTokens(System.getProperties().getProperty("sds.user"),
            System.getProperties().getProperty("sds.key"),
            "invalid");
        new SDSListService(session, new SDSNodeIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureInvalidUser() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.dracoon.com", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        session.retryHandler.setTokens(
            "invalid",
            System.getProperties().getProperty("sds.key"),
            "invalid");
        new SDSListService(session, new SDSNodeIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Ignore
    @Test(expected = ConnectionTimeoutException.class)
    public void testLoginRadius() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/DRACOON (Radius).cyberduckprofile"));
        final Host host = new Host(profile, "duck.dracoon.com", new Credentials(
            "rsa.user1", "1234"
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                assertEquals("Multi-Factor Authentication", reason);
                assertFalse(options.user);
                assertTrue(options.password);
                return new Credentials(StringUtils.EMPTY, "889153");
            }
        }, new DisabledCancelCallback());
        assertFalse(new SDSListService(session, new SDSNodeIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginOAuthExpiredRefreshToken() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/DRACOON (OAuth).cyberduckprofile"));
        final Host host = new Host(profile, "duck.dracoon.com", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(new SDSListService(session, new SDSNodeIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.dracoon.com", new Credentials(
            "a", "s"
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testProxyNoConnect() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.dracoon.com", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService c = new LoginConnectionService(
            new DisabledLoginCallback(),
            new DisabledHostKeyCallback(),
            new DisabledPasswordStore(),
            new DisabledProgressListener(),
            new ProxyFinder() {
                @Override
                public Proxy find(final Host target) {
                    return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                }
            }
        );
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
    }

    @Ignore
    @Test(expected = ProxyLoginFailureException.class)
    public void testConnectProxyInvalidCredentials() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.dracoon.com", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DefaultX509TrustManager(),
            new KeychainX509KeyManager(new DisabledCertificateIdentityCallback(), host, new DisabledCertificateStore())) {
        };
        final LoginConnectionService c = new LoginConnectionService(
            new DisabledLoginCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                    return new Credentials("test", "n");
                }
            },
            new DisabledHostKeyCallback(),
            new DisabledPasswordStore(),
            new DisabledProgressListener(),
            new ProxyFinder() {
                @Override
                public Proxy find(final Host target) {
                    return new Proxy(Proxy.Type.HTTP, "localhost", 3128);
                }
            }
        );
        c.connect(session, PathCache.empty(), new DisabledCancelCallback());
    }
}
