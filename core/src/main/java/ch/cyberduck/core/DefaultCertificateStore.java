package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class DefaultCertificateStore implements CertificateStore {
    private static final Logger log = LogManager.getLogger(DefaultCertificateStore.class);

    /**
     * Trust manager to validate chain against trust anchors
     */
    private final X509TrustManager trust;

    /**
     * Required by factory. Validate against default trust anchors of the JVM
     */
    public DefaultCertificateStore() {
        this(getDefaultTrustManager());
    }

    public DefaultCertificateStore(final X509TrustManager trust) {
        this.trust = trust;
    }

    private final DefaultHostnameVerifier verifier
            = new DefaultHostnameVerifier();

    @Override
    public X509Certificate choose(final CertificateIdentityCallback prompt, final String[] keyTypes, final Principal[] issuers, final Host bookmark) throws ConnectionCanceledException {
        final CertificateStoreX509KeyManager store = new KeychainX509KeyManager(prompt, bookmark, this).init();
        final String[] aliases = store.getClientAliases(keyTypes, issuers);
        if(null == aliases) {
            throw new ConnectionCanceledException(String.format("No certificate matching issuer %s found",
                    Arrays.toString(issuers)));
        }
        for(String alias : aliases) {
            final X509Certificate certificate = store.getCertificate(alias, keyTypes, issuers);
            if(null == certificate) {
                continue;
            }
            return certificate;
        }
        throw new ConnectionCanceledException(String.format("No certificate matching aliases %s found",
                Arrays.toString(aliases)));
    }

    @Override
    public boolean verify(final CertificateTrustCallback prompt, final String hostname, final List<X509Certificate> certificates) {
        if(certificates.isEmpty()) {
            return false;
        }
        // Run all checks and prompt once with all failures
        final List<String> failures = new ArrayList<>();
        for(X509Certificate c : certificates) {
            // Checks that the certificate is currently valid.
            try {
                c.checkValidity();
            }
            catch(CertificateExpiredException e) {
                failures.add(LocaleFactory.localizedString("The certificate for this server has expired.", "Keychain"));
                break;
            }
            catch(CertificateNotYetValidException e) {
                failures.add(LocaleFactory.localizedString("The certificate for this server is not yet valid.", "Keychain"));
                break;
            }
        }
        try {
            verifier.verify(hostname, certificates.get(0));
        }
        catch(SSLException e) {
            failures.add(LocaleFactory.localizedString("The certificate for this server is invalid.", "Keychain"));
        }
        // Validate chain against trust anchors
        try {
            // Key exchange algorithm is not known. Not evaluated for path validation
            trust.checkServerTrusted(certificates.toArray(new X509Certificate[0]), "UNKNOWN");
        }
        catch(CertificateException e) {
            log.warn("Certificate chain not trusted: {}", e.getMessage());
            failures.add(LocaleFactory.localizedString("The certificate for this server was signed by an unknown certifying authority.", "Keychain"));
        }
        if(failures.isEmpty()) {
            return true;
        }
        failures.add(MessageFormat.format(LocaleFactory.localizedString(
            "You might be connecting to a server that is pretending to be {0} which could put your confidential information at risk. Would you like to connect to the server anyway?", "Keychain"),
            hostname));
        return this.confirm(prompt, String.join(" ", failures), certificates);
    }

    /**
     * @param message Reason why the certificate is not trusted
     * @return True if user accepted to connect anyway
     */
    private boolean confirm(final CertificateTrustCallback prompt, final String message, final List<X509Certificate> certificates) {
        try {
            prompt.prompt(message, certificates);
            return true;
        }
        catch(ConnectionCanceledException e) {
            return false;
        }
    }

    private static X509TrustManager getDefaultTrustManager() {
        try {
            final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            for(TrustManager manager : factory.getTrustManagers()) {
                if(manager instanceof X509TrustManager) {
                    return (X509TrustManager) manager;
                }
            }
            throw new IllegalStateException("No default X509 trust manager available");
        }
        catch(NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException(e);
        }
    }
}
