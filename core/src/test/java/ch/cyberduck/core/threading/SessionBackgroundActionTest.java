package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestLoginConnectionService;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.pool.StatelessSessionPool;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import org.junit.Test;

import java.net.SocketTimeoutException;

import static org.junit.Assert.*;

public class SessionBackgroundActionTest {

    @Test
    public void testGetExceptionConnectionCanceledException() throws Exception {
        SessionBackgroundAction<Void> a = new SessionBackgroundAction<Void>(new StatelessSessionPool(
                new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol(), "t")), PathCache.empty(), new DefaultVaultRegistry(new DisabledPasswordCallback())), new DisabledAlertCallback() {
        }, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                //
            }
        }
        ) {

            @Override
            public Void run(final Session<?> session) throws BackgroundException {
                throw new ConnectionCanceledException();
            }
        };
        try {
            a.call();
            fail();
        }
        catch(BackgroundException e) {
            // Ignore
        }
        assertFalse(a.hasFailed());
    }

    @Test
    public void testGetExceptionFailure() throws Exception {
        final BackgroundException failure = new BackgroundException(new RuntimeException());
        SessionBackgroundAction<Void> a = new SessionBackgroundAction<Void>(new StatelessSessionPool(
                new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol(), "t")), PathCache.empty(), new DefaultVaultRegistry(new DisabledPasswordCallback())), new AlertCallback() {
            @Override
            public boolean alert(final Host repeatableBackgroundAction, final BackgroundException f, final StringBuilder transcript) {
                assertEquals(failure, f);
                return false;
            }
        }, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                //
            }
        }
        ) {

            @Override
            public Void run(final Session<?> session) throws BackgroundException {
                throw failure;
            }
        };
        try {
            a.call();
            fail();
        }
        catch(BackgroundException e) {
            // Ignore
        }
        assertTrue(a.hasFailed());
    }

    @Test
    public void testGetExceptionLoginCanceledException() throws Exception {
        final BackgroundException failure = new LoginCanceledException();
        SessionBackgroundAction<Void> a = new SessionBackgroundAction<Void>(new StatelessSessionPool(
                new TestLoginConnectionService(), new NullSession(new Host(new TestProtocol(), "t")), PathCache.empty(), new DefaultVaultRegistry(new DisabledPasswordCallback())), new AlertCallback() {
            @Override
            public boolean alert(final Host repeatableBackgroundAction, final BackgroundException f, final StringBuilder transcript) {
                assertEquals(failure, f);
                return false;
            }
        }, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                //
            }
        }
        ) {

            @Override
            public Void run(final Session<?> session) throws BackgroundException {
                throw failure;
            }
        };
        try {
            a.call();
            fail();
        }
        catch(BackgroundException e) {
            // Ignore
        }
        assertFalse(a.hasFailed());
    }

    @Test
    public void testRetrySocket() throws Exception {
        final BackgroundException failure = new BackgroundException(new SocketTimeoutException(""));
        SessionBackgroundAction<Void> a = new SessionBackgroundAction<Void>(new StatelessSessionPool(
                new TestLoginConnectionService(),
                new NullSession(new Host(new TestProtocol(), "t")), PathCache.empty(), new DefaultVaultRegistry(new DisabledPasswordCallback())), new AlertCallback() {
            @Override
            public boolean alert(final Host repeatableBackgroundAction, final BackgroundException f, final StringBuilder transcript) {
                assertEquals(failure, f);
                return false;
            }
        }, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                //
            }
        }
        ) {
            @Override
            public Void run(final Session<?> session) throws BackgroundException {
                throw failure;
            }
        };
    }
}
