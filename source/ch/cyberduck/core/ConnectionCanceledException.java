package ch.cyberduck.core;

import ch.cyberduck.core.i18n.Locale;

import java.io.IOException;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

/**
 * To be used if a connection attempt is interrupted by the user
 *
 * @version $Id$
 */
public class ConnectionCanceledException extends IOException {
    private static final long serialVersionUID = 1731598032382782206L;

    public ConnectionCanceledException() {
        super(Locale.localizedString("Connection attempt canceled", "Credentials"));
    }

    public ConnectionCanceledException(Throwable cause) {
        super(Locale.localizedString("Connection attempt canceled", "Credentials"), cause);
    }

    public ConnectionCanceledException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConnectionCanceledException(String s) {
        super(s);
    }
}
