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

import ch.cyberduck.binding.Proxy;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;
import ch.cyberduck.core.ssl.DEREncoder;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class Keychain extends HostPasswordStore implements PasswordStore, CertificateStore {
    private static final Logger log = Logger.getLogger(Keychain.class);

    static {
        Native.load("core");
    }

    private final Proxy proxy;

    public Keychain() {
        this(new Proxy());
    }

    public Keychain(final Proxy proxy) {
        this.proxy = proxy;
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

    @Override
    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
        return this.getInternetPasswordFromKeychain(scheme.name(), port, hostname, user);
    }

    @Override
    public String getPassword(final String hostname, final String user) {
        return this.getPasswordFromKeychain(hostname, user);
    }

    @Override
    public void addPassword(final String serviceName, final String user, final String password) {
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
    public boolean isTrusted(final String hostname, final List<X509Certificate> certificates) throws CertificateException {
        if(certificates.isEmpty()) {
            return false;
        }
        final Object[] encoded = new DEREncoder().encode(certificates);
        final AtomicBoolean trusted = new AtomicBoolean(false);
        final DefaultMainAction action = new DefaultMainAction() {
            @Override
            public void run() {
                trusted.set(isTrustedNative(hostname, encoded));
            }
        };
        proxy.invoke(action, action.lock(), true);
        return trusted.get();
    }

    /**
     * @param hostname     Hostname that must match common name in certificate
     * @param certificates An array containing ASN.1 DER encoded certificates
     * @return True if chain is trusted
     */
    private synchronized native boolean isTrustedNative(String hostname, Object[] certificates);

    /**
     * @param certificates Chain of certificates
     * @return True if certificate was selected. False if prompt is dismissed to close the connection
     */
    @Override
    public boolean display(final List<X509Certificate> certificates) throws CertificateException {
        if(certificates.isEmpty()) {
            return false;
        }
        final Object[] encoded = new DEREncoder().encode(certificates);
        final AtomicBoolean accepted = new AtomicBoolean(false);
        final DefaultMainAction action = new DefaultMainAction() {
            @Override
            public void run() {
                accepted.set(displayCertificatesNative(encoded));
            }
        };
        proxy.invoke(action, action.lock(), true);
        return accepted.get();
    }

    /**
     * @param certificates An array containing ASN.1 DER encoded certificates
     * @return True if certificate was selected. False if prompt is dismissed to close the connection
     */
    private native boolean displayCertificatesNative(Object[] certificates);

    @Override
    public X509Certificate choose(final String[] keyTypes, final Principal[] issuers,
                                  final Host bookmark, final String prompt)
            throws ConnectionCanceledException {
        final List<X509Certificate> certificates = new ArrayList<X509Certificate>();
        final CertificateStoreX509KeyManager manager;
        try {
            manager = new KeychainX509KeyManager(bookmark).init();
        }
        catch(IOException e) {
            throw new ConnectionCanceledException(e);
        }
        final String[] aliases = manager.getClientAliases(keyTypes, issuers);
        if(null == aliases) {
            throw new ConnectionCanceledException(String.format("No certificate matching issuer %s found", Arrays.toString(issuers)));
        }
        for(String alias : aliases) {
            certificates.add(manager.getCertificate(alias, keyTypes, issuers));
        }
        try {
            final Object[] encoded = new DEREncoder().encode(certificates);
            final AtomicReference<byte[]> select = new AtomicReference<byte[]>();
            final DefaultMainAction action = new DefaultMainAction() {
                @Override
                public void run() {
                    select.set(chooseCertificateNative(encoded, bookmark.getHostname(), prompt));
                }
            };
            proxy.invoke(action, action.lock(), true);

            if(null == select.get()) {
                if(log.isInfoEnabled()) {
                    log.info("No certificate selected");
                }
                throw new ConnectionCanceledException();
            }
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            final X509Certificate selected = (X509Certificate) factory.generateCertificate(
                    new ByteArrayInputStream(select.get()));
            if(log.isDebugEnabled()) {
                log.info(String.format("Selected certificate %s", selected));
            }
            return selected;
        }
        catch(CertificateException e) {
            throw new ConnectionCanceledException(e);
        }
    }

    /**
     * @param certificates DER encoded certificates to choose from
     * @param prompt       String to display in prompt
     * @return Selected certificate
     */
    private native byte[] chooseCertificateNative(Object[] certificates, String hostname, String prompt);
}
