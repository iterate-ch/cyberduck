package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

public class AbstractBrickTest extends VaultTest {

    protected BrickSession session;

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
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new BrickProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Brick.cyberduckprofile"));
        final Host host = new Host(profile, "mountainduck.files.com", new Credentials(
                PROPERTIES.get("brick.user"), PROPERTIES.get("brick.password")
        ));
        session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }
}
