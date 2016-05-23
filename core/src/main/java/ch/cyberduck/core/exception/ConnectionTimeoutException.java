package ch.cyberduck.core.exception;

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

import ch.cyberduck.core.LocaleFactory;

public class ConnectionTimeoutException extends BackgroundException {
    private static final long serialVersionUID = -7408164299912691588L;

    public ConnectionTimeoutException(final String detail, final Throwable cause) {
        super(LocaleFactory.localizedString("Connection timed out", "Error"), detail, cause);
    }

    public ConnectionTimeoutException(final String message, final String detail, final Throwable cause) {
        super(message, detail, cause);
    }

    @Override
    public String getHelp() {
        return LocaleFactory.localizedString("The connection attempt timed out. The server may be down, " +
                "or your network may not be properly configured.", "Support");
    }
}
