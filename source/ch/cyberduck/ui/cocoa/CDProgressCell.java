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
		//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and 
  //coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
  //invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
//		controlView.lockFocus();

		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();	
		
		float progressHeight = 10;
		float progressWidth = (float)((float)queue.getCurrent()/(float)queue.getSize()*(cellSize.width()-10));
		
		NSRect barRect = new NSRect(cellPoint.x()+5, cellSize.height()/2-progressHeight/2, cellSize.width()-10, progressHeight);
		NSRect barRectFilled = new NSRect(cellPoint.x()+5, cellSize.height()/2-progressHeight/2, progressWidth, progressHeight);

		// cell is selected (white graphic)
		if (this.isHighlighted() && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(NSColor.secondarySelectedControlColor())) {
			NSColor.whiteColor().set();
		}
		// cell is not selected (black font)
		else {
			NSColor.lightGrayColor().set();
		}
		NSBezierPath.strokeRect(barRect);
		NSBezierPath.fillRect(barRectFilled);
		
//		controlView.unlockFocus();
	}
}