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

/**
 * @version $Id$
 */
public abstract class NSDateFormatter extends NSFormatter {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDateFormatter", _Class.class);

    /// <i>native declaration : :24</i>
    public static final int kCFDateFormatterNoStyle = 0;
    /// <i>native declaration : :25</i>
    public static final int kCFDateFormatterShortStyle = 1;
    /// <i>native declaration : :26</i>
    public static final int kCFDateFormatterMediumStyle = 2;
    /// <i>native declaration : :27</i>
    public static final int kCFDateFormatterLongStyle = 3;
    /// <i>native declaration : :28</i>
    public static final int kCFDateFormatterFullStyle = 4;

    public static NSDateFormatter dateFormatter() {
        return CLASS.alloc().init();
    }

    public interface _Class extends org.rococoa.NSClass {
        NSDateFormatter alloc();
    }

    /**
     * Original signature : <code>init()</code><br>
     * <i>native declaration : :18</i>
     */
    public abstract NSDateFormatter init();

    /**
     * Original signature : <code>NSString* stringFromDate(NSDate*)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract String stringFromDate(com.sun.jna.Pointer date);

    /**
     * Original signature : <code>NSDate* dateFromString(NSString*)</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract String dateFromString(com.sun.jna.Pointer string);

    /**
     * Original signature : <code>NSString* dateFormat()</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract String dateFormat();

    /**
     * Original signature : <code>dateStyle()</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract int dateStyle();

    /**
     * <i>native declaration : :42</i><br>
     * Conversion Error : /// Original signature : <code>void setDateStyle(null)</code><br>
     * - (void)setDateStyle:(null)style; (Argument style cannot be converted)
     */
    public abstract void setDateStyle(int style);

    /**
     * Original signature : <code>timeStyle()</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract int timeStyle();
    /**
     * <i>native declaration : :45</i><br>
     * Conversion Error : /// Original signature : <code>void setTimeStyle(null)</code><br>
     * - (void)setTimeStyle:(null)style; (Argument style cannot be converted)
     */
    /**
     * Original signature : <code>NSLocale* locale()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract com.sun.jna.Pointer locale();

    /**
     * Original signature : <code>void setLocale(NSLocale*)</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract void setLocale(com.sun.jna.Pointer locale);

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
}
