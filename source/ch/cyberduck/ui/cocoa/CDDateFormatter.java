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

import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDDateFormatter {
    private static Logger log = Logger.getLogger(CDDateFormatter.class);

    /**
     * TimeDateFormatString set in the system preferences
     */
    private static final NSGregorianDateFormatter longDateFormatter
            = new NSGregorianDateFormatter((String) NSUserDefaults.standardUserDefaults().objectForKey(
            NSUserDefaults.TimeDateFormatString), false);

    /**
     * ShortTimeDateFormatString set in the system preferences
     */
    private static final NSGregorianDateFormatter shortDateFormatter
            = new NSGregorianDateFormatter((String) NSUserDefaults.standardUserDefaults().objectForKey(
            NSUserDefaults.ShortTimeDateFormatString), false);

    /**
     * Will be a negative value
     */
    private static double SECONDS_1970_TO_2001
            = NSDate.DateFor1970.timeIntervalSinceReferenceDate();

    /**
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return Seconds since the first instant of 1 January 2001, GMT
     */
    private static double convertReferenceFrom1970To2001(long milliseconds) {
        // first convert to seconds instead of milliseconds
        double secondsFrom1970 = NSDate.millisecondsToTimeInterval(milliseconds);
        return secondsFrom1970 + SECONDS_1970_TO_2001;
    }

    /**
     * Modification date represented as NSUserDefaults.ShortTimeDateFormatString
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return A short format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getShortFormat(final long milliseconds) {
        return getShortFormat(convertReferenceFrom1970To2001(milliseconds), NSTimeZone.defaultTimeZone());
    }

    /**
     * Modification date represented as NSUserDefaults.ShortTimeDateFormatString
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @param timezone
     * @return A short format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getShortFormat(final long milliseconds, final NSTimeZone timezone) {
        return getShortFormat(convertReferenceFrom1970To2001(milliseconds), timezone);
    }

    /**
     * Modification date represented as NSUserDefaults.ShortTimeDateFormatString
     *
     * @param seconds Seconds since the first instant of 1 January 2001, GMT
     * @return A short format string or "Unknown" if there is a problem converting the time to a string
     * @see com.apple.cocoa.foundation.NSFormatter.FormattingException
     */
    public static String getShortFormat(final double seconds, final NSTimeZone timezone) {
        try {
            // If you do not specify a time zone for an object at initialization time,
            // NSGregorianDate uses the default time zone for the locale.
            return shortDateFormatter.stringForObjectValue(
                    // Creates a new Gregorian date initialized to the absolute
                    // reference date (the first instant of 1 January 2001, GMT) plus seconds,
                    new NSGregorianDate(seconds, timezone));
        }
        catch(NSFormatter.FormattingException e) {
            // If an NSAttributedString cannot be created for anObject,
            // an NSFormatter.FormattingException is thrown.
            log.error(e.getMessage());
        }
        return NSBundle.localizedString("Unknown", "");
    }

    /**
     * Date represented as NSUserDefaults.TimeDateFormatString
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return A long format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getLongFormat(final long milliseconds) {
        return getLongFormat(milliseconds, NSTimeZone.defaultTimeZone());
    }

    /**
     * Date represented as NSUserDefaults.TimeDateFormatString
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return A long format string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getLongFormat(final long milliseconds, final NSTimeZone timezone) {
        return getLongFormat(convertReferenceFrom1970To2001(milliseconds), timezone);
    }

    /**
     * Date represented as NSUserDefaults.TimeDateFormatString
     *
     * @param seconds  Seconds since the first instant of 1 January 2001, GMT
     * @param timezone
     * @return A long format string or "Unknown" if there is a problem converting the time to a string
     * @see com.apple.cocoa.foundation.NSFormatter.FormattingException
     */
    public static String getLongFormat(final double seconds, final NSTimeZone timezone) {
        try {
            return longDateFormatter.stringForObjectValue(
                    // Creates a new Gregorian date initialized to the absolute
                    // reference date (the first instant of 1 January 2001, GMT) plus seconds,
                    new NSGregorianDate(seconds, timezone));
        }
        catch(NSFormatter.FormattingException e) {
            log.error(e.getMessage());
        }
        return NSBundle.localizedString("Unknown", "");
    }
}
