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
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.FileReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by tomascelaya on 5/24/17.
 */
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
        } catch (BackgroundException e) {
            assertTrue(e instanceof LoginFailureException);
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

    // TODO: add a test that uses the passphrase input
}
