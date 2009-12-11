package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.cocoa.foundation.NSDate;
import ch.cyberduck.ui.cocoa.foundation.NSDateFormatter;
import ch.cyberduck.ui.cocoa.foundation.NSTimeZone;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class DateFormatter {
    private static Logger log = Logger.getLogger(DateFormatter.class);

    /**
     * TimeDateFormatString set in the system preferences
     */
    private static final NSDateFormatter longDateFormatter = NSDateFormatter.dateFormatter();

    static {
        longDateFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterLongStyle);
        longDateFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterLongStyle);
    }

    /**
     * ShortTimeDateFormatString set in the system preferences
     */
    private static final NSDateFormatter shortDateFormatter = NSDateFormatter.dateFormatter();

    static {
        shortDateFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterShortStyle);
        shortDateFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterShortStyle);
    }

    /**
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return Seconds since the first instant of 1 January 2001, GMT
     */
    private static NSDate toDate(long milliseconds) {
        // first convert to seconds instead of milliseconds
        return NSDate.dateWithTimeIntervalSince1970(milliseconds / 1000);
    }

    /**
     * Modification date represented as NSUserDefaults.ShortTimeDateFormatString
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return A short format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getShortFormat(final long milliseconds) {
        return getShortFormat(toDate(milliseconds), NSTimeZone.systemTimeZone());
    }

    /**
     * Modification date represented as NSUserDefaults.ShortTimeDateFormatString
     *
     * @return A short format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getShortFormat(final NSDate date, final NSTimeZone timezone) {
        //shortDateFormatter.setTimeZone(timezone);
        return shortDateFormatter.stringFromDate(date);
    }

    /**
     * Date represented as NSUserDefaults.TimeDateFormatString
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return A long format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getLongFormat(final long milliseconds) {
        return getLongFormat(toDate(milliseconds), NSTimeZone.systemTimeZone());
    }

    /**
     * Date represented as NSUserDefaults.TimeDateFormatString
     *
     * @return A long format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getLongFormat(final NSDate date, final NSTimeZone timezone) {
        //longDateFormatter.setTimeZone(timezone);
        return longDateFormatter.stringFromDate(date);
    }
}
