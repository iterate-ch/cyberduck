package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.HostPreferences;

public class DAVWindowsIntegratedCredentialsConfigurator implements CredentialsConfigurator {

    private final CredentialsConfigurator proxy;

    public DAVWindowsIntegratedCredentialsConfigurator(final CredentialsConfigurator proxy) {
        this.proxy = proxy;
    }

    @Override
    public Credentials configure(final Host host) {
        if(new HostPreferences(host).getBoolean("webdav.ntlm.windows.authentication.enable")) {
            return proxy.configure(host);
        }
        return CredentialsConfigurator.DISABLED.configure(host);
    }

    @Override
    public CredentialsConfigurator reload() throws LoginCanceledException {
        return proxy.reload();
    }
}
