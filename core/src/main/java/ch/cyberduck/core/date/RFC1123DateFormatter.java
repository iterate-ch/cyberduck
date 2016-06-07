package ch.cyberduck.core.date;

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

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RFC1123DateFormatter extends AbstractDateFormatter {

    /**
     * Format to RFC 1123 timestamp
     * Expires: Thu, 01 Dec 1994 16:00:00 GMT
     */
    private static final SimpleDateFormat rfc1123 =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ROOT);

    public RFC1123DateFormatter() {
        super(rfc1123);
    }
}