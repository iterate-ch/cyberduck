package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.fail;

public class AbstractDeepboxTest extends VaultTest {

    protected DeepboxSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DeepboxProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Deepbox.cyberduckprofile"));
        // deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Access Token
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(PROPERTIES.get("deepbox.user")));
        session = new DeepboxSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(final String serviceName, final String accountName) {
            if(accountName.equals("deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Token Expiry")) {
                return PROPERTIES.get("deepbox.tokenexpiry");
            }
            return null;
        }

        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            if(user.equals("deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Access Token")) {
                return PROPERTIES.get("deepbox.accesstoken");
            }
            if(user.equals("deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Refresh Token")) {
                return PROPERTIES.get("deepbox.refreshtoken");
            }
            return null;
        }

        @Override
        public void addPassword(final String serviceName, final String accountName, final String password) {
            if(accountName.equals("deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Token Expiry")) {
                VaultTest.add("deepbox.tokenexpiry", password);
            }
        }

        @Override
        public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
            if(user.equals("deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Access Token")) {
                VaultTest.add("deepbox.accesstoken", password);
            }
            if(user.equals("deepbox-desktop-app-int (christian@iterate.ch) OAuth2 Refresh Token")) {
                VaultTest.add("deepbox.refreshtoken", password);
            }
        }
    }
}
