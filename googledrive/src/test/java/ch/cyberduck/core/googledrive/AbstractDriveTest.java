package ch.cyberduck.core.googledrive;

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

public class AbstractDriveTest {

    protected DriveSession session;

    @BeforeClass
    public static void protocol() {
        ProtocolFactory.get().register(new DriveProtocol());
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/default/Google Drive.cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname());
        session = new DriveSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        if(user.equals("Google Drive OAuth2 Access Token")) {
                            return System.getProperties().getProperty("googledrive.accesstoken");
                        }
                        if(user.equals("Google Drive OAuth2 Refresh Token")) {
                            return System.getProperties().getProperty("googledrive.refreshtoken");
                        }
                        return null;
                    }
                }, new DisabledProgressListener()).connect(session, PathCache.empty(), new DisabledCancelCallback());
    }
}
