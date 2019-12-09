package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.library.Native;

import org.apache.log4j.Logger;

public final class KeychainPasswordStore extends DefaultHostPasswordStore implements PasswordStore {
    private static final Logger log = Logger.getLogger(KeychainPasswordStore.class);

    static {
        Native.load("core");
    }

    /**
     * @param protocol    Protocol identifier
     * @param port        Port number
     * @param serviceName Hostname
     * @param user        Username
     * @return Password or null
     */
    public synchronized native String getInternetPasswordFromKeychain(String protocol, int port, String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Username
     * @return Password or null
     */
    public synchronized native String getPasswordFromKeychain(String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Username
     * @param password    Secret
     */
    public synchronized native boolean addPasswordToKeychain(String serviceName, String user, String password);

    /**
     * @param protocol    Protocol identifier
     * @param port        Port number
     * @param serviceName Hostname
     * @param user        Username
     * @param password    Secret
     */
    public synchronized native boolean addInternetPasswordToKeychain(String protocol, int port, String serviceName, String user, String password);

    @Override
    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
        return this.getInternetPasswordFromKeychain(scheme.name(), port, hostname, user);
    }

    @Override
    public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) throws LocalAccessDeniedException {
        if(!this.addInternetPasswordToKeychain(scheme.name(), port, hostname, user, password)) {
            throw new LocalAccessDeniedException(String.format("Failure saving credentials for %s in Keychain", user));
        }
    }

    @Override
    public String getPassword(final String serviceName, final String accountName) {
        return this.getPasswordFromKeychain(serviceName, accountName);
    }

    @Override
    public void addPassword(final String serviceName, final String accountName, final String password) throws LocalAccessDeniedException {
        if(!this.addPasswordToKeychain(serviceName, accountName, password)) {
            throw new LocalAccessDeniedException(String.format("Failure saving credentials for %s in Keychain", accountName));
        }
    }
}
