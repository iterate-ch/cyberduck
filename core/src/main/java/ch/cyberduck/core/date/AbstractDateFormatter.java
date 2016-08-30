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

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public abstract class AbstractDateFormatter implements DateFormatter {

    private final DateFormat format;

    protected AbstractDateFormatter(final DateFormat format) {
        this.format = format;
    }

    @Override
    public String format(final Date input, final TimeZone zone) {
        synchronized(format) {
            format.setTimeZone(zone);
            return format.format(input);
        }
    }

    @Override
    public String format(final long milliseconds, final TimeZone zone) {
        synchronized(format) {
            format.setTimeZone(zone);
            return format.format(milliseconds);
        }
    }

    @Override
    public Date parse(final String input) throws InvalidDateException {
        if(StringUtils.isBlank(input)) {
            throw new InvalidDateException();
        }
        synchronized(format) {
            try {
                return format.parse(input);
            }
            catch(ParseException e) {
                throw new InvalidDateException(e.getMessage(), e);
            }
        }
    }
}
