package ch.cyberduck.core.ssl;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import org.apache.log4j.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Default implementation to choose certificates from key store.

 */
public class DefaultX509KeyManager implements X509KeyManager {
    private static final Logger log = Logger.getLogger(DefaultX509KeyManager.class);

    private javax.net.ssl.X509KeyManager manager;

    @Override
    public X509KeyManager init() {
        try {
            // Get the key manager factory for the default algorithm.
            final KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            // Load default key store
            store.load(null);
            // Load default key manager factory using key store
            factory.init(store, null);
            for(KeyManager m : factory.getKeyManagers()) {
                if(m instanceof javax.net.ssl.X509KeyManager) {
                    // Get the first X509KeyManager in the list
                    manager = (javax.net.ssl.X509KeyManager) m;
                    break;
                }
            }
            if(null == manager) {
                throw new NoSuchAlgorithmException(String.format("The default algorithm %s did not produce a X509 Key manager",
                        KeyManagerFactory.getDefaultAlgorithm()));
            }
        }
        catch(IOException | CertificateException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            log.error(String.format("Initialization of key store failed %s", e.getMessage()));
        }
        return this;
    }

    @Override
    public X509Certificate getCertificate(final String alias, final String[] keyTypes, final Principal[] issuers) {
        return null;
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        return manager.getClientAliases(keyType, issuers);
    }

    /**
     * Choose an alias to authenticate the client side of a secure socket given the public key type and the list of
     * certificate issuer authorities recognized by the peer (if any).
     *
     * @param keyType the key algorithm type name(s), ordered with the most-preferred key type first
     * @param issuers the list of acceptable CA issuer subject names or null if it does not matter which issuers are used
     * @param socket  the socket to be used for this connection. This parameter can be null, which indicates that
     *                implementations are free to select an alias applicable to any socket
     */
    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        return manager.chooseClientAlias(keyType, issuers, socket);
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        return manager.getServerAliases(keyType, issuers);
    }

    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        return manager.chooseServerAlias(keyType, issuers, socket);
    }

    /**
     * Returns the certificate chain associated with the given alias.
     */
    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        return manager.getCertificateChain(alias);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return manager.getPrivateKey(alias);
    }
}
