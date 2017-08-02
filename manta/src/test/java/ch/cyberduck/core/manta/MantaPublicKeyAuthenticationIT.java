package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class MantaPublicKeyAuthenticationIT {

    @Test
    public void testAuthenticateOpenSSHKeyWithoutIdentity() throws Exception {
        try {
            final Credentials credentials = new Credentials(System.getProperty("manta.user"), "");
            // not setting identity file, should fail since Manta REQUIRES keys
            final Host host = new Host(new MantaProtocol(), "test.cyberduck.ch", credentials);
            final MantaSession session = new MantaSession(host);
            session.open(new DisabledHostKeyCallback());
            new MantaPublicKeyAuthentication(session, new DisabledPasswordStore())
                    .authenticate(host, new DisabledLoginCallback(), new DisabledCancelCallback());
            assertEquals(session.getFingerprint(), System.getProperty("manta.key_id"));
            session.close();
        }
        catch(LoginFailureException e) {
            assertTrue(e.getMessage().contains("Login failed"));
            assertTrue(e.getCause().getMessage().contains("Private Key Authentication is required"));
        }

    }

    @Test
    public void testAuthenticateOpenSSHKeyWithoutPassphrase() throws Exception {
        final Credentials credentials = new Credentials(System.getProperty("manta.user"), "");
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        credentials.setIdentity(key);

        try {
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(
                    new FileReader(System.getProperty("manta.key_path")),
                    key.getOutputStream(false),
                    StandardCharsets.UTF_8
            );
            final Host host = new Host(new MantaProtocol(), "test.cyberduck.ch", credentials);
            final MantaSession session = new MantaSession(host);
            session.open(new DisabledHostKeyCallback());
            assertTrue(
                    new MantaPublicKeyAuthentication(session, new DisabledPasswordStore())
                            .authenticate(host, new DisabledLoginCallback(), new DisabledCancelCallback()));
            assertEquals(session.getFingerprint(), System.getProperty("manta.key_id"));
            session.close();
        }
        finally {
            key.delete();
        }
    }

    @Test
    public void testAuthenticateOpenSSHKeyWithPassphrase() throws Exception {
        final Credentials credentials = new Credentials(System.getProperty("manta.user"), "");
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        credentials.setIdentity(key);
        final String passphrasedKeyPath = System.getProperty("manta.passphrase.key_path");
        if(passphrasedKeyPath == null) {
            Assert.fail("No passphrase key configured, please set 'manta.passphrase.key_path' and 'manta.passphrase.key_passphrase' to a key path and passphrase respectively");
        }

        try {
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(
                    new FileReader(passphrasedKeyPath),
                    key.getOutputStream(false),
                    StandardCharsets.UTF_8
            );
            final Host host = new Host(new MantaProtocol(), "test.cyberduck.ch", credentials);
            final MantaSession session = new MantaSession(host);
            session.open(new DisabledHostKeyCallback());
            session.login(new DisabledPasswordStore(),
                    new DisabledLoginCallback() {
                        @Override
                        public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                            final String passphrase = System.getProperty("manta.passphrase.key_passphrase");
                            credentials.setPassword(passphrase);
                        }
                    },
                    new DisabledCancelCallback(),
                    new PathCache(0)
            );
            assertEquals(System.getProperty("manta.passphrase.key_id"), session.getFingerprint());
            session.close();
        }
        finally {
            key.delete();
        }

    }
}
