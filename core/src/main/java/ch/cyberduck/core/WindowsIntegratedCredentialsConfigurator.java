package ch.cyberduck.core;/*
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

import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.http.impl.auth.win.CurrentWindowsCredentials;
import org.apache.http.impl.client.WinHttpClients;

public class WindowsIntegratedCredentialsConfigurator implements CredentialsConfigurator {

    @Override
    public Credentials configure(final Host host) {
        if(new HostPreferences(host).getBoolean("webdav.ntlm.windows.authentication.enable")) {
            if(WinHttpClients.isWinAuthAvailable()) {
                if(!host.getCredentials().validate(host.getProtocol(), new LoginOptions(host.getProtocol()).password(false))) {
                    return new Credentials(host.getCredentials())
                            .withUsername(CurrentWindowsCredentials.INSTANCE.getName())
                            .withPassword(CurrentWindowsCredentials.INSTANCE.getPassword());
                }
            }
        }
        return host.getCredentials();
    }

    @Override
    public CredentialsConfigurator reload() {
        return this;
    }
}
