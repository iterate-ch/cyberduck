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

public class AccessDeniedException extends BackgroundException {
    private static final long serialVersionUID = 1479727475235108160L;

    public AccessDeniedException() {
        super();
    }

    public AccessDeniedException(final String detail) {
        super(LocaleFactory.localizedString("Access denied", "Credentials"), detail);
    }

    public AccessDeniedException(final String detail, final Throwable cause) {
        super(LocaleFactory.localizedString("Access denied", "Credentials"), detail, cause);
    }

    @Override
    public String getHelp() {
        return LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support");
    }
}
