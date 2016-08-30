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

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class DefaultCertificateStore implements CertificateStore {

    private final DefaultHostnameVerifier verifier
            = new DefaultHostnameVerifier();

    @Override
    public X509Certificate choose(final String[] keyTypes, final Principal[] issuers,
                                  final String hostname, final String prompt) throws ConnectionCanceledException {
        final CertificateStoreX509KeyManager store;
        try {
            store = new KeychainX509KeyManager(this).init();
        }
        catch(IOException e) {
            throw new ConnectionCanceledException(e);
        }
        final String[] aliases = store.getClientAliases(keyTypes, issuers);
        if(null == aliases) {
            throw new ConnectionCanceledException(String.format("No certificate matching issuer %s found",
                    Arrays.toString(issuers)));
        }
        for(String alias : aliases) {
            return store.getCertificate(alias, keyTypes, issuers);
        }
        return null;
    }

    @Override
    public boolean display(List<X509Certificate> certificates) {
        return false;
    }

    @Override
    public boolean isTrusted(final String hostname, final List<X509Certificate> certificates) {
        if(certificates.isEmpty()) {
            return false;
        }
        for(X509Certificate c : certificates) {
            // Checks that the certificate is currently valid.
            try {
                c.checkValidity();
            }
            catch(CertificateExpiredException e) {
                return false;
            }
            catch(CertificateNotYetValidException e) {
                return false;
            }
        }
        try {
            verifier.verify(hostname, certificates.get(0));
        }
        catch(SSLException e) {
            return false;
        }
        return true;
    }
}
