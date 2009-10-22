package ch.cyberduck.ui.cocoa.foundation;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSInteger;

public abstract class NSTimeZone extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTimeZone", _Class.class);

    public static NSTimeZone defaultTimeZone() {
        return CLASS.defaultTimeZone();
    }

    public static NSTimeZone systemTimeZone() {
        return CLASS.systemTimeZone();
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSTimeZone* systemTimeZone()</code><br>
         * <i>from NSExtendedTimeZone native declaration : :22</i>
         */
        NSTimeZone systemTimeZone();

        /**
         * Original signature : <code>void resetSystemTimeZone()</code><br>
         * <i>from NSExtendedTimeZone native declaration : :23</i>
         */
        void resetSystemTimeZone();

        /**
         * Original signature : <code>NSTimeZone* defaultTimeZone()</code><br>
         * <i>from NSExtendedTimeZone native declaration : :25</i>
         */
        NSTimeZone defaultTimeZone();

        /**
         * Original signature : <code>void setDefaultTimeZone(NSTimeZone*)</code><br>
         * <i>from NSExtendedTimeZone native declaration : :26</i>
         */
        void setDefaultTimeZone(NSTimeZone aTimeZone);

        /**
         * Original signature : <code>NSTimeZone* localTimeZone()</code><br>
         * <i>from NSExtendedTimeZone native declaration : :28</i>
         */
        NSTimeZone localTimeZone();

        /**
         * Original signature : <code>NSArray* knownTimeZoneNames()</code><br>
         * <i>from NSExtendedTimeZone native declaration : :30</i>
         */
        NSArray knownTimeZoneNames();

        /**
         * Original signature : <code>NSDictionary* abbreviationDictionary()</code><br>
         * <i>from NSExtendedTimeZone native declaration : :32</i>
         */
        NSDictionary abbreviationDictionary();
    }

    /**
     * Original signature : <code>NSString* name()</code><br>
     * <i>native declaration : :9</i>
     */
    public abstract String name();

    /**
     * Original signature : <code>NSData* data()</code><br>
     * <i>native declaration : :10</i>
     */
    public abstract NSData data();

    /**
     * Original signature : <code>NSInteger secondsFromGMTForDate(NSDate*)</code><br>
     * <i>native declaration : :12</i>
     */
    public abstract NSInteger secondsFromGMTForDate(NSDate aDate);

    /**
     * Original signature : <code>NSString* abbreviationForDate(NSDate*)</code><br>
     * <i>native declaration : :13</i>
     */
    public abstract String abbreviationForDate(NSDate aDate);

    /**
     * Original signature : <code>BOOL isDaylightSavingTimeForDate(NSDate*)</code><br>
     * <i>native declaration : :14</i>
     */
    public abstract boolean isDaylightSavingTimeForDate(NSDate aDate);

    /**
     * Original signature : <code>daylightSavingTimeOffsetForDate(NSDate*)</code><br>
     * <i>native declaration : :15</i>
     */
    public abstract com.sun.jna.Pointer daylightSavingTimeOffsetForDate(com.sun.jna.Pointer aDate);

    /**
     * Original signature : <code>NSDate* nextDaylightSavingTimeTransitionAfterDate(NSDate*)</code><br>
     * <i>native declaration : :16</i>
     */
    public abstract NSDate nextDaylightSavingTimeTransitionAfterDate(NSDate aDate);

    /**
     * Original signature : <code>NSInteger secondsFromGMT()</code><br>
     * <i>from NSExtendedTimeZone native declaration : :34</i>
     */
    public abstract NSInteger secondsFromGMT();

    /**
     * Original signature : <code>NSString* abbreviation()</code><br>
     * <i>from NSExtendedTimeZone native declaration : :35</i>
     */
    public abstract String abbreviation();

    /**
     * Original signature : <code>BOOL isDaylightSavingTime()</code><br>
     * <i>from NSExtendedTimeZone native declaration : :36</i>
     */
    public abstract boolean isDaylightSavingTime();

    /**
     * Original signature : <code>daylightSavingTimeOffset()</code><br>
     * for current instant<br>
     * <i>from NSExtendedTimeZone native declaration : :37</i>
     */
    public abstract com.sun.jna.Pointer daylightSavingTimeOffset();

    /**
     * Original signature : <code>NSDate* nextDaylightSavingTimeTransition()</code><br>
     * after current instant<br>
     * <i>from NSExtendedTimeZone native declaration : :38</i>
     */
    public abstract NSDate nextDaylightSavingTimeTransition();

    /**
     * Original signature : <code>BOOL isEqualToTimeZone(NSTimeZone*)</code><br>
     * <i>from NSExtendedTimeZone native declaration : :42</i>
     */
    public abstract boolean isEqualToTimeZone(NSTimeZone aTimeZone);
}
