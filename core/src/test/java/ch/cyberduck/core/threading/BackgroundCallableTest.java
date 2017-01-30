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

        final AbstractBackgroundAction<Object> action = new AbstractBackgroundAction<Object>() {
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
        };
        final BackgroundCallable<Object> c = new BackgroundCallable<>(action, new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }
        }, new BackgroundActionRegistry());
        assertSame(expected, c.call());
        assertFalse(action.isRunning());
        assertFalse(action.isCanceled());
    }

    @Test
    public void testCallReturnValueFailureRetryDisabled() throws Exception {
        final Object prepare = new Object();
        final Object run = new Object();
        final Object finish = new Object();
        final Object cleanup = new Object();
        final Stack<Object> stack = new Stack<>();

        final AbstractBackgroundAction<Object> action = new AbstractBackgroundAction<Object>() {
            final AtomicBoolean exception = new AtomicBoolean();

            @Override
            public void prepare() {
                stack.push(prepare);
                super.prepare();
            }

            @Override
            public Object run() throws BackgroundException {
                stack.push(run);
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
            public void finish() {
                stack.push(finish);
                super.finish();
            }

            @Override
            public void cleanup() {
                stack.push(cleanup);
                super.cleanup();
            }

            @Override
            public boolean alert(final BackgroundException e) {
                return false;
            }
        };
        final BackgroundCallable<Object> c = new BackgroundCallable<>(action, new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        }, new BackgroundActionRegistry());
        assertNull(c.call());
        assertFalse(action.isRunning());
        assertFalse(action.isCanceled());
        assertEquals(4, stack.size());
        assertEquals(cleanup, stack.pop());
        assertEquals(finish, stack.pop());
        assertEquals(run, stack.pop());
        assertEquals(prepare, stack.pop());
    }

    @Test
    public void testCallReturnValueFailureRetryEnabled() throws Exception {
        final Object prepare = new Object();
        final Object run = new Object();
        final Object finish = new Object();
        final Object cleanup = new Object();
        final Stack<Object> stack = new Stack<>();

        final Object expected = new Object();
        final AbstractBackgroundAction<Object> action = new AbstractBackgroundAction<Object>() {
            final AtomicBoolean exception = new AtomicBoolean();
            final AtomicBoolean retry_a = new AtomicBoolean();
            final AtomicBoolean finish_a = new AtomicBoolean();
            final AtomicBoolean cleanup_a = new AtomicBoolean();

            @Override
            public void prepare() {
                stack.push(prepare);
                super.prepare();
            }

            @Override
            public Object run() throws BackgroundException {
                stack.push(run);
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
                stack.push(finish);
                if(finish_a.get()) {
                    fail();
                }
                finish_a.set(true);
                super.finish();
            }

            @Override
            public void cleanup() {
                stack.push(cleanup);
                if(cleanup_a.get()) {
                    fail();
                }
                cleanup_a.set(true);
                super.cleanup();
            }

            @Override
            public boolean alert(final BackgroundException e) {
                try {
                    if(!retry_a.get()) {
                        return true;
                    }
                }
                finally {
                    retry_a.set(true);
                }
                return false;
            }
        };
        final BackgroundCallable<Object> c = new BackgroundCallable<>(action, new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                runnable.run();
            }
        }, new BackgroundActionRegistry());
        assertEquals(expected, c.call());
        assertFalse(action.isRunning());
        assertFalse(action.isCanceled());
        assertEquals(5, stack.size());
        assertEquals(cleanup, stack.pop());
        assertEquals(finish, stack.pop());
        assertEquals(run, stack.pop());
        assertEquals(run, stack.pop());
        assertEquals(prepare, stack.pop());
    }

    @Test
    public void testCallInvokeCleanup() throws Exception {
        final Object prepare = new Object();
        final Object run = new Object();
        final Object finish = new Object();
        final Object cleanup = new Object();
        final Stack<Object> stack = new Stack<>();

        final BackgroundCallable<Object> c = new BackgroundCallable<>(new AbstractBackgroundAction<Object>() {
            @Override
            public void prepare() {
                stack.push(prepare);
                super.prepare();
            }

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
        assertEquals(4, stack.size());
        assertEquals(cleanup, stack.pop());
        assertEquals(finish, stack.pop());
        assertEquals(run, stack.pop());
        assertEquals(prepare, stack.pop());
    }
}