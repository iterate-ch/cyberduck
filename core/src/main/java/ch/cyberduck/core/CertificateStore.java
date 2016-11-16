package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public interface CertificateStore {

    /**
     * @param hostname     Hostname
     * @param certificates Certificate chain
     * @return True if trusted in Keychain
     */
    boolean isTrusted(String hostname, List<X509Certificate> certificates)
            throws CertificateException;

    /**
     * @param certificates X.509 certificates
     * @return False if display is not possible
     */
    boolean display(List<X509Certificate> certificates)
            throws CertificateException;

    /**
     * Prompt user for client certificate
     *
     * @param keyTypes Encryption algorithms
     * @param issuers  Distinguished names. X500 Principal with distinguished name as in RFC 2253
     * @param bookmark Client hostname
     * @param prompt   Display in certificate choose prompt
     * @return Null if no certificate selected
     */
    X509Certificate choose(String[] keyTypes, Principal[] issuers, Host bookmark, String prompt)
            throws ConnectionCanceledException;
}
