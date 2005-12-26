package ch.cyberduck.core.ftps;

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

/**
 * @version $Id$
 */
public class IgnoreX509TrustManager extends AbstractX509TrustManager {
    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, java.lang.String string)
            throws java.security.cert.CertificateException {
        //ignore
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, java.lang.String string)
            throws java.security.cert.CertificateException {
        //ignore
    }
}