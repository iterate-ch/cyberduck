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
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class NSMutableAttributedString extends NSAttributedString {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSMutableAttributedString", _Class.class);

    public static NSMutableAttributedString create(String str) {
        if(null == str) {
            str = "";
        }
        return Rococoa.cast(CLASS.alloc().initWithString(str), NSMutableAttributedString.class);
    }

    public static NSMutableAttributedString create(String str, NSDictionary attrs) {
        if(null == str) {
            str = "";
        }
        return Rococoa.cast(CLASS.alloc().initWithString_attributes(str, attrs), NSMutableAttributedString.class);
    }

    public interface _Class extends ObjCClass {
        NSMutableAttributedString alloc();
    }

    /**
     * <i>native declaration : :32</i><br>
     * Conversion Error : /// Original signature : <code>void replaceCharactersInRange(null, NSString*)</code><br>
     * - (void)replaceCharactersInRange:(null)range withString:(NSString*)str; (Argument range cannot be converted)
     */
    public abstract void replaceCharactersInRange_withString(NSRange range, String str);

    public void replaceCharactersInRange(NSRange range, String attrString) {
        this.replaceCharactersInRange_withString(range, attrString);
    }

    /**
     * <i>native declaration : :33</i><br>
     * Conversion Error : /// Original signature : <code>void setAttributes(NSDictionary*, null)</code><br>
     * - (void)setAttributes:(NSDictionary*)attrs range:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>NSMutableString* mutableString()</code><br>
     * <i>from NSExtendedMutableAttributedString native declaration : :39</i>
     */
    public abstract com.sun.jna.Pointer mutableString();

    /**
     * <i>from NSExtendedMutableAttributedString native declaration : :41</i><br>
     * Conversion Error : /// Original signature : <code>void addAttribute(NSString*, null, null)</code><br>
     * - (void)addAttribute:(NSString*)name value:(null)value range:(null)range; (Argument value cannot be converted)
     */
    public abstract void addAttribute_value_range(String name, NSObject value, NSRange range);

    public void addAttributeInRange(String name, NSObject value, NSRange range) {
        this.addAttribute_value_range(name, value, range);
    }

    public void addAttributeInRange(String name, String value, NSRange range) {
        this.addAttribute_value_range(name, NSString.stringWithString(value), range);
    }

    /**
     * <i>from NSExtendedMutableAttributedString native declaration : :42</i><br>
     * Conversion Error : /// Original signature : <code>void addAttributes(NSDictionary*, null)</code><br>
     * - (void)addAttributes:(NSDictionary*)attrs range:(null)range; (Argument range cannot be converted)
     */
    public abstract void addAttributes_range(NSDictionary attrs, NSRange range);

    public void addAttributesInRange(NSDictionary attrs, NSRange range) {
        this.addAttributes_range(attrs, range);
    }

    /**
     * <i>from NSExtendedMutableAttributedString native declaration : :43</i><br>
     * Conversion Error : /// Original signature : <code>void removeAttribute(NSString*, null)</code><br>
     * - (void)removeAttribute:(NSString*)name range:(null)range; (Argument range cannot be converted)
     */
    public abstract void removeAttribute_range(String name, NSRange range);

    public void removeAttributeInRange(String name, NSRange range) {
        this.removeAttribute_range(name, range);
    }

    /**
     * <i>from NSExtendedMutableAttributedString native declaration : :45</i><br>
     * Conversion Error : /// Original signature : <code>void replaceCharactersInRange(null, NSAttributedString*)</code><br>
     * - (void)replaceCharactersInRange:(null)range withAttributedString:(NSAttributedString*)attrString; (Argument range cannot be converted)
     */
    public abstract void replaceCharactersInRange_withAttributedString(NSRange range, NSAttributedString attrString);

    public void replaceCharactersInRange(NSRange range, NSAttributedString attrString) {
        this.replaceCharactersInRange_withAttributedString(range, attrString);
    }

    /**
     * Original signature : <code>void insertAttributedString(NSAttributedString*, NSUInteger)</code><br>
     * <i>from NSExtendedMutableAttributedString native declaration : :46</i>
     */
    public abstract void insertAttributedString_atIndex(NSAttributedString attrString, NSUInteger loc);

    /**
     * Original signature : <code>void appendAttributedString(NSAttributedString*)</code><br>
     * <i>from NSExtendedMutableAttributedString native declaration : :47</i>
     */
    public abstract void appendAttributedString(NSAttributedString attrString);
    /**
     * <i>from NSExtendedMutableAttributedString native declaration : :48</i><br>
     * Conversion Error : /// Original signature : <code>void deleteCharactersInRange(null)</code><br>
     * - (void)deleteCharactersInRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>void setAttributedString(NSAttributedString*)</code><br>
     * <i>from NSExtendedMutableAttributedString native declaration : :49</i>
     */
    public abstract void setAttributedString(NSAttributedString attrString);

    /**
     * Original signature : <code>void beginEditing()</code><br>
     * <i>from NSExtendedMutableAttributedString native declaration : :51</i>
     */
    public abstract void beginEditing();

    /**
     * Original signature : <code>void endEditing()</code><br>
     * <i>from NSExtendedMutableAttributedString native declaration : :52</i>
     */
    public abstract void endEditing();
}
