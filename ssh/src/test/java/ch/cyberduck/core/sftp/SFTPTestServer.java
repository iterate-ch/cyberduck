package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * SFTP server implementations running in a container to execute integration tests against. Containers are started
 * on first use and shared for the lifetime of the JVM.
 */
public enum SFTPTestServer {
    OPENSSH("/config", 2222, () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-openssh-sftp", false)
            .withFileFromClasspath("Dockerfile", "openssh/Dockerfile"))
            .withEnv("PUID", "1000")
            .withEnv("PGID", "1000")
            .withEnv("PASSWORD_ACCESS", "true")
            .withEnv("USER_NAME", SFTPTestServer.USER)
            .withEnv("USER_PASSWORD", SFTPTestServer.PASSWORD)
            .withExposedPorts(2222)
            .waitingFor(Wait.forLogMessage(".*\\[ls\\.io-init\\] done.*", 1).withStartupTimeout(Duration.ofMinutes(2)))),
    PROFTPD("/home/test", 2222, () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-proftpd-sftp", false)
            .withFileFromPath("Dockerfile", Paths.get("src/test/resources/proftpd/Dockerfile"))
            .withFileFromPath("proftpd.conf", Paths.get("src/test/resources/proftpd/proftpd.conf")))
            .withExposedPorts(2222)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)))),
    SFTPGO("/", 2022, () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-sftpgo-sftp", false)
            .withFileFromClasspath("Dockerfile", "sftpgo/Dockerfile"))
            .withCopyToContainer(MountableFile.forClasspathResource("sftpgo/data.json", 0644), "/etc/sftpgo/data.json")
            .withEnv("SFTPGO_LOADDATA_FROM", "/etc/sftpgo/data.json")
            .withEnv("SFTPGO_LOADDATA_MODE", "1")
            .withExposedPorts(2022, 8080)
            .waitingFor(Wait.forLogMessage(".*server listener registered, address: \\[::\\]:2022.*", 1).withStartupTimeout(Duration.ofMinutes(2))));

    public static final String USER = "test";
    public static final String PASSWORD = "test";

    private final String home;
    private final int port;
    private final Supplier<GenericContainer<?>> factory;
    private GenericContainer<?> container;

    SFTPTestServer(final String home, final int port, final Supplier<GenericContainer<?>> factory) {
        this.home = home;
        this.port = port;
        this.factory = factory;
    }

    /**
     * @return Home directory of user in container
     */
    public String home() {
        return home;
    }

    public synchronized GenericContainer<?> container() {
        if(null == container) {
            container = factory.get();
            container.start();
            final GenericContainer<?> started = container;
            Runtime.getRuntime().addShutdownHook(new Thread(started::stop));
        }
        return container;
    }

    /**
     * Accept public key authentication of the test user with the given key
     *
     * @param key Public key in OpenSSH format (authorized_keys line)
     */
    public void authorize(final String key) throws Exception {
        final GenericContainer<?> c = this.container();
        switch(this) {
            case OPENSSH:
                c.copyFileToContainer(Transferable.of(key + "\n", 0600), "/config/.ssh/authorized_keys");
                c.execInContainer("chown", "-R", "1000:1000", "/config/.ssh");
                c.execInContainer("chmod", "700", "/config/.ssh");
                break;
            case PROFTPD:
                // mod_sftp expects RFC 4716 format
                c.execInContainer("mkdir", "-p", "/home/test/.ssh");
                c.copyFileToContainer(Transferable.of(key + "\n", 0600), "/tmp/authorized_key.pub");
                c.execInContainer("sh", "-c", "ssh-keygen -e -f /tmp/authorized_key.pub > /home/test/.ssh/authorized_keys");
                c.execInContainer("chown", "-R", "test:test", "/home/test/.ssh");
                break;
            case SFTPGO:
                final ObjectMapper mapper = new ObjectMapper();
                final String api = String.format("http://%s:%d/api/v2", this.host(), c.getMappedPort(8080));
                final String credentials = Base64.getEncoder().encodeToString("admin:password".getBytes(StandardCharsets.UTF_8));
                final String token = mapper.readTree(request("GET", String.format("%s/token", api), "Basic " + credentials, null))
                        .get("access_token").asText();
                final ObjectNode user = (ObjectNode) mapper.readTree(request("GET", String.format("%s/users/%s", api, USER), "Bearer " + token, null));
                user.putArray("public_keys").add(key);
                request("PUT", String.format("%s/users/%s", api, USER), "Bearer " + token, mapper.writeValueAsString(user));
                break;
        }
    }

    private static String request(final String method, final String url, final String authorization, final String body) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authorization);
            if(body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            }
            if(connection.getResponseCode() != 200) {
                throw new IOException(String.format("%d %s", connection.getResponseCode(), connection.getResponseMessage()));
            }
            return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        }
        finally {
            connection.disconnect();
        }
    }

    public String host() {
        return this.container().getHost();
    }

    public int port() {
        return this.container().getMappedPort(port);
    }
}
