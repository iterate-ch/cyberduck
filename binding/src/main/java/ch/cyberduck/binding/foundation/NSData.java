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
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :27</i>
public abstract class NSData extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSData", _Class.class);

    public static NSData dataWithContentsOfURL(NSURL url) {
        return CLASS.dataWithContentsOfURL(url);
    }

    public static NSData dataWithBase64EncodedString(String base64String) {
        return CLASS.alloc().initWithBase64Encoding(base64String);
    }

    public interface _Class extends ObjCClass {
        NSData alloc();

        /**
         * Original signature : <code>data()</code><br>
         * <i>from NSDataCreation native declaration : :53</i>
         */
        NSData data();

        /**
         * Original signature : <code>dataWithBytes(const void*, NSUInteger)</code><br>
         * <i>from NSDataCreation native declaration : :54</i>
         */
        NSData dataWithBytes_length(com.sun.jna.Pointer bytes, NSUInteger length);

        /**
         * Original signature : <code>dataWithBytesNoCopy(void*, NSUInteger)</code><br>
         * <i>from NSDataCreation native declaration : :55</i>
         */
        NSData dataWithBytesNoCopy_length(com.sun.jna.Pointer bytes, NSUInteger length);

        /**
         * Original signature : <code>dataWithBytesNoCopy(void*, NSUInteger, BOOL)</code><br>
         * <i>from NSDataCreation native declaration : :57</i>
         */
        NSData dataWithBytesNoCopy_length_freeWhenDone(com.sun.jna.Pointer bytes, NSUInteger length, byte b);

        /**
         * Original signature : <code>dataWithContentsOfFile(NSString*, NSUInteger, NSError**)</code><br>
         * <i>from NSDataCreation native declaration : :60</i>
         */
        NSData dataWithContentsOfFile_options_error(String path, int readOptionsMask, ObjCObjectByReference errorPtr);

        /**
         * Original signature : <code>dataWithContentsOfURL(NSURL*, NSUInteger, NSError**)</code><br>
         * <i>from NSDataCreation native declaration : :61</i>
         */
        NSData dataWithContentsOfURL_options_error(NSURL url, int readOptionsMask, ObjCObjectByReference errorPtr);

        /**
         * Original signature : <code>dataWithContentsOfFile(NSString*)</code><br>
         * <i>from NSDataCreation native declaration : :63</i>
         */
        NSData dataWithContentsOfFile(String path);

        /**
         * Original signature : <code>dataWithContentsOfURL(NSURL*)</code><br>
         * <i>from NSDataCreation native declaration : :64</i>
         */
        NSData dataWithContentsOfURL(NSURL url);

        /**
         * Original signature : <code>dataWithContentsOfMappedFile(NSString*)</code><br>
         * <i>from NSDataCreation native declaration : :65</i>
         */
        NSData dataWithContentsOfMappedFile(String path);

        /**
         * NSData+Base64
         */
        NSData dataWithBase64EncodedString(String string);

        /**
         * Original signature : <code>dataWithData(NSData*)</code><br>
         * <i>from NSDataCreation native declaration : :79</i>
         */
        NSData dataWithData(NSData data);
    }

    /**
     * Original signature : <code>NSUInteger length()</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract NSUInteger length();

    /**
     * Original signature : <code>const void* bytes()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract com.sun.jna.Pointer bytes();

    /**
     * Original signature : <code>NSString* description()</code><br>
     * <i>from NSExtendedData native declaration : :36</i>
     */
    public abstract String description();

    /**
     * Original signature : <code>void getBytes(void*)</code><br>
     * <i>from NSExtendedData native declaration : :37</i>
     */
    public abstract void getBytes(com.sun.jna.Pointer buffer);

    /**
     * Original signature : <code>void getBytes(void*, NSUInteger)</code><br>
     * <i>from NSExtendedData native declaration : :38</i>
     */
    public abstract void getBytes_length(com.sun.jna.Pointer buffer, NSUInteger length);
    /**
     * <i>from NSExtendedData native declaration : :39</i><br>
     * Conversion Error : /// Original signature : <code>void getBytes(void*, null)</code><br>
     * - (void)getBytes:(void*)buffer range:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>BOOL isEqualToData(NSData*)</code><br>
     * <i>from NSExtendedData native declaration : :40</i>
     */
    public abstract byte isEqualToData(NSData other);
    /**
     * <i>from NSExtendedData native declaration : :41</i><br>
     * Conversion Error : /// Original signature : <code>NSData* subdataWithRange(null)</code><br>
     * - (NSData*)subdataWithRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>BOOL writeToFile(NSString*, BOOL)</code><br>
     * <i>from NSExtendedData native declaration : :42</i>
     */
    public abstract byte writeToFile_atomically(String path, boolean useAuxiliaryFile);

    public byte writeToFile(String path) {
        return this.writeToFile_atomically(path, true);
    }

    /**
     * Original signature : <code>BOOL writeToURL(NSURL*, BOOL)</code><br>
     * the atomically flag is ignored if the url is not of a type the supports atomic writes<br>
     * <i>from NSExtendedData native declaration : :43</i>
     */
    public abstract boolean writeToURL_atomically(NSURL url, boolean atomically);

    public boolean writeToURL(NSURL url) {
        return this.writeToURL_atomically(url, true);
    }

    /**
     * Original signature : <code>BOOL writeToFile(NSString*, NSUInteger, NSError**)</code><br>
     * <i>from NSExtendedData native declaration : :45</i>
     */
    public abstract boolean writeToFile_options_error(String path, int writeOptionsMask, ObjCObjectByReference errorPtr);

    /**
     * Original signature : <code>BOOL writeToURL(NSURL*, NSUInteger, NSError**)</code><br>
     * <i>from NSExtendedData native declaration : :46</i>
     */
    public abstract boolean writeToURL_options_error(NSURL url, int writeOptionsMask, ObjCObjectByReference errorPtr);

    /**
     * Original signature : <code>initWithBytes(const void*, NSUInteger)</code><br>
     * <i>from NSDataCreation native declaration : :66</i>
     */
    public abstract NSData initWithBytes_length(com.sun.jna.Pointer bytes, NSUInteger length);

    /**
     * Original signature : <code>initWithBytesNoCopy(void*, NSUInteger)</code><br>
     * <i>from NSDataCreation native declaration : :67</i>
     */
    public abstract NSData initWithBytesNoCopy_length(com.sun.jna.Pointer bytes, int length);

    /**
     * Original signature : <code>initWithBytesNoCopy(void*, NSUInteger, BOOL)</code><br>
     * <i>from NSDataCreation native declaration : :69</i>
     */
    public abstract NSData initWithBytesNoCopy_length_freeWhenDone(com.sun.jna.Pointer bytes, NSUInteger length, byte b);

    /**
     * Original signature : <code>initWithContentsOfFile(NSString*, NSUInteger, NSError**)</code><br>
     * <i>from NSDataCreation native declaration : :72</i>
     */
    public abstract NSData initWithContentsOfFile_options_error(String path, int readOptionsMask, ObjCObjectByReference errorPtr);

    /**
     * Original signature : <code>initWithContentsOfURL(NSURL*, NSUInteger, NSError**)</code><br>
     * <i>from NSDataCreation native declaration : :73</i>
     */
    public abstract NSData initWithContentsOfURL_options_error(NSURL url, int readOptionsMask, ObjCObjectByReference errorPtr);

    /**
     * Original signature : <code>initWithContentsOfFile(NSString*)</code><br>
     * <i>from NSDataCreation native declaration : :75</i>
     */
    public abstract NSData initWithContentsOfFile(String path);

    /**
     * Original signature : <code>initWithContentsOfURL(NSURL*)</code><br>
     * <i>from NSDataCreation native declaration : :76</i>
     */
    public abstract NSData initWithContentsOfURL(NSURL url);

    /**
     * Original signature : <code>initWithContentsOfMappedFile(NSString*)</code><br>
     * <i>from NSDataCreation native declaration : :77</i>
     */
    public abstract NSData initWithContentsOfMappedFile(String path);

    /**
     * Original signature : <code>initWithData(NSData*)</code><br>
     * <i>from NSDataCreation native declaration : :78</i>
     */
    public abstract NSData initWithData(NSData data);

    /**
     * Returns a data object initialized with the given Base-64 encoded string
     */
    public abstract NSData initWithBase64Encoding(String base64String);

    /**
     * Create a Base-64 encoded NSString from the receiver's contents
     */
    public abstract String base64Encoding();

}

