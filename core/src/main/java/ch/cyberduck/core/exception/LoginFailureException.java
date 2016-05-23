package ch.cyberduck.core.exception;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;

public class LoginFailureException extends BackgroundException {
    private static final long serialVersionUID = -7628228280711158915L;

    public LoginFailureException(final String detail) {
        this(detail, null);
    }

    public LoginFailureException(final String detail, final Throwable cause) {
        super(LocaleFactory.localizedString("Login failed", "Credentials"), detail, cause);
    }

    public LoginFailureException(final String message, final String detail, final Throwable cause) {
        super(message, detail, cause);
    }

    @Override
    public String getHelp() {
        return LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support");
    }
}
