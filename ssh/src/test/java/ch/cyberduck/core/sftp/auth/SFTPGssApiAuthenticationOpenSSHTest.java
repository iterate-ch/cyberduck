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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.sftp.SFTPListService;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.sftp.SFTPTestServer;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * GSS-API authentication with OpenSSH built with MIT Kerberos. The server only needs the keytab with the service
 * principal and does not connect to the KDC running in this JVM.
 */
@Category(TestcontainerTest.class)
public class SFTPGssApiAuthenticationOpenSSHTest {

    private static KerberosTestKdc kdc;
    private static GenericContainer<?> container;

    private SFTPSession session;

    @BeforeClass
    public static void start() throws Exception {
        kdc = new KerberosTestKdc();
        container = SFTPTestServer.opensshKerberos();
        container.withLogConsumer(f -> System.out.print("OPENSSH_KRB5 " + f.getUtf8String()));
        container.start();
        // Service principal for the hostname connected to
        final File keytab = kdc.keytab(container.getHost());
        container.copyFileToContainer(MountableFile.forHostPath(keytab.getAbsolutePath(), 0600), "/config/sshd/krb5.keytab");
        // Read by sshd running as the test user
        container.execInContainer("chown", "1000:1000", "/config/sshd/krb5.keytab");
    }

    @AfterClass
    public static void stop() throws Exception {
        try {
            if(null != container) {
                container.stop();
            }
        }
        finally {
            if(null != kdc) {
                kdc.close();
            }
        }
    }

    @Before
    public void session() {
        final Host host = new Host(new SFTPProtocol(), container.getHost(), container.getMappedPort(2222),
                new Credentials(SFTPTestServer.USER)) {
            @Override
            public String getProperty(final String key) {
                // Allow tests to override with custom property
                final String value = super.getProperty(key);
                if(value != null) {
                    return value;
                }
                if("ssh.authentication.gssapi.ticketcache".equals(key)) {
                    return kdc.ticketCache().getAbsolutePath();
                }
                return null;
            }
        };
        session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    @After
    public void close() throws Exception {
        session.close();
    }

    @Test
    public void testAuthenticate() throws Exception {
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        // Realm of server not in Kerberos configuration. Fallback to realm of client principal
        assertTrue(new SFTPGssApiAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback(), CancelCallback.noop));
        assertTrue(session.getClient().isAuthenticated());
    }

    @Test
    public void testAuthenticateRealm() throws Exception {
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        session.getHost().setProperty(SFTPGssApiAuthentication.KERBEROS_REALM_PROPERTY, KerberosTestKdc.REALM);
        assertTrue(new SFTPGssApiAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback(), CancelCallback.noop));
        assertTrue(session.getClient().isAuthenticated());
    }

    @Test
    public void testAuthenticateNoTicket() throws Exception {
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        session.getHost().setProperty("ssh.authentication.gssapi.ticketcache",
                new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()).getAbsolutePath());
        // Fall through to next authentication method
        assertFalse(new SFTPGssApiAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback(), CancelCallback.noop));
        assertFalse(session.getClient().isAuthenticated());
    }

    /**
     * Login without password or key with ticket only and list home directory
     */
    @Test
    public void testLogin() throws Exception {
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        }, HostKeyCallback.noop, new DisabledPasswordStore(), ProgressListener.noop).connect(session, CancelCallback.noop);
        assertTrue(session.getClient().isAuthenticated());
        assertNotNull(new SFTPListService(session).list(new Path("/config", EnumSet.of(AbstractPath.Type.directory)),
                new DisabledListProgressListener()));
    }
}
