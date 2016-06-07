package ch.cyberduck.core.identity;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;

import org.apache.commons.lang3.StringUtils;

public class DefaultCredentialsIdentityConfiguration extends AbstractIdentityConfiguration {

    private Host host;

    private PasswordStore store;

    public DefaultCredentialsIdentityConfiguration(final Host host) {
        this.host = host;
        this.store = PasswordStoreFactory.get();
    }

    public DefaultCredentialsIdentityConfiguration(final Host host, final PasswordStore store) {
        this.host = host;
        this.store = store;
    }

    @Override
    public Credentials getCredentials(final String username) {
        // Ignore user but use username from host credentials
        final String user = host.getCredentials().getUsername();
        final String password = store.getPassword(host.getProtocol().getScheme(), host.getPort(), host.getHostname(), user);
        if(StringUtils.isEmpty(password)) {
            return null;
        }
        return new Credentials(user, password);
    }
}