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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.sftp.AbstractSFTPTest;
import ch.cyberduck.core.sftp.SFTPTestServer;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.DisabledCancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.testcontainers.images.builder.Transferable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

import net.schmizz.sshj.SSHClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Authenticate with a key file for a FIDO/U2F security key without an authentication agent. The file holds the
 * credential handle only, signing is delegated to the middleware of the authenticator.
 */
public class SFTPSecurityKeyFileAuthenticationTest extends AbstractSFTPTest {

    private static final String APPLICATION = "ssh:";
    private static final byte[] HANDLE = "credential-handle".getBytes(StandardCharsets.UTF_8);

    private static final String AUTHORIZED_KEYS = "/config/.ssh/authorized_keys";

    @After
    public void revoke() throws Exception {
        if(server == SFTPTestServer.OPENSSH) {
            server.container().execInContainer("rm", "-f", AUTHORIZED_KEYS);
        }
    }

    /**
     * Sign with the authenticator and authenticate against a server verifying the signature, the setup with a resident
     * credential downloaded with <code>ssh-keygen -K</code> and no agent running.
     */
    @Test
    public void testAuthenticateWithMiddleware() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        this.authenticate(generator.generateKeyPair());
    }

    /**
     * Authenticator with an Ed25519 credential returns the entire signature in place of the integer r
     */
    @Test
    public void testAuthenticateWithMiddlewareEd25519() throws Exception {
        this.authenticate(KeyPairGenerator.getInstance("Ed25519").generateKeyPair());
    }

    private void authenticate(final KeyPair pair) throws Exception {
        assumeTrue("Authorized keys installed in OpenSSH only", server == SFTPTestServer.OPENSSH);
        final SoftwareSecurityKeyMiddleware middleware = new SoftwareSecurityKeyMiddleware(pair, 11L);
        final Local key = SecurityKeyFileWriter.write(pair, APPLICATION, HANDLE, true);
        try {
            this.authorize(pair);
            session.getHost().getCredentials().setIdentity(key);
            // Reconnect without authentication
            session.disconnect();
            session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
            assertTrue(new SFTPPublicKeyAuthentication(session.getClient()) {
                @Override
                protected SecurityKeyMiddleware middleware(final Host bookmark) {
                    return middleware;
                }
            }.authenticate(session.getHost(), new AwaitingLoginCallback(), new DisabledCancelCallback()));
            assertTrue(session.getClient().isAuthenticated());
            assertEquals("Authenticator must have signed once", 1, middleware.getAttempts());
            assertEquals("Credential handle from key file must be sent to the authenticator",
                    new String(HANDLE, StandardCharsets.UTF_8), new String(middleware.getKeyHandle(), StandardCharsets.UTF_8));
            assertEquals("User presence must be requested", (byte) 0x01, middleware.getFlags());
        }
        finally {
            this.delete(key);
        }
    }

    /**
     * Accept the security key for the test user in the container
     */
    private void authorize(final KeyPair pair) throws Exception {
        final String line = String.format("%s %s test@localhost%n", SecurityKeyFileWriter.type(pair),
                Base64.getEncoder().encodeToString(SecurityKeyFileWriter.blob(pair.getPublic(), APPLICATION)));
        server.container().execInContainer("mkdir", "-p", "/config/.ssh");
        server.container().copyFileToContainer(Transferable.of(line.getBytes(StandardCharsets.US_ASCII), 0644), AUTHORIZED_KEYS);
    }

    /**
     * Without a middleware library for the authenticator there is nothing to sign with. The key file holds no private
     * key, which must be reported with a hint instead of failing inside sshj when the signature is requested.
     */
    @Test
    public void testFailureMissingMiddlewareWithCompanionPublicKey() throws Exception {
        this.failure(true);
    }

    /**
     * Without a companion <code>.pub</code> file the key type is read from the private key section, which must parse
     * as a security key and not as an ordinary Ed25519 key.
     */
    @Test
    public void testFailureMissingMiddlewareWithoutCompanionPublicKey() throws Exception {
        this.failure(false);
    }

    /**
     * Without a middleware library configured for the platform there is nothing to sign with
     */
    @Test
    public void testFailureNoProviderConfigured() throws Exception {
        final KeyPair pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        final Local key = SecurityKeyFileWriter.write(pair, APPLICATION, HANDLE, true);
        try {
            new SFTPPublicKeyAuthentication(new SSHClient()) {
                @Override
                protected String provider(final Host bookmark) {
                    return StringUtils.EMPTY;
                }
            }.authenticate(this.bookmark(key), new AwaitingLoginCallback(), new DisabledCancelCallback());
            fail("Expected failure for security key without middleware");
        }
        catch(LoginFailureException e) {
            assertTrue(e.getDetail(false), e.getDetail(false).contains("No security key provider configured"));
            assertTrue(e.getCause() instanceof IOException);
        }
        finally {
            this.delete(key);
        }
    }

    private void failure(final boolean companion) throws Exception {
        final KeyPair pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        final Local key = SecurityKeyFileWriter.write(pair, APPLICATION, HANDLE, companion);
        try {
            // Not connected, failure must be reported before any authentication attempt
            new SFTPPublicKeyAuthentication(new SSHClient()) {
                @Override
                protected String provider(final Host bookmark) {
                    return "/nonexistent/libsk.dylib";
                }
            }.authenticate(this.bookmark(key), new AwaitingLoginCallback(), new DisabledCancelCallback());
            fail("Expected failure for security key without middleware");
        }
        catch(LoginFailureException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        finally {
            this.delete(key);
        }
    }

    private Host bookmark(final Local key) {
        return new Host(new TestProtocol(), "localhost",
                new Credentials()
                        .setUsername("test")
                        .setIdentity(key));
    }

    private void delete(final Local key) throws Exception {
        key.delete();
        final Local pub = LocalFactory.get(String.format("%s.pub", key.getAbsolute()));
        if(pub.exists()) {
            pub.delete();
        }
    }

    /**
     * Wait for the signature in the background instead of displaying a prompt to touch the security key
     */
    private static final class AwaitingLoginCallback extends DisabledLoginCallback {
        @Override
        public void await(final CountDownLatch signal, final Host bookmark, final String title, final String message)
                throws ConnectionCanceledException {
            try {
                signal.await();
            }
            catch(InterruptedException e) {
                throw new ConnectionCanceledException(e);
            }
        }
    }
}
