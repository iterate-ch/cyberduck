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

    private TrustManagerHostnameCallback callback;

    private KeyStore keyStore;

    private CertificateStore certificateStore;

    public CertificateStoreX509KeyManager(final TrustManagerHostnameCallback callback,
                                          final CertificateStore certificateStore) {
        this.callback = callback;
        this.certificateStore = certificateStore;
    }

    public X509KeyManager init() throws IOException {
        try {
            // Get the key manager factory for the default algorithm.
            keyStore = KeyStore.getInstance("KeychainStore", "Apple");
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
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        try {
            // List of issuer distinguished name
            final List<String> list = new ArrayList<String>();
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
                        final X500Principal issuer = ((X509Certificate) cert).getIssuerX500Principal();
                        log.info(String.format("Add X509 certificate entry with issuer %s to list", issuer.getName()));
                        if(Arrays.asList(issuers).contains(issuer)) {
                            list.add(issuer.getName());
                        }
                    }
                }
            }
            final String hostname = callback.getTarget();
            final X509Certificate selected;
            try {
                selected = certificateStore.choose(list.toArray(new String[list.size()]), hostname,
                        MessageFormat.format(LocaleFactory.localizedString("Select the certificate to use when connecting to {0}."), hostname));
            }
            catch(ConnectionCanceledException e) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("No certificate selected for hostname %s", hostname));
                }
                // Return null if there are no matches
                return null;
            }
            final String alias = keyStore.getCertificateAlias(selected);
            log.info(String.format("Selected certificate alias %s for certificate %s", alias, selected));
            return alias;
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
                    result.add((X509Certificate) cert);
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
}
