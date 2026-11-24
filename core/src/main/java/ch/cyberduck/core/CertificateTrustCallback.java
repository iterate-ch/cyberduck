package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.security.cert.X509Certificate;
import java.util.List;

public interface CertificateTrustCallback {

    /**
     * Prompt user to evaluate trust in certificate
     *
     * @param message      Reason why the certificate is not trusted and question to connect anyway. Implementations
     *                     using the native trust panel of the platform that generates the text itself evaluate trust
     *                     for the hostname given instead
     * @param certificates Certificate chain
     * @throws ConnectionCanceledException Prompt dismissed by user
     */
    void prompt(String message, List<X509Certificate> certificates) throws ConnectionCanceledException;
}
