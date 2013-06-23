package ch.cyberduck.core.ssl;

/*
*  Copyright (c) 2005 David Kocher. All rights reserved.
*  http://cyberduck.ch/
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  Bug fixes, suggestions and comments should be sent to:
*  dkocher@cyberduck.ch
*/

import ch.cyberduck.core.KeychainFactory;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

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
public class KeychainX509KeyManager extends AbstractX509KeyManager {
    private static Logger log = Logger.getLogger(KeychainX509KeyManager.class);

    private TrustManagerHostnameCallback callback;

    private KeyStore store;

    public KeychainX509KeyManager(TrustManagerHostnameCallback callback) throws IOException {
        this.callback = callback;
        try {
            // Get the key manager factory for the default algorithm.
            store = KeyStore.getInstance("KeychainStore", "Apple");
            // Load default key store
            store.load(null, null);
        }
        catch(CertificateException e) {
            throw new IOException(e.getMessage(), e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new IOException(e.getMessage(), e);
        }
        catch(KeyStoreException e) {
            throw new IOException(e.getMessage(), e);
        }
        catch(IOException e) {
            throw new IOException(e.getMessage(), e);
        }
        catch(NoSuchProviderException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        try {
            // List of issuer distinguished name
            List<String> list = new ArrayList<String>();
            Enumeration<String> aliases = store.aliases();
            while(aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                log.info(String.format("Alias in Keychain %s", alias));
                if(store.isKeyEntry(alias)) {
                    log.info(String.format("Private key for alias %s", alias));
                    continue;
                }
                if(store.isCertificateEntry(alias)) {
                    Certificate cert = store.getCertificate(alias);
                    if(null == cert) {
                        log.warn(String.format("Failed to retrieve certificate for alias %s", alias));
                        continue;
                    }
                    if(cert instanceof X509Certificate) {
                        if(Arrays.asList(issuers).contains(((X509Certificate) cert).getIssuerX500Principal())) {
                            list.add(((X509Certificate) cert).getIssuerX500Principal().getName());
                        }
                    }
                }
            }
            final String hostname = callback.getTarget();
            X509Certificate cert = KeychainFactory.get().chooseCertificate(list.toArray(new String[list.size()]), hostname,
                    MessageFormat.format(Locale.localizedString("Select the certificate to use when connecting to {0}."), hostname));
            if(null == cert) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("No certificate selected for hostname %s", hostname));
                }
                return null;
            }
            String alias = store.getCertificateAlias(cert);
            log.info(String.format("Certificate alias %s choosen", alias));
            return alias;
        }
        catch(KeyStoreException e) {
            log.error(String.format("Keystore not loaded:%s", e.getMessage()));
        }
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        try {
            List<X509Certificate> result = new ArrayList<X509Certificate>();
            Certificate[] chain = store.getCertificateChain(alias);
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
                Certificate cert = store.getCertificate(alias);
                if(cert instanceof X509Certificate) {
                    result.add((X509Certificate) cert);
                }
            }
            return result.toArray(new X509Certificate[result.size()]);
        }
        catch(KeyStoreException e) {
            log.error("Keystore not loaded:" + e.getMessage());
        }
        return null;
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        try {
            if(store.isKeyEntry(alias)) {
                Key key = store.getKey(alias, "null".toCharArray());
                if(key instanceof PrivateKey) {
                    return (PrivateKey) key;
                }
            }
        }
        catch(KeyStoreException e) {
            log.error("Keystore not loaded:" + e.getMessage());
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