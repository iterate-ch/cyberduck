package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSAttributedString;
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

/**
 * @version $Id:$
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

    public static final NSDictionary PARAGRAPH_DICTIONARY_LEFT_ALIGNEMENT = new NSDictionary(
            new Object[]{PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});

    public static final NSDictionary PARAGRAPH_DICTIONARY_RIGHHT_ALIGNEMENT = new NSDictionary(
            new Object[]{PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});

    public static NSDictionary ALERT_FONT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.redColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    public static final NSDictionary FIXED_FONT = new NSDictionary(
            new Object[]{
                    NSFont.userFixedPitchFontOfSize(9.0f),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName}
    );

    public static NSDictionary BOLD_FONT = new NSDictionary(new Object[]{
            NSFont.boldSystemFontOfSize(11.0f),
            //								   NSColor.darkGrayColor(),
            PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    //								   NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    public static NSDictionary NORMAL_FONT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(11.0f),
                    //								   NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    //								   NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    public static NSDictionary NORMAL_GRAY_FONT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(11.0f),
                    NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    public static NSDictionary TINY_FONT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    public static NSDictionary TINY_FONT_RIGHT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    public static NSDictionary ALERT_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.redColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    public static final NSDictionary FIXED_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.userFixedPitchFontOfSize(9.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName}
    );

    public static NSDictionary BOLD_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(11.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    public static NSDictionary NORMAL_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(11.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    public static NSDictionary TINY_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName
            }

    );

    public static NSDictionary TINY_FONT_HIGHLIGHTED_RIGHT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

}
