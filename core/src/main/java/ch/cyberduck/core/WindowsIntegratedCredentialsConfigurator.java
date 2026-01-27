package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.auth.win.CurrentWindowsCredentials;
import org.apache.http.impl.client.WinHttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WindowsIntegratedCredentialsConfigurator implements CredentialsConfigurator {
    private static final Logger log = LogManager.getLogger(WindowsIntegratedCredentialsConfigurator.class);

    private final boolean includeDomain;

    public WindowsIntegratedCredentialsConfigurator() {
        this(false);
    }

    public WindowsIntegratedCredentialsConfigurator(final boolean includeDomain) {
        this.includeDomain = includeDomain;
    }

    @Override
    public Credentials configure(final Host host) {
        final Credentials credentials = new Credentials(host.getCredentials());
        if(!WinHttpClients.isWinAuthAvailable()) {
            log.warn("No Windows authentication available");
            return credentials;
        }
        if(credentials.validate(host.getProtocol(), new LoginOptions(host.getProtocol()).password(false))) {
            log.warn("Skip auto configuration of credentials for {}", host);
            return credentials;
        }
        // No username preset
        final String nameSamCompatible = CurrentWindowsCredentials.INSTANCE.getName();
                credentials.setPassword(CurrentWindowsCredentials.INSTANCE.getPassword());
        if(!includeDomain && StringUtils.contains(nameSamCompatible, '\\')) {
            credentials.setUsername(StringUtils.split(nameSamCompatible, '\\')[1]);
        }
        else {
            credentials.setUsername(nameSamCompatible);
        }
        log.debug("Configure {} with username {}", host, credentials);
        return credentials;
    }

    @Override
    public CredentialsConfigurator reload() throws LoginCanceledException {
        return this;
    }
}
