package ch.cyberduck.core.sftp.auth;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Public key authentication with an OpenSSH RSA user certificate against OpenSSH. The certificate in the test
 * resources is signed by a throwaway CA trusted by the server with <code>TrustedUserCAKeys</code>.
 */
@Category(IntegrationTest.class)
public class SFTPPublicKeyAuthenticationCertificateTest {

    private static final String USER = "test";
    private static final int SSH_PORT = 2222;

    private static final List<String> FILES = Arrays.asList("id_rsa", "id_rsa-cert.pub");

    /**
     * Server accepting RSA certificates with SHA-2 signatures only, as Azure Blob Storage SFTP does
     */
    private static final GenericContainer<?> sha2 = container("sshd-sha2.conf");
    /**
     * Server without support for RSA certificates with SHA-2 signatures
     */
    private static final GenericContainer<?> legacy = container("sshd-legacy.conf");

    private static GenericContainer<?> container(final String config) {
        return new GenericContainer<>(DockerImageName.parse("linuxserver/openssh-server:latest"))
                .withEnv("PUID", "1000")
                .withEnv("PGID", "1000")
                .withEnv("USER_NAME", USER)
                .withCopyToContainer(MountableFile.forClasspathResource("certificate/ca.pub", 0644), "/config/sshd/ca.pub")
                .withCopyToContainer(MountableFile.forClasspathResource(String.format("certificate/%s", config), 0644), "/config/sshd/sshd_config.d/certificate.conf")
                .withExposedPorts(SSH_PORT)
                .waitingFor(Wait.forLogMessage(".*\\[ls\\.io-init\\] done.*", 1).withStartupTimeout(Duration.ofMinutes(2)));
    }

    @BeforeClass
    public static void start() {
        sha2.start();
        legacy.start();
    }

    @AfterClass
    public static void stop() {
        try {
            sha2.stop();
        }
        finally {
            legacy.stop();
        }
    }

    private SFTPSession session;
    private Path directory;

    @After
    public void disconnect() throws Exception {
        try {
            if(null != session) {
                session.close();
            }
        }
        finally {
            if(null != directory) {
                for(String name : FILES) {
                    Files.deleteIfExists(directory.resolve(name));
                }
                Files.deleteIfExists(directory);
            }
        }
    }

    /**
     * Fails when the client only offers the certificate with the legacy ssh-rsa-cert-v01@openssh.com algorithm
     * or signs with SHA-1 while advertising a SHA-2 algorithm.
     */
    @Test
    public void testAuthenticateRsaCertificateSha2() throws Exception {
        assertTrue(this.authenticate(sha2));
    }

    /**
     * The client must fall back to the legacy ssh-rsa-cert-v01@openssh.com algorithm after the SHA-2 variants
     * are rejected.
     */
    @Test
    public void testAuthenticateRsaCertificateLegacy() throws Exception {
        assertTrue(this.authenticate(legacy));
    }

    private boolean authenticate(final GenericContainer<?> container) throws Exception {
        directory = Files.createTempDirectory("certificate");
        for(String name : FILES) {
            try(InputStream in = this.getClass().getResourceAsStream(String.format("/certificate/%s", name))) {
                Files.copy(in, directory.resolve(name));
            }
        }
        final Credentials credentials = new Credentials(USER);
        credentials.setIdentity(new Local(directory.toString(), "id_rsa"));
        final Host host = new Host(new SFTPProtocol(), container.getHost(), container.getMappedPort(SSH_PORT), credentials);
        session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        return new SFTPPublicKeyAuthentication(session.getClient()).authenticate(host, new DisabledLoginCallback(), CancelCallback.noop);
    }
}
