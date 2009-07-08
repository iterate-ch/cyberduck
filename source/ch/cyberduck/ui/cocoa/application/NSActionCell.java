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

/// <i>native declaration : :10</i>
public interface NSActionCell extends NSCell {

    public interface _Class extends org.rococoa.NSClass {
        NSActionCell alloc();
    }

    /**
     * Original signature : <code>NSView* controlView()</code><br>
     * <i>native declaration : :19</i>
     */
    public abstract NSView controlView();

    /**
     * Original signature : <code>void setControlView(NSView*)</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract void setControlView(NSView view);

    /**
     * Original signature : <code>void setFont(NSFont*)</code><br>
     * <i>native declaration : :23</i>
     */
    public abstract void setFont(NSFont fontObj);
    /**
     * <i>native declaration : :24</i><br>
     * Conversion Error : /// Original signature : <code>void setAlignment(null)</code><br>
     * - (void)setAlignment:(null)mode; (Argument mode cannot be converted)
     */
    /**
     * Original signature : <code>void setBordered(BOOL)</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract void setBordered(boolean flag);

    /**
     * Original signature : <code>void setBezeled(BOOL)</code><br>
     * <i>native declaration : :26</i>
     */
    public abstract void setBezeled(boolean flag);

    /**
     * Original signature : <code>void setEnabled(BOOL)</code><br>
     * <i>native declaration : :27</i>
     */
    public abstract void setEnabled(boolean flag);

    /**
     * Original signature : <code>void setFloatingPointFormat(BOOL, NSUInteger, NSUInteger)</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract void setFloatingPointFormat_left_right(boolean autoRange, int leftDigits, int rightDigits);

    /**
     * Original signature : <code>void setImage(NSImage*)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract void setImage(NSImage image);

    /**
     * Original signature : <code>id target()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract org.rococoa.ID target();

    /**
     * Original signature : <code>void setTarget(id)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void setTarget(org.rococoa.ID anObject);

    /**
     * Original signature : <code>SEL action()</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract org.rococoa.Selector action();

    /**
     * Original signature : <code>void setAction(SEL)</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract void setAction(org.rococoa.Selector aSelector);

    /**
     * Original signature : <code>NSInteger tag()</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract int tag();

    /**
     * Original signature : <code>void setTag(NSInteger)</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract void setTag(int anInt);

    /**
     * Original signature : <code>NSString* stringValue()</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract String stringValue();

    /**
     * Original signature : <code>int intValue()</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract int intValue();

    /**
     * Original signature : <code>float floatValue()</code><br>
     * <i>native declaration : :38</i>
     */
    public abstract float floatValue();

    /**
     * Original signature : <code>double doubleValue()</code><br>
     * <i>native declaration : :39</i>
     */
    public abstract double doubleValue();
    /**
     * <i>native declaration : :40</i><br>
     * Conversion Error : id<NSCopying>
     */
    /**
     * Original signature : <code>NSInteger integerValue()</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract int integerValue();
}
