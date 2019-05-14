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

import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.log4j.Logger;

public class DisabledConnectionCallback implements ConnectionCallback {
    private static final Logger log = Logger.getLogger(DisabledConnectionCallback.class);

    @Override
    public void warn(final Host bookmark, final String title, final String message,
                     final String continueButton, final String disconnectButton, final String preference) {
        log.warn(String.format("Ignore prompt %s for %s", message, bookmark));
    }

    @Override
    public void close(final String input) {
        //
    }

    @Override
    public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        throw new LoginCanceledException();
    }
}
