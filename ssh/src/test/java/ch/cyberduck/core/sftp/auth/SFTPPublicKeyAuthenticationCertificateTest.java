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
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.sftp.SFTPTestServer;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Public key authentication with an OpenSSH RSA user certificate. The certificate in the test resources is signed by
 * a throwaway CA trusted by the servers. ProFTPD is skipped as <code>mod_sftp</code> has no support for user
 * certificates.
 */
public class SFTPPublicKeyAuthenticationCertificateTest extends AbstractSFTPTest {

    private static final int SSH_PORT = 2222;

    private static final List<String> FILES = Arrays.asList("id_rsa", "id_rsa-cert.pub");

    /**
     * OpenSSH without support for RSA certificates with SHA-2 signatures. Started on first use.
     */
    private static GenericContainer<?> legacy;

    private static synchronized GenericContainer<?> legacy() {
        if(null == legacy) {
            legacy = SFTPTestServer.openssh()
                    .withCopyToContainer(MountableFile.forClasspathResource("certificate/sshd-legacy.conf", 0644), "/config/sshd/sshd_config.d/legacy.conf");
            legacy.start();
        }
        return legacy;
    }

    @AfterClass
    public static synchronized void stop() {
        if(null != legacy) {
            legacy.stop();
            legacy = null;
        }
    }

    /**
     * Separate connection as no further authentication is possible on the session of the parent
     */
    private SFTPSession certificate;
    private Path directory;

    @After
    public void close() throws Exception {
        try {
            if(null != certificate) {
                certificate.close();
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
     * Servers only accept RSA certificates with SHA-2 signatures by default, as Azure Blob Storage SFTP does. Fails
     * when the client only offers the certificate with the legacy ssh-rsa-cert-v01@openssh.com algorithm or signs
     * with SHA-1 while advertising a SHA-2 algorithm.
     */
    @Test
    public void testAuthenticateRsaCertificate() throws Exception {
        assumeTrue("No support for user certificates", server != SFTPTestServer.PROFTPD);
        assertTrue(this.authenticate(server.host(), server.port()));
    }

    /**
     * The client must fall back to the legacy ssh-rsa-cert-v01@openssh.com algorithm after the SHA-2 variants
     * are rejected.
     */
    @Test
    public void testAuthenticateRsaCertificateLegacy() throws Exception {
        assumeTrue("Legacy algorithm only configured for OpenSSH", server == SFTPTestServer.OPENSSH);
        assertTrue(this.authenticate(legacy().getHost(), legacy().getMappedPort(SSH_PORT)));
    }

    private boolean authenticate(final String hostname, final int port) throws Exception {
        directory = Files.createTempDirectory("certificate");
        for(String name : FILES) {
            try(InputStream in = this.getClass().getResourceAsStream(String.format("/certificate/%s", name))) {
                Files.copy(in, directory.resolve(name));
            }
        }
        final Credentials credentials = new Credentials(SFTPTestServer.USER);
        credentials.setIdentity(new Local(directory.toString(), "id_rsa"));
        final Host host = new Host(new SFTPProtocol(), hostname, port, credentials);
        certificate = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        certificate.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        return new SFTPPublicKeyAuthentication(certificate.getClient()).authenticate(host, new DisabledLoginCallback(), CancelCallback.noop);
    }
}
