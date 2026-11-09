package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.stream.Collectors;

import com.hierynomus.sshj.key.KeyAlgorithm;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.Factory.Named;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.signature.Signature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SFTPSessionKeyAlgorithmsTest {

    private static List<String> names(final List<Named<KeyAlgorithm>> factories) {
        return factories.stream().map(Named::getName).collect(Collectors.toList());
    }

    @Test
    public void testRsaSha2CertificatesAheadOfLegacy() {
        final List<String> names = names(SFTPSession.withRsaSha2Certificates(new DefaultConfig().getKeyAlgorithms()));
        final int sha512 = names.indexOf("rsa-sha2-512-cert-v01@openssh.com");
        final int sha256 = names.indexOf("rsa-sha2-256-cert-v01@openssh.com");
        final int legacy = names.indexOf("ssh-rsa-cert-v01@openssh.com");
        assertTrue(names.toString(), sha512 >= 0 && sha256 > sha512 && legacy > sha256);
    }

    @Test
    public void testNoDuplicatesWhenAlreadyConfigured() {
        final List<Named<KeyAlgorithm>> once = SFTPSession.withRsaSha2Certificates(new DefaultConfig().getKeyAlgorithms());
        assertSame(once, SFTPSession.withRsaSha2Certificates(once));
    }

    @Test
    public void testSignatureNameIsSha2() throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair pair = generator.generateKeyPair();
        final byte[] message = "test".getBytes();
        for(Named<KeyAlgorithm> factory : SFTPSession.withRsaSha2Certificates(new DefaultConfig().getKeyAlgorithms())) {
            if(!factory.getName().startsWith("rsa-sha2-") || !factory.getName().contains("-cert-")) {
                continue;
            }
            final String expected = factory.getName().substring(0, factory.getName().indexOf("-cert-"));
            final KeyAlgorithm algorithm = factory.create();
            assertEquals(factory.getName(), algorithm.getKeyAlgorithm());
            final Signature signer = algorithm.newSignature();
            assertEquals(expected, signer.getSignatureName());
            signer.initSign(pair.getPrivate());
            signer.update(message, 0, message.length);
            final byte[] signature = new Buffer.PlainBuffer().putString(signer.getSignatureName())
                    .putString(signer.encode(signer.sign())).getCompactData();
            final Signature verifier = algorithm.newSignature();
            verifier.initVerify(pair.getPublic());
            verifier.update(message, 0, message.length);
            assertTrue(factory.getName(), verifier.verify(signature));
        }
    }
}
