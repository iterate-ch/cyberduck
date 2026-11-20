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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.threading.DisabledCancelCallback;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import net.schmizz.sshj.SSHClient;

import static org.junit.Assert.*;

public class SFTPPKCS11AuthenticationTest {

    @Test
    public void testSkipFailureLoadingLibrary() throws Exception {
        final SFTPPKCS11Authentication authentication = new SFTPPKCS11Authentication(new SSHClient(), "n.so");
        assertFalse(authentication.authenticate(new Host(new TestProtocol()), new DisabledLoginCallback(), new DisabledCancelCallback()));
    }

    /**
     * Token must not be loaded prompting for PIN with private key file selected in bookmark
     */
    @Test
    public void testSkipPrivateKeySelected() throws Exception {
        final KeyPair pair = ecdsa();
        final Local key = write(String.format("-----BEGIN PRIVATE KEY-----\n%s\n-----END PRIVATE KEY-----\n",
                Base64.getMimeEncoder().encodeToString(pair.getPrivate().getEncoded())));
        try {
            final Host host = new Host(new TestProtocol());
            host.getCredentials().setIdentity(key);
            final AtomicBoolean loaded = new AtomicBoolean();
            final SFTPPKCS11Authentication authentication = new SFTPPKCS11Authentication(new SSHClient(), "n.so") {
                @Override
                protected KeyStore load(final Host bookmark, final LoginCallback prompt) {
                    loaded.set(true);
                    return null;
                }
            };
            assertFalse(authentication.authenticate(host, new DisabledLoginCallback(), new DisabledCancelCallback()));
            assertFalse(loaded.get());
        }
        finally {
            key.delete();
        }
    }

    /**
     * No key from token offered with public key file selected in bookmark not matching any key on the token
     */
    @Test
    public void testSkipPublicKeySelectedNotOnToken() throws Exception {
        final KeyStore store = store(ecdsa());
        final Local key = write(openssh(ecdsa()));
        try {
            final Host host = new Host(new TestProtocol());
            host.getCredentials().setIdentity(key);
            final AtomicBoolean loaded = new AtomicBoolean();
            final SFTPPKCS11Authentication authentication = new SFTPPKCS11Authentication(new SSHClient(), "n.so") {
                @Override
                protected KeyStore load(final Host bookmark, final LoginCallback prompt) {
                    loaded.set(true);
                    return store;
                }
            };
            // Unconnected client would fail if any key was offered
            assertFalse(authentication.authenticate(host, new DisabledLoginCallback(), new DisabledCancelCallback()));
            assertTrue(loaded.get());
        }
        finally {
            key.delete();
        }
    }

    @Test
    public void testFilter() throws Exception {
        final KeyPair first = ecdsa();
        final KeyPair second = ecdsa();
        final KeyStore store = store(first, second);
        final SFTPPKCS11Authentication authentication = new SFTPPKCS11Authentication(new SSHClient(), "n.so");
        final List<PKCS11KeyProvider> identities = authentication.list(store);
        assertEquals(2, identities.size());
        // All keys offered with no public key configured
        assertEquals(2, authentication.filter(identities, null).size());
        final Local key = write(openssh(second));
        try {
            final List<PKCS11KeyProvider> filtered = authentication.filter(identities,
                    SFTPAgentAuthentication.identityFromPublicKey(key).getBlob());
            assertEquals(1, filtered.size());
            assertEquals("key1", filtered.get(0).getAlias());
        }
        finally {
            key.delete();
        }
        // No match
        assertTrue(authentication.filter(identities, new PKCS11KeyProvider(store(ecdsa()), "key0").getBlob()).isEmpty());
    }

    private static KeyPair ecdsa() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        return generator.generateKeyPair();
    }

    /**
     * @return Public key in OpenSSH format as found in public key file
     */
    private static String openssh(final KeyPair pair) throws Exception {
        return String.format("ecdsa-sha2-nistp256 %s comment\n", Base64.getEncoder().encodeToString(
                new PKCS11KeyProvider(store(pair), "key0").getBlob()));
    }

    private static Local write(final String content) throws Exception {
        final Local file = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try(OutputStream out = file.getOutputStream(false)) {
            IOUtils.write(content, out, StandardCharsets.UTF_8);
        }
        return file;
    }

    /**
     * Software keystore with key entries named key0, key1, ... standing in for keys on a token
     */
    private static KeyStore store(final KeyPair... pairs) throws Exception {
        final KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(null, null);
        for(int i = 0; i < pairs.length; i++) {
            store.setKeyEntry(String.format("key%d", i), pairs[i].getPrivate(), null, new Certificate[]{certificate(pairs[i])});
        }
        return store;
    }

    private static X509Certificate certificate(final KeyPair pair) throws Exception {
        final X500Principal dn = new X500Principal("CN=Test");
        final Date now = new Date();
        final JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(dn, BigInteger.ONE, now,
                new Date(now.getTime() + 365L * 24 * 60 * 60 * 1000), dn, pair.getPublic());
        return new JcaX509CertificateConverter().getCertificate(builder.build(
                new JcaContentSignerBuilder("SHA256withECDSA").build(pair.getPrivate())));
    }
}
