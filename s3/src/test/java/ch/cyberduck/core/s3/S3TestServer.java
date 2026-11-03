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
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
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
    LOCALSTACK,
    /**
     * MinIO AIStor requires a license in environment <code>MINIO_LICENSE</code>. Container images can be overridden
     * with environment <code>MINIO_IMAGE</code> and <code>MINIO_MC_IMAGE</code>.
     */
    MINIO;

    public static final String ACCESS_KEY = "test";
    public static final String SECRET_KEY = "testtest";
    private static final int LOCALSTACK_PORT = 4566;
    private static final int MINIO_PORT = 9000;

    private GenericContainer<?> container;

    /**
     * @return False if prerequisites to run server are not met
     */
    public boolean isAvailable() {
        switch(this) {
            case LOCALSTACK:
                return StringUtils.isNotBlank(System.getenv("LOCALSTACK_AUTH_TOKEN"))
                        || StringUtils.isNotBlank(System.getenv("LOCALSTACK_IMAGE"));
            case MINIO:
                return StringUtils.isNotBlank(System.getenv("MINIO_LICENSE"));
            default:
                return StringUtils.isNotBlank(System.getenv("VAULT_TOKEN"));
        }
    }

    public synchronized GenericContainer<?> container() {
        if(null == container) {
            final GenericContainer<?> created;
            switch(this) {
                case LOCALSTACK:
                    created = localstack();
                    break;
                case MINIO:
                    created = minio();
                    break;
                default:
                    throw new IllegalStateException(String.format("No container for %s", this));
            }
            Runtime.getRuntime().addShutdownHook(new Thread(created::stop));
            container = created;
        }
        return container;
    }

    private static GenericContainer<?> localstack() {
        final GenericContainer<?> created = new GenericContainer<>(DockerImageName.parse(StringUtils.defaultIfBlank(System.getenv("LOCALSTACK_IMAGE"), "localstack/localstack:latest")))
                .withEnv("SERVICES", "s3,kms")
                .withCopyToContainer(MountableFile.forClasspathResource("localstack/init.sh", 0755), "/etc/localstack/init/ready.d/init.sh")
                .withExposedPorts(LOCALSTACK_PORT)
                .waitingFor(Wait.forLogMessage(".*localstack init done.*", 1).withStartupTimeout(Duration.ofMinutes(5)));
        if(StringUtils.isNotBlank(System.getenv("LOCALSTACK_AUTH_TOKEN"))) {
            created.withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv("LOCALSTACK_AUTH_TOKEN"));
        }
        created.start();
        return created;
    }

    private static GenericContainer<?> minio() {
        final Path license;
        try {
            license = Files.createTempFile("minio", ".license", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
            Files.write(license, System.getenv("MINIO_LICENSE").getBytes(StandardCharsets.UTF_8));
            license.toFile().deleteOnExit();
        }
        catch(IOException e) {
            throw new UncheckedIOException(e);
        }
        final Network network = Network.newNetwork();
        final GenericContainer<?> created = new GenericContainer<>(DockerImageName.parse(StringUtils.defaultIfBlank(System.getenv("MINIO_IMAGE"), "quay.io/minio/aistor/minio:latest")))
                .withNetwork(network)
                .withNetworkAliases("minio")
                .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
                .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
                .withCopyToContainer(MountableFile.forHostPath(license, 0444), "/minio.license")
                .withCommand("server", "/data", "--license", "/minio.license")
                .withExposedPorts(MINIO_PORT)
                .waitingFor(Wait.forHttp("/minio/health/ready").forPort(MINIO_PORT).withStartupTimeout(Duration.ofMinutes(5)));
        created.start();
        // Create buckets expected by tests
        try(GenericContainer<?> mc = new GenericContainer<>(DockerImageName.parse(StringUtils.defaultIfBlank(System.getenv("MINIO_MC_IMAGE"), "quay.io/minio/aistor/mc:latest")))
                .withNetwork(network)
                .withCopyToContainer(MountableFile.forClasspathResource("minio/init.sh", 0755), "/init.sh")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh", "-c", "sh /init.sh && sleep 3600"))
                .withEnv("MINIO_ENDPOINT", "http://minio:9000")
                .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
                .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
                .waitingFor(Wait.forLogMessage(".*minio init done.*", 1).withStartupTimeout(Duration.ofMinutes(2)))) {
            mc.start();
        }
        return created;
    }

    public String host() {
        return this.container().getHost();
    }

    public int port() {
        switch(this) {
            case MINIO:
                return this.container().getMappedPort(MINIO_PORT);
            default:
                return this.container().getMappedPort(LOCALSTACK_PORT);
        }
    }
}
