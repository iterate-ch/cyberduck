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

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.After;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Authenticate against a server with a key read from a keystore, the same code path taken for a key on a PKCS#11
 * token but with a software keystore that does not require a hardware token to run.
 */
public class AuthPKCS11PublickeyTest {

    private SshServer server;

    @After
    public void stop() throws Exception {
        if(server != null) {
            server.stop(true);
        }
    }

    @Test
    public void testAuthenticateRsa() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        assertTrue(this.authenticate(generator.generateKeyPair(), "SHA256withRSA"));
    }

    @Test
    public void testAuthenticateEcdsa256() throws Exception {
        assertTrue(this.authenticate(ecdsa("secp256r1"), "SHA256withECDSA"));
    }

    @Test
    public void testAuthenticateEcdsa384() throws Exception {
        assertTrue(this.authenticate(ecdsa("secp384r1"), "SHA384withECDSA"));
    }

    @Test
    public void testAuthenticateEcdsa521() throws Exception {
        assertTrue(this.authenticate(ecdsa("secp521r1"), "SHA512withECDSA"));
    }

    @Test
    public void testAuthenticateFailureUnknownKey() throws Exception {
        final KeyPair pair = ecdsa("secp256r1");
        // Server only accepts a different key
        this.start(ecdsa("secp256r1").getPublic());
        final SSHClient client = new SSHClient(new DefaultConfig());
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect("localhost", server.getPort());
        try {
            client.auth("test", new AuthPKCS11Publickey(new PKCS11KeyProvider(
                    store("alias", pair, "SHA256withECDSA"), "alias")));
        }
        catch(UserAuthException e) {
            // Expected
        }
        finally {
            assertFalse(client.isAuthenticated());
            client.disconnect();
        }
    }

    private boolean authenticate(final KeyPair pair, final String algorithm) throws Exception {
        this.start(pair.getPublic());
        final SSHClient client = new SSHClient(new DefaultConfig());
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect("localhost", server.getPort());
        try {
            client.auth("test", new AuthPKCS11Publickey(new PKCS11KeyProvider(store("alias", pair, algorithm), "alias")));
            return client.isAuthenticated();
        }
        finally {
            client.disconnect();
        }
    }

    private void start(final PublicKey accepted) throws Exception {
        server = SshServer.setUpDefaultServer();
        server.setPort(0);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setPublickeyAuthenticator((username, key, session) -> KeyUtils.compareKeys(accepted, key));
        server.start();
    }

    private static KeyPair ecdsa(final String curve) throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec(curve));
        return generator.generateKeyPair();
    }

    private static KeyStore store(final String alias, final KeyPair pair, final String algorithm) throws Exception {
        final X500Principal dn = new X500Principal("CN=Test");
        final Date now = new Date();
        final JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(dn, BigInteger.ONE, now,
                new Date(now.getTime() + 365L * 24 * 60 * 60 * 1000), dn, pair.getPublic());
        final ContentSigner signer = new JcaContentSignerBuilder(algorithm).build(pair.getPrivate());
        final X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(builder.build(signer));
        final KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(null, null);
        store.setKeyEntry(alias, pair.getPrivate(), null, new Certificate[]{certificate});
        return store;
    }
}
