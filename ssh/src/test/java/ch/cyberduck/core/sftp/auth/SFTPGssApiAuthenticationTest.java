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
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.core.CoreModuleProperties;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPGssApiAuthenticationTest extends AbstractSFTPTest {

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
        session.disconnect();
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
        assertTrue(new SFTPGssApiAuthentication(session.getClient()).authenticate(session.getHost(), new DisabledLoginCallback(), CancelCallback.noop));
        assertTrue(session.getClient().isAuthenticated());
    }

    @Test
    public void testAuthenticateNoTicket() throws Exception {
        session.disconnect();
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
