package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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
import com.apple.cocoa.foundation.NSCoder;
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
	private static NSMutableParagraphStyle PARAGRAPH_STYLE_LEFT_ALIGNMENT;
	private static NSMutableParagraphStyle PARAGRAPH_STYLE_RIGHT_ALIGNMENT;

	public CDTableCell() {
		super();
	}

	protected CDTableCell(NSCoder decoder, long token) {
		super(decoder, token);
	}

	protected void encodeWithCoder(NSCoder encoder) {
		super.encodeWithCoder(encoder);
	}

	static {
		PARAGRAPH_STYLE_LEFT_ALIGNMENT = new NSMutableParagraphStyle();
		PARAGRAPH_STYLE_LEFT_ALIGNMENT.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
		PARAGRAPH_STYLE_LEFT_ALIGNMENT.setAlignment(NSText.LeftTextAlignment);
		PARAGRAPH_STYLE_LEFT_ALIGNMENT.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);
	}

	static {
		PARAGRAPH_STYLE_RIGHT_ALIGNMENT = new NSMutableParagraphStyle();
		PARAGRAPH_STYLE_RIGHT_ALIGNMENT.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
		PARAGRAPH_STYLE_RIGHT_ALIGNMENT.setAlignment(NSText.RightTextAlignment);
		PARAGRAPH_STYLE_RIGHT_ALIGNMENT.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		this.highlighted = this.isHighlighted() && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(NSColor.secondarySelectedControlColor());

		//		Methods supporting the drawing of NSAttributedStrings are found in the Application Kit class NSGraphics.
		if(highlighted) { // cell is selected (white font)
			this.boldFont = BOLD_FONT_HIGHLIGHTED;
			this.normalFont = NORMAL_FONT_HIGHLIGHTED;
			this.tinyFont = TINY_FONT_HIGHLIGHTED;
			this.tinyFontRight = TINY_FONT_HIGHLIGHTED_RIGHT;
		}
		else { // cell is not selected (black font)
			this.boldFont = BOLD_FONT;
			this.normalFont = NORMAL_FONT;
			this.tinyFont = TINY_FONT;
			this.tinyFontRight = TINY_FONT_RIGHT;
		}
	}

	static protected NSDictionary BOLD_FONT = new NSDictionary(new Object[]{
		NSFont.boldSystemFontOfSize(11.0f),
		//								   NSColor.darkGrayColor(),
		PARAGRAPH_STYLE_LEFT_ALIGNMENT}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    //								   NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);

	static protected NSDictionary NORMAL_FONT = new NSDictionary(new Object[]{
		NSFont.systemFontOfSize(10.0f),
		//								   NSColor.darkGrayColor(),
		PARAGRAPH_STYLE_LEFT_ALIGNMENT}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    //								   NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);

	static protected NSDictionary TINY_FONT = new NSDictionary(new Object[]{
		NSFont.systemFontOfSize(10.0f),
		NSColor.darkGrayColor(),
		PARAGRAPH_STYLE_LEFT_ALIGNMENT},
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName});

	static protected NSDictionary TINY_FONT_RIGHT = new NSDictionary(new Object[]{
		NSFont.systemFontOfSize(10.0f),
		NSColor.darkGrayColor(),
		PARAGRAPH_STYLE_RIGHT_ALIGNMENT},
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName});

	static protected NSDictionary BOLD_FONT_HIGHLIGHTED = new NSDictionary(new Object[]{
		NSFont.boldSystemFontOfSize(11.0f),
		NSColor.whiteColor(),
		PARAGRAPH_STYLE_LEFT_ALIGNMENT}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);

	static protected NSDictionary NORMAL_FONT_HIGHLIGHTED = new NSDictionary(new Object[]{
		NSFont.systemFontOfSize(10.0f),
		NSColor.whiteColor(),
		PARAGRAPH_STYLE_LEFT_ALIGNMENT}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);

	static protected NSDictionary TINY_FONT_HIGHLIGHTED = new NSDictionary(new Object[]{
		NSFont.systemFontOfSize(10.0f),
		NSColor.whiteColor(),
		PARAGRAPH_STYLE_LEFT_ALIGNMENT},
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName});

	static protected NSDictionary TINY_FONT_HIGHLIGHTED_RIGHT = new NSDictionary(new Object[]{
		NSFont.systemFontOfSize(10.0f),
		NSColor.whiteColor(),
		PARAGRAPH_STYLE_RIGHT_ALIGNMENT},
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName});
}