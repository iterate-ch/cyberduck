package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.LoginCanceledException;

public class DisabledLoginCallback extends DisabledConnectionCallback implements LoginCallback {

    @Override
    public Credentials prompt(final Host bookmark, final String username, final String title, final String reason,
                              final LoginOptions options) throws LoginCanceledException {
        throw new LoginCanceledException();
    }

    @Override
    public Local select(final Local identity) throws LoginCanceledException {
        throw new LoginCanceledException();
    }
}
