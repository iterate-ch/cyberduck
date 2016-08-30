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
import org.rococoa.ObjCObject;

public abstract class NSKeyedUnarchiver extends NSCoder {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSKeyedUnarchiver", _Class.class);

    /**
     * Original signature : <code>+(id)unarchiveObjectWithData:(NSData*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:94</i>
     */
    public static NSObject unarchiveObjectWithData(NSData data) {
        return CLASS.unarchiveObjectWithData(data);
    }

    /**
     * Original signature : <code>+(id)unarchiveObjectWithFile:(NSString*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:95</i>
     */
    public static NSObject unarchiveObjectWithFile(String path) {
        return CLASS.unarchiveObjectWithFile(path);
    }

    /**
     * Original signature : <code>-(id)initForReadingWithData:(NSData*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:97</i>
     */
    public abstract NSKeyedUnarchiver initForReadingWithData(NSData data);

    /**
     * Factory method<br>
     *
     * @see #initForReadingWithData(NSData)
     */
    public static NSKeyedUnarchiver createForReadingWithData(NSData data) {
        return CLASS.alloc().initForReadingWithData(data);
    }

    /**
     * Original signature : <code>-(void)setDelegate:(id<NSKeyedUnarchiverDelegate>)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:99</i>
     */
    public abstract void setDelegate(ObjCObject delegate);

    /**
     * Original signature : <code>-(id<NSKeyedUnarchiverDelegate>)delegate</code><br>
     * <i>native declaration : NSKeyedArchiver.h:100</i>
     */
    public abstract ObjCObject delegate();

    /**
     * Original signature : <code>-(void)finishDecoding</code><br>
     * <i>native declaration : NSKeyedArchiver.h:102</i>
     */
    public abstract void finishDecoding();

    /**
     * Original signature : <code>+(void)setClass:(Class) forClassName:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:104</i>
     */
    public abstract void setClass_forClassName(ObjCClass cls, String codedName);

    /**
     * Original signature : <code>+(Class)classForClassName:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:109</i>
     */
    public static ObjCClass classForClassName(String codedName) {
        return CLASS.classForClassName(codedName);
    }

    /**
     * Original signature : <code>-(BOOL)containsValueForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:112</i>
     */
    public abstract boolean containsValueForKey(String key);

    /**
     * Original signature : <code>-(id)decodeObjectForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:114</i>
     */
    public abstract NSObject decodeObjectForKey(String key);

    /**
     * Original signature : <code>-(BOOL)decodeBoolForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:115</i>
     */
    public abstract boolean decodeBoolForKey(String key);

    /**
     * may raise a range exception<br>
     * Original signature : <code>-(int)decodeIntForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:116</i>
     */
    public abstract int decodeIntForKey(String key);

    /**
     * Original signature : <code>-(int32_t)decodeInt32ForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:117</i>
     */
    public abstract int decodeInt32ForKey(String key);

    /**
     * Original signature : <code>-(int64_t)decodeInt64ForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:118</i>
     */
    public abstract long decodeInt64ForKey(String key);

    /**
     * Original signature : <code>-(float)decodeFloatForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:119</i>
     */
    public abstract float decodeFloatForKey(String key);

    /**
     * Original signature : <code>-(double)decodeDoubleForKey:(String*)</code><br>
     * <i>native declaration : NSKeyedArchiver.h:120</i>
     */
    public abstract double decodeDoubleForKey(String key);

    /// <i>native declaration : NSKeyedArchiver.h</i>

    public static NSKeyedUnarchiver alloc() {
        return CLASS.alloc();
    }
    /// <i>native declaration : NSKeyedArchiver.h</i>

    public static NSKeyedUnarchiver new_() {
        return CLASS.new_();
    }

    public static abstract class _Class extends NSCoder._class_ {
        /**
         * Original signature : <code>+(id)unarchiveObjectWithData:(NSData*)</code><br>
         * <i>native declaration : NSKeyedArchiver.h:94</i>
         */
        public abstract NSObject unarchiveObjectWithData(NSData data);

        /**
         * Original signature : <code>+(id)unarchiveObjectWithFile:(String*)</code><br>
         * <i>native declaration : NSKeyedArchiver.h:95</i>
         */
        public abstract NSObject unarchiveObjectWithFile(String path);

        /**
         * Original signature : <code>+(void)setClass:(Class) forClassName:(String*)</code><br>
         * <i>native declaration : NSKeyedArchiver.h:104</i>
         */
        public abstract void setClass_forClassName(ObjCClass cls, String codedName);

        /**
         * Original signature : <code>+(Class)classForClassName:(String*)</code><br>
         * <i>native declaration : NSKeyedArchiver.h:109</i>
         */
        public abstract ObjCClass classForClassName(String codedName);
        /// <i>native declaration : NSKeyedArchiver.h</i>

        public abstract NSKeyedUnarchiver alloc();
        /// <i>native declaration : NSKeyedArchiver.h</i>

        public abstract NSKeyedUnarchiver new_();
    }
}
