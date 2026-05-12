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

import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

public class PKCS11CertificateStoreX509KeyManager extends CertificateStoreX509KeyManager {
    private static final Logger log = LogManager.getLogger(PKCS11CertificateStoreX509KeyManager.class);

    public PKCS11CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                final CertificateStore store) {
        this(prompt, bookmark, store, HostPreferencesFactory.get(bookmark).getProperty("connection.ssl.keystore.pkcs11.library"));
    }

    public PKCS11CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                final CertificateStore store, final String libraryPath) {
        super(prompt, bookmark, store, buildKeyStore(libraryPath));
    }

    public PKCS11CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                final CertificateStore store, final LazyInitializer<KeyStore> keystore) {
        super(prompt, bookmark, store, keystore);
    }

    private static LazyInitializer<KeyStore> buildKeyStore(final String libraryPath) {
        return new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() throws ConcurrentException {
                try {
                    log.info("Load PKCS11 store from library {}", libraryPath);
                    final Provider provider = configurePkcs11Provider(libraryPath);
                    final KeyStore store = KeyStore.getInstance("PKCS11", provider);
                    store.load(null, null);
                    return store;
                }
                catch(Exception e) {
                    log.error("Failed to initialize PKCS11 keystore from {}: {}", libraryPath, e.getMessage());
                    throw new ConcurrentException(e);
                }
            }
        };
    }

    private static Provider configurePkcs11Provider(final String libraryPath) throws Exception {
        // Java 9+: standard JCA Provider.configure(String) with inline config (prefix --)
        final Provider base = Security.getProvider("SunPKCS11");
        if(base != null) {
            try {
                final String config = "--\nname=Cyberduck\nlibrary=" + libraryPath + "\n";
                return (Provider) Provider.class.getMethod("configure", String.class).invoke(base, config);
            }
            catch(NoSuchMethodException ignored) {
                // Java 8 does not have Provider.configure() — fall through
            }
        }
        // Java 8: sun.security.pkcs11.SunPKCS11(InputStream) — accessed via reflection so
        // the source compiles without a direct sun.* reference on Java 9+/21
        final String config = "name=Cyberduck\nlibrary=" + libraryPath + "\n";
        final Class<?> cls = Class.forName("sun.security.pkcs11.SunPKCS11");
        return (Provider) cls.getConstructor(java.io.InputStream.class)
                .newInstance(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
    }
}
