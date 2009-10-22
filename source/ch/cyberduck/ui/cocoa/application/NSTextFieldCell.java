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

import org.rococoa.ObjCClass;

/// <i>native declaration : :21</i>
public abstract class NSTextFieldCell extends NSActionCell {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTextFieldCell", _Class.class);

    public static NSTextFieldCell textFieldCell() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        NSTextFieldCell alloc();
    }

    public abstract NSTextFieldCell init();

    /**
     * Original signature : <code>void setBackgroundColor(NSColor*)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract void setBackgroundColor(NSColor color);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract NSColor backgroundColor();

    /**
     * Original signature : <code>void setDrawsBackground(BOOL)</code><br>
     * <i>native declaration : :42</i>
     */
    public abstract void setDrawsBackground(boolean flag);

    /**
     * Original signature : <code>BOOL drawsBackground()</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract boolean drawsBackground();

    /**
     * Original signature : <code>void setTextColor(NSColor*)</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract void setTextColor(NSColor color);

    /**
     * Original signature : <code>NSColor* textColor()</code><br>
     * <i>native declaration : :45</i>
     */
    public abstract NSColor textColor();

    /**
     * Original signature : <code>NSText* setUpFieldEditorAttributes(NSText*)</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract NSText setUpFieldEditorAttributes(NSText textObj);

    /**
     * Original signature : <code>void setBezelStyle(NSTextFieldBezelStyle)</code><br>
     * <i>native declaration : :49</i>
     */
    public abstract void setBezelStyle(int style);

    /**
     * Original signature : <code>NSTextFieldBezelStyle bezelStyle()</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract int bezelStyle();

    /**
     * Original signature : <code>void setPlaceholderString(NSString*)</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract void setPlaceholderString(String string);

    /**
     * Original signature : <code>NSString* placeholderString()</code><br>
     * <i>native declaration : :55</i>
     */
    public abstract String placeholderString();

    /**
     * Original signature : <code>void setPlaceholderAttributedString(NSAttributedString*)</code><br>
     * <i>native declaration : :56</i>
     */
    public abstract void setPlaceholderAttributedString(NSAttributedString string);

    /**
     * Original signature : <code>NSAttributedString* placeholderAttributedString()</code><br>
     * <i>native declaration : :57</i>
     */
    public abstract NSAttributedString placeholderAttributedString();

    /**
     * Original signature : <code>void setWantsNotificationForMarkedText(BOOL)</code><br>
     * <i>native declaration : :61</i>
     */
    public abstract void setWantsNotificationForMarkedText(boolean flag);

    /**
     * Returns an array of locale identifiers representing input sources allowed to be enabled when the receiver has the keyboard focus.<br>
     * Original signature : <code>NSArray* allowedInputSourceLocales()</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract NSArray allowedInputSourceLocales();

    /**
     * Original signature : <code>void setAllowedInputSourceLocales(NSArray*)</code><br>
     * <i>native declaration : :66</i>
     */
    public abstract void setAllowedInputSourceLocales(NSArray localeIdentifiers);
}
