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

package ch.cyberduck.core.pool;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;

public class SingleSessionPool implements SessionPool {

    private final Session<?> session;

    public SingleSessionPool(final Session<?> session) {
        this.session = session;
    }


    @Override
    public Session<?> borrow() throws BackgroundException {
        return session;
    }

    @Override
    public void release(final Session<?> session) {
        //
    }

    @Override
    public void close() {
        //
    }

    @Override
    public Integer getSize() {
        return 1;
    }

    @Override
    public Integer getNumActive() {
        return 1;
    }

    @Override
    public Integer getNumIdle() {
        return 1;
    }

    @Override
    public Session.State getState() {
        return session.getState();
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        return session.getFeature(type);
    }

    @Override
    public Host getHost() {
        return session.getHost();
    }
}
