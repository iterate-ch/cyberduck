package ch.cyberduck.binding.application;

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

import ch.cyberduck.binding.foundation.NSNotification;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :8</i>
public abstract class NSTextField extends NSControl {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTextField", _Class.class);

    public static NSTextField textfieldWithFrame(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public interface _Class extends ObjCClass {
        NSTextField alloc();
    }

    @Override
    public abstract NSTextField initWithFrame(NSRect frameRect);

    /**
     * Original signature : <code>void setBackgroundColor(NSColor*)</code><br>
     * <i>native declaration : :15</i>
     */
    public abstract void setBackgroundColor(NSColor color);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code><br>
     * <i>native declaration : :16</i>
     */
    public abstract NSColor backgroundColor();

    /**
     * Original signature : <code>void setDrawsBackground(BOOL)</code><br>
     * <i>native declaration : :17</i>
     */
    public abstract void setDrawsBackground(boolean flag);

    /**
     * Original signature : <code>BOOL drawsBackground()</code><br>
     * <i>native declaration : :18</i>
     */
    public abstract boolean drawsBackground();

    /**
     * Original signature : <code>void setTextColor(NSColor*)</code><br>
     * <i>native declaration : :19</i>
     */
    public abstract void setTextColor(NSColor color);

    /**
     * Original signature : <code>NSColor* textColor()</code><br>
     * <i>native declaration : :20</i>
     */
    public abstract NSColor textColor();

    /**
     * Original signature : <code>BOOL isBordered()</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract boolean isBordered();

    /**
     * Original signature : <code>void setBordered(BOOL)</code><br>
     * <i>native declaration : :22</i>
     */
    public abstract void setBordered(boolean flag);

    /**
     * Original signature : <code>BOOL isBezeled()</code><br>
     * <i>native declaration : :23</i>
     */
    public abstract boolean isBezeled();

    /**
     * Original signature : <code>void setBezeled(BOOL)</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract void setBezeled(boolean flag);

    /**
     * Original signature : <code>BOOL isEditable()</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract boolean isEditable();

    /**
     * Original signature : <code>void setEditable(BOOL)</code><br>
     * <i>native declaration : :26</i>
     */
    public abstract void setEditable(boolean flag);

    /**
     * Original signature : <code>BOOL isSelectable()</code><br>
     * <i>native declaration : :27</i>
     */
    public abstract boolean isSelectable();

    /**
     * Original signature : <code>void setSelectable(BOOL)</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract void setSelectable(boolean flag);

    /**
     * Original signature : <code>void selectText(id)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract void selectText(final ID sender);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void setDelegate(org.rococoa.ID id);

    /**
     * Original signature : <code>BOOL textShouldBeginEditing(NSText*)</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract boolean textShouldBeginEditing(NSText textObject);

    /**
     * Original signature : <code>BOOL textShouldEndEditing(NSText*)</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract boolean textShouldEndEditing(NSText textObject);

    /**
     * Original signature : <code>void textDidBeginEditing(NSNotification*)</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract void textDidBeginEditing(NSNotification notification);

    /**
     * Original signature : <code>void textDidEndEditing(NSNotification*)</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract void textDidEndEditing(NSNotification notification);

    /**
     * Original signature : <code>void textDidChange(NSNotification*)</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract void textDidChange(NSNotification notification);

    /**
     * <i>native declaration : :40</i><br>
     * Conversion Error : /// Original signature : <code>void setBezelStyle(null)</code><br>
     * - (void)setBezelStyle:(null)style; (Argument style cannot be converted)
     */
    public abstract void setBezelStyle(NSUInteger style);

    /**
     * Original signature : <code>bezelStyle()</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract NSUInteger bezelStyle();

    /**
     * Original signature : <code>void setTitleWithMnemonic(NSString*)</code><br>
     * <i>from NSKeyboardUI native declaration : :46</i>
     */
    public abstract void setTitleWithMnemonic(String stringWithAmpersand);

    /**
     * Original signature : <code>BOOL allowsEditingTextAttributes()</code><br>
     * <i>from NSTextFieldAttributedStringMethods native declaration : :50</i>
     */
    public abstract boolean allowsEditingTextAttributes();

    /**
     * Original signature : <code>void setAllowsEditingTextAttributes(BOOL)</code><br>
     * <i>from NSTextFieldAttributedStringMethods native declaration : :51</i>
     */
    public abstract void setAllowsEditingTextAttributes(boolean flag);

    /**
     * Original signature : <code>BOOL importsGraphics()</code><br>
     * <i>from NSTextFieldAttributedStringMethods native declaration : :52</i>
     */
    public abstract boolean importsGraphics();

    /**
     * Original signature : <code>void setImportsGraphics(BOOL)</code><br>
     * <i>from NSTextFieldAttributedStringMethods native declaration : :53</i>
     */
    public abstract void setImportsGraphics(boolean flag);

    public abstract NSTextFieldCell cell();   
}
