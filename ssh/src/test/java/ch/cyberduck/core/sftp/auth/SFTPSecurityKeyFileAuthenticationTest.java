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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.threading.DisabledCancelCallback;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Authenticate with a key file for a FIDO/U2F security key without an authentication agent. The file holds the
 * credential handle only, signing is delegated to the middleware of the authenticator.
 */
public class SFTPSecurityKeyFileAuthenticationTest {

    private static final String APPLICATION = "ssh:";
    private static final byte[] HANDLE = "credential-handle".getBytes(StandardCharsets.UTF_8);

    private SshServer server;

    @After
    public void stop() throws Exception {
        if(server != null) {
            server.stop(true);
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
        final SoftwareSecurityKeyMiddleware middleware = new SoftwareSecurityKeyMiddleware(pair, 11L);
        final Local key = SecurityKeyFileWriter.write(pair, APPLICATION, HANDLE, true);
        try {
            this.start(new ByteArrayBuffer(SecurityKeyFileWriter.blob(pair.getPublic(), APPLICATION)).getRawPublicKey());
            final SSHClient client = new SSHClient(new DefaultConfig());
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect("localhost", server.getPort());
            try {
                final Host bookmark = this.bookmark(key);
                assertTrue(new SFTPPublicKeyAuthentication(client) {
                    @Override
                    protected SecurityKeyMiddleware middleware(final Host bookmark) {
                        return middleware;
                    }
                }.authenticate(bookmark, new DisabledLoginCallback(), new DisabledCancelCallback()));
                assertTrue(client.isAuthenticated());
            }
            finally {
                client.disconnect();
            }
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
            }.authenticate(this.bookmark(key), new DisabledLoginCallback(), new DisabledCancelCallback());
            fail("Expected failure for security key without middleware");
        }
        catch(LoginFailureException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        finally {
            this.delete(key);
        }
    }

    private void start(final PublicKey accepted) throws Exception {
        server = SshServer.setUpDefaultServer();
        server.setPort(0);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        final java.util.List<NamedFactory<org.apache.sshd.common.signature.Signature>> signatures
                = new java.util.ArrayList<>(server.getSignatureFactories());
        if(!signatures.contains(BuiltinSignatures.sk_ecdsa_sha2_nistp256)) {
            signatures.add(BuiltinSignatures.sk_ecdsa_sha2_nistp256);
        }
        if(!signatures.contains(BuiltinSignatures.sk_ssh_ed25519)) {
            signatures.add(BuiltinSignatures.sk_ssh_ed25519);
        }
        server.setSignatureFactories(signatures);
        server.setPublickeyAuthenticator((username, key, session) -> KeyUtils.compareKeys(accepted, key));
        server.start();
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
}
