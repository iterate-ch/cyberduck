package ch.cyberduck.core.sftp.auth;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.gss.GSSAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPGssApiAuthenticationTest {

    private static KerberosTestKdc kdc;
    private static File keytab;

    private SshServer sshServer;
    private SFTPSession session;

    /**
     * Kerberos KDC issuing a ticket for the test user saved to a credentials cache for GSS-API authentication
     */
    @BeforeClass
    public static void startKdc() throws Exception {
        kdc = new KerberosTestKdc();
        keytab = kdc.keytab("localhost");
    }

    @AfterClass
    public static void stopKdc() throws Exception {
        kdc.close();
    }

    /**
     * Embedded SSH server accepting GSS-API authentication using the keytab of the KDC. Not running in a container
     * because the server must share the Kerberos configuration with the KDC running in this JVM.
     */
    @Before
    public void start() throws Exception {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(0);
        sshServer.setPasswordAuthenticator((username, password, s) ->
                StringUtils.equals("test", username) && StringUtils.equals("test", password));
        sshServer.setPublickeyAuthenticator((username, key, s) -> true);
        final GSSAuthenticator gss = new GSSAuthenticator() {
            @Override
            public boolean validateIdentity(final ServerSession session, final String identity) {
                // Username is not yet set on the session while authentication is in progress
                return KerberosTestKdc.PRINCIPAL.equals(identity);
            }
        };
        gss.setKeytabFile(keytab.getAbsolutePath());
        gss.setServicePrincipalName(KerberosTestKdc.service("localhost"));
        sshServer.setGSSAuthenticator(gss);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(Files.createTempDirectory("sftp")));
        sshServer.start();
        session = new SFTPSession(this.host(), new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    private Host host() {
        return new Host(new SFTPProtocol(), "localhost", sshServer.getPort(), new Credentials("test", "test")) {
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
    }

    @After
    public void stop() throws Exception {
        try {
            session.close();
        }
        finally {
            sshServer.stop();
        }
    }

    @Test
    public void testGetMethod() {
        assertEquals("gssapi-with-mic", new SFTPGssApiAuthentication(null).getMethod());
    }

    @Test
    public void testAuthenticate() throws Exception {
        // Realm of server not set and not in Kerberos configuration. Fallback to realm of client principal
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        assertTrue(new SFTPGssApiAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback(), CancelCallback.noop));
        assertTrue(session.getClient().isAuthenticated());
    }

    @Test
    public void testAuthenticateRealm() throws Exception {
        final String config = System.getProperty("java.security.krb5.conf");
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        session.getHost().setProperty(SFTPGssApiAuthentication.KERBEROS_REALM_PROPERTY, KerberosTestKdc.REALM);
        assertTrue(new SFTPGssApiAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback(), CancelCallback.noop));
        assertTrue(session.getClient().isAuthenticated());
        // No JVM-wide configuration changed
        assertEquals(config, System.getProperty("java.security.krb5.conf"));
        assertNull(System.getProperty("java.security.krb5.realm"));
        assertNull(System.getProperty("java.security.krb5.kdc"));
    }

    @Test
    public void testAuthenticateUnknownRealm() throws Exception {
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        session.getHost().setProperty(SFTPGssApiAuthentication.KERBEROS_REALM_PROPERTY, "NONEXISTENT.INVALID");
        // No KDC found for realm of server
        try {
            assertFalse(new SFTPGssApiAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback(), CancelCallback.noop));
        }
        catch(BackgroundException e) {
            // Expected
        }
        assertFalse(session.getClient().isAuthenticated());
    }

    /**
     * Parallel logins with and without realm set per bookmark must not interfere
     */
    @Test
    public void testAuthenticateConcurrently() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            final List<Future<Boolean>> results = new ArrayList<>();
            for(int i = 0; i < 16; i++) {
                final boolean realm = i % 2 == 0;
                results.add(executor.submit(() -> {
                    final Host host = this.host();
                    if(realm) {
                        host.setProperty(SFTPGssApiAuthentication.KERBEROS_REALM_PROPERTY, KerberosTestKdc.REALM);
                    }
                    final SFTPSession session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
                    try {
                        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
                        return new SFTPGssApiAuthentication(session.getClient()).authenticate(host, new DisabledLoginCallback(), CancelCallback.noop)
                                && session.getClient().isAuthenticated();
                    }
                    finally {
                        session.close();
                    }
                }));
            }
            for(Future<Boolean> result : results) {
                assertTrue(result.get());
            }
        }
        finally {
            executor.shutdownNow();
        }
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

    @Test
    public void testLoginGssApi() throws Exception {
        CoreModuleProperties.AUTH_METHODS.set(sshServer, "gssapi-with-mic");
        new LoginConnectionService(new DisabledLoginCallback(), HostKeyCallback.noop,
                new DisabledPasswordStore(), ProgressListener.noop).connect(session, CancelCallback.noop);
        assertTrue(session.getClient().isAuthenticated());
    }

    @Test
    public void testLoginGssApiDisabled() throws Exception {
        CoreModuleProperties.AUTH_METHODS.set(sshServer, "gssapi-with-mic");
        session.getHost().setProperty("ssh.authentication.gssapi.enable", String.valueOf(false));
        assertThrows(BackgroundException.class, () -> new LoginConnectionService(new DisabledLoginCallback(), HostKeyCallback.noop,
                new DisabledPasswordStore(), ProgressListener.noop).connect(session, CancelCallback.noop));
    }

    @Test
    public void testLoginPublicKeyAndGssApi() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair pair = generator.generateKeyPair();
        final File key = File.createTempFile("id_rsa", null);
        try {
            try(OutputStream out = Files.newOutputStream(key.toPath())) {
                OpenSSHKeyPairResourceWriter.INSTANCE.writePrivateKey(pair, "test", null, out);
            }
            // Both methods required as with AuthenticationMethods in sshd_config
            CoreModuleProperties.AUTH_METHODS.set(sshServer, "publickey,gssapi-with-mic");
            session.close();
            session.getHost().getCredentials().setIdentity(new Local(key.getAbsolutePath()));
            new LoginConnectionService(new DisabledLoginCallback(), HostKeyCallback.noop,
                    new DisabledPasswordStore(), ProgressListener.noop).connect(session, CancelCallback.noop);
            assertTrue(session.getClient().isAuthenticated());
        }
        finally {
            key.delete();
        }
    }
}
