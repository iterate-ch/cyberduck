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

import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

/**
 * ownCloud Infinite Scale and OpenCloud running in a container to execute integration tests against. Containers are
 * started on first use and shared for the lifetime of the JVM.
 */
public enum OwncloudTestServer {
    /**
     * ownCloud Infinite Scale
     */
    OCIS("cyberduck-ocis", "ocis", "ocis", "OCIS"),
    /**
     * OpenCloud
     */
    OPENCLOUD("cyberduck-opencloud", "opencloud", "opencloud", "OC");

    public static final String USER = "admin";
    public static final String PASSWORD = "admin-password";

    private final String image;
    private final String resources;
    private final String binary;
    private final String prefix;
    private GenericContainer<?> container;
    private int port;

    OwncloudTestServer(final String image, final String resources, final String binary, final String prefix) {
        this.image = image;
        this.resources = resources;
        this.binary = binary;
        this.prefix = prefix;
    }

    private synchronized GenericContainer<?> container() {
        if(null == container) {
            try {
                // URL must match the external address and the service calls itself with that URL. Listen on the
                // same port inside the container as published on the host
                try(ServerSocket socket = new ServerSocket(0)) {
                    port = socket.getLocalPort();
                }
                final GenericContainer<?> created = new GenericContainer<>(new ImageFromDockerfile(image, false)
                    .withFileFromClasspath("Dockerfile", String.format("%s/Dockerfile", resources)))
                    .withCommand("-c", String.format("%s init --insecure yes --admin-password %s || true; %s server", binary, PASSWORD, binary))
                    .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/bin/sh"))
                    .withEnv(String.format("%s_URL", prefix), String.format("http://localhost:%d", port))
                    .withEnv("PROXY_HTTP_ADDR", String.format("0.0.0.0:%d", port))
                    .withEnv("PROXY_TLS", "false")
                    .withEnv("PROXY_ENABLE_BASIC_AUTH", "true")
                    .withEnv(String.format("%s_INSECURE", prefix), "true")
                    // Built-in IdP requires https issuer. Authenticate with basic auth against the embedded LDAP instead
                    .withEnv(String.format("%s_EXCLUDE_RUN_SERVICES", prefix), "idp")
                    .withEnv("IDM_ADMIN_PASSWORD", PASSWORD)
                    .withEnv(String.format("%s_LOG_LEVEL", prefix), "error")
                    .withExposedPorts(port)
                    .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(port), new ExposedPort(port))))
                    .waitingFor(Wait.forHttp("/graph/v1.0/me").forPort(port)
                        .withBasicCredentials(USER, PASSWORD).forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)));
                created.start();
                Runtime.getRuntime().addShutdownHook(new Thread(created::stop));
                awaitWebDAV();
                container = created;
            }
            catch(IOException e) {
                throw new IllegalStateException(String.format("Failure starting %s", this), e);
            }
        }
        return container;
    }

    public String host() {
        return this.container().getHost();
    }

    public int port() {
        this.container();
        return port;
    }

    /**
     * Graph API is available before the WebDAV backend is ready to serve requests
     */
    private void awaitWebDAV() throws IOException {
        final String authorization = Base64.getEncoder().encodeToString(String.format("%s:%s", USER, PASSWORD).getBytes(StandardCharsets.UTF_8));
        final long timeout = System.nanoTime() + Duration.ofMinutes(2).toNanos();
        try {
            while(!this.status(authorization).contains(" 207 ")) {
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

    private String status(final String authorization) throws IOException {
        try(Socket socket = new Socket("localhost", port)) {
            socket.getOutputStream().write(String.format("PROPFIND /remote.php/webdav/ HTTP/1.1\r\nHost: localhost:%d\r\nAuthorization: Basic %s\r\nDepth: 0\r\nConnection: close\r\n\r\n",
                port, authorization).getBytes(StandardCharsets.UTF_8));
            final String line = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
            return null == line ? StringUtils.EMPTY : line;
        }
    }
}
