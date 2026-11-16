package ch.cyberduck.core.ssl;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.CertificateStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PKCS11CertificateStoreX509KeyManager extends CertificateStoreX509KeyManager {

    public PKCS11CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                final CertificateStore store, final LoginCallback login) {
        this(prompt, bookmark, store, login, HostPreferencesFactory.get(bookmark).getProperty("connection.ssl.keystore.pkcs11.library"));
    }

    /**
     * @param library Native PKCS11 library name or path
     */
    public PKCS11CertificateStoreX509KeyManager(final CertificateIdentityCallback prompt, final Host bookmark,
                                                final CertificateStore store, final LoginCallback login,
                                                final String library) {
        super(prompt, bookmark, store, PKCS11KeyStore.build(library, bookmark, login));
    }

    /**
     * Prompt with certificates from token which are not known to the system certificate store
     */
    @Override
    protected X509Certificate choose(final String[] keyTypes, final Principal[] issuers) throws ConnectionCanceledException {
        final List<X509Certificate> certificates = new ArrayList<>();
        final String[] aliases = this.getClientAliases(keyTypes, issuers);
        if(null != aliases) {
            for(String alias : aliases) {
                final X509Certificate certificate = this.getCertificate(alias, keyTypes, issuers);
                if(null != certificate) {
                    certificates.add(certificate);
                }
            }
        }
        if(certificates.isEmpty()) {
            throw new ConnectionCanceledException(String.format("No certificate matching issuer %s found", Arrays.toString(issuers)));
        }
        return prompt.prompt(bookmark.getHostname(), certificates);
    }
}
