package ch.cyberduck.core;

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

import org.apache.log4j.Logger;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public class Keychain {
    private static Logger log = Logger.getLogger(Keychain.class);

    private static Keychain instance = null;

    static {
        Native.load("Keychain");
    }

    private static final Object lock = new Object();

    public static Keychain instance() {
        synchronized(lock) {
            if(null == instance) {
                instance = new Keychain();
            }
            return instance;
        }
    }

    /**
     * @param protocol
     * @param serviceName
     * @param user
     * @return
     */
    public synchronized native String getInternetPasswordFromKeychain(String protocol, int port, String serviceName, String user);

    /**
     * @param serviceName
     * @param user
     * @return
     * @deprecated Use #getInternetPasswordFromKeychain
     */
    @Deprecated
    public synchronized native String getPasswordFromKeychain(String serviceName, String user);

    /**
     * @param serviceName
     * @param user
     * @param password
     * @deprecated Use #addInternetPasswordToKeychain
     */
    @Deprecated
    public synchronized native void addPasswordToKeychain(String serviceName, String user, String password);

    /**
     * @param protocol
     * @param port
     * @param serviceName
     * @param user
     * @param password
     */
    public synchronized native void addInternetPasswordToKeychain(String protocol, int port, String serviceName, String user, String password);

    /**
     * @param certs
     * @return
     */
    private Object[] getEncoded(X509Certificate[] certs) {
        final Object[] encoded = new Object[certs.length];
        for(int i = 0; i < encoded.length; i++) {
            try {
                encoded[i] = certs[i].getEncoded();
            }
            catch(CertificateEncodingException c) {
                log.error("Error getting encoded certificate: " + c.getMessage());
            }
        }
        return encoded;
    }

    /**
     * @param certs
     * @return
     */
    public synchronized boolean isTrusted(String hostname, X509Certificate[] certs) {
        return this.isTrusted(hostname, this.getEncoded(certs));
    }

    /**
     * @param certificates An array containing byte[] certificates
     * @return
     */
    private native boolean isTrusted(String hostname, Object[] certificates);

    /**
     * @param certificates
     * @return
     */
    public synchronized boolean displayCertificates(X509Certificate[] certificates) {
        return this.displayCertificates(this.getEncoded(certificates));
    }

    /**
     * @param certificates An array containing byte[] certificates
     * @return
     */
    private native boolean displayCertificates(Object[] certificates);
}
