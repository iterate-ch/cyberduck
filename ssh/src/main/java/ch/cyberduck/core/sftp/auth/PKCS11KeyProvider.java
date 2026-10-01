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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;

import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

/**
 * Single identity from a PKCS#11 token referenced by its alias in {@link KeyStore}. The public key is read from the
 * certificate on the token, the private key is a handle to the key object that never leaves the device.
 *
 * @see ch.cyberduck.core.ssl.PKCS11KeyStore
 */
public class PKCS11KeyProvider implements KeyProvider {
    private static final Logger log = LogManager.getLogger(PKCS11KeyProvider.class);

    private final KeyStore store;
    private final String alias;

    public PKCS11KeyProvider(final KeyStore store, final String alias) {
        this.store = store;
        this.alias = alias;
    }

    @Override
    public PublicKey getPublic() throws IOException {
        try {
            final Certificate certificate = store.getCertificate(alias);
            if(null == certificate) {
                throw new IOException(String.format("No certificate for alias %s in PKCS11 token", alias));
            }
            return certificate.getPublicKey();
        }
        catch(GeneralSecurityException e) {
            throw new IOException(String.format("Failure reading certificate for alias %s from PKCS11 token", alias), e);
        }
    }

    /**
     * @return Handle for the private key object on the token. Signing with this key is delegated to the token and
     * requires the {@link #getProvider()} security provider.
     */
    @Override
    public PrivateKey getPrivate() throws IOException {
        try {
            final java.security.Key key = store.getKey(alias, null);
            if(!(key instanceof PrivateKey)) {
                throw new IOException(String.format("No private key for alias %s in PKCS11 token", alias));
            }
            log.debug("Using private key {} for alias {}", key, alias);
            return (PrivateKey) key;
        }
        catch(GeneralSecurityException e) {
            throw new IOException(String.format("Failure reading private key for alias %s from PKCS11 token", alias), e);
        }
    }

    /**
     * Determined from the certificate on the token. The private key object is opaque and its type cannot be
     * determined from key material.
     */
    @Override
    public KeyType getType() throws IOException {
        return KeyType.fromKey(this.getPublic());
    }

    /**
     * @return Security provider to create the signature engine from for signing with {@link #getPrivate()}
     */
    public Provider getProvider() {
        return store.getProvider();
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PKCS11KeyProvider{");
        sb.append("alias='").append(alias).append('\'');
        sb.append(", provider=").append(store.getProvider().getName());
        sb.append('}');
        return sb.toString();
    }
}
