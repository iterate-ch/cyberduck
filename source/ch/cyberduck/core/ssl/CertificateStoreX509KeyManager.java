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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.log4j.Logger;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

/**
 * @version $Id$
 */
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

    public X509KeyManager init() throws IOException {
        try {
            if(null == store) {
                // Get the key manager factory for the default algorithm.
                String type = Preferences.instance().getProperty("connection.ssl.keystore.type");
                if(log.isInfoEnabled()) {
                    log.info(String.format("Load default store of type %s", type));
                }
                if(null == type) {
                    type = KeyStore.getDefaultType();
                }
                store = KeyStore.getInstance(type,
                        Preferences.instance().getProperty("connection.ssl.keystore.provider"));
            }
            // Load default key store
            store.load(null, null);
        }
        catch(CertificateException e) {
            throw new IOException(e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        catch(KeyStoreException e) {
            throw new IOException(e);
        }
        catch(IOException e) {
            throw new IOException(e);
        }
        catch(NoSuchProviderException e) {
            throw new IOException(e);
        }
        return this;
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        if(null == issuers || Arrays.asList(issuers).isEmpty()) {
            log.warn("No issuer subject names provided");
            return null;
        }
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
                    // returns the first element of the certificate chain of that key entry
                    final Certificate cert = this.getCertificate(alias, keyType, issuers);
                    if(null == cert) {
                        log.warn(String.format("Failed to retrieve certificate for alias %s", alias));
                        continue;
                    }
                    log.info(String.format("Add X509 certificate entry %s to list", cert));
                    list.add(alias);
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

    public X509Certificate getCertificate(final String alias, final String keyType, final Principal[] issuers) {
        if(null == issuers || Arrays.asList(issuers).isEmpty()) {
            log.warn("No issuer subject names provided");
            return null;
        }
        try {
            final Certificate cert = store.getCertificate(alias);
            if(cert instanceof X509Certificate) {
                final X509Certificate x509 = (X509Certificate) cert;
                if(!Arrays.asList(keyType).contains(x509.getPublicKey().getAlgorithm())) {
                    log.warn(String.format("Key type %s does not match", x509.getPublicKey().getAlgorithm()));
                    return null;
                }
                final X500Principal issuer = ((X509Certificate) cert).getIssuerX500Principal();
                if(!Arrays.asList(issuers).contains(issuer)) {
                    log.warn(String.format("Issuer %s does not match", issuer));
                    return null;
                }
                return (X509Certificate) cert;
            }
            else {
                log.warn(String.format("Certificate %s is not of type X509", cert));
            }
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        if(null == issuers || Arrays.asList(issuers).isEmpty()) {
            log.warn("No issuer subject names provided");
            return null;
        }
        try {
            for(String keyType : keyTypes) {
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
                    continue;
                }
                final String[] aliases = this.getClientAliases(keyType, issuers);
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
            }
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
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        catch(NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        catch(UnrecoverableKeyException e) {
            log.error(e.getMessage());
        }
        log.warn(String.format("No private key for alias %s", alias));
        // Return null if the alias can't be found
        return null;
    }
}
