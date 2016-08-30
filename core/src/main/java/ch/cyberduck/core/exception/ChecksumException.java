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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

public class ChecksumException extends BackgroundException {
    private static final long serialVersionUID = 3459390972588992492L;

    public ChecksumException(final String detail, final Throwable cause) {
        super(detail, cause);
    }

    public ChecksumException(final String message, final String detail) {
        super(message, detail);
    }

    public ChecksumException(final String message, final String detail, final Throwable cause) {
        super(message, detail, cause);
    }
}
