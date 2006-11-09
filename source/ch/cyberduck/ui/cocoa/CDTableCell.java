package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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
import com.apple.cocoa.foundation.NSRect;

/**
 * @version $Id$
 */
public class CDTableCell extends NSCell {

    protected boolean highlighted;

    protected NSDictionary boldFont;
    protected NSDictionary normalFont;
    protected NSDictionary tinyFont;
    protected NSDictionary tinyFontRight;
    protected NSDictionary alertFont;
    protected NSDictionary fixedFont;

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


    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        super.drawInteriorWithFrameInView(cellFrame, controlView);
        this.highlighted = this.isHighlighted()
                && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(
                NSColor.secondarySelectedControlColor()
        );

        //		Methods supporting the drawing of NSAttributedStrings are found in the Application Kit class NSGraphics.
        if(highlighted) { // cell is selected (white font)
            this.boldFont = BOLD_FONT_HIGHLIGHTED;
            this.normalFont = NORMAL_FONT_HIGHLIGHTED;
            this.tinyFont = TINY_FONT_HIGHLIGHTED;
            this.tinyFontRight = TINY_FONT_HIGHLIGHTED_RIGHT;
            this.alertFont = ALERT_FONT_HIGHLIGHTED;
            this.fixedFont = FIXED_FONT_HIGHLIGHTED;
        }
        else { // cell is not selected (black font)
            this.boldFont = BOLD_FONT;
            this.normalFont = NORMAL_FONT;
            this.tinyFont = TINY_FONT;
            this.tinyFontRight = TINY_FONT_RIGHT;
            this.alertFont = ALERT_FONT;
            this.fixedFont = FIXED_FONT;
        }
    }

    protected static NSDictionary ALERT_FONT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.redColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    protected static final NSDictionary FIXED_FONT = new NSDictionary(
            new Object[]{
                    NSFont.userFixedPitchFontOfSize(9.0f),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName}
    );

    static protected NSDictionary BOLD_FONT = new NSDictionary(new Object[]{
            NSFont.boldSystemFontOfSize(11.0f),
            //								   NSColor.darkGrayColor(),
            PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    //								   NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    static protected NSDictionary NORMAL_FONT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    //								   NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    //								   NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    static protected NSDictionary TINY_FONT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    static protected NSDictionary TINY_FONT_RIGHT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.darkGrayColor(),
                    PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    protected static NSDictionary ALERT_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.redColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

    protected static final NSDictionary FIXED_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.userFixedPitchFontOfSize(9.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName}
    );

    static protected NSDictionary BOLD_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(11.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    static protected NSDictionary NORMAL_FONT_HIGHLIGHTED = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    static protected NSDictionary TINY_FONT_HIGHLIGHTED = new NSDictionary(
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

    static protected NSDictionary TINY_FONT_HIGHLIGHTED_RIGHT = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(10.0f),
                    NSColor.whiteColor(),
                    PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName});

}