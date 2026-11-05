package ch.cyberduck.core.dav;

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

import java.time.Duration;
import java.util.function.Supplier;

/**
 * WebDAV server implementations running in a container to execute integration tests against. Containers are started
 * on first use and shared for the lifetime of the JVM.
 */
public enum DAVTestServer {
    /**
     * Apache httpd with mod_dav
     */
    HTTPD(80, () -> new GenericContainer<>(new ImageFromDockerfile()
            .withFileFromClasspath("Dockerfile", "httpd/Dockerfile")
            .withFileFromClasspath("httpd.conf", "httpd/httpd.conf")
            .withFileFromClasspath("htpasswd", "httpd/htpasswd"))
            .withExposedPorts(80)
            .waitingFor(Wait.forHttp("/").forPort(80).withBasicCredentials(DAVTestServer.USER, DAVTestServer.PASSWORD).forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(5)))),
    SFTPGO(8090, () -> new GenericContainer<>(new ImageFromDockerfile("cyberduck-sftpgo-dav", false)
            .withFileFromClasspath("Dockerfile", "sftpgo/Dockerfile"))
            .withCopyToContainer(MountableFile.forClasspathResource("sftpgo/data.json", 0644), "/etc/sftpgo/data.json")
            .withEnv("SFTPGO_LOADDATA_FROM", "/etc/sftpgo/data.json")
            .withEnv("SFTPGO_LOADDATA_MODE", "1")
            .withEnv("SFTPGO_WEBDAVD__BINDINGS__0__PORT", "8090")
            .withExposedPorts(8090)
            .waitingFor(Wait.forHttp("/").forPort(8090).withBasicCredentials(DAVTestServer.USER, DAVTestServer.PASSWORD).forStatusCode(207)
                    .withStartupTimeout(Duration.ofMinutes(2))));

    public static final String USER = "cyberduck";
    public static final String PASSWORD = "cyberduck";

    private final int port;
    private final Supplier<GenericContainer<?>> factory;
    private GenericContainer<?> container;

    DAVTestServer(final int port, final Supplier<GenericContainer<?>> factory) {
        this.port = port;
        this.factory = factory;
    }

    public synchronized GenericContainer<?> container() {
        if(null == container) {
            final GenericContainer<?> created = factory.get();
            created.start();
            Runtime.getRuntime().addShutdownHook(new Thread(created::stop));
            container = created;
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
