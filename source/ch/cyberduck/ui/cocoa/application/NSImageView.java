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

import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class NSImageView extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSImageView", _Class.class);

    public interface _Class extends ObjCClass {
        NSImageView alloc();
    }

    /**
     * Original signature : <code>NSImage* image()</code><br>
     * <i>native declaration : :26</i>
     */
    public abstract NSImage image();

    /**
     * Original signature : <code>void setImage(NSImage*)</code><br>
     * <i>native declaration : :27</i>
     */
    public abstract void setImage(NSImage newImage);

    /**
     * Original signature : <code>imageAlignment()</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract NSUInteger imageAlignment();
    /**
     * <i>native declaration : :30</i><br>
     * Conversion Error : /// Original signature : <code>void setImageAlignment(null)</code><br>
     * - (void)setImageAlignment:(null)newAlign; (Argument newAlign cannot be converted)
     */
    /**
     * Original signature : <code>imageScaling()</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract NSUInteger imageScaling();
    /**
     * <i>native declaration : :32</i><br>
     * Conversion Error : /// Original signature : <code>void setImageScaling(null)</code><br>
     * - (void)setImageScaling:(null)newScaling; (Argument newScaling cannot be converted)
     */
    /**
     * Original signature : <code>imageFrameStyle()</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract NSUInteger imageFrameStyle();
    /**
     * <i>native declaration : :34</i><br>
     * Conversion Error : /// Original signature : <code>void setImageFrameStyle(null)</code><br>
     * - (void)setImageFrameStyle:(null)newStyle; (Argument newStyle cannot be converted)
     */
    /**
     * Original signature : <code>void setEditable(BOOL)</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract void setEditable(boolean editable);

    /**
     * Original signature : <code>BOOL isEditable()</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract boolean isEditable();

    /**
     * Original signature : <code>void setAnimates(BOOL)</code><br>
     * <i>native declaration : :39</i>
     */
    public abstract void setAnimates(boolean flag);

    /**
     * Original signature : <code>BOOL animates()</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract boolean animates();

    /**
     * Original signature : <code>BOOL allowsCutCopyPaste()</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract boolean allowsCutCopyPaste();

    /**
     * Original signature : <code>void setAllowsCutCopyPaste(BOOL)</code><br>
     * <i>native declaration : :45</i>
     */
    public abstract void setAllowsCutCopyPaste(boolean allow);
}
