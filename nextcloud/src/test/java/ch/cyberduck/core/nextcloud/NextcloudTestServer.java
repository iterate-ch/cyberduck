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

import org.apache.commons.io.IOUtils;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Nextcloud and ownCloud Server running in a container to execute integration tests against. Containers are started
 * on first use and shared for the lifetime of the JVM.
 */
public enum NextcloudTestServer {
    /**
     * Nextcloud with files_lock app
     */
    NEXTCLOUD(80) {
        @Override
        protected GenericContainer<?> create() throws Exception {
            final GenericContainer<?> container = new GenericContainer<>(new ImageFromDockerfile("cyberduck-nextcloud", false)
                .withFileFromClasspath("Dockerfile", "nextcloud/Dockerfile"))
                .withEnv("SQLITE_DATABASE", "nextcloud")
                .withEnv("NEXTCLOUD_ADMIN_USER", USER)
                .withEnv("NEXTCLOUD_ADMIN_PASSWORD", PASSWORD)
                .withExposedPorts(80)
                .waitingFor(Wait.forHttp("/status.php").forResponsePredicate(body -> body.contains("\"installed\":true"))
                    .withStartupTimeout(Duration.ofMinutes(5)));
            container.start();
            // Advertises files.locking in capabilities
            final Container.ExecResult result = container.execInContainer("runuser", "-u", "www-data", "--",
                "php", "/var/www/html/occ", "app:install", "files_lock");
            if(result.getExitCode() != 0) {
                throw new IOException(String.format("Failure installing files_lock: %s", result.getStderr() + result.getStdout()));
            }
            this.awaitLockingCapability(container);
            return container;
        }
    },
    /**
     * ownCloud Server with built-in file locking
     */
    OWNCLOUD(8080) {
        @Override
        protected GenericContainer<?> create() {
            final GenericContainer<?> container = new GenericContainer<>(new ImageFromDockerfile("cyberduck-owncloud", false)
                .withFileFromClasspath("Dockerfile", "owncloud/Dockerfile"))
                .withEnv("OWNCLOUD_ADMIN_USERNAME", USER)
                .withEnv("OWNCLOUD_ADMIN_PASSWORD", PASSWORD)
                .withEnv("OWNCLOUD_DOMAIN", "localhost")
                .withEnv("OWNCLOUD_TRUSTED_DOMAINS", "localhost")
                .withEnv("OWNCLOUD_REDIS_ENABLED", "false")
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/status.php").forResponsePredicate(body -> body.contains("\"installed\":true"))
                    .withStartupTimeout(Duration.ofMinutes(5)));
            container.start();
            return container;
        }
    };

    public static final String USER = "admin";
    public static final String PASSWORD = "admin-password";

    private final int port;
    private GenericContainer<?> container;

    NextcloudTestServer(final int port) {
        this.port = port;
    }

    protected abstract GenericContainer<?> create() throws Exception;

    private synchronized GenericContainer<?> container() {
        if(null == container) {
            try {
                final GenericContainer<?> created = this.create();
                Runtime.getRuntime().addShutdownHook(new Thread(created::stop));
                container = created;
            }
            catch(Exception e) {
                throw new IllegalStateException(String.format("Failure starting %s", this), e);
            }
        }
        return container;
    }

    public String host() {
        return this.container().getHost();
    }

    public int port() {
        return this.container().getMappedPort(port);
    }

    /**
     * Web server workers may still serve capabilities from before the app was installed
     */
    protected void awaitLockingCapability(final GenericContainer<?> container) throws Exception {
        final URL url = new URL(String.format("http://%s:%d/ocs/v1.php/cloud/capabilities?format=json",
            container.getHost(), container.getMappedPort(port)));
        final String authorization = "Basic " + Base64.getEncoder().encodeToString(
            String.format("%s:%s", USER, PASSWORD).getBytes(StandardCharsets.UTF_8));
        final long timeout = System.currentTimeMillis() + Duration.ofMinutes(1).toMillis();
        while(System.currentTimeMillis() < timeout) {
            try {
                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("OCS-APIRequest", "true");
                connection.setRequestProperty("Authorization", authorization);
                try(InputStream in = connection.getInputStream()) {
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
        throw new IOException("Timeout waiting for files.locking capability");
    }
}
