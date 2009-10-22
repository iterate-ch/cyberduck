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

public abstract class NSDate extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDate", _Class.class);

    public static NSDate date() {
        return CLASS.date();
    }

    public static NSDate dateWithTimeIntervalSinceNow(double secs) {
        return CLASS.dateWithTimeIntervalSinceNow(secs);
    }

    public static NSDate dateWithTimeIntervalSinceReferenceDate(double secs) {
        return CLASS.dateWithTimeIntervalSinceReferenceDate(secs);
    }

    public static NSDate dateWithTimeIntervalSince1970(double secs) {
        return CLASS.dateWithTimeIntervalSince1970(secs);
    }

    public static NSDate distantFuture() {
        return CLASS.distantFuture();
    }

    public static NSDate distantPast() {
        return CLASS.distantPast();
    }


    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>date()</code><br>
         * <i>from NSDateCreation native declaration : :41</i>
         */
        NSDate date();

        /**
         * Original signature : <code>dateWithTimeIntervalSinceNow(NSTimeInterval)</code><br>
         * <i>from NSDateCreation native declaration : :43</i>
         */
        NSDate dateWithTimeIntervalSinceNow(double secs);

        /**
         * Original signature : <code>dateWithTimeIntervalSinceReferenceDate(NSTimeInterval)</code><br>
         * <i>from NSDateCreation native declaration : :44</i>
         */
        NSDate dateWithTimeIntervalSinceReferenceDate(double secs);

        /**
         * Original signature : <code>dateWithTimeIntervalSince1970(NSTimeInterval)</code><br>
         * <i>from NSDateCreation native declaration : :45</i>
         */
        NSDate dateWithTimeIntervalSince1970(double secs);

        /**
         * Original signature : <code>distantFuture()</code><br>
         * <i>from NSDateCreation native declaration : :47</i>
         */
        NSDate distantFuture();

        /**
         * Original signature : <code>distantPast()</code><br>
         * <i>from NSDateCreation native declaration : :48</i>
         */
        NSDate distantPast();
    }

    /**
     * Original signature : <code>NSTimeInterval timeIntervalSinceReferenceDate()</code><br>
     * <i>native declaration : :16</i>
     */
    public abstract double timeIntervalSinceReferenceDate();

    /**
     * Original signature : <code>NSTimeInterval timeIntervalSinceDate(NSDate*)</code><br>
     * <i>from NSExtendedDate native declaration : :22</i>
     */
    public abstract double timeIntervalSinceDate(NSDate anotherDate);

    /**
     * Original signature : <code>NSTimeInterval timeIntervalSinceNow()</code><br>
     * <i>from NSExtendedDate native declaration : :23</i>
     */
    public abstract double timeIntervalSinceNow();

    /**
     * Original signature : <code>NSTimeInterval timeIntervalSince1970()</code><br>
     * <i>from NSExtendedDate native declaration : :24</i>
     */
    public abstract double timeIntervalSince1970();

    /**
     * Original signature : <code>addTimeInterval(NSTimeInterval)</code><br>
     * <i>from NSExtendedDate native declaration : :26</i>
     */
    public abstract NSObject addTimeInterval(double seconds);

    /**
     * Original signature : <code>NSDate* earlierDate(NSDate*)</code><br>
     * <i>from NSExtendedDate native declaration : :28</i>
     */
    public abstract NSDate earlierDate(NSDate anotherDate);

    /**
     * Original signature : <code>NSDate* laterDate(NSDate*)</code><br>
     * <i>from NSExtendedDate native declaration : :29</i>
     */
    public abstract NSDate laterDate(NSDate anotherDate);

    /**
     * Original signature : <code>compare(NSDate*)</code><br>
     * <i>from NSExtendedDate native declaration : :30</i>
     */
    public abstract NSObject compare(NSDate other);

    /**
     * Original signature : <code>NSString* description()</code><br>
     * <i>from NSExtendedDate native declaration : :32</i>
     */
    public abstract String description();

    /**
     * Original signature : <code>BOOL isEqualToDate(NSDate*)</code><br>
     * <i>from NSExtendedDate native declaration : :33</i>
     */
    public abstract byte isEqualToDate(NSDate otherDate);

    /**
     * Original signature : <code>init()</code><br>
     * <i>from NSDateCreation native declaration : :50</i>
     */
    public abstract NSDate init();

    /**
     * Original signature : <code>initWithTimeIntervalSinceReferenceDate(NSTimeInterval)</code><br>
     * <i>from NSDateCreation native declaration : :51</i>
     */
    public abstract NSDate initWithTimeIntervalSinceReferenceDate(double secsToBeAdded);

    /**
     * Original signature : <code>initWithTimeInterval(NSTimeInterval, NSDate*)</code><br>
     * <i>from NSDateCreation native declaration : :52</i>
     */
    public abstract NSDate initWithTimeInterval_sinceDate(double secsToBeAdded, NSDate anotherDate);

    /**
     * Original signature : <code>initWithTimeIntervalSinceNow(NSTimeInterval)</code><br>
     * <i>from NSDateCreation native declaration : :53</i>
     */
    public abstract NSDate initWithTimeIntervalSinceNow(double secsToBeAddedToNow);
}
