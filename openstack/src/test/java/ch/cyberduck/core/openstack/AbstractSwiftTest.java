package ch.cyberduck.core.openstack;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Scheme;
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

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.fail;

@Category(TestcontainerTest.class)
public abstract class AbstractSwiftTest {

    private static final String USER = "test:tester";
    private static final String PASSWORD = "testing";
    private static final int HTTP_PORT = 8080;

    private static final GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("openstackswift/saio:latest"))
            .withExposedPorts(HTTP_PORT)
            .waitingFor(Wait.forHttp("/healthcheck").forPort(HTTP_PORT).withStartupTimeout(Duration.ofMinutes(2)));

    @BeforeClass
    public static void start() throws Exception {
        container.start();
        final String base = String.format("http://%s:%d", container.getHost(), container.getMappedPort(HTTP_PORT));
        final HttpURLConnection auth = (HttpURLConnection) new URL(String.format("%s/auth/v1.0", base)).openConnection();
        auth.setRequestProperty("X-Auth-User", USER);
        auth.setRequestProperty("X-Auth-Key", PASSWORD);
        final String token = auth.getHeaderField("X-Auth-Token");
        final String storage = auth.getHeaderField("X-Storage-Url");
        if(null == token || null == storage) {
            fail(String.format("Failure authenticating with %d", auth.getResponseCode()));
        }
        // Storage URL is relative to host and port of request
        final HttpURLConnection put = (HttpURLConnection) new URL(String.format("%s/test.cyberduck.ch", storage)).openConnection();
        put.setRequestMethod("PUT");
        put.setRequestProperty("X-Auth-Token", token);
        if(put.getResponseCode() / 100 != 2) {
            fail(String.format("Failure creating container with %d", put.getResponseCode()));
        }
    }

    @AfterClass
    public static void stop() {
        container.stop();
    }

    protected SwiftSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultVersion.Type.V8, VaultVersion.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultVersion.Type vaultVersion;

    protected static Host bookmark() {
        return bookmark(new Credentials(USER, PASSWORD), Collections.emptyMap());
    }

    protected static Host bookmark(final Credentials credentials) {
        return bookmark(credentials, Collections.emptyMap());
    }

    /**
     * @param properties Bookmark specific properties
     */
    protected static Host bookmark(final Map<String, String> properties) {
        return bookmark(new Credentials(USER, PASSWORD), properties);
    }

    private static Host bookmark(final Credentials credentials, final Map<String, String> properties) {
        return new Host(new SwiftProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }

            @Override
            public String getContext() {
                return "/auth/v1.0";
            }
        }, container.getHost(), container.getMappedPort(HTTP_PORT), credentials) {
            @Override
            public String getProperty(final String key) {
                return properties.getOrDefault(key, super.getProperty(key));
            }
        };
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        session = new SwiftSession(bookmark(), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, HostKeyCallback.noop,
                new DisabledPasswordStore(), ProgressListener.noop);
        login.check(session, CancelCallback.noop);
    }
}
