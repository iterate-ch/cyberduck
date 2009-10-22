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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;

/// <i>native declaration : :150</i>
public abstract class NSMutableParagraphStyle extends NSParagraphStyle {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSMutableParagraphStyle", _Class.class);

    public interface _Class extends ObjCClass {
        NSMutableParagraphStyle alloc();
    }

    public static NSMutableParagraphStyle paragraphStyle() {
        return CLASS.alloc().init();
    }

    public abstract NSMutableParagraphStyle init();

    /**
     * Original signature : <code>void setLineSpacing(CGFloat)</code><br>
     * <i>native declaration : :152</i>
     */
    public abstract void setLineSpacing(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setParagraphSpacing(CGFloat)</code><br>
     * <i>native declaration : :153</i>
     */
    public abstract void setParagraphSpacing(CGFloat aFloat);

    /**
     * <i>native declaration : :154</i><br>
     * Conversion Error : /// Original signature : <code>public abstract void setAlignment(null)</code><br>
     * - (void)setAlignment:(null)alignment; (Argument alignment cannot be converted)
     */
    public abstract void setAlignment(int alignment);

    /**
     * Original signature : <code>public abstract void setFirstLineHeadIndent(CGFloat)</code><br>
     * <i>native declaration : :155</i>
     */
    public abstract void setFirstLineHeadIndent(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setHeadIndent(CGFloat)</code><br>
     * <i>native declaration : :156</i>
     */
    public abstract void setHeadIndent(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setTailIndent(CGFloat)</code><br>
     * <i>native declaration : :157</i>
     */
    public abstract void setTailIndent(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setLineBreakMode(NSLineBreakMode)</code><br>
     * <i>native declaration : :158</i>
     */
    public abstract void setLineBreakMode(int mode);

    /**
     * Original signature : <code>public abstract void setMinimumLineHeight(CGFloat)</code><br>
     * <i>native declaration : :159</i>
     */
    public abstract void setMinimumLineHeight(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setMaximumLineHeight(CGFloat)</code><br>
     * <i>native declaration : :160</i>
     */
    public abstract void setMaximumLineHeight(CGFloat aFloat);
    /**
     * Original signature : <code>public abstract void addTabStop(NSTextTab*)</code><br>
     * <i>native declaration : :161</i>
     */
//	public abstract void addTabStop(NSTextTab anObject);
    /**
     * Original signature : <code>public abstract void removeTabStop(NSTextTab*)</code><br>
     * <i>native declaration : :162</i>
     */
    //	public abstract void removeTabStop(NSTextTab anObject);
    /**
     * Original signature : <code>public abstract void setTabStops(NSArray*)</code><br>
     * <i>native declaration : :163</i>
     */
    public abstract void setTabStops(NSArray array);

    /**
     * Original signature : <code>public abstract void setParagraphStyle(NSParagraphStyle*)</code><br>
     * <i>native declaration : :164</i>
     */
    public abstract void setParagraphStyle(NSParagraphStyle obj);
    /**
     * <i>native declaration : :166</i><br>
     * Conversion Error : /// Original signature : <code>public abstract void setBaseWritingDirection(null)</code><br>
     * - (void)setBaseWritingDirection:(null)writingDirection; (Argument writingDirection cannot be converted)
     */
    /**
     * Original signature : <code>public abstract void setLineHeightMultiple(CGFloat)</code><br>
     * <i>native declaration : :169</i>
     */
    public abstract void setLineHeightMultiple(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setParagraphSpacingBefore(CGFloat)</code><br>
     * <i>native declaration : :170</i>
     */
    public abstract void setParagraphSpacingBefore(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setDefaultTabInterval(CGFloat)</code><br>
     * <i>native declaration : :171</i>
     */
    public abstract void setDefaultTabInterval(CGFloat aFloat);

    /**
     * Original signature : <code>public abstract void setTextBlocks(NSArray*)</code><br>
     * <i>native declaration : :174</i>
     */
    public abstract void setTextBlocks(NSArray array);

    /**
     * Original signature : <code>public abstract void setTextLists(NSArray*)</code><br>
     * <i>native declaration : :175</i>
     */
    public abstract void setTextLists(NSArray array);

    /**
     * Original signature : <code>public abstract void setHyphenationFactor(float)</code><br>
     * <i>native declaration : :176</i>
     */
    public abstract void setHyphenationFactor(float aFactor);

    /**
     * Original signature : <code>public abstract void setTighteningFactorForTruncation(float)</code><br>
     * <i>native declaration : :177</i>
     */
    public abstract void setTighteningFactorForTruncation(float aFactor);

    /**
     * Original signature : <code>public abstract void setHeaderLevel(NSInteger)</code><br>
     * <i>native declaration : :178</i>
     */
    public abstract void setHeaderLevel(NSInteger level);
}
