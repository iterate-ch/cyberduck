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
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation for certificate trust settings.
 *
 * @version $Id$
 */
public abstract class AbstractX509TrustManager implements X509TrustManager, X509KeyManager {
    protected static Logger log = Logger.getLogger(AbstractX509TrustManager.class);

    /**
     * A set of all X509 certificates accepted by the user that contains
     * no duplicate elements
     */
    private Set<X509Certificate> accepted
            = new HashSet<X509Certificate>();

    protected void acceptCertificate(final X509Certificate[] certs) {
        if(log.isDebugEnabled()) {
            for(X509Certificate cert : certs) {
                log.debug("Certificate trusted:" + cert.toString());
            }
        }
        accepted.addAll(Arrays.asList(certs));
    }

    protected void acceptCertificate(final X509Certificate cert) {
        if(log.isDebugEnabled()) {
            log.debug("Certificate trusted:" + cert.toString());
        }
        accepted.add(cert);
    }

    /**
     * @return All accepted certificates
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.accepted.toArray(new X509Certificate[this.accepted.size()]);
    }

    private static X509KeyManager manager;

    private X509KeyManager init() {
        if(null == manager) {
            try {
                // Get the key manager factory for the default algorithm.
                KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
                // Load default key store
                store.load(null);
                // Load default key manager factory using key store
                factory.init(store, null);
                for(KeyManager keyManager : factory.getKeyManagers()) {
                    if(keyManager instanceof X509KeyManager) {
                        // Get the first X509KeyManager in the list
                        manager = (X509KeyManager) keyManager;
                        break;
                    }
                }
                if(null == manager) {
                    throw new NoSuchAlgorithmException("The default algorithm :" +
                            KeyManagerFactory.getDefaultAlgorithm() + " did not produce a X509 Key manager");
                }
            }
            catch(CertificateException e) {
                log.error(e.getMessage());
            }
            catch(UnrecoverableKeyException e) {
                log.error(e.getMessage());
            }
            catch(NoSuchAlgorithmException e) {
                log.error(e.getMessage());
            }
            catch(KeyStoreException e) {
                log.error(e.getMessage());
            }
            catch(IOException e) {
                log.error(e.getMessage());
            }
        }
        return manager;
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return this.init().getClientAliases(keyType, issuers);
    }

    /**
     * Choose an alias to authenticate the client side of a secure socket given the public key type and the list of
     * certificate issuer authorities recognized by the peer (if any).
     *
     * @param keyType
     * @param issuers
     * @param socket
     * @return
     */
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return this.init().chooseClientAlias(keyType, issuers, socket);
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return this.init().getServerAliases(keyType, issuers);
    }

    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return this.init().chooseServerAlias(keyType, issuers, socket);
    }

    /**
     * Returns the certificate chain associated with the given alias.
     *
     * @param alias
     * @return
     */
    public X509Certificate[] getCertificateChain(String alias) {
        return this.init().getCertificateChain(alias);
    }

    public PrivateKey getPrivateKey(String alias) {
        return this.init().getPrivateKey(alias);
    }
}
