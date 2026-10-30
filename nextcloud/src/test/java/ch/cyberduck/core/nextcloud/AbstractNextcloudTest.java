package ch.cyberduck.core.nextcloud;

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
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.Assert.fail;

@Category(TestcontainerTest.class)
public abstract class AbstractNextcloudTest {

    private static final String USER = "admin";
    private static final String PASSWORD = "admin-password";
    private static final int HTTP_PORT = 80;

    private static final GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("nextcloud:32-apache"))
            .withEnv("SQLITE_DATABASE", "nextcloud")
            .withEnv("NEXTCLOUD_ADMIN_USER", USER)
            .withEnv("NEXTCLOUD_ADMIN_PASSWORD", PASSWORD)
            .withExposedPorts(HTTP_PORT)
            .waitingFor(Wait.forHttp("/status.php").forResponsePredicate(body -> body.contains("\"installed\":true"))
                    .withStartupTimeout(Duration.ofMinutes(5)));

    @BeforeClass
    public static void start() throws Exception {
        container.start();
        // Advertises files.locking in capabilities
        final Container.ExecResult result = container.execInContainer("runuser", "-u", "www-data", "--",
                "php", "/var/www/html/occ", "app:install", "files_lock");
        if(result.getExitCode() != 0) {
            fail(String.format("Failure installing files_lock: %s", result.getStderr() + result.getStdout()));
        }
        awaitLockingCapability();
    }

    /**
     * Web server workers may still serve capabilities from before the app was installed
     */
    private static void awaitLockingCapability() throws Exception {
        final URL url = new URL(String.format("http://%s:%d/ocs/v1.php/cloud/capabilities?format=json",
                container.getHost(), container.getMappedPort(HTTP_PORT)));
        final String authorization = "Basic " + Base64.getEncoder().encodeToString(
                String.format("%s:%s", USER, PASSWORD).getBytes(StandardCharsets.UTF_8));
        final long timeout = System.currentTimeMillis() + Duration.ofMinutes(1).toMillis();
        while(System.currentTimeMillis() < timeout) {
            try {
                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("OCS-APIRequest", "true");
                connection.setRequestProperty("Authorization", authorization);
                try (InputStream in = connection.getInputStream()) {
                    if(IOUtils.toString(in, StandardCharsets.UTF_8).contains("\"locking\"")) {
                        return;
                    }
                }
            }
            catch(IOException e) {
                // Retry
            }
            Thread.sleep(1000L);
        }
        fail("Timeout waiting for files.locking capability");
    }

    @AfterClass
    public static void stop() {
        container.stop();
    }

    protected NextcloudSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final Host host = new Host(new NextcloudProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, container.getHost(), container.getMappedPort(HTTP_PORT), new Credentials(USER, PASSWORD));
        session = new NextcloudSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
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
