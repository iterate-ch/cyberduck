package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
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

public class DefaultCertificateStoreTest {

    @Test
    public void testVerifyUntrustedSelfSigned() throws Exception {
        assertFalse(new DefaultCertificateStore().verify(new DisabledCertificateTrustCallback(), "localhost",
            Collections.singletonList(selfsigned("localhost", "EC", 256, "SHA256withECDSA"))));
    }

    @Test
    public void testVerifyUntrustedPromptsAndAccepted() throws Exception {
        final AtomicInteger prompts = new AtomicInteger();
        final X509Certificate cert = selfsigned("localhost", "EC", 256, "SHA256withECDSA");
        assertTrue(new DefaultCertificateStore().verify((message, certificates) -> prompts.incrementAndGet(), "localhost", Collections.singletonList(cert)));
        assertEquals(1, prompts.get());
    }

    @Test
    public void testVerifyUntrustedPromptCancelled() throws Exception {
        final AtomicInteger prompts = new AtomicInteger();
        final X509Certificate cert = selfsigned("localhost", "EC", 256, "SHA256withECDSA");
        assertFalse(new DefaultCertificateStore().verify((message, certificates) -> {
            prompts.incrementAndGet();
            throw new ConnectionCanceledException();
        }, "localhost", Collections.singletonList(cert)));
        assertEquals(1, prompts.get());
    }

    @Test
    public void testVerifyHostnameMismatchPrompts() throws Exception {
        final AtomicInteger prompts = new AtomicInteger();
        final X509Certificate cert = selfsigned("wrong.invalid", "EC", 256, "SHA256withECDSA");
        assertFalse(new DefaultCertificateStore(trust(cert)).verify((message, certificates) -> {
            prompts.incrementAndGet();
            throw new ConnectionCanceledException();
        }, "localhost", Collections.singletonList(cert)));
        assertEquals(1, prompts.get());
    }

    @Test
    public void testVerifyCollectsAllFailuresInSinglePrompt() throws Exception {
        final AtomicInteger prompts = new AtomicInteger();
        final StringBuilder message = new StringBuilder();
        // Expired, for another host and not anchored in trust store
        final X509Certificate cert = selfsigned("wrong.invalid", "EC", 256, "SHA256withECDSA", -7_200_000, -3_600_000);
        assertTrue(new DefaultCertificateStore().verify((m, certificates) -> {
            prompts.incrementAndGet();
            message.append(m);
        }, "localhost", Collections.singletonList(cert)));
        assertEquals(1, prompts.get());
        assertTrue(message.toString().contains("has expired"));
        assertTrue(message.toString().contains("is not valid for localhost"));
        assertTrue(message.toString().contains("unknown certifying authority"));
    }

    @Test
    public void testVerifyTrustedNoPrompt() throws Exception {
        final AtomicInteger prompts = new AtomicInteger();
        final X509Certificate cert = selfsigned("localhost", "EC", 256, "SHA256withECDSA");
        assertTrue(new DefaultCertificateStore(trust(cert)).verify((message, certificates) -> prompts.incrementAndGet(), "localhost", Collections.singletonList(cert)));
        assertEquals(0, prompts.get());
    }

    @Test
    public void testVerifyTrustedEC() throws Exception {
        this.verifyTrusted("EC", 256, "SHA256withECDSA");
    }

    @Test
    public void testVerifyTrustedRSA() throws Exception {
        this.verifyTrusted("RSA", 2048, "SHA256withRSA");
    }

    @Test
    public void testVerifyHostnameMismatch() throws Exception {
        final X509Certificate cert = selfsigned("wrong.invalid", "EC", 256, "SHA256withECDSA");
        assertFalse(new DefaultCertificateStore(trust(cert)).verify(new DisabledCertificateTrustCallback(), "localhost", Collections.singletonList(cert)));
    }

    private void verifyTrusted(final String algorithm, final int size, final String signature) throws Exception {
        final X509Certificate cert = selfsigned("localhost", algorithm, size, signature);
        assertTrue(new DefaultCertificateStore(trust(cert)).verify(new DisabledCertificateTrustCallback(), "localhost", Collections.singletonList(cert)));
    }

    private static X509TrustManager trust(final X509Certificate anchor) throws Exception {
        final KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(null, null);
        store.setCertificateEntry("test", anchor);
        final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(store);
        return (X509TrustManager) factory.getTrustManagers()[0];
    }

    private static X509Certificate selfsigned(final String san, final String algorithm, final int size, final String signature) throws Exception {
        return selfsigned(san, algorithm, size, signature, -60_000, 3_600_000);
    }

    private static X509Certificate selfsigned(final String san, final String algorithm, final int size, final String signature,
                                              final long notBefore, final long notAfter) throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
        generator.initialize(size);
        final KeyPair pair = generator.generateKeyPair();
        final X500Principal subject = new X500Principal("CN=" + san);
        final long now = System.currentTimeMillis();
        return new JcaX509CertificateConverter().getCertificate(new JcaX509v3CertificateBuilder(subject,
            BigInteger.valueOf(now), new Date(now + notBefore), new Date(now + notAfter), subject, pair.getPublic())
            .addExtension(Extension.subjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.dNSName, san)))
            .build(new JcaContentSignerBuilder(signature).build(pair.getPrivate())));
    }
}
