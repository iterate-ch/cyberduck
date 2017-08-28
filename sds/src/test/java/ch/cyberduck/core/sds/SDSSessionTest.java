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
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSSessionTest {

    @BeforeClass
    public static void protocol() {
        ProtocolFactory.get().register(new SDSProtocol());
    }

    @Test
    public void testLoginUserPassword() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertFalse(session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginRadius() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/Secure Data Space (Radius).cyberduckprofile"));
        final Host host = new Host(profile, "duck.ssp-europe.eu", new Credentials(
                "rsa.user1", "1234"
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                return new Credentials(username, "889153");
            }
        }, new DisabledCancelCallback(), PathCache.empty());
        assertFalse(session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginOAuthExpiredRefreshToken() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/Secure Data Space (OAuth).cyberduckprofile"));
        final Host host = new Host(profile, "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore() {
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
        }, new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertFalse(session.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()).isEmpty());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testLoginFailure() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                "a", "s"
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        session.close();
    }
}