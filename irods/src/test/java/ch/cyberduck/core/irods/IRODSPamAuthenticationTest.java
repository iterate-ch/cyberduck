package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.test.TestcontainerTest;

import org.irods.irods4j.authentication.NativeAuthPlugin;
import org.irods.irods4j.high_level.administration.IRODSUsers;
import org.irods.irods4j.high_level.administration.IRODSZones;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class IRODSPamAuthenticationTest {

    private static final X509TrustManager cyberduckTrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public X509TrustManager init() throws IOException {
            return null;
        }

        @Override
        public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
        }
    };

    private static final TrustManager[] irodsTrustManagers = new TrustManager[]{new javax.net.ssl.X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }};

    private static final ComposeContainer container = new ComposeContainer(
            new File(IRODSDockerComposeManager.class.getResource("/docker/docker-compose.pam.yml").getFile()))
            .withPull(false)
            .withExposedService("irods-catalog-provider-1", 1447,
                    Wait.forLogMessage(".*\"log_message\":\"Initializing delay server.\".*", 1)
                            .withStartupTimeout(Duration.ofMinutes(5)));

    @BeforeClass
    public static void start() {
        container.start();
    }

    @AfterClass
    public static void shutdown() {
        container.stop();
    }

    @Test
    public void testPamPasswordsContainingSpecialCharactersAreHandledCorrectly() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new IRODSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/iRODS_pam.cyberduckprofile"));

        final String username = "john";
        final String password = "=i;r@o\\d&s";
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(username, password));
        final IRODSUsers.User testUser = new IRODSUsers.User(username, Optional.of(host.getRegion()));

        IRODSApi.ConnectionOptions options = new IRODSApi.ConnectionOptions();
        options.clientServerNegotiation = "CS_NEG_REQUIRE";
        options.trustManagers = irodsTrustManagers;

        try(IRODSConnection conn = new IRODSConnection(options)) {
            // Create a test user named john. We do not set a password for this user because
            // they are using PAM authentication.
            conn.connect(host.getHostname(), host.getPort(), new QualifiedUsername("rods", host.getRegion()));
            conn.authenticate(new NativeAuthPlugin(), "rods");
            IRODSUsers.addUser(conn.getRcComm(), testUser, IRODSUsers.UserType.RODSUSER, IRODSZones.ZoneType.LOCAL);

            try {
                final IRODSSession session = new IRODSSession(host, cyberduckTrustManager, null);

                assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
                assertTrue(session.isConnected());
                assertNotNull(session.getClient());
                session.login(new DisabledLoginCallback(), new DisabledCancelCallback());

                session.close();
                assertFalse(session.isConnected());
            }
            finally {
                IRODSUsers.removeUser(conn.getRcComm(), testUser);
            }
        }
    }

    @Test
    public void testIncorrectPamPasswordFails() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new IRODSProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/iRODS_pam.cyberduckprofile"));

        final String username = "john";
        final String password = "incorrect";
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials(username, password));
        final IRODSUsers.User testUser = new IRODSUsers.User(username, Optional.of(host.getRegion()));

        IRODSApi.ConnectionOptions options = new IRODSApi.ConnectionOptions();
        options.clientServerNegotiation = "CS_NEG_REQUIRE";
        options.trustManagers = irodsTrustManagers;

        try(IRODSConnection conn = new IRODSConnection(options)) {
            // Create a test user named john. We do not set a password for this user because
            // they are using PAM authentication.
            conn.connect(host.getHostname(), host.getPort(), new QualifiedUsername("rods", host.getRegion()));
            conn.authenticate(new NativeAuthPlugin(), "rods");
            IRODSUsers.addUser(conn.getRcComm(), testUser, IRODSUsers.UserType.RODSUSER, IRODSZones.ZoneType.LOCAL);

            try {
                final IRODSSession session = new IRODSSession(host, cyberduckTrustManager, null);

                assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
                assertTrue(session.isConnected());
                assertNotNull(session.getClient());
                assertThrows(BackgroundException.class, () -> session.login(new DisabledLoginCallback(), new DisabledCancelCallback()));

                session.close();
                assertFalse(session.isConnected());
            }
            finally {
                IRODSUsers.removeUser(conn.getRcComm(), testUser);
            }
        }
    }
}
