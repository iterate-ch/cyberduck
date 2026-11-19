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
import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Certificates with private key on a PKCS#11 token. Only used with {@link DelegatingCertificateStoreX509KeyManager}
 * prompting to choose from the certificates of all stores.
 */
public class PKCS11CertificateStoreX509KeyManager extends CertificateStoreX509KeyManager {
    private static final Logger log = LogManager.getLogger(PKCS11CertificateStoreX509KeyManager.class);

    public PKCS11CertificateStoreX509KeyManager(final Host bookmark, final CertificateStore store, final LoginCallback login) {
        this(bookmark, store, login, HostPreferencesFactory.get(bookmark).getProperty("connection.keystore.pkcs11.library"));
    }

    /**
     * @param library Native PKCS11 library name or path. No certificates are listed when blank
     */
    public PKCS11CertificateStoreX509KeyManager(final Host bookmark, final CertificateStore store, final LoginCallback login,
                                                final String library) {
        super(CertificateIdentityCallback.noop, bookmark, store, StringUtils.isBlank(library) ? none() : PKCS11KeyStore.build(library, bookmark, login));
    }

    /**
     * @return Empty keystore with no PKCS11 library configured
     */
    private static LazyInitializer<KeyStore> none() {
        return new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() throws ConcurrentException {
                log.debug("No PKCS11 library configured");
                try {
                    final KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
                    store.load(null, null);
                    return store;
                }
                catch(IOException | GeneralSecurityException e) {
                    throw new ConcurrentException(e);
                }
            }
        };
    }

    /**
     * @return True as the PIN of the token is required to read its key entries
     */
    @Override
    public boolean isAuthenticatedToList() {
        return true;
    }
}
