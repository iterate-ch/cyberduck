package ch.cyberduck.ui.cocoa.application;

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

import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;

import org.rococoa.Rococoa;

/// <i>native declaration : :21</i>
public interface NSTextFieldCell extends NSActionCell {
    static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTextFieldCell", _Class.class);

    public static class Factory {
        public static NSTextFieldCell create() {
            return Rococoa.cast(CLASS.alloc().init().autorelease(), NSTextFieldCell.class);
        }
    }

    public interface _Class extends org.rococoa.NSClass {
        NSTextFieldCell alloc();
    }

    public abstract NSTextFieldCell init();

    /**
     * Original signature : <code>void setBackgroundColor(NSColor*)</code><br>
     * <i>native declaration : :40</i>
     */
    void setBackgroundColor(NSColor color);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code><br>
     * <i>native declaration : :41</i>
     */
    NSColor backgroundColor();

    /**
     * Original signature : <code>void setDrawsBackground(BOOL)</code><br>
     * <i>native declaration : :42</i>
     */
    void setDrawsBackground(boolean flag);

    /**
     * Original signature : <code>BOOL drawsBackground()</code><br>
     * <i>native declaration : :43</i>
     */
    boolean drawsBackground();

    /**
     * Original signature : <code>void setTextColor(NSColor*)</code><br>
     * <i>native declaration : :44</i>
     */
    void setTextColor(NSColor color);

    /**
     * Original signature : <code>NSColor* textColor()</code><br>
     * <i>native declaration : :45</i>
     */
    NSColor textColor();

    /**
     * Original signature : <code>NSText* setUpFieldEditorAttributes(NSText*)</code><br>
     * <i>native declaration : :46</i>
     */
    com.sun.jna.Pointer setUpFieldEditorAttributes(NSText textObj);

    /**
     * Original signature : <code>void setBezelStyle(NSTextFieldBezelStyle)</code><br>
     * <i>native declaration : :49</i>
     */
    void setBezelStyle(int style);

    /**
     * Original signature : <code>NSTextFieldBezelStyle bezelStyle()</code><br>
     * <i>native declaration : :50</i>
     */
    int bezelStyle();

    /**
     * Original signature : <code>void setPlaceholderString(NSString*)</code><br>
     * <i>native declaration : :54</i>
     */
    void setPlaceholderString(String string);

    /**
     * Original signature : <code>NSString* placeholderString()</code><br>
     * <i>native declaration : :55</i>
     */
    String placeholderString();

    /**
     * Original signature : <code>void setPlaceholderAttributedString(NSAttributedString*)</code><br>
     * <i>native declaration : :56</i>
     */
    void setPlaceholderAttributedString(NSAttributedString string);

    /**
     * Original signature : <code>NSAttributedString* placeholderAttributedString()</code><br>
     * <i>native declaration : :57</i>
     */
    NSAttributedString placeholderAttributedString();

    /**
     * Original signature : <code>void setWantsNotificationForMarkedText(BOOL)</code><br>
     * <i>native declaration : :61</i>
     */
    void setWantsNotificationForMarkedText(boolean flag);

    /**
     * Returns an array of locale identifiers representing input sources allowed to be enabled when the receiver has the keyboard focus.<br>
     * Original signature : <code>NSArray* allowedInputSourceLocales()</code><br>
     * <i>native declaration : :65</i>
     */
    NSArray allowedInputSourceLocales();

    /**
     * Original signature : <code>void setAllowedInputSourceLocales(NSArray*)</code><br>
     * <i>native declaration : :66</i>
     */
    void setAllowedInputSourceLocales(NSArray localeIdentifiers);
}
