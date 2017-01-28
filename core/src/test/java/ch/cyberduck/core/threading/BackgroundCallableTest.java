/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.threading;

import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class BackgroundCallableTest {

    @Test
    public void testCallReturnValueFailureRetry() throws Exception {
        final Object expected = new Object();

        final BackgroundCallable<Object> c = new BackgroundCallable<>(new AbstractBackgroundAction<Object>() {
            final AtomicBoolean exception = new AtomicBoolean();

            @Override
            public Object run() throws BackgroundException {
                try {
                    if(!exception.get()) {
                        throw new BackgroundException();
                    }
                }
                finally {
                    exception.set(true);
                }
                return expected;
            }

            @Override
            public boolean alert(final BackgroundException e) {
                // Retry enabled
                return true;
            }
        }, new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }
        }, new BackgroundActionRegistry());
        assertSame(expected, c.call());
    }

    @Test
    public void testCallReturnValueFailureRetryDisabled() throws Exception {
        final BackgroundCallable<Object> c = new BackgroundCallable<>(new AbstractBackgroundAction<Object>() {
            final AtomicBoolean exception = new AtomicBoolean();

            @Override
            public Object run() throws BackgroundException {
                try {
                    if(!exception.get()) {
                        throw new BackgroundException();
                    }
                }
                finally {
                    exception.set(true);
                }
                return new Object();
            }

            @Override
            public boolean alert(final BackgroundException e) {
                return false;
            }
        }, new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        }, new BackgroundActionRegistry());
        assertNull(c.call());
    }

    @Test
    public void testCallReturnValueFailureRetryEnabled() throws Exception {
        final Object expected = new Object();
        final BackgroundCallable<Object> c = new BackgroundCallable<>(new AbstractBackgroundAction<Object>() {
            final AtomicBoolean exception = new AtomicBoolean();
            final AtomicBoolean retry = new AtomicBoolean();
            final AtomicBoolean finish = new AtomicBoolean();
            final AtomicBoolean cleanup = new AtomicBoolean();

            @Override
            public Object run() throws BackgroundException {
                try {
                    if(!exception.get()) {
                        throw new BackgroundException();
                    }
                }
                finally {
                    exception.set(true);
                }
                return expected;
            }

            @Override
            public void finish() {
                if(finish.get()) {
                    fail();
                }
                finish.set(true);
            }

            @Override
            public void cleanup() {
                if(cleanup.get()) {
                    fail();
                }
                cleanup.set(true);
            }

            @Override
            public boolean alert(final BackgroundException e) {
                try {
                    if(!retry.get()) {
                        return true;
                    }
                }
                finally {
                    retry.set(true);
                }
                return false;
            }
        }, new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        }, new BackgroundActionRegistry());
        assertEquals(expected, c.call());
    }

    @Test
    public void testCallInvokeCleanup() throws Exception {
        final Object run = new Object();
        final Object finish = new Object();
        final Object cleanup = new Object();
        final Stack<Object> stack = new Stack<>();

        final BackgroundCallable<Object> c = new BackgroundCallable<>(new AbstractBackgroundAction<Object>() {
            @Override
            public Object run() throws BackgroundException {
                stack.push(run);
                return run;
            }

            @Override
            public void finish() {
                stack.push(finish);
            }

            @Override
            public void cleanup() {
                stack.push(cleanup);
            }
        }, new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        }, new BackgroundActionRegistry());
        assertSame(run, c.call());
        assertEquals(cleanup, stack.pop());
        assertEquals(finish, stack.pop());
        assertEquals(run, stack.pop());
    }
}