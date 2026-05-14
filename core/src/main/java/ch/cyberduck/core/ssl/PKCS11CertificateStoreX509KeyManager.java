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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

public class PKCS11CertificateStoreX509KeyManager extends CertificateStoreX509KeyManager {
    private static final Logger log = LogManager.getLogger(PKCS11CertificateStoreX509KeyManager.class);

    public PKCS11CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                final CertificateStore store, final LoginCallback login) {
        this(prompt, bookmark, store, login, HostPreferencesFactory.get(bookmark).getProperty("connection.ssl.keystore.pkcs11.library"));
    }

    public PKCS11CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                final CertificateStore store, final LoginCallback login,
                                                final String libraryPath) {
        super(prompt, bookmark, store, buildKeyStore(libraryPath, bookmark, login));
    }

    private static LazyInitializer<KeyStore> buildKeyStore(final String libraryPath, final Host bookmark, final LoginCallback login) {
        return new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() throws ConcurrentException {
                try {
                    log.info("Load PKCS11 store from library {}", libraryPath);
                    // SunPKCS11 names the configured provider as "SunPKCS11-{name}"
                    final String providerName = "SunPKCS11-Cyberduck";
                    Provider provider = Security.getProvider(providerName);
                    if(provider == null) {
                        provider = configurePkcs11Provider(libraryPath);
                        // Register globally so JSSE can resolve RSASSA-PSS from this provider
                        // during TLS 1.3 CertificateVerify — required for RSA keys on hardware tokens
                        Security.addProvider(provider);
                        log.debug("Registered PKCS11 provider {}", provider.getName());
                    }
                    else {
                        log.debug("Reusing existing PKCS11 provider {}", providerName);
                    }
                    final KeyStore store = KeyStore.getInstance("PKCS11", provider);
                    char[] pin = null;
                    while(true) {
                        try {
                            store.load(null, pin);
                            break;
                        }
                        catch(IOException e) {
                            if(e.getCause() instanceof LoginException) {
                                // Token requires PIN or provided PIN was incorrect — prompt and retry
                                log.debug("Token requires PIN: {}", e.getCause().getMessage());
                                final Credentials credentials = login.prompt(bookmark,
                                        bookmark.getCredentials().getUsername(),
                                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                                        LocaleFactory.localizedString("Enter PIN for PKCS11 token", "Credentials"),
                                        new LoginOptions().user(false).password(true).keychain(false).icon(bookmark.getProtocol().disk())
                                );
                                pin = credentials.getPassword().toCharArray();
                            }
                            else {
                                throw e;
                            }
                        }
                    }
                    return store;
                }
                catch(LoginCanceledException e) {
                    log.info("PIN prompt canceled for {}", libraryPath);
                    throw new ConcurrentException(e);
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
