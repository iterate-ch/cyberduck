package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.IOException;
import java.net.InetAddress;

public class DefaultInetAddressReachability implements Reachability {

    private Preferences preferences
            = PreferencesFactory.get();

    @Override
    public boolean isReachable(final Host host) {
        try {
            return InetAddress.getByName(host.getHostname()).isReachable(
                    preferences.getInteger("connection.timeout.seconds") * 1000
            );
        }
        catch(IOException e) {
            return false;
        }
    }

    @Override
    public void diagnose(final Host host) {
        // Not implemented
    }
}
