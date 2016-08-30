package ch.cyberduck.core.ssl;

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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class DEREncoder implements CertificateEncoder {

    /**
     * @param certificates Chain of certificates
     * @return ASN.1 DER encoded
     */
    @Override
    public Object[] encode(final List<X509Certificate> certificates) throws CertificateException {
        final Object[] encoded = new Object[certificates.size()];
        for(int i = 0; i < certificates.size(); i++) {
            encoded[i] = certificates.get(i).getEncoded();
        }
        return encoded;
    }
}
