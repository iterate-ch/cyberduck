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

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;

import com.hierynomus.sshj.key.KeyAlgorithm;
import com.hierynomus.sshj.key.KeyAlgorithms;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.signature.Signature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PKCS11SignatureTest {

    @Test
    public void testAlgorithm() throws Exception {
        assertEquals("SHA1withRSA", PKCS11Signature.algorithm("ssh-rsa"));
        assertEquals("SHA256withRSA", PKCS11Signature.algorithm("rsa-sha2-256"));
        assertEquals("SHA512withRSA", PKCS11Signature.algorithm("rsa-sha2-512"));
        assertEquals("SHA256withECDSA", PKCS11Signature.algorithm("ecdsa-sha2-nistp256"));
        assertEquals("SHA384withECDSA", PKCS11Signature.algorithm("ecdsa-sha2-nistp384"));
        assertEquals("SHA512withECDSA", PKCS11Signature.algorithm("ecdsa-sha2-nistp521"));
        assertEquals("Ed25519", PKCS11Signature.algorithm("ssh-ed25519"));
    }

    @Test(expected = NoSuchAlgorithmException.class)
    public void testAlgorithmUnsupported() throws Exception {
        PKCS11Signature.algorithm("sk-ssh-ed25519@openssh.com");
    }

    @Test
    public void testSignRsa() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        this.verify(KeyAlgorithms.RSASHA256().create(), generator.generateKeyPair());
        this.verify(KeyAlgorithms.RSASHA512().create(), generator.generateKeyPair());
        this.verify(KeyAlgorithms.SSHRSA().create(), generator.generateKeyPair());
    }

    @Test
    public void testSignEcdsa() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        this.verify(KeyAlgorithms.ECDSASHANistp256().create(), generator.generateKeyPair());
        generator.initialize(new ECGenParameterSpec("secp384r1"));
        this.verify(KeyAlgorithms.ECDSASHANistp384().create(), generator.generateKeyPair());
        generator.initialize(new ECGenParameterSpec("secp521r1"));
        this.verify(KeyAlgorithms.ECDSASHANistp521().create(), generator.generateKeyPair());
    }

    @Test
    public void testSignEd25519() throws Exception {
        final KeyPair pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        final byte[] data = "Signed with key on token".getBytes(StandardCharsets.UTF_8);
        final java.security.Signature engine = PKCS11Signature.create(KeyAlgorithms.EdDSA25519().create().getKeyAlgorithm(), null);
        engine.initSign(pair.getPrivate());
        engine.update(data);
        final byte[] signature = engine.sign();
        final java.security.Signature verifier = java.security.Signature.getInstance("Ed25519");
        verifier.initVerify(pair.getPublic());
        verifier.update(data);
        assertTrue(verifier.verify(signature));
    }

    /**
     * Sign with the engine created for the token and verify the signature encoded to SSH wire format with sshj as the
     * server would.
     */
    private void verify(final KeyAlgorithm algorithm, final KeyPair pair) throws Exception {
        final byte[] data = "Signed with key on token".getBytes(StandardCharsets.UTF_8);
        final java.security.Signature engine = PKCS11Signature.create(algorithm.getKeyAlgorithm(), null);
        engine.initSign(pair.getPrivate());
        engine.update(data);
        final Signature signature = algorithm.newSignature();
        final byte[] encoded = signature.encode(engine.sign());
        signature.initVerify(pair.getPublic());
        signature.update(data, 0, data.length);
        assertTrue(String.format("Failure verifying signature for %s", algorithm.getKeyAlgorithm()),
                signature.verify(new Buffer.PlainBuffer()
                        .putString(signature.getSignatureName())
                        .putBytes(encoded).getCompactData()));
    }
}
