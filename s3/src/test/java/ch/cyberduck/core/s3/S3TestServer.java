package ch.cyberduck.core.s3;

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

import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

/**
 * S3 server implementations to execute integration tests against.
 */
public enum S3TestServer {
    /**
     * Hosted Amazon S3
     */
    AWS,
    /**
     * LocalStack requires an auth token in environment <code>LOCALSTACK_AUTH_TOKEN</code>. Container image can be
     * overridden with environment <code>LOCALSTACK_IMAGE</code>.
     */
    LOCALSTACK;

    public static final String ACCESS_KEY = "test";
    public static final String SECRET_KEY = "test";
    private static final int LOCALSTACK_PORT = 4566;

    private GenericContainer<?> container;

    /**
     * @return False if prerequisites to run server are not met
     */
    public boolean isAvailable() {
        switch(this) {
            case LOCALSTACK:
                return StringUtils.isNotBlank(System.getenv("LOCALSTACK_AUTH_TOKEN"))
                        || StringUtils.isNotBlank(System.getenv("LOCALSTACK_IMAGE"));
            default:
                return StringUtils.isNotBlank(System.getenv("VAULT_TOKEN"));
        }
    }

    public synchronized GenericContainer<?> container() {
        if(null == container) {
            final GenericContainer<?> created = new GenericContainer<>(DockerImageName.parse(StringUtils.defaultIfBlank(System.getenv("LOCALSTACK_IMAGE"), "localstack/localstack:latest")))
                    .withEnv("SERVICES", "s3,kms")
                    .withCopyToContainer(MountableFile.forClasspathResource("localstack/init.sh", 0755), "/etc/localstack/init/ready.d/init.sh")
                    .withExposedPorts(LOCALSTACK_PORT)
                    .waitingFor(Wait.forLogMessage(".*localstack init done.*", 1).withStartupTimeout(Duration.ofMinutes(5)));
            if(StringUtils.isNotBlank(System.getenv("LOCALSTACK_AUTH_TOKEN"))) {
                created.withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv("LOCALSTACK_AUTH_TOKEN"));
            }
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
        return this.container().getMappedPort(LOCALSTACK_PORT);
    }
}
