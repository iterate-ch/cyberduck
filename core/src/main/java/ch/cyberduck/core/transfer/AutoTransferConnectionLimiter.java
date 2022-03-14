package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class AutoTransferConnectionLimiter implements TransferConnectionLimiter {

    private final Preferences preferences = PreferencesFactory.get();

    @Override
    public int getLimit(final Host host) {
        switch(Host.TransferType.getType(host)) {
            case newconnection:
                return 1;
        }
        if(TransferConnectionLimiter.AUTO == preferences.getInteger("queue.connections.limit")) {
            // Determine number of connections depending on protocol
            final int limit = preferences.getInteger(String.format("queue.connections.limit.%s",
                    host.getProtocol().getType().name()));
            if(-1 == limit) {
                // No explicit setting for protocol
                return preferences.getInteger("queue.connections.limit.default");
            }
            return limit;
        }
        // Custom explicit user setting
        return preferences.getInteger("queue.connections.limit");
    }
}
