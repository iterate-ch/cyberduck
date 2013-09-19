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

import ch.cyberduck.core.library.Native;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @version $Id$
 */
public final class Keychain extends HostPasswordStore implements PasswordStore, CertificateStore {
    private static final Logger log = Logger.getLogger(Keychain.class);

    public static void register() {
        PasswordStoreFactory.addFactory(Factory.NATIVE_PLATFORM, new PasswordStoreFactory() {
            @Override
            protected HostPasswordStore create() {
                return new Keychain();
            }
        });
        CertificateStoreFactory.addFactory(Factory.NATIVE_PLATFORM, new CertificateStoreFactory() {
            @Override
            protected CertificateStore create() {
                return new Keychain();
            }
        });
    }

    static {
        Native.load("Keychain");
    }

    public Keychain() {
        //
    }

    /**
     * @param protocol    Protocol identifier
     * @param port        Port number
     * @param serviceName Hostname
     * @param user        Username
     * @return Password or null
     */
    public synchronized native String getInternetPasswordFromKeychain(String protocol, int port, String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Username
     * @return Password or null
     */
    public synchronized native String getPasswordFromKeychain(String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Username
     * @param password    Secret
     */
    public synchronized native void addPasswordToKeychain(String serviceName, String user, String password);

    /**
     * @param protocol    Protocol identifier
     * @param port        Port number
     * @param serviceName Hostname
     * @param user        Username
     * @param password    Secret
     */
    public synchronized native void addInternetPasswordToKeychain(String protocol, int port, String serviceName, String user, String password);

    /**
     * @param certificates Chain of certificates
     * @return ASN.1 DER encoded
     */
    private Object[] getEncoded(final List<X509Certificate> certificates) throws CertificateException {
        final Object[] encoded = new Object[certificates.size()];
        int i = 0;
        for(X509Certificate certificate : certificates) {
            encoded[i] = certificate.getEncoded();
            i++;
        }
        return encoded;
    }

    @Override
    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
        return this.getInternetPasswordFromKeychain(scheme.name(), port, hostname, user);
    }

    @Override
    public String getPassword(final String hostname, final String user) {
        return this.getPasswordFromKeychain(hostname, user);
    }

    @Override
    public void addPassword(final String serviceName, String user, final String password) {
        this.addPasswordToKeychain(serviceName, user, password);
    }

    @Override
    public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
        this.addInternetPasswordToKeychain(scheme.name(), port, hostname, user, password);
    }

    /**
     * @param certificates Chain of certificates
     * @return True if chain is trusted
     */
    @Override
    public synchronized boolean isTrusted(final String hostname, final List<X509Certificate> certificates) throws CertificateException {
        return this.isTrustedNative(hostname, this.getEncoded(certificates));
    }

    /**
     * @param hostname     Hostname that must match common name in certificate
     * @param certificates An array containing ASN.1 DER encoded certificates
     * @return True if chain is trusted
     */
    private native boolean isTrustedNative(String hostname, Object[] certificates);

    /**
     * @param certificates Chain of certificates
     * @return True if certificate was selected. False if prompt is dismissed to close the connection
     */
    @Override
    public boolean display(final List<X509Certificate> certificates) throws CertificateException {
        return this.displayCertificatesNative(this.getEncoded(certificates));
    }

    /**
     * @param certificates An array containing ASN.1 DER encoded certificates
     * @return True if certificate was selected. False if prompt is dismissed to close the connection
     */
    private native boolean displayCertificatesNative(Object[] certificates);

    @Override
    public X509Certificate choose(final String[] issuers, final String hostname, final String prompt) {
        byte[] cert = this.chooseCertificateNative(issuers, hostname, prompt);
        if(null == cert) {
            log.info("No certificate selected");
            return null;
        }
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate selected = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(cert));
            if(log.isDebugEnabled()) {
                log.info(String.format("Selected certificate %s", selected));
            }
            return selected;
        }
        catch(CertificateException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param issuers Distinguished names
     * @param prompt  String to display in prompt
     * @return Selected certificate
     */
    private native byte[] chooseCertificateNative(String[] issuers, String hostname, String prompt);
}
