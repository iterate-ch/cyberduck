package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class CertificateStoreX509KeyManager extends AbstractX509KeyManager {
    private static final Logger log = Logger.getLogger(CertificateStoreX509KeyManager.class);

    private KeyStore store;

    private CertificateStore callback;

    public CertificateStoreX509KeyManager(final CertificateStore callback) {
        this.callback = callback;
    }

    public CertificateStoreX509KeyManager(final CertificateStore callback, final KeyStore store) {
        this.callback = callback;
        this.store = store;
    }

    public CertificateStoreX509KeyManager init() {
        String type = null;
        try {
            if(null == store) {
                // Get the key manager factory for the default algorithm.
                final Preferences preferences = PreferencesFactory.get();
                type = preferences.getProperty("connection.ssl.keystore.type");
                if(log.isInfoEnabled()) {
                    log.info(String.format("Load default store of type %s", type));
                }
                if(null == type) {
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
            // Load default key store
            store.load(null, null);
        }
        catch(Exception e) {
            try {
                log.error(String.format("Could not load default store of type %s", type), e);
                if(log.isInfoEnabled()) {
                    log.info("Load default store of default type");
                }
                store = KeyStore.getInstance(KeyStore.getDefaultType());
                store.load(null, null);
            }
            catch(NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException ex) {
                log.error(String.format("Initialization of key store failed %s", e.getMessage()));
            }
        }
        return this;
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        return this.getClientAliases(new String[]{keyType}, issuers);
    }

    public String[] getClientAliases(final String[] keyTypes, final Principal[] issuers) {
        // List of issuer distinguished name
        final List<String> list = new ArrayList<String>();
        try {
            final Enumeration<String> aliases = store.aliases();
            while(aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Alias in Keychain %s", alias));
                }
                if(store.isKeyEntry(alias)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Found private key for %s", alias));
                    }
                    // returns the first element of the certificate chain of that key entry
                    final Certificate cert = this.getCertificate(alias, keyTypes, issuers);
                    if(null == cert) {
                        log.warn(String.format("Failed to retrieve certificate for alias %s", alias));
                        continue;
                    }
                    log.info(String.format("Add X509 certificate entry %s to list", cert));
                    list.add(alias);
                }
                else {
                    log.warn(String.format("Missing private key for alias %s", alias));
                }
            }
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        if(list.isEmpty()) {
            // null if there were no matches
            return null;
        }
        return list.toArray(new String[list.size()]);
    }

    public X509Certificate getCertificate(final String alias, final String[] keyTypes, final Principal[] issuers) {
        try {
            final Certificate cert = store.getCertificate(alias);
            if(this.matches(cert, keyTypes, issuers)) {
                return (X509Certificate) cert;
            }
            for(Certificate c : store.getCertificateChain(alias)) {
                if(c instanceof X509Certificate) {
                    if(this.matches(c, keyTypes, issuers)) {
                        return (X509Certificate) cert;
                    }
                }
            }
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("No matching certificate found for alias %s and issuers %s",
                    alias, Arrays.toString(issuers)));
        }
        return null;
    }

    /**
     * @param issuers The list of acceptable CA issuer subject names or null if it does not matter which issuers are used
     * @return True if certificate matches issuer and key type
     */
    protected boolean matches(final Certificate c, final String[] keyTypes, final Principal[] issuers) {
        if(!(c instanceof X509Certificate)) {
            log.warn(String.format("Certificate %s is not of type X509", c));
            return false;
        }
        if(!Arrays.asList(keyTypes).contains(c.getPublicKey().getAlgorithm())) {
            log.warn(String.format("Key type %s does not match any of %s", c.getPublicKey().getAlgorithm(),
                    Arrays.toString(keyTypes)));
            return false;
        }
        if(null == issuers || Arrays.asList(issuers).isEmpty()) {
            // null if it does not matter which issuers are used
            return true;
        }
        final X500Principal issuer = ((X509Certificate) c).getIssuerX500Principal();
        if(!Arrays.asList(issuers).contains(issuer)) {
            log.warn(String.format("Issuer %s does not match", issuer));
            return false;
        }
        return true;
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        try {
            final X509Certificate selected;
            try {
                final String hostname = socket.getInetAddress().getHostName();
                selected = callback.choose(keyTypes,
                        issuers, hostname, MessageFormat.format(LocaleFactory.localizedString(
                                "The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to {0}."),
                                hostname));
            }
            catch(ConnectionCanceledException e) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("No certificate selected for socket %s", socket));
                }
                return null;
            }
            if(null == selected) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("No certificate selected for socket %s", socket));
                }
                // Disconnect
                return null;
            }
            final String[] aliases = this.getClientAliases(keyTypes, issuers);
            if(null != aliases) {
                for(String alias : aliases) {
                    if(store.getCertificate(alias).equals(selected)) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Selected certificate alias %s for certificate %s", alias, selected));
                        }
                        return alias;
                    }
                }
            }
            log.warn(String.format("No matching alias found for selected certificate %s", selected));
            // Return null if there are no matches
            return null;
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        // Return null if there are no matches
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        try {
            final List<X509Certificate> result = new ArrayList<X509Certificate>();
            final Certificate[] chain = store.getCertificateChain(alias);
            if(null == chain) {
                log.warn(String.format("No certificate chain for alias %s", alias));
                // Return null if the alias can't be found
                return null;
            }
            else {
                for(Certificate cert : chain) {
                    if(cert instanceof X509Certificate) {
                        result.add((X509Certificate) cert);
                    }
                }
            }
            if(result.isEmpty()) {
                log.warn(String.format("No certificate chain for alias %s", alias));
                final Certificate cert = store.getCertificate(alias);
                if(null == cert) {
                    // Return null if the alias can't be found
                    return null;
                }
                if(cert instanceof X509Certificate) {
                    final X509Certificate x509 = (X509Certificate) cert;
                    result.add(x509);
                }
            }
            return result.toArray(new X509Certificate[result.size()]);
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(final String alias) {
        try {
            if(store.isKeyEntry(alias)) {
                final Key key = store.getKey(alias, "null".toCharArray());
                if(key instanceof PrivateKey) {
                    return (PrivateKey) key;
                }
                else {
                    log.warn(String.format("Key %s for alias %s is not a private key", key, alias));
                }
            }
            else {
                log.warn(String.format("Alias %s is not a key entry", alias));
            }
        }
        catch(KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        log.warn(String.format("No private key for alias %s", alias));
        // Return null if the alias can't be found
        return null;
    }
}
