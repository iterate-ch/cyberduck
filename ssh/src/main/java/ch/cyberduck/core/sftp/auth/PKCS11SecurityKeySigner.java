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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import com.hierynomus.sshj.userauth.fido.SecurityKeySignatureData;
import com.hierynomus.sshj.userauth.fido.SecurityKeySigner;
import com.hierynomus.sshj.userauth.fido.SecurityKeySigningRequest;

/**
 * {@link SecurityKeySigner} backed by a PKCS#11 hardware token.
 * <p>
 * The token must contain the private key that corresponds to the {@code sk-*} public key loaded
 * from the key file. The matching key entry is found by comparing the token certificate's public
 * key against the sk-* public key. Signing produces the FIDO2 authenticator data structure
 * ({@code SHA256(application) || flags || counter || challenge}) and signs it with the PKCS#11
 * key using the appropriate algorithm ({@code SHA256withECDSA} or {@code Ed25519}).
 * <p>
 * The native library is read from the {@code connection.ssl.keystore.pkcs11.library} host
 * preference, the same token already used for mutual-TLS.
 *
 * @see ch.cyberduck.core.ssl.PKCS11CertificateStoreX509KeyManager
 */
public class PKCS11SecurityKeySigner implements SecurityKeySigner {
    private static final Logger log = LogManager.getLogger(PKCS11SecurityKeySigner.class);

    // SSH_SK_USER_PRESENCE_REQD (bit 0): token was physically present during signing
    private static final byte FLAG_USER_PRESENCE = 0x01;

    private final PublicKey publicKey;
    private final LazyInitializer<KeyStore> keyStore;

    public PKCS11SecurityKeySigner(final PublicKey publicKey, final LazyInitializer<KeyStore> keyStore) {
        this.publicKey = publicKey;
        this.keyStore = keyStore;
    }

    @Override
    public SecurityKeySignatureData sign(final SecurityKeySigningRequest request) throws IOException {
        final KeyStore store;
        try {
            store = keyStore.get();
        }
        catch(ConcurrentException e) {
            throw new IOException("Failed to load PKCS11 keystore for security key signing", e.getCause());
        }
        try {
            // Flags: token must assert user presence (physical touch). Counter is 0 — PKCS11
            // tokens have no built-in FIDO2 signature counter.
            final byte flags = FLAG_USER_PRESENCE;
            final long counter = 0L;
            // Build the structure the authenticator signs: SHA256(application) || flags || counter || challenge.
            // 'challenge' is already SHA-256(sshData) as set by AbstractSecurityKeySignature.
            final byte[] signedData = buildSignedData(
                    sha256(request.getApplication().getBytes(StandardCharsets.UTF_8)),
                    flags, counter, request.getChallenge());

            final Enumeration<String> aliases = store.aliases();
            while(aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if(!store.isKeyEntry(alias)) {
                    continue;
                }
                final Certificate cert = store.getCertificate(alias);
                if(cert == null) {
                    log.debug("No certificate for alias {}, skipping", alias);
                    continue;
                }
                if(!Arrays.equals(cert.getPublicKey().getEncoded(), publicKey.getEncoded())) {
                    continue;
                }
                log.debug("Found matching PKCS11 key entry for alias {}", alias);
                final PrivateKey pk = (PrivateKey) store.getKey(alias, null);
                if(pk == null) {
                    log.warn("Private key not accessible for alias {}", alias);
                    continue;
                }
                final String providerName = String.format("SunPKCS11-%s",
                        PreferencesFactory.get().getProperty("application.name"));
                final Provider provider = Security.getProvider(providerName);
                final Signature sig = provider != null
                        ? Signature.getInstance(signingAlgorithm(request.getKeyTypeName()), provider)
                        : Signature.getInstance(signingAlgorithm(request.getKeyTypeName()));
                sig.initSign(pk);
                sig.update(signedData);
                return new SecurityKeySignatureData(flags, counter, sig.sign());
            }
            throw new IOException(String.format(
                    "No matching private key found in PKCS11 token for security key type %s",
                    request.getKeyTypeName()));
        }
        catch(KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException |
              InvalidKeyException | SignatureException e) {
            throw new IOException("PKCS11 security key signing failed", e);
        }
    }

    /**
     * @return {@code SHA256withECDSA} for sk-ecdsa-sha2-nistp256, {@code Ed25519} for sk-ssh-ed25519
     */
    private static String signingAlgorithm(final String keyTypeName) {
        if(keyTypeName.startsWith("sk-ecdsa")) {
            return "SHA256withECDSA";
        }
        return "Ed25519";
    }

    /**
     * Builds {@code SHA256(application) || flags || counter_BE_uint32 || challenge} — the exact
     * byte sequence that the JCA {@code SHA256withECDSA} / {@code Ed25519} engine will hash and sign,
     * matching what {@code AbstractSecurityKeySignature.verify()} rebuilds for verification.
     */
    private static byte[] buildSignedData(final byte[] applicationHash, final byte flags,
                                          final long counter, final byte[] challenge) {
        final byte[] result = new byte[applicationHash.length + 1 + 4 + challenge.length];
        int pos = 0;
        System.arraycopy(applicationHash, 0, result, pos, applicationHash.length);
        pos += applicationHash.length;
        result[pos++] = flags;
        result[pos++] = (byte) ((counter >> 24) & 0xFF);
        result[pos++] = (byte) ((counter >> 16) & 0xFF);
        result[pos++] = (byte) ((counter >> 8) & 0xFF);
        result[pos++] = (byte) (counter & 0xFF);
        System.arraycopy(challenge, 0, result, pos, challenge.length);
        return result;
    }

    private static byte[] sha256(final byte[] data) throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }
}
