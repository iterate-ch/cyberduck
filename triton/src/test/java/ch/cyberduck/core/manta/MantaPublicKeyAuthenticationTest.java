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
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class MantaPublicKeyAuthenticationTest {

    @Test
    @Ignore
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
            final Host host = new Host(new MantaProtocol(), "us-east.manta.joyent.com", credentials);
            final MantaSession session = new MantaSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
            session.open(new DisabledHostKeyCallback());
            session.login(new DisabledPasswordStore(),
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                        // no passphrase to set
                        return null;
                    }
                },
                new DisabledCancelCallback()
            );
            assertEquals(session.getClient().getContext().getMantaKeyId(), System.getProperty("manta.key_id"));
            session.close();
        }
        finally {
            key.delete();
        }
    }

    @Test
    @Ignore
    public void testAuthenticateOpenSSHKeyWithPassphrase() throws Exception {
        final Credentials credentials = new Credentials(System.getProperty("manta.user"), "");
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        credentials.setIdentity(key);
        final String passphraseKeyPassword = System.getProperty("manta.passphrase.password");
        final String passphraseKeyPath = System.getProperty("manta.passphrase.key_path");
        if(passphraseKeyPassword == null || passphraseKeyPath == null) {
            Assert.fail("No passphrase key configured, please set 'manta.passphrase.key_path' and 'manta.passphrase.password' to a key path and passphrase respectively");
        }

        try {

            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(
                new FileReader(passphraseKeyPath),
                key.getOutputStream(false),
                StandardCharsets.UTF_8
            );
            final Host host = new Host(new MantaProtocol(), "us-east.manta.joyent.com", credentials);
            final MantaSession session = new MantaSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
            session.open(new DisabledHostKeyCallback());
            session.login(new DisabledPasswordStore(),
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                        return new Credentials(username, passphraseKeyPassword);
                    }
                },
                new DisabledCancelCallback()
            );
            assertEquals(System.getProperty("manta.passphrase.key_id"), session.getClient().getContext().getMantaKeyId());
            session.close();
        }
        finally {
            key.delete();
        }

    }
}
