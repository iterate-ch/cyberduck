package ch.cyberduck.core.pool;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Test;

import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatelessSessionPoolTest {

    @Test
    public void testCheckReconnectSocketFailure() throws Exception {
        final AtomicBoolean interrupt = new AtomicBoolean();
        final StatelessSessionPool pool = new StatelessSessionPool(new TestLoginConnectionService() {
            @Override
            public boolean check(final Session<?> session, final Cache<Path> cache, final CancelCallback callback) throws BackgroundException {
                return true;
            }
        }, new NullSession(new Host(new TestProtocol())) {
            @Override
            public void interrupt() throws BackgroundException {
                interrupt.set(true);
                super.interrupt();
            }
        }, PathCache.empty(), new DefaultVaultRegistry(new DisabledPasswordCallback()));
        final Session<?> session = pool.borrow(BackgroundActionState.running);
        pool.release(session, new BackgroundException("m", new SocketException("m")));
        assertTrue(interrupt.get());
    }

    @Test
    public void testCheckReconnectApplicationFailure() throws Exception {
        final AtomicBoolean interrupt = new AtomicBoolean();
        final StatelessSessionPool pool = new StatelessSessionPool(new TestLoginConnectionService() {
            @Override
            public boolean check(final Session<?> session, final Cache<Path> cache, final CancelCallback callback) throws BackgroundException {
                return true;
            }
        }, new NullSession(new Host(new TestProtocol())) {
            @Override
            public void interrupt() throws BackgroundException {
                interrupt.set(true);
                super.interrupt();
            }
        }, PathCache.empty(), new DefaultVaultRegistry(new DisabledPasswordCallback()));
        final Session<?> session = pool.borrow(BackgroundActionState.running);
        pool.release(session, new BackgroundException("m", "d"));
        assertFalse(interrupt.get());
    }
}