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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.DateFormatter;
import ch.cyberduck.ui.DateFormatterFactory;
import ch.cyberduck.ui.cocoa.foundation.NSDate;
import ch.cyberduck.ui.cocoa.foundation.NSDateFormatter;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class UserDefaultsDateFormatter implements DateFormatter {
    private static Logger log = Logger.getLogger(UserDefaultsDateFormatter.class);

    public static void register() {
        DateFormatterFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends DateFormatterFactory {
        @Override
        protected DateFormatter create() {
            return new UserDefaultsDateFormatter();
        }
    }

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

    private static final NSDateFormatter mediumDateFormatter = NSDateFormatter.dateFormatter();

    static {
        mediumDateFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterMediumStyle);
        mediumDateFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterMediumStyle);
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
    public String getShortFormat(final long milliseconds) {
        if(-1 == milliseconds) {
            return Locale.localizedString("Unknown");
        }
        return shortDateFormatter.stringFromDate(toDate(milliseconds));
    }

    public String getMediumFormat(final long milliseconds) {
        return mediumDateFormatter.stringFromDate(toDate(milliseconds));
    }

    /**
     * Date represented as NSUserDefaults.TimeDateFormatString
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return A long format string or "Unknown" if there is a problem converting the time to a string
     */
    public String getLongFormat(final long milliseconds) {
        if(-1 == milliseconds) {
            return Locale.localizedString("Unknown");
        }
        return longDateFormatter.stringFromDate(toDate(milliseconds));
    }
}
