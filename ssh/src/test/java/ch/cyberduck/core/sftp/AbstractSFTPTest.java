package ch.cyberduck.core.sftp;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.VaultVersion;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.junit.Assert.fail;

@Category(TestcontainerTest.class)
public abstract class AbstractSFTPTest {

    private static final String USER = "test";
    private static final String PASSWORD = "test";
    private static final int SSH_PORT = 2222;
    /**
     * Home directory of user in container
     */
    protected static final String HOME = "/config";

    private static final GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("linuxserver/openssh-server:latest"))
            .withEnv("PUID", "1000")
            .withEnv("PGID", "1000")
            .withEnv("PASSWORD_ACCESS", "true")
            .withEnv("USER_NAME", USER)
            .withEnv("USER_PASSWORD", PASSWORD)
            .withExposedPorts(SSH_PORT)
            .waitingFor(Wait.forLogMessage(".*\\[ls\\.io-init\\] done.*", 1).withStartupTimeout(Duration.ofMinutes(2)));

    @BeforeClass
    public static void start() {
        container.start();
    }

    @AfterClass
    public static void stop() {
        container.stop();
    }

    protected SFTPSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultVersion.Type.V8, VaultVersion.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultVersion.Type vaultVersion;

    @Before
    public void setup() throws Exception {
        final Host host = new Host(new SFTPProtocol(), container.getHost(), container.getMappedPort(SSH_PORT), new Credentials(USER, PASSWORD));
        session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        },
                HostKeyCallback.noop,
                new DisabledPasswordStore(),
                ProgressListener.noop).connect(session, CancelCallback.noop);
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }
}
