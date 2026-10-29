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

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.Assert.fail;

@Category(TestcontainerTest.class)
public abstract class AbstractOcisTest {

    private static final String USER = "admin";
    private static final String PASSWORD = "admin-password";
    private static final int HTTP_PORT = 9200;

    private static GenericContainer<?> container;
    private static int port;

    @BeforeClass
    public static void start() throws IOException {
        // OC_URL must match the external address, so bind to a known host port
        try(ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        container = new GenericContainer<>(DockerImageName.parse("opencloudeu/opencloud-rolling:latest"))
                .withCommand("-c", "opencloud init --insecure yes --admin-password " + PASSWORD + " || true; opencloud server")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/bin/sh"))
                .withEnv("OC_URL", String.format("http://localhost:%d", port))
                .withEnv("PROXY_TLS", "false")
                .withEnv("PROXY_ENABLE_BASIC_AUTH", "true")
                .withEnv("OC_INSECURE", "true")
                // Built-in IdP requires https issuer. Authenticate with basic auth against the embedded LDAP instead
                .withEnv("OC_EXCLUDE_RUN_SERVICES", "idp")
                .withEnv("IDM_ADMIN_PASSWORD", PASSWORD)
                .withEnv("OC_LOG_LEVEL", "error")
                .withExposedPorts(HTTP_PORT)
                .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(port), new ExposedPort(HTTP_PORT))))
                .waitingFor(Wait.forHttp("/graph/v1.0/me").forPort(HTTP_PORT)
                        .withBasicCredentials(USER, PASSWORD).forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)));
        container.start();
        awaitWebDAV();
    }

    /**
     * Graph API is available before the WebDAV backend is ready to serve requests
     */
    private static void awaitWebDAV() throws IOException {
        final String authorization = Base64.getEncoder().encodeToString(String.format("%s:%s", USER, PASSWORD).getBytes(StandardCharsets.UTF_8));
        final long timeout = System.nanoTime() + Duration.ofMinutes(2).toNanos();
        try {
            while(!status(authorization).contains(" 207 ")) {
                if(System.nanoTime() > timeout) {
                    throw new IOException("Timeout waiting for WebDAV");
                }
                Thread.sleep(1000L);
            }
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private static String status(final String authorization) throws IOException {
        try(Socket socket = new Socket("localhost", port)) {
            socket.getOutputStream().write(String.format("PROPFIND /remote.php/webdav/ HTTP/1.1\r\nHost: localhost:%d\r\nAuthorization: Basic %s\r\nDepth: 0\r\nConnection: close\r\n\r\n",
                    port, authorization).getBytes(StandardCharsets.UTF_8));
            final String line = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
            return null == line ? StringUtils.EMPTY : line;
        }
    }

    @AfterClass
    public static void stop() {
        container.stop();
    }

    protected OwncloudSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultVersion.Type.V8, VaultVersion.Type.UVF};
    }

    @Parameterized.Parameter
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
        }, "localhost", port, "/remote.php/webdav/", new Credentials(USER, PASSWORD));
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
