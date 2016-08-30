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

import ch.cyberduck.binding.foundation.NSDate;
import ch.cyberduck.binding.foundation.NSDateFormatter;
import ch.cyberduck.binding.foundation.NSLocale;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.date.AbstractUserDateFormatter;
import ch.cyberduck.core.date.UserDateFormatter;

public class UserDefaultsDateFormatter extends AbstractUserDateFormatter implements UserDateFormatter {

    private static final NSLocale locale = NSLocale.currentLocale();
    //NSLocale.createWithLocaleIdentifier(NSLocale.canonicalLocaleIdentifierFromString(PreferencesFactory.get().locale()));

    private static NSLocale locale() {
        return locale;
    }

    private static final NSDateFormatter longDateFormatter = NSDateFormatter.dateFormatter();

    static {
        longDateFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterLongStyle);
        longDateFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterLongStyle);
        longDateFormatter.setLocale(locale());
    }

    private static final NSDateFormatter shortDateFormatter = NSDateFormatter.dateFormatter();

    static {
        shortDateFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterShortStyle);
        shortDateFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterShortStyle);
        shortDateFormatter.setLocale(locale());
    }

    private static final NSDateFormatter mediumDateFormatter = NSDateFormatter.dateFormatter();

    static {
        mediumDateFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterMediumStyle);
        mediumDateFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterMediumStyle);
        mediumDateFormatter.setLocale(locale());
    }

    private static final NSDateFormatter longDateNaturalFormatter = NSDateFormatter.dateFormatter();

    static {
        longDateNaturalFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterLongStyle);
        longDateNaturalFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterLongStyle);
        longDateNaturalFormatter.setLocale(locale());
        longDateNaturalFormatter.setDoesRelativeDateFormatting(true);
    }

    private static final NSDateFormatter shortDateNaturalFormatter = NSDateFormatter.dateFormatter();

    static {
        shortDateNaturalFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterShortStyle);
        shortDateNaturalFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterShortStyle);
        shortDateNaturalFormatter.setLocale(locale());
        shortDateNaturalFormatter.setDoesRelativeDateFormatting(true);
    }

    private static final NSDateFormatter mediumDateNaturalFormatter = NSDateFormatter.dateFormatter();

    static {
        mediumDateNaturalFormatter.setDateStyle(NSDateFormatter.kCFDateFormatterMediumStyle);
        mediumDateNaturalFormatter.setTimeStyle(NSDateFormatter.kCFDateFormatterMediumStyle);
        mediumDateNaturalFormatter.setLocale(locale());
        mediumDateNaturalFormatter.setDoesRelativeDateFormatting(true);
    }

    /**
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @return Seconds since the first instant of 1 January 2001, GMT
     */
    private static NSDate toDate(long milliseconds) {
        // first convert to seconds instead of milliseconds
        return NSDate.dateWithTimeIntervalSince1970(milliseconds / 1000d);
    }

    /**
     * Date represented as NSDateFormatter.kCFDateFormatterShortStyle
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @param natural      Use natural language
     * @return A short format string or "Unknown" if there is a problem converting the time to a string
     */
    @Override
    public String getShortFormat(final long milliseconds, boolean natural) {
        synchronized(shortDateFormatter) {
            if(-1 == milliseconds) {
                return LocaleFactory.localizedString("Unknown");
            }
            if(natural) {
                return shortDateNaturalFormatter.stringFromDate(toDate(milliseconds));
            }
            return shortDateFormatter.stringFromDate(toDate(milliseconds));
        }
    }

    /**
     * Date represented as NSDateFormatter.kCFDateFormatterMediumStyle
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @param natural      Use natural language
     * @return A medium format string or "Unknown" if there is a problem converting the time to a string
     */
    @Override
    public String getMediumFormat(final long milliseconds, boolean natural) {
        synchronized(mediumDateFormatter) {
            if(-1 == milliseconds) {
                return LocaleFactory.localizedString("Unknown");
            }
            if(natural) {
                return mediumDateNaturalFormatter.stringFromDate(toDate(milliseconds));
            }
            return mediumDateFormatter.stringFromDate(toDate(milliseconds));
        }
    }

    /**
     * Date represented as NSDateFormatter.kCFDateFormatterLongStyle
     *
     * @param milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT
     * @param natural      Use natural language
     * @return A long format string or "Unknown" if there is a problem converting the time to a string
     */
    @Override
    public String getLongFormat(final long milliseconds, boolean natural) {
        synchronized(longDateFormatter) {
            if(-1 == milliseconds) {
                return LocaleFactory.localizedString("Unknown");
            }
            if(natural) {
                return longDateNaturalFormatter.stringFromDate(toDate(milliseconds));
            }
            return longDateFormatter.stringFromDate(toDate(milliseconds));
        }
    }
}
