package ch.cyberduck.core.owncloud;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.VaultVersion;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@Category(TestcontainerTest.class)
@RunWith(Parameterized.class)
public abstract class AbstractOwncloudTest {

    protected OwncloudSession session;

    /**
     * Run against every server implementation
     */
    @Parameterized.Parameters(name = "server = {0}")
    public static Object[][] configurations() {
        final Object[][] data = new Object[OwncloudTestServer.values().length][];
        for(int i = 0; i < data.length; i++) {
            data[i] = new Object[]{OwncloudTestServer.values()[i], null};
        }
        return data;
    }

    /**
     * Run against every server implementation with all vault versions
     */
    protected static Object[][] parameterMatrix() {
        final Object[][] data = new Object[OwncloudTestServer.values().length * 2][];
        int i = 0;
        for(OwncloudTestServer server : OwncloudTestServer.values()) {
            for(VaultVersion.Type type : new VaultVersion.Type[]{VaultVersion.Type.V8, VaultVersion.Type.UVF}) {
                data[i++] = new Object[]{server, type};
            }
        }
        return data;
    }

    @Parameterized.Parameter(0)
    public OwncloudTestServer server;

    @Parameterized.Parameter(1)
    public VaultVersion.Type vaultVersion;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final Host host = new Host(new OwncloudProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, server.host(), server.port(), "/remote.php/webdav/", new Credentials(OwncloudTestServer.USER, OwncloudTestServer.PASSWORD));
        session = new OwncloudSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
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
        }, HostKeyCallback.noop, new DisabledPasswordStore(), ProgressListener.noop);
        login.check(session, CancelCallback.noop);
    }

    /**
     * Skip test unless running against the given server implementation
     */
    protected void assumeOnly(final OwncloudTestServer expected, final String reason) {
        assumeTrue(reason, server == expected);
    }
}
