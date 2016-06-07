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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public abstract class HostPasswordStore implements PasswordStore {
    private static final Logger log = Logger.getLogger(HostPasswordStore.class);

    /**
     * @param host Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    public String find(final Host host) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching password from keychain for %s", host));
        }
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        final Credentials credentials = host.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        String p;
        if(credentials.isPublicKeyAuthentication()) {
            final Local key = credentials.getIdentity();
            p = this.getPassword(host.getHostname(), key.getAbbreviatedPath());
            if(null == p) {
                // Interoperability with OpenSSH (ssh, ssh-agent, ssh-add)
                p = this.getPassword("SSH", key.getAbsolute());
            }
            if(null == p) {
                // Backward compatibility
                p = this.getPassword("SSHKeychain", key.getAbbreviatedPath());
            }
        }
        else {
            p = this.getPassword(host.getProtocol().getScheme(), host.getPort(),
                    host.getHostname(), credentials.getUsername());
        }
        if(null == p) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Password not found in keychain for %s", host));
            }
        }
        return p;
    }

    /**
     * Adds the password to the login keychain
     *
     * @param host Hostname
     * @see ch.cyberduck.core.Host#getCredentials()
     */
    public void save(final Host host) {
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return;
        }
        final Credentials credentials = host.getCredentials();
        if(!credentials.isSaved()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip writing credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn(String.format("No username in credentials for host %s", host.getHostname()));
            return;
        }
        if(StringUtils.isEmpty(credentials.getPassword())) {
            log.warn(String.format("No password in credentials for host %s", host.getHostname()));
            return;
        }
        if(credentials.isAnonymousLogin()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Do not write anonymous credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Add password for host %s", host));
        }
        if(credentials.isPublicKeyAuthentication()) {
            this.addPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath(),
                    credentials.getPassword());
        }
        else {
            this.addPassword(host.getProtocol().getScheme(), host.getPort(),
                    host.getHostname(), credentials.getUsername(), credentials.getPassword());
        }
    }
}
