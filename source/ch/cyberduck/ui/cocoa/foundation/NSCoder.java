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

/// <i>native declaration : :10</i>
public interface NSCoder extends NSObject {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSCoder", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSCoder alloc();
    }

    /**
     * Original signature : <code>void encodeValueOfObjCType(const char*, const void*)</code><br>
     * <i>native declaration : :12</i><br>
     *
     * @deprecated use the safer method {@link #NSCoder_encodeValueOfObjCType_at(java.lang.String, com.sun.jna.Pointer)} instead
     */
    @java.lang.Deprecated
    void encodeValueOfObjCType_at(com.sun.jna.ptr.ByteByReference type, com.sun.jna.Pointer addr);

    /**
     * Original signature : <code>void encodeValueOfObjCType(const char*, const void*)</code><br>
     * <i>native declaration : :12</i>
     */
    void encodeValueOfObjCType_at(java.lang.String type, com.sun.jna.Pointer addr);

    /**
     * Original signature : <code>void encodeDataObject(NSData*)</code><br>
     * <i>native declaration : :13</i>
     */
    void encodeDataObject(com.sun.jna.Pointer data);

    /**
     * Original signature : <code>void decodeValueOfObjCType(const char*, void*)</code><br>
     * <i>native declaration : :14</i><br>
     *
     * @deprecated use the safer method {@link #decodeValueOfObjCType_at(java.lang.String, com.sun.jna.Pointer)} instead
     */
    @java.lang.Deprecated
    void decodeValueOfObjCType_at(com.sun.jna.ptr.ByteByReference type, com.sun.jna.Pointer data);

    /**
     * Original signature : <code>void decodeValueOfObjCType(const char*, void*)</code><br>
     * <i>native declaration : :14</i>
     */
    void decodeValueOfObjCType_at(java.lang.String type, com.sun.jna.Pointer data);

    /**
     * Original signature : <code>NSData* decodeDataObject()</code><br>
     * <i>native declaration : :15</i>
     */
    com.sun.jna.Pointer decodeDataObject();

    /**
     * Original signature : <code>NSInteger versionForClassName(NSString*)</code><br>
     * <i>native declaration : :16</i>
     */
    int versionForClassName(com.sun.jna.Pointer className);
    /**
     * <i>from NSExtendedCoder native declaration : :22</i><br>
     * Conversion Error : /// Original signature : <code>void encodeObject(null)</code><br>
     * - (void)encodeObject:(null)object; (Argument object cannot be converted)
     */
    /**
     * <i>from NSExtendedCoder native declaration : :23</i><br>
     * Conversion Error : /// Original signature : <code>void encodePropertyList(null)</code><br>
     * - (void)encodePropertyList:(null)aPropertyList; (Argument aPropertyList cannot be converted)
     */
    /**
     * <i>from NSExtendedCoder native declaration : :24</i><br>
     * Conversion Error : /// Original signature : <code>void encodeRootObject(null)</code><br>
     * - (void)encodeRootObject:(null)rootObject; (Argument rootObject cannot be converted)
     */
    /**
     * <i>from NSExtendedCoder native declaration : :25</i><br>
     * Conversion Error : /// Original signature : <code>void encodeBycopyObject(null)</code><br>
     * - (void)encodeBycopyObject:(null)anObject; (Argument anObject cannot be converted)
     */
    /**
     * <i>from NSExtendedCoder native declaration : :26</i><br>
     * Conversion Error : /// Original signature : <code>void encodeByrefObject(null)</code><br>
     * - (void)encodeByrefObject:(null)anObject; (Argument anObject cannot be converted)
     */
    /**
     * <i>from NSExtendedCoder native declaration : :27</i><br>
     * Conversion Error : /// Original signature : <code>void encodeConditionalObject(null)</code><br>
     * - (void)encodeConditionalObject:(null)object; (Argument object cannot be converted)
     */
    /**
     * Original signature : <code>void encodeValuesOfObjCTypes(const char*, null)</code><br>
     * <i>from NSExtendedCoder native declaration : :28</i><br>
     *
     * @deprecated use the safer method {@link #encodeValuesOfObjCTypes(java.lang.String, NSObject)} instead
     */
    @java.lang.Deprecated
    void encodeValuesOfObjCTypes(com.sun.jna.ptr.ByteByReference types, NSObject... varargs);

    /**
     * Original signature : <code>void encodeValuesOfObjCTypes(const char*, null)</code><br>
     * <i>from NSExtendedCoder native declaration : :28</i>
     */
    void encodeValuesOfObjCTypes(java.lang.String types, NSObject... varargs);

    /**
     * Original signature : <code>void encodeArrayOfObjCType(const char*, NSUInteger, const void*)</code><br>
     * <i>from NSExtendedCoder native declaration : :29</i><br>
     *
     * @deprecated use the safer method {@link #encodeArrayOfObjCType_count_at(java.lang.String, int, com.sun.jna.Pointer)} instead
     */
    @java.lang.Deprecated
    void encodeArrayOfObjCType_count_at(com.sun.jna.ptr.ByteByReference type, int count, NSArray array);

    /**
     * Original signature : <code>void encodeArrayOfObjCType(const char*, NSUInteger, const void*)</code><br>
     * <i>from NSExtendedCoder native declaration : :29</i>
     */
    void encodeArrayOfObjCType_count_at(java.lang.String type, int count, NSArray array);

    /**
     * Original signature : <code>void encodeBytes(const void*, NSUInteger)</code><br>
     * <i>from NSExtendedCoder native declaration : :30</i>
     */
    void encodeBytes_length(com.sun.jna.Pointer byteaddr, int length);

    /**
     * Original signature : <code>decodeObject()</code><br>
     * <i>from NSExtendedCoder native declaration : :32</i>
     */
    NSObject decodeObject();

    /**
     * Original signature : <code>decodePropertyList()</code><br>
     * <i>from NSExtendedCoder native declaration : :33</i>
     */
    NSObject decodePropertyList();

    /**
     * Original signature : <code>void decodeValuesOfObjCTypes(const char*, null)</code><br>
     * <i>from NSExtendedCoder native declaration : :34</i><br>
     *
     * @deprecated use the safer method {@link #decodeValuesOfObjCTypes(java.lang.String, NSObject)} instead
     */
    @java.lang.Deprecated
    void decodeValuesOfObjCTypes(com.sun.jna.ptr.ByteByReference types, NSObject... varargs);

    /**
     * Original signature : <code>void decodeValuesOfObjCTypes(const char*, null)</code><br>
     * <i>from NSExtendedCoder native declaration : :34</i>
     */
    void decodeValuesOfObjCTypes(java.lang.String types, NSObject... varargs);

    /**
     * Original signature : <code>void decodeArrayOfObjCType(const char*, NSUInteger, void*)</code><br>
     * <i>from NSExtendedCoder native declaration : :35</i><br>
     *
     * @deprecated use the safer method {@link #decodeArrayOfObjCType_count_at(java.lang.String, int, com.sun.jna.Pointer)} instead
     */
    @java.lang.Deprecated
    void decodeArrayOfObjCType_count_at(com.sun.jna.ptr.ByteByReference itemType, int count, NSArray array);

    /**
     * Original signature : <code>void decodeArrayOfObjCType(const char*, NSUInteger, void*)</code><br>
     * <i>from NSExtendedCoder native declaration : :35</i>
     */
    void decodeArrayOfObjCType_count_at(java.lang.String itemType, int count, NSArray array);

    /**
     * Original signature : <code>void* decodeBytesWithReturnedLength(NSUInteger*)</code><br>
     * <i>from NSExtendedCoder native declaration : :36</i><br>
     *
     * @deprecated use the safer method {@link #decodeBytesWithReturnedLength(java.nio.IntBuffer)} instead
     */
    @java.lang.Deprecated
    com.sun.jna.Pointer decodeBytesWithReturnedLength(com.sun.jna.ptr.IntByReference lengthp);

    /**
     * Original signature : <code>void* decodeBytesWithReturnedLength(NSUInteger*)</code><br>
     * <i>from NSExtendedCoder native declaration : :36</i>
     */
    com.sun.jna.Pointer decodeBytesWithReturnedLength(java.nio.IntBuffer lengthp);

    /**
     * Original signature : <code>void setObjectZone(NSZone*)</code><br>
     * <i>from NSExtendedCoder native declaration : :38</i>
     */
    void setObjectZone(com.sun.jna.Pointer zone);

    /**
     * Original signature : <code>NSZone* objectZone()</code><br>
     * <i>from NSExtendedCoder native declaration : :39</i>
     */
    com.sun.jna.Pointer objectZone();

    /**
     * Original signature : <code>unsigned systemVersion()</code><br>
     * <i>from NSExtendedCoder native declaration : :41</i>
     */
    int systemVersion();

    /**
     * Original signature : <code>BOOL allowsKeyedCoding()</code><br>
     * <i>from NSExtendedCoder native declaration : :44</i>
     */
    boolean allowsKeyedCoding();
    /**
     * <i>from NSExtendedCoder native declaration : :46</i><br>
     * Conversion Error : /// Original signature : <code>void encodeObject(null, NSString*)</code><br>
     * - (void)encodeObject:(null)objv forKey:(NSString*)key; (Argument objv cannot be converted)
     */
    /**
     * <i>from NSExtendedCoder native declaration : :47</i><br>
     * Conversion Error : /// Original signature : <code>void encodeConditionalObject(null, NSString*)</code><br>
     * - (void)encodeConditionalObject:(null)objv forKey:(NSString*)key; (Argument objv cannot be converted)
     */
    /**
     * Original signature : <code>void encodeBool(BOOL, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :48</i>
     */
    void encodeBool_forKey(boolean boolv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeInt(int, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :49</i>
     */
    void encodeInt_forKey(int intv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeInt32(int32_t, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :50</i>
     */
    void encodeInt32_forKey(int intv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeInt64(int64_t, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :51</i>
     */
    void encodeInt64_forKey(long intv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeFloat(float, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :52</i>
     */
    void encodeFloat_forKey(float realv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeDouble(double, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :53</i>
     */
    void encodeDouble_forKey(double realv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeBytes(const uint8_t*, NSUInteger, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :54</i><br>
     *
     * @deprecated use the safer methods {@link #encodeBytes_length_forKey(byte[], int, com.sun.jna.Pointer)} and {@link #encodeBytes_length_forKey(java.nio.ByteBuffer, int, com.sun.jna.Pointer)} instead
     */
    @java.lang.Deprecated
    void encodeBytes_length_forKey(com.sun.jna.ptr.ByteByReference bytesp, int lenv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeBytes(const uint8_t*, NSUInteger, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :54</i>
     */
    void encodeBytes_length_forKey(byte bytesp[], int lenv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>void encodeBytes(const uint8_t*, NSUInteger, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :54</i>
     */
    void encodeBytes_length_forKey(java.nio.ByteBuffer bytesp, int lenv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>BOOL containsValueForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :56</i>
     */
    boolean containsValueForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>decodeObjectForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :57</i>
     */
    NSObject decodeObjectForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>BOOL decodeBoolForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :58</i>
     */
    boolean decodeBoolForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>int decodeIntForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :59</i>
     */
    int decodeIntForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>int32_t decodeInt32ForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :60</i>
     */
    int decodeInt32ForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>int64_t decodeInt64ForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :61</i>
     */
    long decodeInt64ForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>float decodeFloatForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :62</i>
     */
    float decodeFloatForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>double decodeDoubleForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :63</i>
     */
    double decodeDoubleForKey(com.sun.jna.Pointer key);

    /**
     * Original signature : <code>const uint8_t* decodeBytesForKey(NSString*, NSUInteger*)</code><br>
     * returned bytes immutable!<br>
     * <i>from NSExtendedCoder native declaration : :64</i><br>
     *
     * @deprecated use the safer method {@link #decodeBytesForKey_returnedLength(com.sun.jna.Pointer, java.nio.IntBuffer)} instead
     */
    @java.lang.Deprecated
    com.sun.jna.ptr.ByteByReference decodeBytesForKey_returnedLength(com.sun.jna.Pointer key, com.sun.jna.ptr.IntByReference lengthp);

    /**
     * Original signature : <code>const uint8_t* decodeBytesForKey(NSString*, NSUInteger*)</code><br>
     * returned bytes immutable!<br>
     * <i>from NSExtendedCoder native declaration : :64</i>
     */
    com.sun.jna.ptr.ByteByReference decodeBytesForKey_returnedLength(com.sun.jna.Pointer key, java.nio.IntBuffer lengthp);

    /**
     * Original signature : <code>void encodeInteger(NSInteger, NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :68</i>
     */
    void encodeInteger_forKey(int intv, com.sun.jna.Pointer key);

    /**
     * Original signature : <code>NSInteger decodeIntegerForKey(NSString*)</code><br>
     * <i>from NSExtendedCoder native declaration : :69</i>
     */
    int decodeIntegerForKey(com.sun.jna.Pointer key);
    /**
     * <i>from NSTypedstreamCompatibility native declaration : :80</i><br>
     * Conversion Error : /// Original signature : <code>void encodeNXObject(null)</code><br>
     * - (void)encodeNXObject:(null)object; (Argument object cannot be converted)
     */
    /**
     * Original signature : <code>decodeNXObject()</code><br>
     * <i>from NSTypedstreamCompatibility native declaration : :88</i>
     */
    NSObject decodeNXObject();
}
