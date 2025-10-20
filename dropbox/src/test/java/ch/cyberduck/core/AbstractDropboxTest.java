package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.dropbox.DropboxProtocol;
import ch.cyberduck.core.dropbox.DropboxSession;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.fail;

public class AbstractDropboxTest extends VaultTest {

    protected DropboxSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultMetadata.Type.V8, VaultMetadata.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultMetadata.Type vaultVersion;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DropboxProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Dropbox.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("cyberduck"));
        session = new DropboxSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
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
            if(accountName.equals("Dropbox (cyberduck) OAuth2 Token Expiry")) {
                return PROPERTIES.get("dropbox.tokenexpiry");
            }
            return null;
        }

        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            if(user.equals("Dropbox (cyberduck) OAuth2 Access Token")) {
                return PROPERTIES.get("dropbox.accesstoken");
            }
            if(user.equals("Dropbox (cyberduck) OAuth2 Refresh Token")) {
                return PROPERTIES.get("dropbox.refreshtoken");
            }
            return null;
        }

        @Override
        public void addPassword(final String serviceName, final String accountName, final String password) {
            if(accountName.equals("Dropbox (cyberduck) OAuth2 Token Expiry")) {
                VaultTest.add("dropbox.tokenexpiry", password);
            }
        }

        @Override
        public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
            if(user.equals("Dropbox (cyberduck) OAuth2 Access Token")) {
                VaultTest.add("dropbox.accesstoken", password);
            }
            if(user.equals("Dropbox (cyberduck) OAuth2 Refresh Token")) {
                VaultTest.add("dropbox.refreshtoken", password);
            }
        }
    }
}
