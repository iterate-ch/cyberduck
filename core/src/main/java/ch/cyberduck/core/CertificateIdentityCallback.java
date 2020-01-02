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

public interface CertificateIdentityCallback {

    /**
     * Prompt user to select certificate
     *
     * @param hostname     Hostname
     * @param certificates Available certificates found in store
     * @return Selected certificate with private key
     * @throws ConnectionCanceledException Prompt dismissed by user
     */
    X509Certificate prompt(String hostname, List<X509Certificate> certificates) throws ConnectionCanceledException;
}
