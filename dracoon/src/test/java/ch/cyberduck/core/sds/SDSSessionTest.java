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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSessionTest {

    @Test
    public void testLoginUserPassword() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginNotfound() throws Exception {
        final Host host = new Host(new SDSProtocol(), "heroes.dracoon.team", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        try {
            session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
            fail();
        }
        catch(LoginFailureException e) {
            assertEquals("Login failed", e.getMessage());
            assertEquals("Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testLoginRefreshToken() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        session.retryHandler.setTokens(System.getProperties().getProperty("sds.user"),
            System.getProperties().getProperty("sds.key"),
            "invalid");
        session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailureInvalidUser() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        session.retryHandler.setTokens(
            "invalid",
            System.getProperties().getProperty("sds.key"),
            "invalid");
        session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginRadius() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/DRACOON (Radius).cyberduckprofile"));
        final Host host = new Host(profile, "duck.ssp-europe.eu", new Credentials(
            "rsa.user1", "1234"
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                assertEquals("Multi-Factor Authentication", reason);
                assertFalse(options.user);
                assertTrue(options.password);
                return new Credentials(username, "889153");
            }
        }, new DisabledCancelCallback());
        assertFalse(session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginOAuthExpiredRefreshToken() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
            new Local("../profiles/DRACOON (OAuth).cyberduckprofile"));
        final Host host = new Host(profile, "duck.ssp-europe.eu", new Credentials(
            System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledPasswordStore() {
            @Override
            public String getPassword(Scheme scheme, int port, String hostname, String user) {
                if(user.equals("Secure Data Space (post@iterate.ch) OAuth2 Access Token")) {
                    return System.getProperties().getProperty("sds.accesstoken");
                }
                if(user.equals("Secure Data Space (post@iterate.ch) OAuth2 Refresh Token")) {
                    return System.getProperties().getProperty("sds.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
            "a", "s"
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }
}
