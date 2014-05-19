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

import org.apache.commons.lang3.ArrayUtils;
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
public class CertificateStoreX509KeyManager implements X509KeyManager {
    private static final Logger log = Logger.getLogger(CertificateStoreX509KeyManager.class);

    private KeyStore keyStore;

    private CertificateStore chooseCallback;

    private TrustManagerHostnameCallback hostnameCallback;

    public CertificateStoreX509KeyManager(final TrustManagerHostnameCallback hostname,
                                          final CertificateStore callback) {
        this.chooseCallback = callback;
        this.hostnameCallback = hostname;
    }

    public X509KeyManager init() throws IOException {
        try {
            // Get the key manager factory for the default algorithm.
            if(null == Preferences.instance().getProperty("connection.ssl.keystore.type")) {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            }
            else {
                keyStore = KeyStore.getInstance(Preferences.instance().getProperty("connection.ssl.keystore.type"),
                        Preferences.instance().getProperty("connection.ssl.keystore.provider"));
            }
            // Load default key store
            keyStore.load(null, null);
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
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        // List of issuer distinguished name
        final List<String> list = new ArrayList<String>();
        try {
            final Enumeration<String> aliases = keyStore.aliases();
            while(aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Alias in Keychain %s", alias));
                }
                if(keyStore.isKeyEntry(alias)) {
                    log.info(String.format("Private key for alias %s", alias));
                    continue;
                }
                if(keyStore.isCertificateEntry(alias)) {
                    final Certificate cert = keyStore.getCertificate(alias);
                    if(null == cert) {
                        log.warn(String.format("Failed to retrieve certificate for alias %s", alias));
                        continue;
                    }
                    if(cert instanceof X509Certificate) {
                        final X509Certificate x509 = (X509Certificate) cert;
                        if(!Arrays.asList(keyType).contains(x509.getPublicKey().getAlgorithm())) {
                            continue;
                        }
                        final X500Principal issuer = x509.getSubjectX500Principal();
                        if(!Arrays.asList(issuers).contains(issuer)) {
                            continue;
                        }
                        log.info(String.format("Add X509 certificate entry with issuer %s to list", issuer.getName()));
                        list.add(issuer.getName());
                    }
                }
            }
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket) {
        try {
            for(String keyType : keyTypes) {
                final String[] aliases = this.getClientAliases(keyType, issuers);
                final X509Certificate selected;
                try {
                    selected = chooseCallback.choose(aliases, hostnameCallback.getTarget(),
                            MessageFormat.format(LocaleFactory.localizedString(
                                    "Select the certificate to use when connecting to {0}."), hostnameCallback.getTarget()));
                }
                catch(ConnectionCanceledException e) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("No certificate selected for hostname %s", hostnameCallback.getTarget()));
                    }
                    return null;
                }
                if(null == selected) {
                    continue;
                }
                final String alias = keyStore.getCertificateAlias(selected);
                log.info(String.format("Selected certificate alias %s for certificate %s", alias, selected));
                return alias;
            }
            // Return null if there are no matches
            return null;
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        try {
            final List<X509Certificate> result = new ArrayList<X509Certificate>();
            final Certificate[] chain = keyStore.getCertificateChain(alias);
            if(null == chain) {
                log.warn(String.format("No certificate chain for alias %s", alias));
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
                final Certificate cert = keyStore.getCertificate(alias);
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
            if(keyStore.isKeyEntry(alias)) {
                final Key key = keyStore.getKey(alias, "null".toCharArray());
                if(key instanceof PrivateKey) {
                    return (PrivateKey) key;
                }
                else {
                    log.warn(String.format("Key %s for alias %s is not a private key", key, alias));
                }
            }
            else {
                log.warn(String.format("Alias %s is not a key entry", alias));
                // Return null if the alias can't be found
                return null;
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
        return null;
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        return null;
    }
}
