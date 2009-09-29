package ch.cyberduck.core;

import java.security.cert.X509Certificate;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

/**
 * @version $Id:$
 */
public abstract class AbstractKeychain {

    /**
     * @param protocol
     * @param serviceName
     * @param user
     * @return
     */
    public abstract String getPassword(String protocol, int port, String serviceName, String user);

    /**
     * @param serviceName
     * @param user
     * @return
     */
    public abstract String getPassword(String serviceName, String user);

    /**
     * @param serviceName
     * @param user
     * @param password
     */
    public abstract void addPassword(String serviceName, String user, String password);

    /**
     * @param protocol
     * @param port
     * @param serviceName
     * @param user
     * @param password
     */
    public abstract void addPassword(String protocol, int port, String serviceName, String user, String password);

    /**
     * @param certs Certificate chain
     * @return True if trusted in Keychain
     */
    public abstract boolean isTrusted(String hostname, X509Certificate[] certs);

    /**
     * @param certificates
     * @return
     */
    public abstract boolean displayCertificates(X509Certificate[] certificates);
}
