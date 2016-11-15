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
import java.security.cert.X509Certificate;
import java.util.List;

public class DisabledCertificateStore implements CertificateStore {

    @Override
    public boolean isTrusted(final String hostname, List<X509Certificate> certificates) {
        return true;
    }

    @Override
    public boolean display(List<X509Certificate> certificates) {
        return false;
    }

    @Override
    public X509Certificate choose(String[] keyTypes, final Principal[] issuers, final Host bookmark, final String prompt)
            throws ConnectionCanceledException {
        throw new ConnectionCanceledException(prompt);
    }
}
