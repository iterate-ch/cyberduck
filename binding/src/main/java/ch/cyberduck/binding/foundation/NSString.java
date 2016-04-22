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

import org.rococoa.Foundation;
import org.rococoa.ObjCClass;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :85</i>
public abstract class NSString extends NSObject implements NSCopying {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSString", _Class.class);

    public static NSString stringWithString(String string) {
        return CLASS.stringWithString(string);
    }

    public static String stringByAbbreviatingWithTildeInPath(String string) {
        return CLASS.alloc().initWithString(string).stringByAbbreviatingWithTildeInPath().toString();
    }

    public static String stringByExpandingTildeInPath(String string) {
        return CLASS.alloc().initWithString(string).stringByExpandingTildeInPath().toString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof NSString) {
            return this.toString().equals(other.toString());
        }
        return false;
    }

    @Override
    public String toString() {
        return Foundation.toString(this.id());
    }

    public interface _Class extends ObjCClass {
        NSString alloc();

        /**
         * User-dependent encoding who value is derived from user's default language and potentially other factors. The use of this encoding might sometimes be needed when interpreting user documents with unknown encodings, in the absence of other hints.  This encoding should be used rarely, if at all. Note that some potential values here might result in unexpected encoding conversions of even fairly straightforward NSString content --- for instance, punctuation characters with a bidirectional encoding.<br>
         * Original signature : <code>NSStringEncoding defaultCStringEncoding()</code><br>
         * Should be rarely used<br>
         * <i>from NSStringExtensionMethods native declaration : :242</i>
         */
        NSUInteger defaultCStringEncoding();

        /**
         * Original signature : <code>const NSStringEncoding* availableStringEncodings()</code><br>
         * <i>from NSStringExtensionMethods native declaration : :244</i>
         */
        com.sun.jna.ptr.IntByReference availableStringEncodings();

        /**
         * Original signature : <code>NSString* localizedNameOfStringEncoding(NSStringEncoding)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :245</i>
         */
        NSString localizedNameOfStringEncoding(NSUInteger encoding);

        /**
         * Original signature : <code>string()</code><br>
         * <i>from NSStringExtensionMethods native declaration : :266</i>
         */
        String string();

        /**
         * Original signature : <code>stringWithString(NSString*)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :267</i>
         */
        NSString stringWithString(String string);

        /**
         * Original signature : <code>stringWithCharacters(const unichar*, NSUInteger)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :268</i>
         */
        NSString stringWithCharacters_length(char characters[], NSUInteger length);

        /**
         * Original signature : <code>stringWithUTF8String(const char*)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :269</i>
         */
        NSString stringWithUTF8String(String nullTerminatedCString);

        /**
         * Original signature : <code>stringWithFormat(NSString*, null)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :270</i>
         */
        NSString stringWithFormat(NSString format, NSObject... varargs);

        /**
         * Original signature : <code>localizedStringWithFormat(NSString*, null)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :271</i>
         */
        NSString localizedStringWithFormat(NSString format, NSObject... varargs);

        /**
         * Original signature : <code>stringWithCString(const char*, NSStringEncoding)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :275</i>
         */
        NSString stringWithCString_encoding(String cString, NSUInteger enc);

        /**
         * Original signature : <code>stringWithContentsOfURL(NSURL*, NSStringEncoding, NSError**)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :281</i>
         */
        NSString stringWithContentsOfURL_encoding_error(NSURL url, NSUInteger enc, ObjCObjectByReference error);

        /**
         * Original signature : <code>stringWithContentsOfFile(NSString*, NSStringEncoding, NSError**)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :282</i>
         */
        NSString stringWithContentsOfFile_encoding_error(NSString path, NSUInteger enc, ObjCObjectByReference error);

        /**
         * Original signature : <code>stringWithContentsOfURL(NSURL*, NSStringEncoding*, NSError**)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :288</i>
         */
        NSString stringWithContentsOfURL_usedEncoding_error(NSURL url, java.nio.IntBuffer enc, ObjCObjectByReference error);

        /**
         * Original signature : <code>stringWithContentsOfFile(NSString*, NSStringEncoding*, NSError**)</code><br>
         * <i>from NSStringExtensionMethods native declaration : :289</i>
         */
        NSString stringWithContentsOfFile_usedEncoding_error(NSString path, java.nio.IntBuffer enc, ObjCObjectByReference error);

        /**
         * Original signature : <code>stringWithContentsOfFile(NSString*)</code><br>
         * <i>from NSStringDeprecated native declaration : :358</i>
         */
        NSString stringWithContentsOfFile(NSString path);

        /**
         * Original signature : <code>stringWithContentsOfURL(NSURL*)</code><br>
         * <i>from NSStringDeprecated native declaration : :359</i>
         */
        NSString stringWithContentsOfURL(NSURL url);

        /**
         * Original signature : <code>stringWithCString(const char*, NSUInteger)</code><br>
         * <i>from NSStringDeprecated native declaration : :364</i>
         */
        NSString stringWithCString_length(String bytes, NSUInteger length);

        /**
         * Original signature : <code>stringWithCString(const char*)</code><br>
         * <i>from NSStringDeprecated native declaration : :365</i>
         */
        NSString stringWithCString(String bytes);
    }

    /**
     * NSString primitive (funnel) methods. A minimal subclass of NSString just needs to implement these, although we also recommend getCharacters:range:. See below for the other methods.<br>
     * Original signature : <code>NSUInteger length()</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract NSUInteger length();

    /**
     * Original signature : <code>unichar characterAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract char characterAtIndex(NSUInteger index);

    /**
     * Original signature : <code>void getCharacters(unichar*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :96</i>
     */
    public abstract void getCharacters(char buffer);
    /**
     * <i>from NSStringExtensionMethods native declaration : :97</i><br>
     * Conversion Error : /// Original signature : <code>void getCharacters(unichar*, null)</code><br>
     * - (void)getCharacters:(unichar*)buffer range:(null)aRange; (Argument aRange cannot be converted)
     */
    /**
     * Original signature : <code>NSString* substringFromIndex(NSUInteger)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :99</i>
     */
    public abstract NSString substringFromIndex(NSUInteger from);

    /**
     * Original signature : <code>NSString* substringToIndex(NSUInteger)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :100</i>
     */
    public abstract NSString substringToIndex(NSUInteger to);
    /**
     * <i>from NSStringExtensionMethods native declaration : :101</i><br>
     * Conversion Error : /// Original signature : <code>NSString* substringWithRange(null)</code><br>
     * - (NSString*)substringWithRange:(null)range; // Hint: Use with rangeOfComposedCharacterSequencesForRange: to avoid breaking up composed characters<br>
     *  (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>compare(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :103</i>
     */
    public abstract com.sun.jna.Pointer compare(NSString string);

    /**
     * Original signature : <code>compare(NSString*, NSStringCompareOptions)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :104</i>
     */
    public abstract com.sun.jna.Pointer compare_options(NSString string, int mask);
    /**
     * <i>from NSStringExtensionMethods native declaration : :105</i><br>
     * Conversion Error : /// Original signature : <code>compare(NSString*, NSStringCompareOptions, null)</code><br>
     * - (null)compare:(NSString*)string options:(NSStringCompareOptions)mask range:(null)compareRange; (Argument compareRange cannot be converted)
     */
    /**
     * <i>from NSStringExtensionMethods native declaration : :106</i><br>
     * Conversion Error : /// Original signature : <code>compare(NSString*, NSStringCompareOptions, null, null)</code><br>
     * - (null)compare:(NSString*)string options:(NSStringCompareOptions)mask range:(null)compareRange locale:(null)locale; // locale arg used to be a dictionary pre-Leopard. We now accepts NSLocale. Assumes the current locale if non-nil and non-NSLocale.<br>
     *  (Argument compareRange cannot be converted)
     */
    /**
     * Original signature : <code>caseInsensitiveCompare(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :107</i>
     */
    public abstract com.sun.jna.Pointer caseInsensitiveCompare(NSString string);

    /**
     * Original signature : <code>localizedCompare(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :108</i>
     */
    public abstract com.sun.jna.Pointer localizedCompare(NSString string);

    /**
     * Original signature : <code>localizedCaseInsensitiveCompare(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :109</i>
     */
    public abstract com.sun.jna.Pointer localizedCaseInsensitiveCompare(NSString string);

    /**
     * Original signature : <code>BOOL isEqualToString(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :111</i>
     */
    public abstract boolean isEqualToString(String aString);

    /**
     * Original signature : <code>BOOL hasPrefix(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :113</i>
     */
    public abstract boolean hasPrefix(NSString aString);

    /**
     * Original signature : <code>BOOL hasSuffix(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :114</i>
     */
    public abstract boolean hasSuffix(NSString aString);

    /**
     * These methods return length==0 if the target string is not found. So, to check for containment: ([str rangeOfString:@"target"].length > 0).  Note that the length of the range returned by these methods might be different than the length of the target string, due composed characters and such.<br>
     * Original signature : <code>rangeOfString(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :118</i>
     */
    public abstract NSRange rangeOfString(NSString aString);

    /**
     * Original signature : <code>rangeOfString(NSString*, NSStringCompareOptions)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :119</i>
     */
    public abstract NSRange rangeOfString_options(NSString aString, int mask);
    /**
     * <i>from NSStringExtensionMethods native declaration : :120</i><br>
     * Conversion Error : /// Original signature : <code>rangeOfString(NSString*, NSStringCompareOptions, null)</code><br>
     * - (null)rangeOfString:(NSString*)aString options:(NSStringCompareOptions)mask range:(null)searchRange; (Argument searchRange cannot be converted)
     */
    /**
     * <i>from NSStringExtensionMethods native declaration : :122</i><br>
     * Conversion Error : /// Original signature : <code>rangeOfString(NSString*, NSStringCompareOptions, null, NSLocale*)</code><br>
     * - (null)rangeOfString:(NSString*)aString options:(NSStringCompareOptions)mask range:(null)searchRange locale:(NSLocale*)locale; (Argument searchRange cannot be converted)
     */
    /**
     * These return the range of the first character from the set in the string, not the range of a sequence of characters.<br>
     * Original signature : <code>rangeOfCharacterFromSet(NSCharacterSet*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :127</i>
     */
    public abstract NSRange rangeOfCharacterFromSet(com.sun.jna.Pointer aSet);

    /**
     * Original signature : <code>rangeOfCharacterFromSet(NSCharacterSet*, NSStringCompareOptions)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :128</i>
     */
    public abstract NSRange rangeOfCharacterFromSet_options(com.sun.jna.Pointer aSet, int mask);
    /**
     * <i>from NSStringExtensionMethods native declaration : :129</i><br>
     * Conversion Error : /// Original signature : <code>rangeOfCharacterFromSet(NSCharacterSet*, NSStringCompareOptions, null)</code><br>
     * - (null)rangeOfCharacterFromSet:(NSCharacterSet*)aSet options:(NSStringCompareOptions)mask range:(null)searchRange; (Argument searchRange cannot be converted)
     */
    /**
     * Original signature : <code>rangeOfComposedCharacterSequenceAtIndex(NSUInteger)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :131</i>
     */
    public abstract NSRange rangeOfComposedCharacterSequenceAtIndex(NSUInteger index);
    /**
     * <i>from NSStringExtensionMethods native declaration : :133</i><br>
     * Conversion Error : /// Original signature : <code>rangeOfComposedCharacterSequencesForRange(null)</code><br>
     * - (null)rangeOfComposedCharacterSequencesForRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>NSString* stringByAppendingString(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :136</i>
     */
    public abstract NSString stringByAppendingString(NSString aString);

    /**
     * Original signature : <code>NSString* stringByAppendingFormat(NSString*, null)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :137</i>
     */
    public abstract NSString stringByAppendingFormat(NSString format, NSObject... varargs);

    /**
     * The following convenience methods all skip initial space characters (whitespaceSet) and ignore trailing characters. NSScanner can be used for more "exact" parsing of numbers.<br>
     * Original signature : <code>double doubleValue()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :141</i>
     */
    public abstract double doubleValue();

    /**
     * Original signature : <code>float floatValue()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :142</i>
     */
    public abstract float floatValue();

    /**
     * Original signature : <code>int intValue()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :143</i>
     */
    public abstract int intValue();

    /**
     * Original signature : <code>NSInteger integerValue()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :145</i>
     */
    public abstract NSInteger integerValue();

    /**
     * Original signature : <code>long long longLongValue()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :146</i>
     */
    public abstract long longLongValue();

    /**
     * Original signature : <code>BOOL boolValue()</code><br>
     * Skips initial space characters (whitespaceSet), or optional -/+ sign followed by zeroes. Returns YES on encountering one of "Y", "y", "T", "t", or a digit 1-9. It ignores any trailing characters.<br>
     * <i>from NSStringExtensionMethods native declaration : :147</i>
     */
    public abstract boolean boolValue();

    /**
     * Original signature : <code>NSArray* componentsSeparatedByString(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :150</i>
     */
    public abstract NSArray componentsSeparatedByString(NSString separator);

    /**
     * Original signature : <code>NSArray* componentsSeparatedByCharactersInSet(NSCharacterSet*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :152</i>
     */
    public abstract NSArray componentsSeparatedByCharactersInSet(com.sun.jna.Pointer separator);

    /**
     * Original signature : <code>NSString* commonPrefixWithString(NSString*, NSStringCompareOptions)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :155</i>
     */
    public abstract NSString commonPrefixWithString_options(NSString aString, int mask);

    /**
     * Original signature : <code>NSString* uppercaseString()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :157</i>
     */
    public abstract NSString uppercaseString();

    /**
     * Original signature : <code>NSString* lowercaseString()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :158</i>
     */
    public abstract NSString lowercaseString();

    /**
     * Original signature : <code>NSString* capitalizedString()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :159</i>
     */
    public abstract NSString capitalizedString();

    /**
     * Original signature : <code>NSString* stringByTrimmingCharactersInSet(NSCharacterSet*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :162</i>
     */
    public abstract NSString stringByTrimmingCharactersInSet(com.sun.jna.Pointer set);

    /**
     * Original signature : <code>NSString* stringByPaddingToLength(NSUInteger, NSString*, NSUInteger)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :163</i>
     */
    public abstract NSString stringByPaddingToLength_withString_startingAtIndex(NSUInteger newLength, NSString padString, NSUInteger padIndex);
    /**
     * <i>from NSStringExtensionMethods native declaration : :166</i><br>
     * Conversion Error : /// Original signature : <code>void getLineStart(NSUInteger*, NSUInteger*, NSUInteger*, null)</code><br>
     * - (void)getLineStart:(NSUInteger*)startPtr end:(NSUInteger*)lineEndPtr contentsEnd:(NSUInteger*)contentsEndPtr forRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>from NSStringExtensionMethods native declaration : :167</i><br>
     * Conversion Error : /// Original signature : <code>lineRangeForRange(null)</code><br>
     * - (null)lineRangeForRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>from NSStringExtensionMethods native declaration : :170</i><br>
     * Conversion Error : /// Original signature : <code>void getParagraphStart(NSUInteger*, NSUInteger*, NSUInteger*, null)</code><br>
     * - (void)getParagraphStart:(NSUInteger*)startPtr end:(NSUInteger*)parEndPtr contentsEnd:(NSUInteger*)contentsEndPtr forRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>from NSStringExtensionMethods native declaration : :171</i><br>
     * Conversion Error : /// Original signature : <code>paragraphRangeForRange(null)</code><br>
     * - (null)paragraphRangeForRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>NSString* description()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :174</i>
     */
    public abstract String description();

    /**
     * If two objects are equal (as determined by the isEqual: method), they must have the same hash value. This
     * last point is particularly important if you define hash in a subclass and intend to put
     * instances of that subclass into a collection.
     * <p/>
     * If a mutable object is added to a collection that uses hash values to determine the object’s
     * position in the collection, the value returned by the hash method of the object must not change
     * while the object is in the collection. Therefore, either the hash method must not rely on any of
     * the object’s internal state information or you must make sure the object’s internal state information
     * does not change while the object is in the collection. Thus, for example, a mutable dictionary can be
     * put in a hash table but you must not change it while it is in there. (Note that it can be difficult to
     * know whether or not a given object is in a collection.)
     * <p/>
     * Original signature : <code>NSUInteger hash()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :176</i>
     *
     * @return An integer that can be used as a table address in a hash table structure.
     */
    public abstract NSUInteger hash();

    /**
     * Original signature : <code>NSStringEncoding fastestEncoding()</code><br>
     * Result in O(1) time; a rough estimate<br>
     * <i>from NSStringExtensionMethods native declaration : :180</i>
     */
    public abstract NSUInteger fastestEncoding();

    /**
     * Original signature : <code>NSStringEncoding smallestEncoding()</code><br>
     * Result in O(n) time; the encoding in which the string is most compact<br>
     * <i>from NSStringExtensionMethods native declaration : :181</i>
     */
    public abstract NSUInteger smallestEncoding();

    /**
     * Original signature : <code>NSData* dataUsingEncoding(NSStringEncoding, BOOL)</code><br>
     * External representation<br>
     * <i>from NSStringExtensionMethods native declaration : :183</i>
     */
    public abstract NSData dataUsingEncoding_allowLossyConversion(NSUInteger encoding, boolean lossy);

    /**
     * Original signature : <code>NSData* dataUsingEncoding(NSStringEncoding)</code><br>
     * External representation<br>
     * <i>from NSStringExtensionMethods native declaration : :184</i>
     */
    public abstract NSData dataUsingEncoding(NSUInteger encoding);

    /**
     * Original signature : <code>BOOL canBeConvertedToEncoding(NSStringEncoding)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :186</i>
     */
    public abstract boolean canBeConvertedToEncoding(NSUInteger encoding);

    /**
     * Methods to convert NSString to a NULL-terminated cString using the specified encoding. Note, these are the "new" cString methods, and are not deprecated like the older cString methods which do not take encoding arguments.<br>
     * Original signature : <code>const char* cStringUsingEncoding(NSStringEncoding)</code><br>
     * "Autoreleased"; NULL return if encoding conversion not possible; for performance reasons, lifetime of this should not be considered longer than the lifetime of the receiving string (if the receiver string is freed, this might go invalid then, before the end of the autorelease scope)<br>
     * <i>from NSStringExtensionMethods native declaration : :191</i>
     */
    public abstract com.sun.jna.ptr.ByteByReference cStringUsingEncoding(NSUInteger encoding);

    /**
     * Original signature : <code>BOOL getCString(char*, NSUInteger, NSStringEncoding)</code><br>
     * NO return if conversion not possible due to encoding errors or too small of a buffer. The buffer should include room for maxBufferCount bytes; this number should accomodate the expected size of the return value plus the NULL termination character, which this method adds. (So note that the maxLength passed to this method is one more than the one you would have passed to the deprecated getCString:maxLength:.)<br>
     * <i>from NSStringExtensionMethods native declaration : :192</i>
     */
    public abstract boolean getCString_maxLength_encoding(java.nio.ByteBuffer buffer, NSUInteger maxBufferCount, NSUInteger encoding);
    /**
     * <i>from NSStringExtensionMethods native declaration : :205</i><br>
     * Conversion Error : /**<br>
     *  * Use this to convert string section at a time into a fixed-size buffer, without any allocations.  Does not NULL-terminate. <br>
     *  * buffer is the buffer to write to; if NULL, this method can be used to computed size of needed buffer.<br>
     *  * maxBufferCount is the length of the buffer in bytes. It's a good idea to make sure this is at least enough to hold one character's worth of conversion. <br>
     *  * usedBufferCount is the length of the buffer used up by the current conversion. Can be NULL.<br>
     *  * encoding is the encoding to convert to.<br>
     *  * options specifies the options to apply.<br>
     *  * range is the range to convert.<br>
     *  * leftOver is the remaining range. Can be NULL.<br>
     *  * YES return indicates some characters were converted. Conversion might usually stop when the buffer fills, <br>
     *  * but it might also stop when the conversion isn't possible due to the chosen encoding.<br>
     *  * Original signature : <code>BOOL getBytes(void*, NSUInteger, NSUInteger*, NSStringEncoding, NSStringEncodingConversionOptions, null, null)</code><br>
     *  * /<br>
     * - (BOOL)getBytes:(void*)buffer maxLength:(NSUInteger)maxBufferCount usedLength:(NSUInteger*)usedBufferCount encoding:(NSStringEncoding)encoding options:(NSStringEncodingConversionOptions)options range:(null)range remainingRange:(null)leftover; (Argument range cannot be converted)
     */
    /**
     * These return the maximum and exact number of bytes needed to store the receiver in the specified encoding in non-external representation. The first one is O(1), while the second one is O(n). These do not include space for a terminating null.<br>
     * Original signature : <code>NSUInteger maximumLengthOfBytesUsingEncoding(NSStringEncoding)</code><br>
     * Result in O(1) time; the estimate may be way over what's needed<br>
     * <i>from NSStringExtensionMethods native declaration : :209</i>
     */
    public abstract NSUInteger maximumLengthOfBytesUsingEncoding(int enc);

    /**
     * Original signature : <code>NSUInteger lengthOfBytesUsingEncoding(NSStringEncoding)</code><br>
     * Result in O(n) time; the result is exact<br>
     * <i>from NSStringExtensionMethods native declaration : :210</i>
     */
    public abstract NSUInteger lengthOfBytesUsingEncoding(int enc);

    /**
     * Original signature : <code>NSString* decomposedStringWithCanonicalMapping()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :214</i>
     */
    public abstract NSString decomposedStringWithCanonicalMapping();

    /**
     * Original signature : <code>NSString* precomposedStringWithCanonicalMapping()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :215</i>
     */
    public abstract NSString precomposedStringWithCanonicalMapping();

    /**
     * Original signature : <code>NSString* decomposedStringWithCompatibilityMapping()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :216</i>
     */
    public abstract NSString decomposedStringWithCompatibilityMapping();

    /**
     * Original signature : <code>NSString* precomposedStringWithCompatibilityMapping()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :217</i>
     */
    public abstract NSString precomposedStringWithCompatibilityMapping();

    /**
     * Returns a string with the character folding options applied. theOptions is a mask of compare flags with *InsensitiveSearch suffix.<br>
     * Original signature : <code>NSString* stringByFoldingWithOptions(NSStringCompareOptions, NSLocale*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :223</i>
     */
    public abstract NSString stringByFoldingWithOptions_locale(int options, com.sun.jna.Pointer locale);
    /**
     * <i>from NSStringExtensionMethods native declaration : :227</i><br>
     * Conversion Error : /**<br>
     *  * Replace all occurrences of the target string in the specified range with replacement. Specified compare options are used for matching target.<br>
     *  * Original signature : <code>NSString* stringByReplacingOccurrencesOfString(NSString*, NSString*, NSStringCompareOptions, null)</code><br>
     *  * /<br>
     * - (NSString*)stringByReplacingOccurrencesOfString:(NSString*)target withString:(NSString*)replacement options:(NSStringCompareOptions)options range:(null)searchRange; (Argument searchRange cannot be converted)
     */
    /**
     * Replace all occurrences of the target string with replacement. Invokes the above method with 0 options and range of the whole string.<br>
     * Original signature : <code>NSString* stringByReplacingOccurrencesOfString(NSString*, NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :231</i>
     */
    public abstract NSString stringByReplacingOccurrencesOfString_withString(NSString target, NSString replacement);
    /**
     * <i>from NSStringExtensionMethods native declaration : :235</i><br>
     * Conversion Error : /**<br>
     *  * Replace characters in range with the specified string, returning new string.<br>
     *  * Original signature : <code>NSString* stringByReplacingCharactersInRange(null, NSString*)</code><br>
     *  * /<br>
     * - (NSString*)stringByReplacingCharactersInRange:(null)range withString:(NSString*)replacement; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>const char* UTF8String()</code><br>
     * Convenience to return null-terminated UTF8 representation<br>
     * <i>from NSStringExtensionMethods native declaration : :238</i>
     */
    public abstract com.sun.jna.ptr.ByteByReference UTF8String();

    /**
     * In general creation methods in NSString do not apply to subclassers, as subclassers are assumed to provide their own init methods which create the string in the way the subclass wishes.  Designated initializers of NSString are thus init and initWithCoder:.<br>
     * Original signature : <code>init()</code><br>
     * <i>from NSStringExtensionMethods native declaration : :251</i>
     */
    public abstract NSString init();

    /**
     * Original signature : <code>initWithCharactersNoCopy(unichar*, NSUInteger, BOOL)</code><br>
     * "NoCopy" is a hint<br>
     * <i>from NSStringExtensionMethods native declaration : :252</i>
     */
    public abstract NSString initWithCharactersNoCopy_length_freeWhenDone(char characters, NSUInteger length, boolean freeBuffer);

    /**
     * Original signature : <code>initWithCharacters(const unichar*, NSUInteger)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :253</i>
     */
    public abstract NSString initWithCharacters_length(char characters[], NSUInteger length);

    /**
     * Original signature : <code>initWithUTF8String(const char*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :254</i>
     */
    public abstract NSString initWithUTF8String(String nullTerminatedCString);

    /**
     * Original signature : <code>initWithString(NSString*)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :255</i>
     */
    public abstract NSString initWithString(String aString);

    /**
     * Original signature : <code>initWithFormat(NSString*, null)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :256</i>
     */
    public abstract NSString initWithFormat(String format, NSObject... varargs);
    /**
     * <i>from NSStringExtensionMethods native declaration : :257</i><br>
     * Conversion Error : /// Original signature : <code>initWithFormat(NSString*, null)</code><br>
     * - (null)initWithFormat:(NSString*)format arguments:(null)argList; (Argument argList cannot be converted)
     */
    /**
     * <i>from NSStringExtensionMethods native declaration : :258</i><br>
     * Conversion Error : /// Original signature : <code>initWithFormat(NSString*, null, null)</code><br>
     * - (null)initWithFormat:(NSString*)format locale:(null)locale, ...; (Argument locale cannot be converted)
     */
    /**
     * <i>from NSStringExtensionMethods native declaration : :259</i><br>
     * Conversion Error : /// Original signature : <code>initWithFormat(NSString*, null, null)</code><br>
     * - (null)initWithFormat:(NSString*)format locale:(null)locale arguments:(null)argList; (Argument locale cannot be converted)
     */
    /**
     * Original signature : <code>initWithData(NSData*, NSStringEncoding)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :260</i>
     */
    public abstract NSString initWithData_encoding(NSData data, NSUInteger encoding);

    /**
     * Original signature : <code>initWithBytes(const void*, NSUInteger, NSStringEncoding)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :261</i>
     */
    public abstract NSString initWithBytes_length_encoding(com.sun.jna.Pointer bytes, NSUInteger len, NSUInteger encoding);

    /**
     * Original signature : <code>initWithBytesNoCopy(void*, NSUInteger, NSStringEncoding, BOOL)</code><br>
     * "NoCopy" is a hint<br>
     * <i>from NSStringExtensionMethods native declaration : :263</i>
     */
    public abstract NSString initWithBytesNoCopy_length_encoding_freeWhenDone(com.sun.jna.Pointer bytes, NSUInteger len, NSUInteger encoding, boolean freeBuffer);

    /**
     * Original signature : <code>initWithCString(const char*, NSStringEncoding)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :274</i>
     */
    public abstract NSString initWithCString_encoding(String nullTerminatedCString, NSUInteger encoding);

    /**
     * These use the specified encoding.  If nil is returned, the optional error return indicates problem that was encountered (for instance, file system or encoding errors).<br>
     * Original signature : <code>initWithContentsOfURL(NSURL*, NSStringEncoding, NSError**)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :279</i>
     */
    public abstract NSString initWithContentsOfURL_encoding_error(NSURL url, NSUInteger enc, ObjCObjectByReference error);

    /**
     * Original signature : <code>initWithContentsOfFile(NSString*, NSStringEncoding, NSError**)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :280</i>
     */
    public abstract NSString initWithContentsOfFile_encoding_error(NSString path, NSUInteger enc, ObjCObjectByReference error);

    /**
     * These try to determine the encoding, and return the encoding which was used.  Note that these methods might get "smarter" in subsequent releases of the system, and use additional techniques for recognizing encodings. If nil is returned, the optional error return indicates problem that was encountered (for instance, file system or encoding errors).<br>
     * Original signature : <code>initWithContentsOfURL(NSURL*, NSStringEncoding*, NSError**)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :286</i>
     */
    public abstract NSString initWithContentsOfURL_usedEncoding_error(NSURL url, java.nio.IntBuffer enc, ObjCObjectByReference error);

    /**
     * Original signature : <code>initWithContentsOfFile(NSString*, NSStringEncoding*, NSError**)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :287</i>
     */
    public abstract NSString initWithContentsOfFile_usedEncoding_error(NSString path, java.nio.IntBuffer enc, ObjCObjectByReference error);

    /**
     * Write to specified url or path using the specified encoding.  The optional error return is to indicate file system or encoding errors.<br>
     * Original signature : <code>BOOL writeToURL(NSURL*, BOOL, NSStringEncoding, NSError**)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :293</i>
     */
    public abstract boolean writeToURL_atomically_encoding_error(NSURL url, boolean useAuxiliaryFile, NSUInteger enc, ObjCObjectByReference error);

    /**
     * Original signature : <code>BOOL writeToFile(NSString*, BOOL, NSStringEncoding, NSError**)</code><br>
     * <i>from NSStringExtensionMethods native declaration : :294</i>
     */
    public abstract boolean writeToFile_atomically_encoding_error(NSString path, boolean useAuxiliaryFile, NSUInteger enc, ObjCObjectByReference error);

    /**
     * Original signature : <code>propertyList()</code><br>
     * <i>from NSExtendedStringPropertyListParsing native declaration : :335</i>
     */
    public abstract com.sun.jna.Pointer propertyList();

    /**
     * Original signature : <code>NSDictionary* propertyListFromStringsFileFormat()</code><br>
     * <i>from NSExtendedStringPropertyListParsing native declaration : :336</i>
     */
    public abstract NSDictionary propertyListFromStringsFileFormat();

    /**
     * The methods in this category are deprecated and will be removed from this header file in the near future. These methods use [NSString defaultCStringEncoding] as the encoding to convert to, which means the results depend on the user's language and potentially other settings. This might be appropriate in some cases, but often these methods are misused, resulting in issues when running in languages other then English. UTF8String in general is a much better choice when converting arbitrary NSStrings into 8-bit representations. Additional potential replacement methods are being introduced in NSString as appropriate.<br>
     * Original signature : <code>const char* cString()</code><br>
     * <i>from NSStringDeprecated native declaration : :346</i>
     */
    public abstract com.sun.jna.ptr.ByteByReference cString();

    /**
     * Original signature : <code>const char* lossyCString()</code><br>
     * <i>from NSStringDeprecated native declaration : :347</i>
     */
    public abstract com.sun.jna.ptr.ByteByReference lossyCString();

    /**
     * Original signature : <code>NSUInteger cStringLength()</code><br>
     * <i>from NSStringDeprecated native declaration : :348</i>
     */
    public abstract NSUInteger cStringLength();

    /**
     * Original signature : <code>void getCString(char*)</code><br>
     * <i>from NSStringDeprecated native declaration : :349</i>
     */
    public abstract void getCString(java.nio.ByteBuffer bytes);

    /**
     * Original signature : <code>void getCString(char*, NSUInteger)</code><br>
     * <i>from NSStringDeprecated native declaration : :350</i>
     */
    public abstract void getCString_maxLength(java.nio.ByteBuffer bytes, NSUInteger maxLength);
    /**
     * <i>from NSStringDeprecated native declaration : :351</i><br>
     * Conversion Error : /// Original signature : <code>void getCString(char*, NSUInteger, null, null)</code><br>
     * - (void)getCString:(char*)bytes maxLength:(NSUInteger)maxLength range:(null)aRange remainingRange:(null)leftoverRange; (Argument aRange cannot be converted)
     */
    /**
     * Original signature : <code>BOOL writeToFile(NSString*, BOOL)</code><br>
     * <i>from NSStringDeprecated native declaration : :353</i>
     */
    public abstract boolean writeToFile_atomically(NSString path, boolean useAuxiliaryFile);

    /**
     * Original signature : <code>BOOL writeToURL(NSURL*, BOOL)</code><br>
     * <i>from NSStringDeprecated native declaration : :354</i>
     */
    public abstract boolean writeToURL_atomically(NSURL url, boolean atomically);

    /**
     * Original signature : <code>initWithContentsOfFile(NSString*)</code><br>
     * <i>from NSStringDeprecated native declaration : :356</i>
     */
    public abstract NSString initWithContentsOfFile(NSString path);

    /**
     * Original signature : <code>initWithContentsOfURL(NSURL*)</code><br>
     * <i>from NSStringDeprecated native declaration : :357</i>
     */
    public abstract NSString initWithContentsOfURL(NSURL url);

    /**
     * Original signature : <code>initWithCStringNoCopy(char*, NSUInteger, BOOL)</code><br>
     * <i>from NSStringDeprecated native declaration : :361</i>
     */
    public abstract NSString initWithCStringNoCopy_length_freeWhenDone(java.nio.ByteBuffer bytes, NSUInteger length, boolean freeBuffer);

    /**
     * Original signature : <code>initWithCString(const char*, NSUInteger)</code><br>
     * <i>from NSStringDeprecated native declaration : :362</i>
     */
    public abstract NSString initWithCString_length(String bytes, NSUInteger length);

    /**
     * Original signature : <code>initWithCString(const char*)</code><br>
     * <i>from NSStringDeprecated native declaration : :363</i>
     */
    public abstract NSString initWithCString(String bytes);

    /**
     * Original signature : <code>NSArray* pathComponents()</code><br>
     * <i>native declaration : :20</i>
     */
    public abstract NSArray pathComponents();

    /**
     * Original signature : <code>BOOL isAbsolutePath()</code><br>
     * <i>native declaration : :22</i>
     */
    public abstract boolean isAbsolutePath();

    /**
     * Original signature : <code>NSString* lastPathComponent()</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract NSString lastPathComponent();

    /**
     * Original signature : <code>NSString* stringByDeletingLastPathComponent()</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract NSString stringByDeletingLastPathComponent();

    /**
     * Original signature : <code>NSString* stringByAppendingPathComponent(NSString*)</code><br>
     * <i>native declaration : :26</i>
     */
    public abstract NSString stringByAppendingPathComponent(NSString str);

    /**
     * Original signature : <code>NSString* pathExtension()</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract NSString pathExtension();

    /**
     * Original signature : <code>NSString* stringByDeletingPathExtension()</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract NSString stringByDeletingPathExtension();

    /**
     * Original signature : <code>NSString* stringByAppendingPathExtension(NSString*)</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract NSString stringByAppendingPathExtension(NSString str);

    /**
     * Original signature : <code>NSString* stringByAbbreviatingWithTildeInPath()</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract NSString stringByAbbreviatingWithTildeInPath();

    /**
     * Original signature : <code>NSString* stringByExpandingTildeInPath()</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract NSString stringByExpandingTildeInPath();

    /**
     * Original signature : <code>NSString* stringByStandardizingPath()</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract NSString stringByStandardizingPath();

    /**
     * Original signature : <code>NSString* stringByResolvingSymlinksInPath()</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract NSString stringByResolvingSymlinksInPath();

    /**
     * Original signature : <code>NSArray* stringsByAppendingPaths(NSArray*)</code><br>
     * <i>native declaration : :39</i>
     */
    public abstract NSArray stringsByAppendingPaths(NSArray paths);
    /**
     * Original signature : <code>NSUInteger completePathIntoString(NSString**, BOOL, NSArray**, NSArray*)</code><br>
     * <i>native declaration : :41</i>
     */
}

