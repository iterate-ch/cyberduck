package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.HashSet;

public class AbstractSDSTest extends VaultTest {

    protected SDSSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SDSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/DRACOON (CLI).cyberduckprofile"));
        final Host host = new Host(profile, "duck.dracoon.com", new Credentials(
                PROPERTIES.get("dracoon.user"), PROPERTIES.get("dracoon.key")));
        session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService connect = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                throw new LoginCanceledException();
            }
        }, new DisabledHostKeyCallback(),
                new TestPasswordStore(), new DisabledProgressListener());
        connect.check(session, new DisabledCancelCallback());
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(final String serviceName, final String accountName) {
            if(accountName.equals("DRACOON (OAuth) (dkocher+test@iterate.ch) OAuth2 Token Expiry")) {
                return PROPERTIES.get("dracoon.tokenexpiry");
            }
            return null;
        }

        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            if(user.equals("DRACOON (OAuth) (dkocher+test@iterate.ch) OAuth2 Access Token")) {
                return PROPERTIES.get("dracoon.accesstoken");
            }
            if(user.equals("DRACOON (OAuth) (dkocher+test@iterate.ch) OAuth2 Refresh Token")) {
                return PROPERTIES.get("dracoon.refreshtoken");
            }
            return null;
        }

        @Override
        public void addPassword(final String serviceName, final String accountName, final String password) {
            if(accountName.equals("DRACOON (OAuth) (dkocher+test@iterate.ch) OAuth2 Token Expiry")) {
                VaultTest.add("dracoon.tokenexpiry", password);
            }
        }

        @Override
        public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
            if(user.equals("DRACOON (OAuth) (dkocher+test@iterate.ch) OAuth2 Access Token")) {
                VaultTest.add("dracoon.accesstoken", password);
            }
            if(user.equals("DRACOON (OAuth) (dkocher+test@iterate.ch) OAuth2 Refresh Token")) {
                VaultTest.add("dracoon.refreshtoken", password);
            }
        }
    }
}
