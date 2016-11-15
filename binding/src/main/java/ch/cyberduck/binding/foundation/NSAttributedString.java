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
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:9</i>
public abstract class NSAttributedString extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSAttributedString", _Class.class);

    public static NSAttributedString attributedString(String str) {
        if(null == str) {
            str = "";
        }
        return CLASS.alloc().initWithString(str);
    }

    public static NSAttributedString attributedStringWithAttributes(String str, NSDictionary attrs) {
        if(null == str) {
            str = "";
        }
        return CLASS.alloc().initWithString_attributes(str, attrs);
    }

    public interface _Class extends ObjCClass {
        /**
         * Methods to determine what types can be loaded as NSAttributedStrings.<br>
         * Original signature : <code>NSArray* textTypes()</code><br>
         * <i>from NSAttributedStringKitAdditions native declaration : :183</i>
         */
        NSArray textTypes();

        /**
         * Original signature : <code>NSArray* textUnfilteredTypes()</code><br>
         * <i>from NSAttributedStringKitAdditions native declaration : :184</i>
         */
        NSArray textUnfilteredTypes();

        /**
         * Methods that were deprecated in Mac OS 10.5. You can now use +textTypes and +textUnfilteredTypes to get arrays of Uniform Type Identifiers (UTIs).<br>
         * Original signature : <code>NSArray* textFileTypes()</code><br>
         * <i>from NSDeprecatedKitAdditions native declaration : :249</i>
         */
        NSArray textFileTypes();

        /**
         * Original signature : <code>NSArray* textPasteboardTypes()</code><br>
         * <i>from NSDeprecatedKitAdditions native declaration : :250</i>
         */
        NSArray textPasteboardTypes();

        /**
         * Original signature : <code>NSArray* textUnfilteredFileTypes()</code><br>
         * <i>from NSDeprecatedKitAdditions native declaration : :251</i>
         */
        NSArray textUnfilteredFileTypes();

        /**
         * Original signature : <code>NSArray* textUnfilteredPasteboardTypes()</code><br>
         * <i>from NSDeprecatedKitAdditions native declaration : :252</i>
         */
        NSArray textUnfilteredPasteboardTypes();

        NSAttributedString alloc();
    }

    /**
     * Original signature : <code>NSString* string()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:11</i>
     */
    public abstract String string();
    /**
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:12</i><br>
     * Conversion Error : /// Original signature : <code>NSDictionary* attributesAtIndex(NSUInteger, null)</code><br>
     * - (NSDictionary*)attributesAtIndex:(NSUInteger)location effectiveRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>NSUInteger length()</code><br>
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:18</i>
     */
    public abstract NSUInteger length();
    /**
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:19</i><br>
     * Conversion Error : /// Original signature : <code>attribute(NSString*, NSUInteger, null)</code><br>
     * - (null)attribute:(NSString*)attrName atIndex:(NSUInteger)location effectiveRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:20</i><br>
     * Conversion Error : /// Original signature : <code>NSAttributedString* attributedSubstringFromRange(null)</code><br>
     * - (NSAttributedString*)attributedSubstringFromRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:22</i><br>
     * Conversion Error : /// Original signature : <code>NSDictionary* attributesAtIndex(NSUInteger, null, null)</code><br>
     * - (NSDictionary*)attributesAtIndex:(NSUInteger)location longestEffectiveRange:(null)range inRange:(null)rangeLimit; (Argument range cannot be converted)
     */
    /**
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:23</i><br>
     * Conversion Error : /// Original signature : <code>attribute(NSString*, NSUInteger, null, null)</code><br>
     * - (null)attribute:(NSString*)attrName atIndex:(NSUInteger)location longestEffectiveRange:(null)range inRange:(null)rangeLimit; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>BOOL isEqualToAttributedString(NSAttributedString*)</code><br>
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:25</i>
     */
    public abstract byte isEqualToAttributedString(NSAttributedString other);

    /**
     * Original signature : <code>initWithString(NSString*)</code><br>
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:27</i>
     */
    public abstract NSAttributedString initWithString(String str);

    /**
     * Original signature : <code>initWithString(String*, NSDictionary*)</code><br>
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:28</i>
     */
    public abstract NSAttributedString initWithString_attributes(String str, NSDictionary attrs);

    /**
     * Original signature : <code>initWithAttributedString(NSAttributedString*)</code><br>
     * <i>from NSExtendedAttributedString native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSAttributedString.h:29</i>
     */
    public abstract NSAttributedString initWithAttributedString(NSAttributedString attrStr);
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :156</i><br>
     * Conversion Error : /**<br>
     *  * Attributes which should be copied/pasted with "copy font".<br>
     *  * Original signature : <code>NSDictionary* fontAttributesInRange(null)</code><br>
     *  * /<br>
     * - (NSDictionary*)fontAttributesInRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :160</i><br>
     * Conversion Error : /**<br>
     *  * Attributes which should be copied/pasted with "copy ruler".<br>
     *  * Original signature : <code>NSDictionary* rulerAttributesInRange(null)</code><br>
     *  * /<br>
     * - (NSDictionary*)rulerAttributesInRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>BOOL containsAttachments()</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :162</i>
     */
    public abstract byte containsAttachments();
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :166</i><br>
     * Conversion Error : /**<br>
     *  * Returns NSNotFound if no line break location found in the specified range; otherwise returns the index of the first character that should go on the NEXT line.<br>
     *  * Original signature : <code>NSUInteger lineBreakBeforeIndex(NSUInteger, null)</code><br>
     *  * /<br>
     * - (NSUInteger)lineBreakBeforeIndex:(NSUInteger)location withinRange:(null)aRange; (Argument aRange cannot be converted)
     */
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :168</i><br>
     * Conversion Error : /// Original signature : <code>NSUInteger lineBreakByHyphenatingBeforeIndex(NSUInteger, null)</code><br>
     * - (NSUInteger)lineBreakByHyphenatingBeforeIndex:(NSUInteger)location withinRange:(null)aRange; (Argument aRange cannot be converted)
     */
    /**
     * Original signature : <code>doubleClickAtIndex(NSUInteger)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :171</i>
     */
    public abstract NSObject doubleClickAtIndex(NSUInteger location);

    /**
     * Original signature : <code>NSUInteger nextWordFromIndex(NSUInteger, BOOL)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :172</i>
     */
    public abstract NSUInteger nextWordFromIndex_forward(NSUInteger location, byte isForward);
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :177</i><br>
     * Conversion Error : /**<br>
     *  * Returns a URL either from a link attribute or from text at the given location that appears to be a URL string, for use in automatic link detection.  The effective range is the range of the link attribute or URL string.<br>
     *  * Original signature : <code>NSURL* URLAtIndex(NSUInteger, null)</code><br>
     *  * /<br>
     * - (NSURL*)URLAtIndex:(NSUInteger)location effectiveRange:(null)effectiveRange; (Argument effectiveRange cannot be converted)
     */
    /**
     * Convenience methods for calculating the range of an individual text block, range of an entire table, range of a list, and the index within a list.<br>
     * Original signature : <code>rangeOfTextBlock(NSTextBlock*, NSUInteger)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :190</i>
     */
    public abstract NSObject rangeOfTextBlock_atIndex(com.sun.jna.Pointer block, NSUInteger location);

    /**
     * Original signature : <code>rangeOfTextTable(NSTextTable*, NSUInteger)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :191</i>
     */
    public abstract NSObject rangeOfTextTable_atIndex(com.sun.jna.Pointer table, NSUInteger location);

    /**
     * Original signature : <code>rangeOfTextList(NSTextList*, NSUInteger)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :192</i>
     */
    public abstract NSObject rangeOfTextList_atIndex(com.sun.jna.Pointer list, NSUInteger location);

    /**
     * Original signature : <code>NSInteger itemNumberInTextList(NSTextList*, NSUInteger)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :193</i>
     */
    public abstract NSInteger itemNumberInTextList_atIndex(com.sun.jna.Pointer list, NSUInteger location);

    /**
     * These first two general methods supersede the previous versions shown below.  They take a dictionary of options to specify how the document should be loaded.  The various possible options are specified above, as NS...DocumentOption.  If NSDocumentTypeDocumentOption is specified, the document will be treated as being in the specified format.  If NSDocumentTypeDocumentOption is not specified, these methods will examine the document and do their best to load it using whatever format it seems to contain.<br>
     * Original signature : <code>initWithURL(NSURL*, NSDictionary*, NSDictionary**, NSError**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :201</i>
     */
    public abstract NSAttributedString initWithURL_options_documentAttributes_error(com.sun.jna.Pointer url, NSDictionary options, ObjCObjectByReference dict, ObjCObjectByReference error);

    /**
     * Original signature : <code>initWithData(NSData*, NSDictionary*, NSDictionary**, NSError**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :202</i>
     */
    public abstract NSAttributedString initWithData_options_documentAttributes_error(com.sun.jna.Pointer data, NSDictionary options, ObjCObjectByReference dict, ObjCObjectByReference error);

    /**
     * These two superseded methods are similar to the first listed above except that they lack the options dictionary and error return arguments.  They will always attempt to determine the format from the document.<br>
     * Original signature : <code>initWithPath(String*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :207</i>
     */
    public abstract NSAttributedString initWithPath_documentAttributes(String path, ObjCObjectByReference dict);

    /**
     * Original signature : <code>initWithURL(NSURL*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :208</i>
     */
    public abstract NSAttributedString initWithURL_documentAttributes(com.sun.jna.Pointer url, ObjCObjectByReference dict);

    /**
     * The following methods should now be considered as conveniences for various common document types.<br>
     * Original signature : <code>initWithRTF(NSData*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :212</i>
     */
    public abstract NSAttributedString initWithRTF_documentAttributes(com.sun.jna.Pointer data, ObjCObjectByReference dict);

    /**
     * Original signature : <code>initWithRTFD(NSData*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :213</i>
     */
    public abstract NSAttributedString initWithRTFD_documentAttributes(com.sun.jna.Pointer data, ObjCObjectByReference dict);

    /**
     * Original signature : <code>initWithHTML(NSData*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :214</i>
     */
    public abstract NSAttributedString initWithHTML_documentAttributes(com.sun.jna.Pointer data, ObjCObjectByReference dict);

    /**
     * Original signature : <code>initWithHTML(NSData*, NSURL*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :215</i>
     */
    public abstract NSAttributedString initWithHTML_baseURL_documentAttributes(com.sun.jna.Pointer data, com.sun.jna.Pointer base, ObjCObjectByReference dict);

    /**
     * Original signature : <code>initWithDocFormat(NSData*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :217</i>
     */
    public abstract NSAttributedString initWithDocFormat_documentAttributes(com.sun.jna.Pointer data, ObjCObjectByReference dict);

    /**
     * Original signature : <code>initWithHTML(NSData*, NSDictionary*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :218</i>
     */
    public abstract NSAttributedString initWithHTML_options_documentAttributes(com.sun.jna.Pointer data, NSDictionary options, ObjCObjectByReference dict);

    /**
     * A separate method is available for initializing from an RTFD file wrapper.  No options apply in this case.<br>
     * Original signature : <code>initWithRTFDFileWrapper(NSFileWrapper*, NSDictionary**)</code><br>
     * <i>from NSAttributedStringKitAdditions native declaration : :223</i>
     */
    public abstract NSAttributedString initWithRTFDFileWrapper_documentAttributes(com.sun.jna.Pointer wrapper, ObjCObjectByReference dict);
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :230</i><br>
     * Conversion Error : /**<br>
     *  * These first two methods generalize on the more specific previous versions shown below.  They require a document attributes dict specifying at least the NSDocumentTypeDocumentAttribute to determine the format to be written.  The file wrapper method will return a directory file wrapper for those document types for which it is appropriate, otherwise a regular-file file wrapper.<br>
     *  * Original signature : <code>NSData* dataFromRange(null, NSDictionary*, NSError**)</code><br>
     *  * /<br>
     * - (NSData*)dataFromRange:(null)range documentAttributes:(NSDictionary*)dict error:(NSError**)error; (Argument range cannot be converted)
     */
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :231</i><br>
     * Conversion Error : /// Original signature : <code>NSFileWrapper* fileWrapperFromRange(null, NSDictionary*, NSError**)</code><br>
     * - (NSFileWrapper*)fileWrapperFromRange:(null)range documentAttributes:(NSDictionary*)dict error:(NSError**)error; (Argument range cannot be converted)
     */
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :236</i><br>
     * Conversion Error : /**<br>
     *  * The following methods should now be considered as conveniences for various common document types.  In these methods the document attributes dictionary is optional.<br>
     *  * Original signature : <code>NSData* RTFFromRange(null, NSDictionary*)</code><br>
     *  * /<br>
     * - (NSData*)RTFFromRange:(null)range documentAttributes:(NSDictionary*)dict; (Argument range cannot be converted)
     */
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :237</i><br>
     * Conversion Error : /// Original signature : <code>NSData* RTFDFromRange(null, NSDictionary*)</code><br>
     * - (NSData*)RTFDFromRange:(null)range documentAttributes:(NSDictionary*)dict; (Argument range cannot be converted)
     */
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :238</i><br>
     * Conversion Error : /// Original signature : <code>NSFileWrapper* RTFDFileWrapperFromRange(null, NSDictionary*)</code><br>
     * - (NSFileWrapper*)RTFDFileWrapperFromRange:(null)range documentAttributes:(NSDictionary*)dict; (Argument range cannot be converted)
     */
    /**
     * <i>from NSAttributedStringKitAdditions native declaration : :240</i><br>
     * Conversion Error : /// Original signature : <code>NSData* docFormatFromRange(null, NSDictionary*)</code><br>
     * - (NSData*)docFormatFromRange:(null)range documentAttributes:(NSDictionary*)dict; (Argument range cannot be converted)
     */

    public static final String FontAttributeName = "NSFont";
    public static final String ParagraphStyleAttributeName = "NSParagraphStyle";
    public static final String ForegroundColorAttributeName = "NSColor";
    public static final String UnderlineStyleAttributeName = "NSUnderline";
    public static final String SuperscriptAttributeName = "NSSuperScript";
    public static final String BackgroundColorAttributeName = "NSBackgroundColor";
    public static final String AttachmentAttributeName = "NSAttachment";
    public static final String LigatureAttributeName = "NSLigature";
    public static final String BaselineOffsetAttributeName = "NSBaselineOffset";
    public static final String KernAttributeName = "NSKern";
    public static final String LinkAttributeName = "NSLink";
    public static final String CharacterShapeAttributeName = "NSCharacterShape";
    public static final String StrokeWidthAttributeName = "NSStrokeWidth";
    public static final String StrokeColorAttributeName = "NSStrokeColor";
    public static final String UnderlineColorAttributeName = "NSUnderlineColor";
    public static final String StrikethroughStyleAttributeName = "NSStrikethrough";
    public static final String StrikethroughColorAttributeName = "NSStrikethroughColor";
    public static final String ShadowAttributeName = "NSShadow";
    public static final String ObliquenessAttributeName = "NSObliqueness";
    public static final String ExpansionAttributeName = "NSExpansion";
    public static final String CursorAttributeName = "NSCursor";
    public static final String ToolTipAttributeName = "NSToolTip";
    public static final String NSPlainTextDocumentType = "NSPlainText";
    public static final String NSRTFTextDocumentType = "NSRTF";
    public static final String NSRTFDTextDocumentType = "NSRTFD";
    public static final String NSMacSimpleTextDocumentType = "NSMacSimpleText";
    public static final String NSHTMLTextDocumentType = "NSHTML";
    public static final String NSDocFormatTextDocumentType = "NSDocFormat";
    public static final String NSWordMLTextDocumentType = "NSWordML";
    public static final int UnderlineStyleNone = 0;
    public static final int UnderlineStyleSingle = 1;
    public static final int UnderlineStyleThick = 2;
    public static final int UnderlineStyleDouble = 9;
    public static final int UnderlinePatternSolid = 0;
    public static final int UnderlinePatternDot = 256;
    public static final int UnderlinePatternDash = 512;
    public static final int UnderlinePatternDashDot = 768;
    public static final int UnderlinePatternDashDotDot = 1024;
    public static final int UnderlineByWordMask = 32768;
    public static final int NoUnderlineStyle = 0;
    public static final int SingleUnderlineStyle = 1;
    public static final int UnderlineStrikethroughMask = 16384;
}
