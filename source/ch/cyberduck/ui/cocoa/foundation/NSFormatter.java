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

public abstract class NSFormatter extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSFormatter", _Class.class);

    public interface _Class extends ObjCClass {
        NSFormatter alloc();
    }

    /**
     * <i>native declaration : :15</i><br>
     * Conversion Error : /// Original signature : <code>NSString* stringForObjectValue(null)</code><br>
     * - (NSString*)stringForObjectValue:(null)obj; (Argument obj cannot be converted)
     */
    public abstract String stringForObjectValue(NSObject obj);
    /**
     * <i>native declaration : :17</i><br>
     * Conversion Error : /// Original signature : <code>NSAttributedString* attributedStringForObjectValue(null, NSDictionary*)</code><br>
     * - (NSAttributedString*)attributedStringForObjectValue:(null)obj withDefaultAttributes:(NSDictionary*)attrs; (Argument obj cannot be converted)
     */
    /**
     * <i>native declaration : :19</i><br>
     * Conversion Error : /// Original signature : <code>NSString* editingStringForObjectValue(null)</code><br>
     * - (NSString*)editingStringForObjectValue:(null)obj; (Argument obj cannot be converted)
     */
    /**
     * Original signature : <code>BOOL getObjectValue(id*, NSString*, NSString**)</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract boolean getObjectValue_forString_errorDescription(NSObject obj, String string, com.sun.jna.ptr.PointerByReference error);

    /**
     * Original signature : <code>BOOL isPartialStringValid(NSString*, NSString**, NSString**)</code><br>
     * <i>native declaration : :23</i>
     */
    public abstract boolean isPartialStringValid_newEditingString_errorDescription(String partialString, com.sun.jna.ptr.PointerByReference newString, com.sun.jna.ptr.PointerByReference error);
    /**
     * <i>native declaration : :26</i><br>
     * Conversion Error : /// Original signature : <code>BOOL isPartialStringValid(NSString**, null, NSString*, null, NSString**)</code><br>
     * - (BOOL)isPartialStringValid:(NSString**)partialStringPtr proposedSelectedRange:(null)proposedSelRangePtr originalString:(NSString*)origString originalSelectedRange:(null)origSelRange errorDescription:(NSString**)error; (Argument proposedSelRangePtr cannot be converted)
     */
}
