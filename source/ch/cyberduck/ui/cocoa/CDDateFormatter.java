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
     * Modification date represented as NSUserDefaults.ShortTimeDateFormatString
     * @return The cal as a short string or "Unknown" if there is a problem converting the time to a string
     */
    public static String getShortFormat(final long time) {
        try {
            return shortDateFormatter.stringForObjectValue(
                    new NSGregorianDate((double) time / 1000, NSDate.DateFor1970));
//                            (NSTimeZone)NSTimeZone.timeZoneWithName(timezone.getID(), false)));
        }
        catch (NSFormatter.FormattingException e) {
            log.error(e.getMessage());
        }
        return NSBundle.localizedString("Unknown", "");
    }

    /**
     * Modification date represented as NSUserDefaults.TimeDateFormatString
     * @return the modification date of this file or null if there is a problem converting the time to a string
     */
    public static String getLongFormat(final long time) {
        try {
            return longDateFormatter.stringForObjectValue(
                    new NSGregorianDate((double) time / 1000, NSDate.DateFor1970));
//                            (NSTimeZone)NSTimeZone.timeZoneWithName(timezone.getID(), false)));
        }
        catch (NSFormatter.FormattingException e) {
            log.error(e.getMessage());
        }
        return NSBundle.localizedString("Unknown", "");
    }
}
