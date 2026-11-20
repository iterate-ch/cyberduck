package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCertificateStore;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DelegatingCertificateStoreX509KeyManagerTest {

    private static final X500Principal ISSUER = new X500Principal("CN=Test CA");

    @Test
    public void testChooseClientAliasSinglePrompt() throws Exception {
        final Host bookmark = new Host(new TestProtocol(), "test.cyberduck.ch");
        final X509Certificate system = certificate("RSA", ISSUER);
        final X509Certificate token = certificate("EC", ISSUER);
        final AtomicInteger prompts = new AtomicInteger();
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager((hostname, certificates) -> {
            prompts.incrementAndGet();
            assertEquals("test.cyberduck.ch", hostname);
            // Certificates from both stores to choose from
            assertEquals(2, certificates.size());
            assertTrue(certificates.contains(system));
            assertTrue(certificates.contains(token));
            return token;
        }, bookmark, manager(bookmark, store("system", system)), manager(bookmark, store("token", token)));
        assertEquals("token", m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        assertEquals(1, prompts.get());
        assertEquals("token", bookmark.getCredentials().getCertificate());
        assertEquals(token, m.getCertificateChain("token")[0]);
        // No prompt with certificate saved
        assertEquals("token", m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        assertEquals(1, prompts.get());
    }

    @Test
    public void testChooseClientAliasCanceled() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager((hostname, certificates) -> {
            throw new ConnectionCanceledException();
        }, bookmark, manager(bookmark, store("system", certificate("RSA", ISSUER))), manager(bookmark, store("token", certificate("EC", ISSUER))));
        assertNull(m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        // Empty selection saved
        assertEquals(StringUtils.EMPTY, bookmark.getCredentials().getCertificate());
    }

    @Test
    public void testChooseClientAliasNoCertificate() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(fail(), bookmark,
                manager(bookmark, store()), manager(bookmark, store()));
        assertNull(m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        // Empty selection saved
        assertEquals(StringUtils.EMPTY, bookmark.getCredentials().getCertificate());
    }

    @Test
    public void testChooseClientAliasIssuerNotAccepted() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(fail(), bookmark,
                manager(bookmark, store("system", certificate("RSA", new X500Principal("CN=Other CA")))), manager(bookmark, store()));
        assertNull(m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        // Empty selection saved
        assertEquals(StringUtils.EMPTY, bookmark.getCredentials().getCertificate());
    }

    @Test
    public void testChooseClientAliasIntermediateAuthorityOtherKeyType() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        // Issued by intermediate authority with other key type. Only the root authority is accepted by the server
        final X509Certificate intermediate = certificate("RSA", ISSUER);
        final X509Certificate certificate = certificate("EC", intermediate);
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager((hostname, certificates) -> {
            assertEquals(Collections.singletonList(certificate), certificates);
            return certificate;
        }, bookmark, manager(bookmark, store("system", certificate, intermediate)));
        assertEquals("system", m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        assertEquals("system", bookmark.getCredentials().getCertificate());
    }

    @Test
    public void testChooseClientAliasOtherKeyType() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        final X509Certificate token = certificate("RSA", ISSUER);
        final AtomicInteger prompts = new AtomicInteger();
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager((hostname, certificates) -> {
            prompts.incrementAndGet();
            assertEquals(1, certificates.size());
            return certificates.get(0);
        }, bookmark, manager(bookmark, store()), manager(bookmark, store("token", token)));
        // Requested for single key type first. Prompt with certificate of other key type
        assertNull(m.chooseClientAlias(new String[]{"EC"}, new Principal[]{ISSUER}, null));
        assertEquals(1, prompts.get());
        // Selection saved but not returned for key type not matching
        assertEquals("token", bookmark.getCredentials().getCertificate());
        // Returned without prompt when requested with matching key type
        assertEquals("token", m.chooseClientAlias(new String[]{"RSA"}, new Principal[]{ISSUER}, null));
        assertEquals(1, prompts.get());
    }

    @Test
    public void testChooseClientAliasMixedKeyTypes() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        final X509Certificate system = certificate("RSA", ISSUER);
        final X509Certificate token = certificate("EC", ISSUER);
        final AtomicInteger prompts = new AtomicInteger();
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager((hostname, certificates) -> {
            prompts.incrementAndGet();
            // Certificates of all key types to choose from although requested for single key type
            assertEquals(2, certificates.size());
            assertTrue(certificates.contains(system));
            assertTrue(certificates.contains(token));
            return system;
        }, bookmark, manager(bookmark, store("system", system)), manager(bookmark, store("token", token)));
        // Requested for key type preferred by server first
        assertNull(m.chooseClientAlias(new String[]{"EC"}, new Principal[]{ISSUER}, null));
        assertEquals(1, prompts.get());
        assertEquals("system", bookmark.getCredentials().getCertificate());
        // Selected certificate returned when requested with its key type
        assertEquals("system", m.chooseClientAlias(new String[]{"RSA"}, new Principal[]{ISSUER}, null));
        assertEquals(1, prompts.get());
        assertEquals(system, m.getCertificateChain("system")[0]);
    }

    @Test
    public void testChooseClientAliasDuplicateCertificate() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        // Same certificate available from both stores
        final X509Certificate certificate = certificate("RSA", ISSUER);
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager((hostname, certificates) -> {
            assertEquals(1, certificates.size());
            return certificates.get(0);
        }, bookmark, manager(bookmark, store("system", certificate)), manager(bookmark, store("token", certificate)));
        // First store preferred
        assertEquals("system", m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        assertEquals("system", bookmark.getCredentials().getCertificate());
    }

    @Test
    public void testChooseClientAliasDuplicateCertificateAuthenticatedStore() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        // Same certificate available from system store and from token unlocked to list its certificates
        final X509Certificate certificate = certificate("RSA", ISSUER);
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager((hostname, certificates) -> {
            assertEquals(1, certificates.size());
            return certificates.get(0);
        }, bookmark, manager(bookmark, store("system", certificate)), authenticated(bookmark, store("token", certificate)));
        // Store already authenticated preferred to not prompt again when signing
        assertEquals("token", m.chooseClientAlias(new String[]{"RSA", "EC"}, new Principal[]{ISSUER}, null));
        assertEquals("token", bookmark.getCredentials().getCertificate());
        assertEquals(certificate, m.getCertificateChain("token")[0]);
        assertNotNull(m.getPrivateKey("token"));
    }

    @Test
    public void testChooseClientAliasSavedEmptySelection() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        bookmark.getCredentials().setCertificate(StringUtils.EMPTY);
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(fail(), bookmark,
                manager(bookmark, store("system", certificate("RSA", ISSUER))), manager(bookmark, store()));
        assertNull(m.chooseClientAlias(new String[]{"RSA"}, new Principal[]{ISSUER}, null));
        assertEquals(StringUtils.EMPTY, bookmark.getCredentials().getCertificate());
    }

    /**
     * No PKCS11 library configured such as by default on Windows
     */
    @Test
    public void testNoLibrary() {
        final Host bookmark = new Host(new TestProtocol());
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(fail(), bookmark, new DisabledCertificateStore(),
                new DisabledLoginCallback() {
                    @Override
                    public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                        throw new AssertionError("No prompt for PIN with no PKCS11 library");
                    }
                }, StringUtils.EMPTY);
        // Only certificates from the system store
        assertEquals(new KeychainX509KeyManager(CertificateIdentityCallback.noop, bookmark, new DisabledCertificateStore()).list(), m.list());
        assertNull(m.getPrivateKey("alias"));
    }

    @Test
    public void testList() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        // Certificates of any issuer and key type
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(fail(), bookmark,
                manager(bookmark, store("system", certificate("RSA", new X500Principal("CN=Other CA")))),
                manager(bookmark, store("token", certificate("EC", ISSUER))));
        assertEquals(Arrays.asList("system", "token"), m.list());
    }

    @Test
    public void testListDuplicateCertificate() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        // Same certificate available from both stores
        final X509Certificate certificate = certificate("RSA", ISSUER);
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(fail(), bookmark,
                manager(bookmark, store("system", certificate)), manager(bookmark, store("token", certificate)));
        // First store preferred
        assertEquals(Collections.singletonList("system"), m.list());
    }

    @Test
    public void testListDuplicateCertificateAuthenticatedStore() throws Exception {
        final Host bookmark = new Host(new TestProtocol());
        // Same certificate available from system store and from token unlocked to list its certificates
        final X509Certificate certificate = certificate("RSA", ISSUER);
        final X509KeyManager m = new DelegatingCertificateStoreX509KeyManager(fail(), bookmark,
                manager(bookmark, store("system", certificate)), authenticated(bookmark, store("token", certificate)));
        // Store already authenticated preferred as when choosing from prompt
        assertEquals(Collections.singletonList("token"), m.list());
        assertEquals(certificate, m.getCertificateChain("token")[0]);
        // Alias not listed still resolved such as saved in bookmark previously
        assertEquals(certificate, m.getCertificateChain("system")[0]);
        assertNotNull(m.getPrivateKey("system"));
    }

    /**
     * Prompt not expected
     */
    private static CertificateIdentityCallback fail() {
        return (hostname, certificates) -> {
            throw new AssertionError("Unexpected prompt");
        };
    }

    private static CertificateStoreX509KeyManager manager(final Host bookmark, final KeyStore store) {
        return new DefaultCertificateStoreX509KeyManager(CertificateIdentityCallback.noop, bookmark, new DisabledCertificateStore(),
                new LazyInitializer<KeyStore>() {
                    @Override
                    protected KeyStore initialize() {
                        return store;
                    }
                });
    }

    /**
     * @return Key manager for store requiring authentication to list certificates such as a token
     */
    private static CertificateStoreX509KeyManager authenticated(final Host bookmark, final KeyStore store) {
        return new DefaultCertificateStoreX509KeyManager(CertificateIdentityCallback.noop, bookmark, new DisabledCertificateStore(),
                new LazyInitializer<KeyStore>() {
                    @Override
                    protected KeyStore initialize() {
                        return store;
                    }
                }) {
            @Override
            public boolean isAuthenticatedToList() {
                return true;
            }
        };
    }

    private static final List<KeyPair> keys = new ArrayList<>();

    /**
     * @param type Key type
     * @return Certificate with key pair to add to store
     */
    private static X509Certificate certificate(final String type, final X500Principal issuer) throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(type);
        if("EC".equals(type)) {
            generator.initialize(new ECGenParameterSpec("secp256r1"));
        }
        else {
            generator.initialize(2048);
        }
        final KeyPair pair = generator.generateKeyPair();
        keys.add(pair);
        final Date now = new Date();
        return new JcaX509CertificateConverter().getCertificate(new JcaX509v3CertificateBuilder(issuer,
                BigInteger.valueOf(keys.size()), now, new Date(now.getTime() + 24L * 60 * 60 * 1000),
                new X500Principal(String.format("CN=Test %d", keys.size())), pair.getPublic()).build(
                new JcaContentSignerBuilder("EC".equals(type) ? "SHA256withECDSA" : "SHA256withRSA").build(pair.getPrivate())));
    }

    /**
     * @param authority Certificate of issuing authority with key pair to sign with
     * @return Certificate with key pair to add to store signed by the authority
     */
    private static X509Certificate certificate(final String type, final X509Certificate authority) throws Exception {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(type);
        if("EC".equals(type)) {
            generator.initialize(new ECGenParameterSpec("secp256r1"));
        }
        else {
            generator.initialize(2048);
        }
        final KeyPair pair = generator.generateKeyPair();
        final KeyPair signer = keys.stream().filter(k -> k.getPublic().equals(authority.getPublicKey())).findFirst().orElseThrow(IllegalArgumentException::new);
        keys.add(pair);
        final Date now = new Date();
        return new JcaX509CertificateConverter().getCertificate(new JcaX509v3CertificateBuilder(authority.getSubjectX500Principal(),
                BigInteger.valueOf(keys.size()), now, new Date(now.getTime() + 24L * 60 * 60 * 1000),
                new X500Principal(String.format("CN=Test %d", keys.size())), pair.getPublic()).build(
                new JcaContentSignerBuilder("EC".equals(authority.getPublicKey().getAlgorithm()) ? "SHA256withECDSA" : "SHA256withRSA").build(signer.getPrivate())));
    }

    private static KeyStore store() throws Exception {
        final KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(null, null);
        return store;
    }

    /**
     * @param authorities Certificates of the issuing authorities to add to the certificate chain
     * @return Store with private key and certificate for alias
     */
    private static KeyStore store(final String alias, final X509Certificate certificate, final X509Certificate... authorities) throws Exception {
        final KeyStore store = store();
        final List<Certificate> chain = new ArrayList<>();
        chain.add(certificate);
        chain.addAll(Arrays.asList(authorities));
        for(KeyPair pair : keys) {
            if(pair.getPublic().equals(certificate.getPublicKey())) {
                store.setKeyEntry(alias, pair.getPrivate(), null, chain.toArray(new Certificate[0]));
            }
        }
        return store;
    }
}
