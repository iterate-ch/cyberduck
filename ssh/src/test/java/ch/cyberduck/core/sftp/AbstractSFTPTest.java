package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kerby.kerberos.kerb.client.KrbClient;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;
import org.apache.kerby.util.NetworkUtil;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.util.OsUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.gss.GSSAuthenticator;
import org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.StaticPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpFileSystemAccessor;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.apache.sshd.sftp.server.SftpSubsystemProxy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.PublicKey;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.UUID;

import static org.junit.Assert.fail;

public class AbstractSFTPTest {

    protected static final String KERBEROS_REALM = "EXAMPLE.COM";
    protected static final String KERBEROS_PRINCIPAL = String.format("test@%s", KERBEROS_REALM);
    private static final String KERBEROS_SERVICE_PRINCIPAL = String.format("host/localhost@%s", KERBEROS_REALM);

    private static File kerberos;
    private static SimpleKdcServer kdc;

    protected SshServer sshServer;

    protected SFTPSession session;

    @Parameterized.Parameters(name = "vaultVersion = {0}")
    public static Object[] data() {
        return new Object[]{VaultVersion.Type.V8, VaultVersion.Type.UVF};
    }

    @Parameterized.Parameter
    public VaultVersion.Type vaultVersion;

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
        final File ticketCache = new File(kerberos, "krb5cc");
        client.storeTicket(client.requestTgt(KERBEROS_PRINCIPAL, password), ticketCache);
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

    @Before
    public void start() throws Exception {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(2202);
        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                if(!StringUtils.equals("test", username)) {
                    return false;
                }
                if(!StringUtils.equals("test", password)) {
                    return false;
                }
                return true;
            }
        });
        sshServer.setKeyboardInteractiveAuthenticator(new DefaultKeyboardInteractiveAuthenticator());
        sshServer.setPublickeyAuthenticator(new StaticPublickeyAuthenticator(true) {
            @Override
            protected void handleAcceptance(final String username, final PublicKey key, final ServerSession session) {
                super.handleAcceptance(username, key, session);
            }

            @Override
            protected void handleRejection(final String username, final PublicKey key, final ServerSession session) {
                super.handleRejection(username, key, session);
            }
        });
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
        final SftpSubsystemFactory factory = new SftpSubsystemFactory();
        factory.setFileSystemAccessor(new SftpFileSystemAccessor() {
            @Override
            public NavigableMap<String, Object> resolveReportedFileAttributes(final SftpSubsystemProxy subsystem, final Path file, final int flags, final NavigableMap<String, Object> attrs, final LinkOption... options) throws IOException {
                final NavigableMap<String, Object> reported = SftpFileSystemAccessor.super.resolveReportedFileAttributes(subsystem, file, flags, attrs, options);
                if(OsUtils.isWin32() && file.toFile().isDirectory()) {
                    reported.put("Permissions", EnumSet.allOf(PosixFilePermission.class));
                }
                return reported;
            }
        });
        sshServer.setSubsystemFactories(Collections.singletonList(factory));
        final Local directory = LocalFactory.get(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        directory.mkdir();
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(Paths.get(directory.getAbsolute())));
        sshServer.start();
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

    @Before
    public void setup() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new SFTPProtocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/SFTP.cyberduckprofile"));
        final Host host = new Host(profile, "localhost", 2202, new Credentials("test", "test")) {
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
                return super.getProperty(key);
            }
        };
        session = new SFTPSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                fail(reason);
                return null;
            }
        },
                HostKeyCallback.noop,
                new DisabledPasswordStore(),
                ProgressListener.noop).connect(session, CancelCallback.noop);
    }
}
