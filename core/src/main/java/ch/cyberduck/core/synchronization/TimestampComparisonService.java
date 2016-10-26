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

import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.CalendarService;
import ch.cyberduck.core.date.Instant;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;

import java.util.TimeZone;

public class TimestampComparisonService implements ComparisonService {
    private static final Logger log = Logger.getLogger(TimestampComparisonService.class);

    private final CalendarService calendarService;

    public TimestampComparisonService(final TimeZone tz) {
        this.calendarService = new CalendarService(tz);
    }

    @Override
    public Comparison compare(final PathAttributes remote, final LocalAttributes local) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Compare timestamp for %s with %s", remote, local));
        }
        if(-1 == remote.getModificationDate()) {
            log.warn(String.format("No remote modification date available for comparison for %s", remote));
            return Comparison.local;
        }
        if(-1 == local.getModificationDate()) {
            log.warn(String.format("No local modification date available for comparison for %s", local));
            return Comparison.remote;
        }
        if(calendarService.asDate(local.getModificationDate(), Instant.SECOND).before(
                calendarService.asDate(remote.getModificationDate(), Instant.SECOND))) {
            return Comparison.remote;
        }
        if(calendarService.asDate(local.getModificationDate(), Instant.SECOND).after(
                calendarService.asDate(remote.getModificationDate(), Instant.SECOND))) {
            return Comparison.local;
        }
        // Same timestamp
        return Comparison.equal;
    }
}
