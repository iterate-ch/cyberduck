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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.apache.commons.lang3.ObjectUtils;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;

import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;
import sun.security.rsa.RSAKeyPairGenerator;

import static org.junit.Assert.fail;

public abstract class AbstractMantaTest {

    protected MantaSession session;

    @BeforeClass
    public static void protocol() {
        ProtocolFactory.register(new MantaProtocol());
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        System.out.print(">>>> test setup <<<<");
        final Profile profile = ProfileReaderFactory.get().read(
                new Local("../profiles/Triton Manta.cyberduckprofile"));

        final String keyPath = ObjectUtils.firstNonNull(System.getProperty("manta.key_path"), "");
        final Host host = new Host(
                profile,
                profile.getDefaultHostname(),
                new Credentials(System.getProperty("manta.user"))
                        .withIdentity(new Local(keyPath)));

        session = new MantaSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        new LoginConnectionService(
                new DisabledLoginCallback() {
                    @Override
                    public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                        fail(reason);
                    }
                },
                new DisabledHostKeyCallback(),
                new DisabledPasswordStore(),
                new DisabledProgressListener()
        ).connect(session, PathCache.empty(), new DisabledCancelCallback());
    }
}
