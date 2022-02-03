package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnonymousConnectionService implements ConnectionService {
    private static final Logger log = LogManager.getLogger(AnonymousConnectionService.class);

    @Override
    public boolean check(final Session<?> session, final CancelCallback callback) throws BackgroundException {
        if(session.isConnected()) {
            // Connection already open
            return false;
        }
        this.connect(session, callback);
        return true;
    }

    @Override
    public void connect(final Session<?> session, final CancelCallback cancel) throws BackgroundException {
        session.open(ProxyFactory.get().find(session.getHost().getHostname()),
            new DisabledHostKeyCallback(), new DisabledLoginCallback(), cancel);
    }

    @Override
    public void close(final Session<?> session) throws BackgroundException {
        session.close();
    }
}
