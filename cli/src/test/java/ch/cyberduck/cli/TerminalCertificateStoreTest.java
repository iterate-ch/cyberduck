package ch.cyberduck.cli;

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

import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerminalCertificateStoreTest {

    @Test
    public void testVerifyUntrustedSelfSignedPrompts() throws Exception {
        final AtomicInteger prompts = new AtomicInteger();
        final TerminalCertificateTrustCallback callback = callback(prompts, false);
        final TerminalCertificateStore store = new TerminalCertificateStore();
        final X509Certificate cert = selfsigned("localhost");
        // Valid dates and matching hostname but not issued by any trusted anchor
        assertFalse(store.verify(callback, "localhost", Collections.singletonList(cert)));
        assertEquals(1, prompts.get());
    }

    @Test
    public void testVerifyUntrustedAcceptedByUser() throws Exception {
        final TerminalCertificateTrustCallback callback = callback(new AtomicInteger(), true);
        final TerminalCertificateStore store = new TerminalCertificateStore();
        assertTrue(store.verify(callback, "localhost", Collections.singletonList(selfsigned("localhost"))));
    }

    @Test
    public void testVerifyHostnameMismatchPrompts() throws Exception {
        final AtomicInteger prompts = new AtomicInteger();
        final TerminalCertificateTrustCallback callback = callback(prompts, false);
        final TerminalCertificateStore store = new TerminalCertificateStore();
        assertFalse(store.verify(callback, "localhost", Collections.singletonList(selfsigned("wrong.invalid"))));
        assertEquals(1, prompts.get());
    }

    @Test
    public void testVerifyTrustedAnchorEC() throws Exception {
        this.verifyTrusted("EC", 256, "SHA256withECDSA");
    }

    @Test
    public void testVerifyTrustedAnchorRSA() throws Exception {
        this.verifyTrusted("RSA", 2048, "SHA256withRSA");
    }

    /**
     * Chain anchored in default trust store is accepted without prompt using generic auth type regardless of key algorithm
     */
    private void verifyTrusted(final String algorithm, final int size, final String signature) throws Exception {
        final X509Certificate cert = selfsigned("localhost", algorithm, size, signature);
        final KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(null, null);
        store.setCertificateEntry("test", cert);
        final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(store);
        final AtomicInteger prompts = new AtomicInteger();
        final TerminalCertificateTrustCallback callback = callback(prompts, false);
        final TerminalCertificateStore verifier = new TerminalCertificateStore((X509TrustManager) factory.getTrustManagers()[0]);
        assertTrue(verifier.verify(callback, "localhost", Collections.singletonList(cert)));
        assertEquals(0, prompts.get());
    }

    private static TerminalCertificateTrustCallback callback(final AtomicInteger prompts, final boolean accept) {
        return new TerminalCertificateTrustCallback(message -> {
            prompts.incrementAndGet();
            return accept;
        });
    }

    private static X509Certificate selfsigned(final String san) throws Exception {
        return selfsigned(san, "EC", 256, "SHA256withECDSA");
    }

    private static X509Certificate selfsigned(final String san, final String algorithm, final int size, final String signature) throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        generator.initialize(size);
        final KeyPair pair = generator.generateKeyPair();
        final X500Principal subject = new X500Principal("CN=" + san);
        final long now = System.currentTimeMillis();
        return new JcaX509CertificateConverter().getCertificate(new JcaX509v3CertificateBuilder(subject,
            BigInteger.valueOf(now), new Date(now - 60_000), new Date(now + 3_600_000), subject, pair.getPublic())
            .addExtension(Extension.subjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.dNSName, san)))
            .build(new JcaContentSignerBuilder(signature).build(pair.getPrivate())));
    }
}
