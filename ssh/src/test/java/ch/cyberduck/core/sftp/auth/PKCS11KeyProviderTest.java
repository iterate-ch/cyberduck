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

import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;

import net.schmizz.sshj.common.KeyType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PKCS11KeyProviderTest {

    @Test
    public void testReadRsaIdentity() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair pair = generator.generateKeyPair();
        final PKCS11KeyProvider provider = new PKCS11KeyProvider(store("alias", pair, "SHA256withRSA"), "alias");
        assertEquals(pair.getPublic(), provider.getPublic());
        assertEquals(KeyType.RSA, provider.getType());
        assertNotNull(provider.getPrivate());
        assertNotNull(provider.getProvider());
    }

    @Test
    public void testReadEcdsaIdentity() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        final KeyPair pair = generator.generateKeyPair();
        final PKCS11KeyProvider provider = new PKCS11KeyProvider(store("alias", pair, "SHA256withECDSA"), "alias");
        assertEquals(KeyType.ECDSA256, provider.getType());
        assertEquals("alias", provider.getAlias());
    }

    @Test(expected = IOException.class)
    public void testUnknownAlias() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        new PKCS11KeyProvider(store("alias", generator.generateKeyPair(), "SHA256withECDSA"), "n").getPublic();
    }

    private static KeyStore store(final String alias, final KeyPair pair, final String algorithm) throws Exception {
        final X509Certificate certificate = certificate(pair, algorithm);
        final KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(null, null);
        store.setKeyEntry(alias, pair.getPrivate(), null, new Certificate[]{certificate});
        return store;
    }

    private static X509Certificate certificate(final KeyPair pair, final String algorithm) throws Exception {
        final X500Principal dn = new X500Principal("CN=Test");
        final Date now = new Date();
        final JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(dn, BigInteger.ONE, now,
                new Date(now.getTime() + 365L * 24 * 60 * 60 * 1000), dn, pair.getPublic());
        final ContentSigner signer = new JcaContentSignerBuilder(algorithm).build(pair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
    }
}
