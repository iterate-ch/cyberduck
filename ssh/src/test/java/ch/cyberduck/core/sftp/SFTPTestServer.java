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
import org.testcontainers.utility.MountableFile;

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
            .withExposedPorts(2022)
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

    public String host() {
        return this.container().getHost();
    }

    public int port() {
        return this.container().getMappedPort(port);
    }
}
