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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * @version $Id$
 */
public class Keychain extends AbstractKeychain {
    private static Logger log = Logger.getLogger(Keychain.class);

    public static void register() {
        KeychainFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends KeychainFactory {
        @Override
        protected AbstractKeychain create() {
            return new Keychain();
        }
    }

    private Keychain() {
        ;
    }

    private static boolean JNI_LOADED = false;

    /**
     * Load native library extensions
     *
     * @return
     */
    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Keychain");
        }
        return JNI_LOADED;
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
     */
    public synchronized native String getPasswordFromKeychain(String serviceName, String user);

    /**
     * @param serviceName
     * @param user
     * @param password
     */
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

    @Override
    public String getPassword(String protocol, int port, String serviceName, String user) {
        if(!loadNative()) {
            return null;
        }
        return this.getInternetPasswordFromKeychain(protocol, port, serviceName, user);
    }

    @Override
    public String getPassword(String serviceName, String user) {
        if(!loadNative()) {
            return null;
        }
        return this.getPasswordFromKeychain(serviceName, user);
    }

    @Override
    public void addPassword(String serviceName, String user, String password) {
        if(!loadNative()) {
            return;
        }
        this.addPasswordToKeychain(serviceName, user, password);
    }

    @Override
    public void addPassword(String protocol, int port, String serviceName, String user, String password) {
        if(!loadNative()) {
            return;
        }
        this.addInternetPasswordToKeychain(protocol, port, serviceName, user, password);
    }

    /**
     * @param certs
     * @return
     */
    @Override
    public synchronized boolean isTrusted(String hostname, X509Certificate[] certs) {
        if(!loadNative()) {
            return false;
        }
        return this.isTrustedNative(hostname, this.getEncoded(certs));
    }

    /**
     * @param certificates An array containing byte[] certificates
     * @return
     */
    private native boolean isTrustedNative(String hostname, Object[] certificates);

    /**
     * @param certificates
     * @return
     */
    @Override
    public synchronized boolean displayCertificates(X509Certificate[] certificates) {
        if(!loadNative()) {
            return false;
        }
        return this.displayCertificatesNative(this.getEncoded(certificates));
    }

    /**
     * @param certificates An array containing byte[] certificates
     * @return
     */
    private native boolean displayCertificatesNative(Object[] certificates);

    @Override
    public synchronized X509Certificate chooseCertificate(String[] issuers, String prompt) {
        if(!loadNative()) {
            return null;
        }
        byte[] cert = this.chooseCertificateNative(issuers, prompt);
        if(null == cert) {
            log.info("No certificate selected");
            return null;
        }
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate selected = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(cert));
            if(log.isDebugEnabled()) {
                log.info("Selected certificate:" + selected);
            }
            return selected;
        }
        catch(CertificateException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * @param prompt
     * @return
     */
    private native byte[] chooseCertificateNative(String[] issuers, String prompt);
}
