package ch.cyberduck.core.date;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DefaultUserDateFormatter extends AbstractUserDateFormatter {

    private final String tz;

    public DefaultUserDateFormatter(final String tz) {
        this.tz = tz;
    }

    @Override
    public String getShortFormat(final long milliseconds, final boolean natural) {
        if(-1 == milliseconds) {
            return LocaleFactory.localizedString("Unknown");
        }
        final DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        format.setTimeZone(TimeZone.getTimeZone(tz));
        return format.format(milliseconds);
    }

    @Override
    public String getMediumFormat(final long milliseconds, final boolean natural) {
        if(-1 == milliseconds) {
            return LocaleFactory.localizedString("Unknown");
        }
        final DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        format.setTimeZone(TimeZone.getTimeZone(tz));
        return format.format(milliseconds);
    }

    @Override
    public String getLongFormat(final long milliseconds, final boolean natural) {
        if(-1 == milliseconds) {
            return LocaleFactory.localizedString("Unknown");
        }
        final DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        format.setTimeZone(TimeZone.getTimeZone(tz));
        return format.format(milliseconds);
    }
}
