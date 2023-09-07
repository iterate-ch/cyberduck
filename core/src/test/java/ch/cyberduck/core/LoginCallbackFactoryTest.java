package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.MainAction;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoginCallbackFactoryTest {

    @Test
    public void testCreate() {
        assertNotNull(LoginCallbackFactory.get(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {

            }
        }));
    }

    @Test
    public void testCreateOrder() {
        final MemoryPreferences preferences = new MemoryPreferences();
        PreferencesFactory.set(preferences);
        preferences.setDefault("factory.logincallback.class", TestLoginCallback.class.getName());
        assertEquals(DisabledLoginCallback.class, LoginCallbackFactory.get(new BaseController()).getClass());
        assertEquals(TestLoginCallback.class, LoginCallbackFactory.get(new TestBrowserController()).getClass());
        assertEquals(DisabledLoginCallback.class, LoginCallbackFactory.get(new BaseController()).getClass());
    }

    final static class BaseController extends AbstractController {
        @Override
        public void invoke(MainAction runnable, boolean wait) {
        }
    }

    final static class TestBrowserController extends AbstractController {
        @Override
        public void invoke(MainAction runnable, boolean wait) {
        }
    }

    final static class TestLoginCallback implements LoginCallback {
        public TestLoginCallback(TestBrowserController controller) {
        }

        @Override
        public void warn(final Host bookmark, final String title, final String message, final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
        }

        @Override
        public void await(final CountDownLatch signal, final Host bookmark, final String title, final String message) throws ConnectionCanceledException {
        }

        @Override
        public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
            return null;
        }

        @Override
        public Local select(final Local identity) throws LoginCanceledException {
            return null;
        }

        @Override
        public void close(final String input) {
        }

        @Override
        public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
            return null;
        }
    }

}