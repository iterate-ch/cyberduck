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

import ch.cyberduck.core.Queue;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

public class CDProgressCell extends NSCell {
	private static Logger log = Logger.getLogger(CDProgressCell.class);
	
	private Queue queue;
	
	public void setObjectValue(Object queue) {
//		log.debug("setObjectValue:"+queue);
		this.queue = (Queue)queue;
    }
	
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
//		log.debug("drawInteriorWithFrameInView");

		NSMutableParagraphStyle paragraphStyle = new NSMutableParagraphStyle();
		paragraphStyle.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle()); 
		paragraphStyle.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);

		NSDictionary tinyFont;
		// cell is selected (white font)
		if (this.isHighlighted() && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(NSColor.secondarySelectedControlColor())) {
			tinyFont = new NSDictionary(
							   new Object[]{
								   NSFont.systemFontOfSize(10.0f), 
								   NSColor.whiteColor(),
								   paragraphStyle},
							   new Object[]{
								   NSAttributedString.FontAttributeName, 
								   NSAttributedString.ForegroundColorAttributeName, 
								   NSAttributedString.ParagraphStyleAttributeName}
							   );
			//			NSGraphics.fillRectListWithColors(new NSRect[]{new NSRect(cellPoint.x()-2, cellSize.height(), cellSize.width()+2, 1)}, new NSColor[]{NSColor.whiteColor()});
		}
		// cell is not selected (black font)
		else {
			tinyFont = new NSDictionary(
							   new Object[]{
								   NSFont.systemFontOfSize(10.0f), 
								   NSColor.darkGrayColor(),
								   paragraphStyle},
							   new Object[]{
								   NSAttributedString.FontAttributeName, 
								   NSAttributedString.ForegroundColorAttributeName, 
								   NSAttributedString.ParagraphStyleAttributeName}
							   );
			//			NSGraphics.fillRectListWithColors(new NSRect[]{new NSRect(cellPoint.x()-2, cellSize.height(), cellSize.width()+2, 1)}, new NSColor[]{NSColor.darkGrayColor()});
		}

		
		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();	
		
		float progressHeight = 10;
		float progress = (float)((float)queue.getCurrent()/(float)queue.getSize());
		float progressWidth = progress*(cellSize.width()-10);
		
		NSRect barRect = new NSRect(cellPoint.x()+5, cellPoint.y()+cellSize.height()/2-progressHeight/2, cellSize.width()-10, progressHeight);
		NSRect barRectFilled = new NSRect(cellPoint.x()+5, cellPoint.y()+cellSize.height()/2-progressHeight/2, progressWidth, progressHeight);

		// cell is selected (white graphic)
		if (this.isHighlighted() && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(NSColor.secondarySelectedControlColor())) {
			NSColor.whiteColor().set();
		}
		// cell is not selected (black font)
		else {
			NSColor.lightGrayColor().set();
		}

		//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and 
  //coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
  //invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
		controlView.lockFocus();
		
		NSBezierPath.strokeRect(barRect);
		NSBezierPath.fillRect(barRectFilled);

		NSGraphics.drawAttributedString(
								  new NSAttributedString((int)(progress*100)+"%", tinyFont), 
								  new NSRect(cellPoint.x()+5, cellPoint.y()+cellSize.height()/2+progressHeight+3, cellSize.width()-10, cellSize.height())
								  );
		
		controlView.unlockFocus();
	}
}