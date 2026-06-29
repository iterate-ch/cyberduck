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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;
import java.security.cert.CertificateException;

/**
 * Shared PKCS#11 provider loading and {@link KeyStore} construction used by both mutual-TLS and
 * SSH security key authentication.
 */
public final class PKCS11KeyStore {
    private static final Logger log = LogManager.getLogger(PKCS11KeyStore.class);

    private PKCS11KeyStore() {
        // utility
    }

    /**
     * Returns a {@link LazyInitializer} that, on first access, loads the SunPKCS11 provider from
     * {@code library}, registers it globally, opens a PKCS11 {@link KeyStore}, and prompts for a
     * PIN via {@code login} if the token requires one.
     *
     * @param library  path to the native PKCS#11 library
     * @param bookmark host used for PIN prompt context and protocol icon
     * @param login    callback for PIN prompt
     * @return lazy {@link KeyStore} initializer
     */
    public static LazyInitializer<KeyStore> build(final String library, final Host bookmark,
                                                  final LoginCallback login) {
        return new LazyInitializer<KeyStore>() {
            @Override
            protected KeyStore initialize() throws ConcurrentException {
                try {
                    log.info("Load PKCS11 store from library {}", library);
                    // SunPKCS11 names the configured provider as "SunPKCS11-{name}"
                    final String providerName = String.format("SunPKCS11-%s",
                            PreferencesFactory.get().getProperty("application.name"));
                    Provider provider = Security.getProvider(providerName);
                    if(provider == null) {
                        try {
                            if(LocalFactory.get(library).exists()) {
                                provider = load(library);
                            }
                            else {
                                provider = load(LocalFactory.get(
                                        System.getProperty("java.library.path"), library).getAbsolute());
                            }
                        }
                        catch(ReflectiveOperationException e) {
                            log.error("Failed to load PKCS11 provider from {}: {}", library,
                                    ExceptionUtils.getRootCause(e).getMessage());
                            throw new ConcurrentException(e);
                        }
                        log.debug("Loaded PKCS11 provider {}", provider.getName());
                        // Register globally so JSSE can resolve algorithms from this provider
                        // (e.g. RSASSA-PSS during TLS 1.3 CertificateVerify)
                        Security.addProvider(provider);
                        log.debug("Registered PKCS11 provider {}", provider.getName());
                    }
                    else {
                        log.debug("Reusing existing PKCS11 provider {}", providerName);
                    }
                    final KeyStore store = KeyStore.getInstance("PKCS11", provider);
                    try {
                        store.load(null, null);
                    }
                    catch(IOException e) {
                        if(ExceptionUtils.getRootCause(e) instanceof LoginException) {
                            // Token requires PIN or provided PIN was incorrect — prompt and retry
                            log.debug("Token requires PIN: {}", e.getCause().getMessage());
                            final Credentials credentials;
                            try {
                                credentials = login.prompt(bookmark,
                                        bookmark.getCredentials().getUsername(),
                                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                                        LocaleFactory.localizedString("Enter PIN for PKCS11 token", "Credentials"),
                                        new LoginOptions().user(false).password(true).keychain(false)
                                                .icon(bookmark.getProtocol().disk()));
                            }
                            catch(LoginCanceledException ex) {
                                log.info("PIN prompt canceled for {}", library);
                                throw new ConcurrentException(e);
                            }
                            // Retry with PIN entry
                            store.load(null, credentials.getPassword().toCharArray());
                        }
                        else {
                            log.error("Failed to initialize PKCS11 keystore from {}: {}", library, e.getMessage());
                            throw new ConcurrentException(e);
                        }
                    }
                    catch(CertificateException | NoSuchAlgorithmException e) {
                        log.error("Failed to initialize PKCS11 keystore from {}: {}", library, e.getMessage());
                        throw new ConcurrentException(e);
                    }
                    return store;
                }
                catch(ProviderException e) {
                    // Token has been removed
                    throw new ConcurrentException(new UnsupportedException(e));
                }
                catch(IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
                    throw new ConcurrentException(e);
                }
            }
        };
    }

    /**
     * Load and configure a SunPKCS11 {@link Provider} from a native library.
     *
     * @param libraryPath absolute path to the native PKCS#11 library
     * @return configured (but not yet registered) {@link Provider}
     */
    static Provider load(final String libraryPath) throws ReflectiveOperationException {
        final String config = String.format("--\nname=%s\nlibrary=%s\n",
                PreferencesFactory.get().getProperty("application.name"), libraryPath);
        // Java 9+: standard JCA Provider.configure(String) with inline config (prefix --)
        final Provider base = Security.getProvider("SunPKCS11");
        if(base != null) {
            try {
                final Method configure = Provider.class.getMethod("configure", String.class);
                return (Provider) configure.invoke(base, config);
            }
            catch(NoSuchMethodException ignored) {
                // Java 8 does not have Provider.configure() — fall through
                log.warn("Fall through to reflection to load PKCS11 provider");
            }
        }
        // Java 8: sun.security.pkcs11.SunPKCS11(InputStream) — accessed via reflection so
        // the source compiles without a direct sun.* reference on Java 9+/21
        final Class<?> cls = Class.forName("sun.security.pkcs11.SunPKCS11");
        return (Provider) cls.getConstructor(InputStream.class)
                .newInstance(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
    }
}
