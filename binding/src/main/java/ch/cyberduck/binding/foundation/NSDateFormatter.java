package ch.cyberduck.binding.foundation;

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
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class NSDateFormatter extends NSFormatter {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDateFormatter", _Class.class);

    /// <i>native declaration : :24</i>
    public static final NSUInteger kCFDateFormatterNoStyle = new NSUInteger(0);
    /// <i>native declaration : :25</i>
    public static final NSUInteger kCFDateFormatterShortStyle = new NSUInteger(1);
    /// <i>native declaration : :26</i>
    public static final NSUInteger kCFDateFormatterMediumStyle = new NSUInteger(2);
    /// <i>native declaration : :27</i>
    public static final NSUInteger kCFDateFormatterLongStyle = new NSUInteger(3);
    /// <i>native declaration : :28</i>
    public static final NSUInteger kCFDateFormatterFullStyle = new NSUInteger(4);

    public static NSDateFormatter dateFormatter() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        NSDateFormatter alloc();
    }

    /**
     * Original signature : <code>init()</code><br>
     * <i>native declaration : :18</i>
     */
    public abstract NSDateFormatter init();

    public abstract void setDoesRelativeDateFormatting(boolean relative);


    /**
     * Original signature : <code>NSString* stringFromDate(NSDate*)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract String stringFromDate(NSDate date);

    /**
     * Original signature : <code>NSDate* dateFromString(NSString*)</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract String dateFromString(String string);

    /**
     * Original signature : <code>NSString* dateFormat()</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract String dateFormat();

    /**
     * Original signature : <code>dateStyle()</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract NSUInteger dateStyle();

    /**
     * <i>native declaration : :42</i><br>
     * Conversion Error : /// Original signature : <code>void setDateStyle(null)</code><br>
     * - (void)setDateStyle:(null)style; (Argument style cannot be converted)
     */
    public abstract void setDateStyle(NSUInteger style);

    /**
     * Original signature : <code>timeStyle()</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract NSUInteger timeStyle();

    /**
     * <i>native declaration : :45</i><br>
     * Conversion Error : /// Original signature : <code>void setTimeStyle(null)</code><br>
     */
    public abstract void setTimeStyle(NSUInteger style);

    /**
     * Original signature : <code>NSLocale* locale()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract com.sun.jna.Pointer locale();

    /**
     * Original signature : <code>void setLocale(NSLocale*)</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract void setLocale(NSLocale locale);

    /**
     * Original signature : <code>BOOL generatesCalendarDates()</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract boolean generatesCalendarDates();

    /**
     * Original signature : <code>void setGeneratesCalendarDates(BOOL)</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract void setGeneratesCalendarDates(boolean b);

    /**
     * Original signature : <code>-(NSTimeZone*)timeZone</code><br>
     * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFDateFormatter.h:55</i>
     */
    public abstract NSTimeZone timeZone();

    /**
     * Original signature : <code>-(void)setTimeZone:(NSTimeZone*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFDateFormatter.h:56</i>
     */
    public abstract void setTimeZone(NSTimeZone tz);

    /**
     * Original signature : <code>-(BOOL)isLenient</code><br>
     * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFDateFormatter.h:61</i>
     */
    public abstract boolean isLenient();

    /**
     * Original signature : <code>-(void)setLenient:(BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/CoreFoundation.framework/Headers/CFDateFormatter.h:62</i>
     */
    public abstract void setLenient(boolean b);
}
