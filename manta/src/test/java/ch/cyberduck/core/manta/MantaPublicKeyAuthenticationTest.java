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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.FileReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Category(IntegrationTest.class)
public class MantaPublicKeyAuthenticationTest {

    @Test
    public void testAuthenticateOpenSSHKeyWithoutPassphrase() throws Exception {
        Assume.assumeNotNull(PROPERTIES.get("manta.url"), PROPERTIES.get("manta.key_id"), PROPERTIES.get("manta.key_path"));

        final Credentials credentials = new Credentials(PROPERTIES.get("manta.user"), "");
        final Local key = new Local(PROPERTIES.get("java.io.tmpdir"), UUID.randomUUID().toString());
        credentials.setIdentity(key);

        try {
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(
                    new FileReader(PROPERTIES.get("manta.key_path")),
                    key.getOutputStream(false),
                    StandardCharsets.UTF_8
            );
            final String hostname = new URL(PROPERTIES.get("manta.url")).getHost();
            final Host host = new Host(new MantaProtocol(), hostname, credentials);
            final MantaSession session = new MantaSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
            session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.login(Proxy.DIRECT,
                    new DisabledLoginCallback() {
                        @Override
                        public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                            throw new LoginCanceledException();
                        }
                    },
                    new DisabledCancelCallback()
            );
            assertEquals(session.getClient().getContext().getMantaKeyId(), PROPERTIES.get("manta.key_id"));
            session.close();
        }
        finally {
            key.delete();
        }
    }

    @Test
    public void testAuthenticateOpenSSHKeyWithPassphrase() throws Exception {
        Assume.assumeNotNull(PROPERTIES.get("manta.url"), PROPERTIES.get("manta.passphrase.key_id"), PROPERTIES.get("manta.passphrase.key_path"), PROPERTIES.get("manta.passphrase.password"));

        final Credentials credentials = new Credentials(PROPERTIES.get("manta.user"), "");
        final Local key = new Local(PROPERTIES.get("java.io.tmpdir"), UUID.randomUUID().toString());
        credentials.setIdentity(key);
        final String passphraseKeyPassword = PROPERTIES.get("manta.passphrase.password");
        final String passphraseKeyPath = PROPERTIES.get("manta.passphrase.key_path");
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
            final String hostname = new URL(PROPERTIES.get("manta.url")).getHost();
            final Host host = new Host(new MantaProtocol(), hostname, credentials);
            final MantaSession session = new MantaSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
            session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.login(Proxy.DIRECT,
                    new DisabledLoginCallback() {
                        @Override
                        public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                            return new VaultCredentials(passphraseKeyPassword);
                        }
                    },
                    new DisabledCancelCallback()
            );
            assertEquals(PROPERTIES.get("manta.passphrase.key_id"), session.getClient().getContext().getMantaKeyId());
            session.close();
        }
        finally {
            key.delete();
        }

    }
}
