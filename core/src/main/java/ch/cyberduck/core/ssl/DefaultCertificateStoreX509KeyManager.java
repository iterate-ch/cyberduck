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
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.cert.CertificateException;

public class DefaultCertificateStoreX509KeyManager extends CertificateStoreX509KeyManager {
    private static final Logger log = LogManager.getLogger(DefaultCertificateStoreX509KeyManager.class);

    public DefaultCertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark, final CertificateStore callback) {
        this(prompt, bookmark, callback, new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() throws ConcurrentException {
                KeyStore store;
                String type = null;
                try {
                    final Preferences preferences = PreferencesFactory.get();
                    type = preferences.getProperty("connection.ssl.keystore.type");
                    log.info("Load default store of type {}", type);
                    if(StringUtils.isBlank(type)) {
                        type = KeyStore.getDefaultType();
                    }
                    final String provider = preferences.getProperty("connection.ssl.keystore.provider");
                    if(StringUtils.isBlank(provider)) {
                        store = KeyStore.getInstance(type);
                    }
                    else {
                        store = KeyStore.getInstance(type, provider);
                    }
                }
                catch(Exception e) {
                    try {
                        log.error(String.format("Could not load default store of type %s", type), e);
                        log.info("Load default store of default type");
                        store = KeyStore.getInstance(KeyStore.getDefaultType());
                    }
                    catch(KeyStoreException ex) {
                        log.error("Initialization of key store failed. {}", e.getMessage());
                        throw new ConcurrentException(e);
                    }
                }
                try {
                    store.load(null, null);
                }
                catch(IOException | NoSuchAlgorithmException | CertificateException | ProviderException e) {
                    log.error("Loading of key store failed. {}", e.getMessage());
                    throw new ConcurrentException(e);
                }
                return store;
            }
        });
    }

    public DefaultCertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark, final CertificateStore callback,
                                                 final LazyInitializer<KeyStore> keystore) {
        super(prompt, bookmark, callback, keystore);
    }
}
