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

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;

import com.hierynomus.sshj.userauth.fido.SecurityKeySignatureData;
import com.hierynomus.sshj.userauth.fido.SecurityKeySigningRequest;

import static org.junit.Assert.*;

public class PKCS11SecurityKeySignerTest {

    private static X509Certificate selfSignedCertificate(final KeyPair kp, final String sigAlgorithm)
            throws Exception {
        final X500Principal dn = new X500Principal("CN=Test");
        final Date now = new Date();
        final Date later = new Date(now.getTime() + 365L * 24 * 60 * 60 * 1000);
        final JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                dn, BigInteger.ONE, now, later, dn, kp.getPublic());
        final ContentSigner signer = new JcaContentSignerBuilder(sigAlgorithm).build(kp.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
    }

    private static KeyStore pkcs12With(final String alias, final KeyPair kp,
                                       final String sigAlgorithm) throws Exception {
        final X509Certificate cert = selfSignedCertificate(kp, sigAlgorithm);
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(alias, kp.getPrivate(), null, new Certificate[]{cert});
        return ks;
    }

    private static KeyStore emptyPkcs12() throws Exception {
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        return ks;
    }

    private static LazyInitializer<KeyStore> lazyOf(final KeyStore store) {
        return new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() {
                return store;
            }
        };
    }

    /**
     * Build the expected signed data: SHA256(application) || flags || counter_BE || challenge
     */
    private static byte[] expectedSignedData(final String application, final byte flags,
                                             final long counter, final byte[] challenge) throws Exception {
        final byte[] appHash = MessageDigest.getInstance("SHA-256")
                .digest(application.getBytes(StandardCharsets.UTF_8));
        final byte[] result = new byte[32 + 1 + 4 + challenge.length];
        System.arraycopy(appHash, 0, result, 0, 32);
        result[32] = flags;
        result[33] = (byte) ((counter >> 24) & 0xFF);
        result[34] = (byte) ((counter >> 16) & 0xFF);
        result[35] = (byte) ((counter >> 8) & 0xFF);
        result[36] = (byte) (counter & 0xFF);
        System.arraycopy(challenge, 0, result, 37, challenge.length);
        return result;
    }

    @Test
    public void testSignEcdsaProducesValidSignature() throws Exception {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        final KeyPair kp = kpg.generateKeyPair();

        final byte[] challenge = new byte[32];
        for(int i = 0; i < challenge.length; i++) {
            challenge[i] = (byte) i;
        }
        final PKCS11SecurityKeySigner signer = new PKCS11SecurityKeySigner(
                kp.getPublic(), lazyOf(pkcs12With("key", kp, "SHA256withECDSA")));

        final SecurityKeySignatureData result = signer.sign(new SecurityKeySigningRequest(
                "sk-ecdsa-sha2-nistp256@openssh.com", "ssh:", new byte[0], challenge, (byte) 0x01));

        assertEquals("user-presence flag must be set", (byte) 0x01, result.getFlags());
        assertEquals("counter must be zero for PKCS11", 0L, result.getCounter());
        assertNotNull(result.getSignature());

        // Verify the DER ECDSA signature covers exactly the FIDO2 authenticator signed data
        final byte[] signedData = expectedSignedData("ssh:", (byte) 0x01, 0L, challenge);
        final Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(kp.getPublic());
        verifier.update(signedData);
        assertTrue("signature must verify against the expected signed data", verifier.verify(result.getSignature()));
    }

    @Test
    public void testSignEd25519ProducesValidSignature() throws Exception {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        final KeyPair kp = kpg.generateKeyPair();

        final byte[] challenge = new byte[32];
        for(int i = 0; i < challenge.length; i++) {
            challenge[i] = (byte) (i + 1);
        }
        final PKCS11SecurityKeySigner signer = new PKCS11SecurityKeySigner(
                kp.getPublic(), lazyOf(pkcs12With("key", kp, "Ed25519")));

        final SecurityKeySignatureData result = signer.sign(new SecurityKeySigningRequest(
                "sk-ssh-ed25519@openssh.com", "ssh:", new byte[0], challenge, (byte) 0x01));

        assertEquals((byte) 0x01, result.getFlags());
        assertEquals(0L, result.getCounter());

        final byte[] signedData = expectedSignedData("ssh:", (byte) 0x01, 0L, challenge);
        final Signature verifier = Signature.getInstance("Ed25519");
        verifier.initVerify(kp.getPublic());
        verifier.update(signedData);
        assertTrue("Ed25519 signature must verify against the expected signed data", verifier.verify(result.getSignature()));
    }

    @Test
    public void testSignCustomApplicationHash() throws Exception {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        final KeyPair kp = kpg.generateKeyPair();

        final byte[] challenge = new byte[32];
        final String app = "sk://example.com";
        final PKCS11SecurityKeySigner signer = new PKCS11SecurityKeySigner(
                kp.getPublic(), lazyOf(pkcs12With("key", kp, "SHA256withECDSA")));

        final SecurityKeySignatureData result = signer.sign(new SecurityKeySigningRequest(
                "sk-ecdsa-sha2-nistp256@openssh.com", app, new byte[0], challenge, (byte) 0x01));

        // Verify: application hash in signed data must match the custom application string
        final byte[] signedData = expectedSignedData(app, (byte) 0x01, 0L, challenge);
        final Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(kp.getPublic());
        verifier.update(signedData);
        assertTrue(verifier.verify(result.getSignature()));

        // And must NOT verify with the default "ssh:" application
        final byte[] wrongAppData = expectedSignedData("ssh:", (byte) 0x01, 0L, challenge);
        final Signature wrongVerifier = Signature.getInstance("SHA256withECDSA");
        wrongVerifier.initVerify(kp.getPublic());
        wrongVerifier.update(wrongAppData);
        assertTrue("signature must not verify when application differs",
                !wrongVerifier.verify(result.getSignature()));
    }

    @Test
    public void testSignNoMatchingKeyThrows() throws Exception {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        final KeyPair storeKp = kpg.generateKeyPair();
        final KeyPair otherKp = kpg.generateKeyPair(); // not in store

        final PKCS11SecurityKeySigner signer = new PKCS11SecurityKeySigner(
                otherKp.getPublic(), lazyOf(pkcs12With("key", storeKp, "SHA256withECDSA")));

        try {
            signer.sign(new SecurityKeySigningRequest(
                    "sk-ecdsa-sha2-nistp256@openssh.com", "ssh:", new byte[0], new byte[32], (byte) 0x01));
            fail("Expected IOException for missing key");
        }
        catch(IOException e) {
            assertTrue(e.getMessage().contains("No matching private key found"));
        }
    }

    @Test
    public void testSignEmptyStoreThrows() throws Exception {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        final PublicKey publicKey = kpg.generateKeyPair().getPublic();

        final PKCS11SecurityKeySigner signer = new PKCS11SecurityKeySigner(
                publicKey, lazyOf(emptyPkcs12()));

        try {
            signer.sign(new SecurityKeySigningRequest(
                    "sk-ecdsa-sha2-nistp256@openssh.com", "ssh:", new byte[0], new byte[32], (byte) 0x01));
            fail("Expected IOException for empty keystore");
        }
        catch(IOException e) {
            assertTrue(e.getMessage().contains("No matching private key found"));
        }
    }

    @Test
    public void testSignKeyStoreFailureWrapped() {
        final LazyInitializer<KeyStore> failing = new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() throws ConcurrentException {
                throw new ConcurrentException(new IOException("token removed"));
            }
        };
        final PKCS11SecurityKeySigner signer = new PKCS11SecurityKeySigner(
                null, failing);

        try {
            signer.sign(new SecurityKeySigningRequest(
                    "sk-ecdsa-sha2-nistp256@openssh.com", "ssh:", new byte[0], new byte[32], (byte) 0x01));
            fail("Expected IOException when KeyStore fails to load");
        }
        catch(IOException e) {
            assertNotNull("cause must be propagated", e.getCause());
        }
    }
}
