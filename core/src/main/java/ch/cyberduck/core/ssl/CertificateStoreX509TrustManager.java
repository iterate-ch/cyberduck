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

import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.LocaleFactory;

import org.apache.log4j.Logger;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class CertificateStoreX509TrustManager extends AbstractX509TrustManager {
    private static Logger log = Logger.getLogger(CertificateStoreX509TrustManager.class);

    private TrustManagerHostnameCallback callback;

    private CertificateStore store;

    public CertificateStoreX509TrustManager(final TrustManagerHostnameCallback callback, final CertificateStore store) {
        this.callback = callback;
        this.store = store;
    }

    @Override
    public X509TrustManager init() {
        return this;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String cipher)
            throws CertificateException {

        this.verify(x509Certificates, cipher);
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String cipher)
            throws CertificateException {

        this.verify(x509Certificates, cipher);
    }

    private void verify(final X509Certificate[] certs, final String cipher) throws CertificateException {
        this.verify(callback.getTarget(), certs, cipher);
    }

    @Override
    public void verify(final String hostname, final X509Certificate[] certs, final String cipher) throws CertificateException {
        if(Arrays.asList(this.getAcceptedIssuers()).containsAll(Arrays.asList(certs))) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Certificate for %s previously trusted", hostname));
            }
            return;
        }
        if(store.isTrusted(hostname, Arrays.asList(certs))) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Certificate for %s trusted in Keychain", hostname));
            }
            // We still accept the certificate if we find it in the Keychain
            // regardless of its trust settings. There is currently no way I am
            // aware of to read the trust settings for a certificate in the Keychain
            this.accept(Arrays.asList(certs));
        }
        else {
            // The certificate has not been trusted
            throw new CertificateException(
                    LocaleFactory.localizedString("No trusted certificate found", "Status"));
        }
    }
}