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
import org.testcontainers.images.builder.ImageFromDockerfile;
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
     * Hosted Amazon S3 with bucket name in the hostname. Only run tests annotated with {@link RunOn}.
     */
    AWS_VIRTUALHOST,
    /**
     * Hosted Amazon S3 with CloudFront distribution using a bucket as origin. Only run tests annotated with
     * {@link RunOn}.
     */
    AWS_CLOUDFRONT,
    /**
     * LocalStack requires an auth token in environment <code>LOCALSTACK_AUTH_TOKEN</code>. Container image can be
     * overridden with environment <code>LOCALSTACK_IMAGE</code>.
     */
    LOCALSTACK,
    /**
     * MinIO AIStor requires a license in environment <code>MINIO_LICENSE</code>. Container images can be overridden
     * with environment <code>MINIO_IMAGE</code> and <code>MC_IMAGE</code>.
     */
    MINIO,
    /**
     * Ceph RADOS Gateway. Container image can be overridden with environment <code>CEPH_IMAGE</code>.
     */
    CEPH,
    /**
     * RustFS. Container image can be overridden with environment <code>RUSTFS_IMAGE</code>.
     */
    RUSTFS,
    /**
     * SeaweedFS. Container image can be overridden with environment <code>SEAWEEDFS_IMAGE</code>.
     */
    SEAWEEDFS,
    /**
     * Scality CloudServer. Container image can be overridden with environment <code>CLOUDSERVER_IMAGE</code>.
     */
    CLOUDSERVER;

    public static final String ACCESS_KEY = "test";
    public static final String SECRET_KEY = "testtest";
    private static final int LOCALSTACK_PORT = 4566;
    private static final int MINIO_PORT = 9000;
    private static final int CEPH_PORT = 8080;
    private static final int RUSTFS_PORT = 9000;
    private static final int SEAWEEDFS_PORT = 8333;
    private static final int CLOUDSERVER_PORT = 8000;

    private GenericContainer<?> container;

    /**
     * @return True for configurations of a server that only run tests annotated with {@link RunOn}
     */
    public boolean isVariant() {
        return this == AWS_VIRTUALHOST || this == AWS_CLOUDFRONT;
    }

    /**
     * @return True for hosted Amazon S3
     */
    public boolean isHosted() {
        return this == AWS || this.isVariant();
    }

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
            case CEPH:
            case RUSTFS:
            case SEAWEEDFS:
            case CLOUDSERVER:
                return true;
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
                case CEPH:
                    created = ceph();
                    break;
                case RUSTFS:
                    created = rustfs();
                    break;
                case SEAWEEDFS:
                    created = seaweedfs();
                    break;
                case CLOUDSERVER:
                    created = cloudserver();
                    break;
                default:
                    throw new IllegalStateException(String.format("No container for %s", this));
            }
            Runtime.getRuntime().addShutdownHook(new Thread(created::stop));
            container = created;
        }
        return container;
    }

    /**
     * @param environment Name of environment variable to override the image
     * @param directory   Directory in test resources with the Dockerfile that defines the image to allow updates with Dependabot
     */
    private static GenericContainer<?> image(final String environment, final String directory) {
        final String override = System.getenv(environment);
        if(StringUtils.isNotBlank(override)) {
            return new GenericContainer<>(DockerImageName.parse(override));
        }
        return new GenericContainer<>(new ImageFromDockerfile(String.format("cyberduck-s3-%s", directory), false)
                .withFileFromClasspath("Dockerfile", String.format("%s/Dockerfile", directory)));
    }

    private static GenericContainer<?> localstack() {
        final GenericContainer<?> created = image("LOCALSTACK_IMAGE", "localstack")
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
        final GenericContainer<?> created = image("MINIO_IMAGE", "minio")
                .withNetwork(network)
                .withNetworkAliases("minio")
                .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
                .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
                .withCopyToContainer(MountableFile.forHostPath(license, 0444), "/minio.license")
                .withCommand("server", "/data", "--license", "/minio.license")
                .withExposedPorts(MINIO_PORT)
                .waitingFor(Wait.forHttp("/minio/health/ready").forPort(MINIO_PORT).withStartupTimeout(Duration.ofMinutes(5)));
        created.start();
        createBuckets(network, null, "http://minio:9000", null);
        return created;
    }

    private static GenericContainer<?> rustfs() {
        final Network network = Network.newNetwork();
        final GenericContainer<?> created = image("RUSTFS_IMAGE", "rustfs")
                .withNetwork(network)
                .withNetworkAliases("rustfs")
                .withEnv("RUSTFS_ACCESS_KEY", ACCESS_KEY)
                .withEnv("RUSTFS_SECRET_KEY", SECRET_KEY)
                .withExposedPorts(RUSTFS_PORT)
                .waitingFor(Wait.forHttp("/health").forPort(RUSTFS_PORT).withStartupTimeout(Duration.ofMinutes(5)));
        created.start();
        createBuckets(network, null, "http://rustfs:9000", null);
        return created;
    }

    private static GenericContainer<?> seaweedfs() {
        final Network network = Network.newNetwork();
        final GenericContainer<?> created = image("SEAWEEDFS_IMAGE", "seaweedfs")
                .withNetwork(network)
                .withNetworkAliases("seaweedfs")
                .withCopyToContainer(MountableFile.forClasspathResource("seaweedfs/s3.json"), "/etc/seaweedfs/s3.json")
                .withCommand("server", "-s3", "-s3.config=/etc/seaweedfs/s3.json", "-dir=/data")
                .withExposedPorts(SEAWEEDFS_PORT)
                // Unauthenticated request is rejected once the S3 gateway is ready
                .waitingFor(Wait.forHttp("/").forPort(SEAWEEDFS_PORT).forStatusCode(403).withStartupTimeout(Duration.ofMinutes(5)));
        created.start();
        createBuckets(network, null, "http://seaweedfs:8333", null);
        return created;
    }

    private static GenericContainer<?> cloudserver() {
        final GenericContainer<?> created = image("CLOUDSERVER_IMAGE", "cloudserver")
                .withEnv("S3BACKEND", "mem")
                .withEnv("REMOTE_MANAGEMENT_DISABLE", "1")
                .withEnv("SCALITY_ACCESS_KEY_ID", ACCESS_KEY)
                .withEnv("SCALITY_SECRET_ACCESS_KEY", SECRET_KEY)
                .withExposedPorts(CLOUDSERVER_PORT)
                // Unauthenticated request is rejected once ready
                .waitingFor(Wait.forHttp("/").forPort(CLOUDSERVER_PORT).forStatusCode(403).withStartupTimeout(Duration.ofMinutes(5)));
        created.start();
        // Only the default location constraint is configured. Requests are only accepted with a known host name
        createBuckets(null, "container:" + created.getContainerId(), "http://localhost:8000", "us-east-1");
        return created;
    }

    private static GenericContainer<?> ceph() {
        final GenericContainer<?> created = image("CEPH_IMAGE", "ceph")
                .withCopyToContainer(MountableFile.forClasspathResource("ceph/init.sh", 0755), "/init.sh")
                .withCreateContainerCmdModifier(cmd -> cmd.withUser("root").withEntrypoint("sh", "-c", "sh /init.sh && sleep infinity"))
                .withEnv("S3_ACCESS_KEY", ACCESS_KEY)
                .withEnv("S3_SECRET_KEY", SECRET_KEY)
                .withExposedPorts(CEPH_PORT)
                .waitingFor(Wait.forLogMessage(".*ceph init done.*", 1).withStartupTimeout(Duration.ofMinutes(10)));
        created.start();
        // Location constraint must match the zonegroup. Requests are only accepted with the host name of the gateway
        createBuckets(null, "container:" + created.getContainerId(), "http://localhost:8080", "us-east-1");
        return created;
    }

    /**
     * Create buckets expected by tests
     *
     * @param network     Network of the server or null
     * @param networkMode Network mode of the server container when sharing its network stack or null
     * @param region      Create all buckets in this region or null to use the region in the bucket name
     */
    private static void createBuckets(final Network network, final String networkMode, final String endpoint, final String region) {
        try(GenericContainer<?> mc = image("MC_IMAGE", "mc")
                .withCopyToContainer(MountableFile.forClasspathResource("mc/init.sh", 0755), "/init.sh")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh", "-c", "sh /init.sh && sleep 3600"))
                .withEnv("S3_ENDPOINT", endpoint)
                .withEnv("S3_ACCESS_KEY", ACCESS_KEY)
                .withEnv("S3_SECRET_KEY", SECRET_KEY)
                .withEnv("BUCKET_REGION", StringUtils.defaultString(region))
                .waitingFor(Wait.forLogMessage(".*buckets init done.*", 1).withStartupTimeout(Duration.ofMinutes(2)))) {
            if(null != network) {
                mc.withNetwork(network);
            }
            if(null != networkMode) {
                mc.withNetworkMode(networkMode);
            }
            mc.start();
        }
    }

    public String host() {
        return this.container().getHost();
    }

    public int port() {
        switch(this) {
            case MINIO:
                return this.container().getMappedPort(MINIO_PORT);
            case CEPH:
                return this.container().getMappedPort(CEPH_PORT);
            case RUSTFS:
                return this.container().getMappedPort(RUSTFS_PORT);
            case SEAWEEDFS:
                return this.container().getMappedPort(SEAWEEDFS_PORT);
            case CLOUDSERVER:
                return this.container().getMappedPort(CLOUDSERVER_PORT);
            default:
                return this.container().getMappedPort(LOCALSTACK_PORT);
        }
    }
}
