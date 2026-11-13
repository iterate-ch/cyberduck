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
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kerby.kerberos.kerb.client.KrbClient;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;
import org.apache.kerby.util.NetworkUtil;
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
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPGssApiAuthenticationTest {

    private static final String KERBEROS_REALM = "EXAMPLE.COM";
    private static final String KERBEROS_PRINCIPAL = String.format("test@%s", KERBEROS_REALM);
    private static final String KERBEROS_SERVICE_PRINCIPAL = String.format("host/localhost@%s", KERBEROS_REALM);

    private static File kerberos;
    private static SimpleKdcServer kdc;

    private SshServer sshServer;
    private SFTPSession session;

    /**
     * Kerberos KDC issuing a ticket for the test user saved to a credentials cache for GSS-API authentication
     */
    @BeforeClass
    public static void startKdc() throws Exception {
        kerberos = Files.createTempDirectory("kdc").toFile();
        final int port = NetworkUtil.getServerPort();
        kdc = new SimpleKdcServer();
        kdc.setWorkDir(kerberos);
        kdc.setKdcHost("localhost");
        kdc.setKdcRealm(KERBEROS_REALM);
        kdc.setAllowUdp(false);
        kdc.setAllowTcp(true);
        kdc.setKdcTcpPort(port);
        kdc.init();
        kdc.start();
        final String password = UUID.randomUUID().toString();
        kdc.createPrincipal(KERBEROS_PRINCIPAL, password);
        kdc.createAndExportPrincipals(new File(kerberos, "sshd.keytab"), KERBEROS_SERVICE_PRINCIPAL);
        // Configuration shared by the client and the server running in this JVM
        final File krb5conf = new File(kerberos, "client-krb5.conf");
        try(PrintWriter w = new PrintWriter(krb5conf)) {
            w.println("[libdefaults]");
            w.println("    default_realm = " + KERBEROS_REALM);
            w.println("    udp_preference_limit = 1");
            w.println("    dns_lookup_kdc = false");
            w.println("    dns_lookup_realm = false");
            // Service ticket must be requested for host/localhost matching the keytab
            w.println("    dns_canonicalize_hostname = false");
            w.println("    rdns = false");
            w.println("[realms]");
            w.println("    " + KERBEROS_REALM + " = {");
            w.println("        kdc = localhost:" + port);
            w.println("    }");
            w.println("[domain_realm]");
            w.println("    localhost = " + KERBEROS_REALM);
        }
        // Equivalent to kinit
        final KrbClient client = kdc.getKrbClient();
        client.storeTicket(client.requestTgt(KERBEROS_PRINCIPAL, password), new File(kerberos, "krb5cc"));
    }

    @AfterClass
    public static void stopKdc() throws Exception {
        try {
            kdc.stop();
        }
        finally {
            FileUtils.deleteQuietly(kerberos);
        }
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
                return KERBEROS_PRINCIPAL.equals(identity);
            }
        };
        gss.setKeytabFile(new File(kerberos, "sshd.keytab").getAbsolutePath());
        gss.setServicePrincipalName(KERBEROS_SERVICE_PRINCIPAL);
        sshServer.setGSSAuthenticator(gss);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(Files.createTempDirectory("sftp")));
        sshServer.start();
        final Host host = new Host(new SFTPProtocol(), "localhost", sshServer.getPort(), new Credentials("test", "test")) {
            @Override
            public String getProperty(final String key) {
                // Allow tests to override with custom property
                final String value = super.getProperty(key);
                if(value != null) {
                    return value;
                }
                if("java.security.krb5.conf".equals(key)) {
                    return new File(kerberos, "client-krb5.conf").getAbsolutePath();
                }
                if("ssh.authentication.gssapi.ticketcache".equals(key)) {
                    return new File(kerberos, "krb5cc").getAbsolutePath();
                }
                return null;
            }
        };
        session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
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

    @After
    public void clearRealm() {
        System.clearProperty("java.security.krb5.realm");
        System.clearProperty("java.security.krb5.kdc");
    }

    @Test
    public void testGetMethod() {
        assertEquals("gssapi-with-mic", new SFTPGssApiAuthentication(null).getMethod());
    }

    @Test
    public void testRealmAndKdc() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.nonexistent.invalid", new Credentials("user", "")) {
            @Override
            public String getProperty(final String key) {
                if("ssh.authentication.gssapi.ticketcache".equals(key)) {
                    return new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()).getAbsolutePath();
                }
                return super.getProperty(key);
            }
        };
        host.setProperty("java.security.krb5.realm", "NONEXISTENT.INVALID");
        host.setProperty("java.security.krb5.kdc", "kdc.nonexistent.invalid");
        assertFalse(new SFTPGssApiAuthentication(null).authenticate(host, LoginCallback.noop, CancelCallback.noop));
        assertEquals("NONEXISTENT.INVALID", System.getProperty("java.security.krb5.realm"));
        assertEquals("kdc.nonexistent.invalid", System.getProperty("java.security.krb5.kdc"));
    }

    @Test
    public void testRealmWithoutKdc() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.nonexistent.invalid", new Credentials("user", "")) {
            @Override
            public String getProperty(final String key) {
                if("ssh.authentication.gssapi.ticketcache".equals(key)) {
                    return new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()).getAbsolutePath();
                }
                return super.getProperty(key);
            }
        };
        host.setProperty("java.security.krb5.realm", "NONEXISTENT.INVALID");
        assertFalse(new SFTPGssApiAuthentication(null).authenticate(host, LoginCallback.noop, CancelCallback.noop));
        // JDK refuses configuration with only one of both set
        assertNull(System.getProperty("java.security.krb5.realm"));
        assertNull(System.getProperty("java.security.krb5.kdc"));
    }

    @Test
    public void testAuthenticate() throws Exception {
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
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
