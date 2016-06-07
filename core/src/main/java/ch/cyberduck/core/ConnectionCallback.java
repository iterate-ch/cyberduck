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

public interface ConnectionCallback {
    /**
     * Display warning sheet. Block connection until decision is made.
     *
     * @param title         Title in alert window
     * @param message       Message in alert window
     * @param defaultButton Button title for default button
     * @param cancelButton  Button title for other button
     * @param preference    Where to save preference if dismissed
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException If the other option has been selected.
     */
    void warn(Protocol protocol, String title, String message, String defaultButton, String cancelButton,
              String preference) throws ConnectionCanceledException;

}
