package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.preferences.PreferencesFactory;

public class TransferTypeFinder {
    public Host.TransferType type(final Session<?> session, final Transfer transfer) {
        switch(session.getTransferType()) {
            case concurrent:
                switch(transfer.getType()) {
                    case copy:
                    case move:
                        break;
                    default:
                        // Setup concurrent worker if not already pooled internally
                        final int connections = PreferencesFactory.get().getInteger("queue.maxtransfers");
                        if(connections > 1) {
                            return Host.TransferType.concurrent;
                        }
                }
        }
        return Host.TransferType.newconnection;
    }
}
