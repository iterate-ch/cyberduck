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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * SFTP server implementations running in a container to execute integration tests against. Containers are started
 * on first use and shared for the lifetime of the JVM.
 */
public enum SFTPTestServer {
    OPENSSH("/config", () -> new GenericContainer<>(DockerImageName.parse("linuxserver/openssh-server:latest"))
            .withEnv("PUID", "1000")
            .withEnv("PGID", "1000")
            .withEnv("PASSWORD_ACCESS", "true")
            .withEnv("USER_NAME", SFTPTestServer.USER)
            .withEnv("USER_PASSWORD", SFTPTestServer.PASSWORD)
            .withExposedPorts(SFTPTestServer.PORT)
            .waitingFor(Wait.forLogMessage(".*\\[ls\\.io-init\\] done.*", 1).withStartupTimeout(Duration.ofMinutes(2)))),
    PROFTPD("/home/test", () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-proftpd-sftp", false)
            .withFileFromPath("Dockerfile", Paths.get("src/test/resources/proftpd/Dockerfile"))
            .withFileFromPath("proftpd.conf", Paths.get("src/test/resources/proftpd/proftpd.conf")))
            .withExposedPorts(SFTPTestServer.PORT)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5))));

    public static final String USER = "test";
    public static final String PASSWORD = "test";
    public static final int PORT = 2222;

    private final String home;
    private final Supplier<GenericContainer<?>> factory;
    private GenericContainer<?> container;

    SFTPTestServer(final String home, final Supplier<GenericContainer<?>> factory) {
        this.home = home;
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

    public String host() {
        return this.container().getHost();
    }

    public int port() {
        return this.container().getMappedPort(PORT);
    }
}
