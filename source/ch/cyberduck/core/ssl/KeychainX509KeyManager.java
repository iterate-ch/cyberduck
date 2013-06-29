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
import ch.cyberduck.core.CertificateStoreFactory;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public class KeychainX509KeyManager extends CertificateStoreX509KeyManager {
    private static final Logger log = Logger.getLogger(KeychainX509KeyManager.class);

    private CertificateStore store;

    public KeychainX509KeyManager(final TrustManagerHostnameCallback callback) throws IOException {
        super(callback, CertificateStoreFactory.get());
    }
}