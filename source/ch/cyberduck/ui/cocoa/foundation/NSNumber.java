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
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:37</i>
public abstract class NSNumber extends NSValue {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSNumber", _Class.class);

    public static NSNumber numberWithInt(int value) {
        return CLASS.numberWithInt(value);
    }

    public static NSNumber numberWithDouble(double value) {
        return CLASS.numberWithDouble(value);
    }

    public static NSNumber numberWithFloat(float value) {
        return CLASS.numberWithFloat(value);
    }

    public static NSNumber numberWithBoolean(boolean value) {
        return CLASS.numberWithBool(value);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSNumber* numberWithChar(char)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:87</i>
         */
        NSNumber numberWithChar(byte value);

        /**
         * Original signature : <code>NSNumber* numberWithUnsignedChar(unsigned char)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:88</i>
         */
        NSNumber numberWithUnsignedChar(byte value);

        /**
         * Original signature : <code>NSNumber* numberWithShort(short)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:89</i>
         */
        NSNumber numberWithShort(short value);

        /**
         * Original signature : <code>NSNumber* numberWithUnsignedShort(unsigned short)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:90</i>
         */
        NSNumber numberWithUnsignedShort(short value);

        /**
         * Original signature : <code>NSNumber* numberWithInt(int)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:91</i>
         */
        NSNumber numberWithInt(int value);

        /**
         * Original signature : <code>NSNumber* numberWithUnsignedInt(unsigned int)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:92</i>
         */
        NSNumber numberWithUnsignedInt(int value);

        /**
         * Original signature : <code>NSNumber* numberWithLong(long)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:93</i>
         */
        NSNumber numberWithLong(com.sun.jna.NativeLong value);

        /**
         * Original signature : <code>NSNumber* numberWithUnsignedLong(unsigned long)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:94</i>
         */
        NSNumber numberWithUnsignedLong(com.sun.jna.NativeLong value);

        /**
         * Original signature : <code>NSNumber* numberWithLongLong(long long)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:95</i>
         */
        NSNumber numberWithLongLong(long value);

        /**
         * Original signature : <code>NSNumber* numberWithUnsignedLongLong(unsigned long long)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:96</i>
         */
        NSNumber numberWithUnsignedLongLong(long value);

        /**
         * Original signature : <code>NSNumber* numberWithFloat(float)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:97</i>
         */
        NSNumber numberWithFloat(float value);

        /**
         * Original signature : <code>NSNumber* numberWithDouble(double)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:98</i>
         */
        NSNumber numberWithDouble(double value);

        /**
         * Original signature : <code>NSNumber* numberWithBool(BOOL)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:99</i>
         */
        NSNumber numberWithBool(boolean value);

        /**
         * Original signature : <code>NSNumber* numberWithInteger(NSInteger)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:101</i>
         */
        NSNumber numberWithInteger(NSNumber value);

        /**
         * Original signature : <code>NSNumber* numberWithUnsignedInteger(NSUInteger)</code><br>
         * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:102</i>
         */
        NSNumber numberWithUnsignedInteger(NSUInteger value);
    }

    /**
     * Original signature : <code>char charValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:39</i>
     */
    public abstract byte charValue();

    /**
     * Original signature : <code>unsigned char unsignedCharValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:40</i>
     */
    public abstract byte unsignedCharValue();

    /**
     * Original signature : <code>short shortValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:41</i>
     */
    public abstract short shortValue();

    /**
     * Original signature : <code>unsigned short unsignedShortValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:42</i>
     */
    public abstract short unsignedShortValue();

    /**
     * Original signature : <code>int intValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:43</i>
     */
    public abstract int intValue();

    /**
     * Original signature : <code>unsigned int unsignedIntValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:44</i>
     */
    public abstract int unsignedIntValue();

    /**
     * Original signature : <code>long longValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:45</i>
     */
    public abstract long longValue();

    /**
     * Original signature : <code>unsigned long unsignedLongValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:46</i>
     */
    public abstract long unsignedLongValue();

    /**
     * Original signature : <code>float floatValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:49</i>
     */
    public abstract float floatValue();

    /**
     * Original signature : <code>double doubleValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:50</i>
     */
    public abstract double doubleValue();

    /**
     * Original signature : <code>BOOL boolValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:51</i>
     */
    public abstract byte boolValue();

    /**
     * Original signature : <code>NSInteger integerValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:53</i>
     */
    public abstract NSInteger integerValue();

    /**
     * Original signature : <code>NSUInteger unsignedIntegerValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:54</i>
     */
    public abstract int unsignedIntegerValue();

    /**
     * Original signature : <code>NSString* stringValue()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:57</i>
     */
    public abstract String stringValue();

    /**
     * Original signature : <code>compare(NSNumber*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:59</i>
     */
    public abstract NSObject compare(NSNumber otherNumber);

    /**
     * Original signature : <code>BOOL isEqualToNumber(NSNumber*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:61</i>
     */
    public abstract byte isEqualToNumber(NSNumber number);
    /**
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:63</i><br>
     * Conversion Error : /// Original signature : <code>NSString* descriptionWithLocale(null)</code><br>
     * - (NSString*)descriptionWithLocale:(null)locale; (Argument locale cannot be converted)
     */
    /**
     * Original signature : <code>initWithChar(char)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:69</i>
     */
    public abstract NSNumber initWithChar(byte value);

    /**
     * Original signature : <code>initWithUnsignedChar(unsigned char)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:70</i>
     */
    public abstract NSNumber initWithUnsignedChar(byte value);

    /**
     * Original signature : <code>initWithShort(short)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:71</i>
     */
    public abstract NSNumber initWithShort(short value);

    /**
     * Original signature : <code>initWithUnsignedShort(unsigned short)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:72</i>
     */
    public abstract NSNumber initWithUnsignedShort(short value);

    /**
     * Original signature : <code>initWithInt(int)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:73</i>
     */
    public abstract NSNumber initWithInt(int value);

    /**
     * Original signature : <code>initWithUnsignedInt(unsigned int)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:74</i>
     */
    public abstract NSNumber initWithUnsignedInt(int value);

    /**
     * Original signature : <code>initWithLong(long)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:75</i>
     */
    public abstract NSNumber initWithLong(com.sun.jna.NativeLong value);

    /**
     * Original signature : <code>initWithUnsignedLong(unsigned long)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:76</i>
     */
    public abstract NSNumber initWithUnsignedLong(com.sun.jna.NativeLong value);

    /**
     * Original signature : <code>initWithLongLong(long long)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:77</i>
     */
    public abstract NSNumber initWithLongLong(long value);

    /**
     * Original signature : <code>initWithUnsignedLongLong(unsigned long long)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:78</i>
     */
    public abstract NSNumber initWithUnsignedLongLong(long value);

    /**
     * Original signature : <code>initWithFloat(float)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:79</i>
     */
    public abstract NSNumber initWithFloat(float value);

    /**
     * Original signature : <code>initWithDouble(double)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:80</i>
     */
    public abstract NSNumber initWithDouble(double value);

    /**
     * Original signature : <code>initWithBool(BOOL)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:81</i>
     */
    public abstract NSNumber initWithBool(boolean value);

    /**
     * Original signature : <code>initWithInteger(NSInteger)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:83</i>
     */
    public abstract NSNumber initWithInteger(NSInteger value);

    /**
     * Original signature : <code>initWithUnsignedInteger(NSUInteger)</code><br>
     * <i>from NSNumberCreation native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSValue.h:84</i>
     */
    public abstract NSNumber initWithUnsignedInteger(int value);

    /**
     * Original signature : <code>decimalValue()</code><br>
     * <i>from NSDecimalNumberExtensions native declaration : :141</i>
     */
    public abstract NSObject decimalValue();
}
