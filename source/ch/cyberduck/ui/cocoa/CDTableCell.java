package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

public class CDTableCell extends NSCell {

	protected boolean highlighted;
	protected NSDictionary boldFont;
	protected NSDictionary normalFont;
	protected NSDictionary tinyFont;
	private static NSMutableParagraphStyle paragraphStyle;

	static {
		paragraphStyle = new NSMutableParagraphStyle();
		paragraphStyle.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle());
		paragraphStyle.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		this.highlighted = this.isHighlighted() && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(NSColor.secondarySelectedControlColor());

		//		Methods supporting the drawing of NSAttributedStrings are found in the Application Kit class NSGraphics.
		if (highlighted) { // cell is selected (white font)
			this.boldFont = BOLD_FONT_HIGHLIGHTED;
			this.normalFont = NORMAL_FONT_HIGHLIGHTED;
			this.tinyFont = TINY_FONT_HIGHLIGHTED;
		}
		else { // cell is not selected (black font)
			this.boldFont = BOLD_FONT;
			this.normalFont = NORMAL_FONT;
			this.tinyFont = TINY_FONT;
		}
	}

	static protected NSDictionary BOLD_FONT = new NSDictionary(
	    new Object[]{
		    NSFont.boldSystemFontOfSize(11.0f),
		    //								   NSColor.darkGrayColor(),
		    paragraphStyle}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    //								   NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);
	static protected NSDictionary NORMAL_FONT = new NSDictionary(
	    new Object[]{
		    NSFont.systemFontOfSize(10.0f),
		    //								   NSColor.darkGrayColor(),
		    paragraphStyle}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    //								   NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);
	static protected NSDictionary TINY_FONT = new NSDictionary(
	    new Object[]{
		    NSFont.systemFontOfSize(10.0f),
		    NSColor.darkGrayColor(),
		    paragraphStyle},
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName}
	);
	static protected NSDictionary BOLD_FONT_HIGHLIGHTED = new NSDictionary(
	    new Object[]{
		    NSFont.boldSystemFontOfSize(11.0f),
		    NSColor.whiteColor(),
		    paragraphStyle}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);
	static protected NSDictionary NORMAL_FONT_HIGHLIGHTED = new NSDictionary(
	    new Object[]{
		    NSFont.systemFontOfSize(10.0f),
		    NSColor.whiteColor(),
		    paragraphStyle}, //objects
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName} //keys
	);
	static protected NSDictionary TINY_FONT_HIGHLIGHTED = new NSDictionary(
	    new Object[]{
		    NSFont.systemFontOfSize(10.0f),
		    NSColor.whiteColor(),
		    paragraphStyle},
	    new Object[]{
		    NSAttributedString.FontAttributeName,
		    NSAttributedString.ForegroundColorAttributeName,
		    NSAttributedString.ParagraphStyleAttributeName}
	);

}