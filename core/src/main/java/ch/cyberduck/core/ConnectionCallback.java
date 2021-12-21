package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.util.concurrent.CountDownLatch;

public interface ConnectionCallback extends PasswordCallback {
    /**
     * Display warning sheet. Block connection until decision is made.
     *
     * @param bookmark      Host
     * @param title         Title in alert window
     * @param message       Message in alert window
     * @param defaultButton Button title for default button
     * @param cancelButton  Button title for other button
     * @param preference    Where to save preference if dismissed
     * @throws ConnectionCanceledException If the other option has been selected.
     */
    void warn(Host bookmark, String title, String message, String defaultButton, String cancelButton,
              String preference) throws ConnectionCanceledException;

    /**
     * Alert with indeterminate progress to await result from background task
     *
     * @param signal   Await signal on latch prior returning
     * @param bookmark Host
     * @param title    Title in alert window
     * @param message  Message in alert window
     * @throws ConnectionCanceledException If await is canceled by the user
     */
    void await(CountDownLatch signal, Host bookmark, String title, String message) throws ConnectionCanceledException;
}
