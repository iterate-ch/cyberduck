package ch.cyberduck.core.synchronization;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.CalendarService;
import ch.cyberduck.core.date.Instant;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class TimestampComparisonService implements ComparisonService {
    private static final Logger log = Logger.getLogger(CombinedComparisionService.class);

    private CalendarService calendarService;

    public TimestampComparisonService(final TimeZone tz) {
        this.calendarService = new CalendarService(tz);
    }

    @Override
    public Comparison compare(final Path file) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Compare timestamp for %s", file));
        }
        final PathAttributes attributes = file.attributes();
        if(-1 == attributes.getModificationDate()) {
            log.warn(String.format("No remote modification date available for comparison for %s", file));
            return Comparison.local;
        }
        if(-1 == file.getLocal().attributes().getModificationDate()) {
            log.warn(String.format("No local modification date available for comparison for %s", file));
            return Comparison.remote;
        }
        final Calendar remote = calendarService.asDate(attributes.getModificationDate(), Instant.SECOND);
        final Calendar local = calendarService.asDate(file.getLocal().attributes().getModificationDate(), Instant.SECOND);
        if(local.before(remote)) {
            return Comparison.remote;
        }
        if(local.after(remote)) {
            return Comparison.local;
        }
        // Same timestamp
        return Comparison.equal;
    }
}
