package ch.cyberduck.core.dav.microsoft;

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
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.fail;

public class AbstractMicrosoftIISDAVTest extends VaultTest {

    protected DAVSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new DAVProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/DAV.cyberduckprofile"));
        session = new DAVSession(new Host(profile, "winbuild.iterate.ch", profile.getDefaultPort(), "/WebDAV", new Credentials(
                PROPERTIES.get("webdav.iis.user"), PROPERTIES.get("webdav.iis.password")
        )), new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }

            @Override
            public void warn(final Host bookmark, final String title, final String message, final String continueButton, final String disconnectButton, final String preference) {
                //
            }
        }, new DisabledHostKeyCallback(), new TestPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    public static class TestPasswordStore extends DisabledPasswordStore {
        @Override
        public String getPassword(Scheme scheme, int port, String hostname, String user) {
            return PROPERTIES.get("webdav.iis.password");
        }
    }
}
