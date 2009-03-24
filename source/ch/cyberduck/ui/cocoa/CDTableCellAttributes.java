package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.Preferences;

/**
 * @version $Id$
 */
public class CDTableCellAttributes {

    protected static final NSMutableParagraphStyle PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE;

    static {
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE = new NSMutableParagraphStyle();
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setAlignment(NSText.LeftTextAlignment);
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
    }

    protected static final NSMutableParagraphStyle PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL;

    static {
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL = new NSMutableParagraphStyle();
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setAlignment(NSText.LeftTextAlignment);
        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);
    }

    protected static final NSMutableParagraphStyle PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL;

    static {
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL = new NSMutableParagraphStyle();
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setAlignment(NSText.RightTextAlignment);
        PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);
    }

    public static NSDictionary browserFontLeftAlignment() {
        return new NSDictionary(
                new Object[]{NSFont.systemFontOfSize(Preferences.instance().getFloat("browser.font.size")), PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
                new Object[]{NSAttributedString.FontAttributeName, NSAttributedString.ParagraphStyleAttributeName});

    }

    public static NSDictionary browserFontRightAlignment() {
        return new NSDictionary(
                new Object[]{NSFont.systemFontOfSize(Preferences.instance().getFloat("browser.font.size")), PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL},
                new Object[]{NSAttributedString.FontAttributeName, NSAttributedString.ParagraphStyleAttributeName});
    }

    public static NSDictionary boldFontWithSize(float size) {
        return new NSDictionary(new Object[]{
                NSFont.boldSystemFontOfSize(size),
                PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
                new Object[]{
                        NSAttributedString.FontAttributeName,
                        NSAttributedString.ParagraphStyleAttributeName} //keys
        );
    }

    public static NSDictionary normalFontWithSize(float size) {
        return new NSDictionary(new Object[]{
                NSFont.systemFontOfSize(size),
                PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
                new Object[]{
                        NSAttributedString.FontAttributeName,
                        NSAttributedString.ParagraphStyleAttributeName} //keys
        );
    }

    public static NSDictionary darkFontWithSize(float size) {
        return new NSDictionary(
                new Object[]{
                        NSFont.systemFontOfSize(size),
                        NSColor.darkGrayColor(),
                        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
                new Object[]{
                        NSAttributedString.FontAttributeName,
                        NSAttributedString.ForegroundColorAttributeName,
                        NSAttributedString.ParagraphStyleAttributeName} //keys
        );
    }

    public static NSDictionary highlightedBoldFontWithSize(float size) {
        return new NSDictionary(
                new Object[]{
                        NSFont.boldSystemFontOfSize(size),
                        NSColor.whiteColor(),
                        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
                new Object[]{
                        NSAttributedString.FontAttributeName,
                        NSAttributedString.ForegroundColorAttributeName,
                        NSAttributedString.ParagraphStyleAttributeName} //keys
        );
    }

    public static NSDictionary highlightedFontWithSize(float size) {
        return new NSDictionary(new Object[]{
                NSFont.systemFontOfSize(size),
                NSColor.whiteColor(),
                PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
                new Object[]{
                        NSAttributedString.FontAttributeName,
                        NSAttributedString.ForegroundColorAttributeName,
                        NSAttributedString.ParagraphStyleAttributeName} //keys
        );
    }
}
