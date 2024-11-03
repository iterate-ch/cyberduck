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
        if(WinHttpClients.isWinAuthAvailable()) {
            if(!host.getCredentials().validate(host.getProtocol(), new LoginOptions(host.getProtocol()).password(false))) {
                final String nameSamCompatible = CurrentWindowsCredentials.INSTANCE.getName();
                final Credentials credentials = new Credentials(host.getCredentials())
                        .withPassword(CurrentWindowsCredentials.INSTANCE.getPassword());
                if(!includeDomain && StringUtils.contains(nameSamCompatible, '\\')) {
                    credentials.setUsername(StringUtils.split(nameSamCompatible, '\\')[1]);
                }
                else {
                    credentials.setUsername(nameSamCompatible);
                }
                log.debug("Configure {} with username {}", host, credentials);
                return credentials;
            }
        }
        return CredentialsConfigurator.DISABLED.configure(host);
    }

    @Override
    public CredentialsConfigurator reload() throws LoginCanceledException {
        return this;
    }
}
