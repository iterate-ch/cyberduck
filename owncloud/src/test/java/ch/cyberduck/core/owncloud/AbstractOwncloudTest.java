package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.test.TestcontainerTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.junit.Assert.fail;

@Category(TestcontainerTest.class)
public abstract class AbstractOwncloudTest {

    private static final String USER = "admin";
    private static final String PASSWORD = "admin-password";
    private static final int HTTP_PORT = 8080;

    private static final GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("owncloud/server:latest"))
            .withEnv("OWNCLOUD_ADMIN_USERNAME", USER)
            .withEnv("OWNCLOUD_ADMIN_PASSWORD", PASSWORD)
            .withEnv("OWNCLOUD_DOMAIN", "localhost")
            .withEnv("OWNCLOUD_TRUSTED_DOMAINS", "localhost")
            .withEnv("OWNCLOUD_REDIS_ENABLED", "false")
            .withExposedPorts(HTTP_PORT)
            .waitingFor(Wait.forHttp("/status.php").forResponsePredicate(body -> body.contains("\"installed\":true"))
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @BeforeClass
    public static void start() {
        container.start();
    }

    @AfterClass
    public static void stop() {
        container.stop();
    }

    protected OwncloudSession session;

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
        }, container.getHost(), container.getMappedPort(HTTP_PORT), new Credentials(USER, PASSWORD));
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
}
