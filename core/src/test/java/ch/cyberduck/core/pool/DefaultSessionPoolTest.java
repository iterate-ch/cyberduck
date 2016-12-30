package ch.cyberduck.core.pool;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Test;

public class DefaultSessionPoolTest {

    @Test(expected = ConnectionCanceledException.class)
    public void testShutdown() throws Exception {
        final DefaultSessionPool pool = new DefaultSessionPool(new TestLoginConnectionService(), new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DefaultVaultRegistry(new DisabledPasswordCallback()), PathCache.empty(), new DisabledProgressListener(), new Host(new TestProtocol()));
        pool.shutdown();
        pool.borrow(BackgroundActionState.running);
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectRefuse() throws Exception {
        final DefaultSessionPool pool = new DefaultSessionPool(new TestLoginConnectionService() {
            @Override
            public boolean check(final Session session, final Cache<Path> cache) throws BackgroundException {
                throw new ConnectionRefusedException("t", new RuntimeException());
            }
        }, new DisabledX509TrustManager(), new DefaultX509KeyManager(),
                new DefaultVaultRegistry(new DisabledPasswordCallback()), PathCache.empty(), new DisabledProgressListener(), new Host(new TestProtocol(), "t"));
        pool.borrow(BackgroundActionState.running);
    }
}