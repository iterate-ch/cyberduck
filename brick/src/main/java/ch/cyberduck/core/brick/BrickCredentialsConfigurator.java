package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrickCredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(BrickCredentialsConfigurator.class);

    @Override
    public Credentials configure(final Host host) {
        final Credentials credentials = new Credentials(host.getCredentials());
        if(credentials.isTokenAuthentication()) {
            log.warn("Skip auto configuration of credentials for {}", host);
            return credentials;
        }
        log.debug("Set new random token for {}", host);
        credentials.setToken(new AlphanumericRandomStringService().random());
        return credentials;
    }

    @Override
    public CredentialsConfigurator reload() throws LoginCanceledException {
        return this;
    }
}
