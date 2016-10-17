package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

public abstract class NSLocale extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSLocale", _Class.class);

    public interface _Class extends ObjCClass {
        /// <i>native declaration : NSLocale.h</i>

        public abstract NSLocale alloc();

        /**
         * Original signature : <code>NSArray* availableLocaleIdentifiers()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:40</i>
         */
        public abstract NSArray availableLocaleIdentifiers();

        /**
         * Original signature : <code>NSArray* ISOLanguageCodes()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:41</i>
         */
        public abstract NSArray ISOLanguageCodes();

        /**
         * Original signature : <code>NSArray* ISOCountryCodes()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:42</i>
         */
        public abstract NSArray ISOCountryCodes();

        /**
         * Original signature : <code>NSArray* ISOCurrencyCodes()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:43</i>
         */
        public abstract NSArray ISOCurrencyCodes();

        /**
         * Original signature : <code>NSArray* commonISOCurrencyCodes()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:44</i>
         */
        public abstract NSArray commonISOCurrencyCodes();

        /**
         * Original signature : <code>NSArray* preferredLanguages()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:45</i>
         */
        public abstract NSArray preferredLanguages();

        /**
         * Original signature : <code>NSDictionary* componentsFromLocaleIdentifier(NSString*)</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:47</i>
         */
        public abstract NSDictionary componentsFromLocaleIdentifier(String string);

        /**
         * Original signature : <code>NSString* localeIdentifierFromComponents(NSDictionary*)</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:48</i>
         */
        public abstract String localeIdentifierFromComponents(NSDictionary dict);

        /**
         * Original signature : <code>NSString* canonicalLocaleIdentifierFromString(NSString*)</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:50</i>
         */
        public abstract String canonicalLocaleIdentifierFromString(String string);

        /**
         * Original signature : <code>systemLocale()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleCreation native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:30</i>
         */
        public abstract NSLocale systemLocale();

        /**
         * Original signature : <code>currentLocale()</code><br>
         * From category NSLocale<br>
         * <i>from NSLocaleCreation native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:31</i>
         */
        public abstract NSLocale currentLocale();
    }

    /**
     * Original signature : <code>NSArray* availableLocaleIdentifiers()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:40</i>
     */
    public static NSArray availableLocaleIdentifiers() {
        return CLASS.availableLocaleIdentifiers();
    }

    /**
     * Original signature : <code>NSArray* ISOLanguageCodes()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:41</i>
     */
    public static NSArray ISOLanguageCodes() {
        return CLASS.ISOLanguageCodes();
    }

    /**
     * Original signature : <code>NSArray* ISOCountryCodes()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:42</i>
     */
    public static NSArray ISOCountryCodes() {
        return CLASS.ISOCountryCodes();
    }

    /**
     * Original signature : <code>NSArray* ISOCurrencyCodes()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:43</i>
     */
    public static NSArray ISOCurrencyCodes() {
        return CLASS.ISOCurrencyCodes();
    }

    /**
     * Original signature : <code>NSArray* commonISOCurrencyCodes()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:44</i>
     */
    public static NSArray commonISOCurrencyCodes() {
        return CLASS.commonISOCurrencyCodes();
    }

    /**
     * Original signature : <code>NSArray* preferredLanguages()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:45</i>
     */
    public static NSArray preferredLanguages() {
        return CLASS.preferredLanguages();
    }

    /**
     * Original signature : <code>NSDictionary* componentsFromLocaleIdentifier(NSString*)</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:47</i>
     */
    public static NSDictionary componentsFromLocaleIdentifier(String string) {
        return CLASS.componentsFromLocaleIdentifier(string);
    }

    /**
     * Original signature : <code>NSString* localeIdentifierFromComponents(NSDictionary*)</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:48</i>
     */
    public static String localeIdentifierFromComponents(NSDictionary dict) {
        return CLASS.localeIdentifierFromComponents(dict);
    }

    /**
     * Original signature : <code>NSString* canonicalLocaleIdentifierFromString(NSString*)</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleGeneralInfo native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:50</i>
     */
    public static String canonicalLocaleIdentifierFromString(String string) {
        return CLASS.canonicalLocaleIdentifierFromString(string);
    }

    /**
     * Original signature : <code>systemLocale()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleCreation native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:30</i>
     */
    public static NSLocale systemLocale() {
        return CLASS.systemLocale();
    }

    /**
     * Original signature : <code>currentLocale()</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleCreation native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:31</i>
     */
    public static NSLocale currentLocale() {
        return CLASS.currentLocale();
    }

    /**
     * Original signature : <code>initWithLocaleIdentifier(NSString*)</code><br>
     * From category NSLocale<br>
     * <i>from NSLocaleCreation native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:34</i>
     */
    public abstract NSLocale initWithLocaleIdentifier(String string);

    /**
     * Factory method<br>
     *
     * @see #initWithLocaleIdentifier(String)
     */
    public static NSLocale createWithLocaleIdentifier(String string) {
        return CLASS.alloc().initWithLocaleIdentifier(string);
    }

    /**
     * Original signature : <code>NSString* localeIdentifier()</code><br>
     * From category NSLocale<br>
     * same as NSLocaleIdentifier<br>
     * <i>from NSExtendedLocale native declaration : /System/Library/Frameworks/framework/Headers/NSLocale.h:24</i>
     */
    public abstract String localeIdentifier();

    /**
     * Original signature : <code>-(NSString*)displayNameForKey:(id) value:(id)</code><br>
     * <i>native declaration : NSLocale.h:19</i>
     */
    public abstract String displayNameForKey_value(String key, String value);
}
