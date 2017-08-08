package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.junit.Assert.fail;

public class AbstractGoogleStorageTest {

    protected GoogleStorageSession session;

    @BeforeClass
    public static void protocol() {
        ProtocolFactory.get().register(new GoogleStorageProtocol());
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/default/Google Cloud Storage.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("google.projectid"), null
        ));
        session = new GoogleStorageSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final String username, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                        if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Access Token")) {
                            return System.getProperties().getProperty("google.accesstoken");
                        }
                        if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Refresh Token")) {
                            return System.getProperties().getProperty("google.refreshtoken");
                        }
                        return null;
                    }
                }, new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
    }
}
