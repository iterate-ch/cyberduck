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

public class CDProgressCell extends CDTableCell {
	private static Logger log = Logger.getLogger(CDProgressCell.class);
	
	private Queue queue;
	
	public void setObjectValue(Object queue) {
//		log.debug("setObjectValue:"+queue);
		this.queue = (Queue)queue;
    }
	
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);
//		log.debug("drawInteriorWithFrameInView");

		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();	
		
		final float SPACE = 5;
		final float PROGRESS_HEIGHT = 10;
		float progress;
		if(queue.getSize() > 0)
			progress = (float)((float)queue.getCurrent()/(float)queue.getSize());
		else
			progress = 0;
		log.debug("progress:"+progress);
		final float PROGRESS_WIDTH = progress*(cellSize.width()-SPACE*2);

		NSRect barRect = new NSRect(cellPoint.x()+SPACE, 
									cellPoint.y()+cellSize.height()/2-PROGRESS_HEIGHT/2, 
									cellSize.width()-SPACE*2, 
									PROGRESS_HEIGHT);
		NSRect barRectFilled = new NSRect(cellPoint.x()+SPACE, 
										  cellPoint.y()+cellSize.height()/2-PROGRESS_HEIGHT/2, 
										  PROGRESS_WIDTH, 
										  PROGRESS_HEIGHT);

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
		
		// drawing progress bar
		NSBezierPath.strokeRect(barRect);
		NSBezierPath.fillRect(barRectFilled);

		// drawing current of size string
		NSGraphics.drawAttributedString(
										new NSAttributedString((int)(progress*100)+"%"
															   +" - "+
															   queue.getCurrentAsString()
															   +" of "+
															   queue.getSizeAsString(),
															   tinyFont),
										new NSRect(cellPoint.x()+SPACE, 
												   cellPoint.y()+cellSize.height()/2-PROGRESS_HEIGHT/2-10-SPACE, 
												   cellSize.width()-SPACE, 
												   cellSize.height())
										);
		
		// drawing percentage and speed
		NSGraphics.drawAttributedString(
								  new NSAttributedString(queue.getSpeedAsString()
														 +" - "+
														 queue.getTimeLeft(),
														 tinyFont), 
								  new NSRect(cellPoint.x()+SPACE, 
											 cellPoint.y()+cellSize.height()/2+PROGRESS_HEIGHT/2+SPACE, 
											 cellSize.width()-SPACE, 
											 cellSize.height())
								  );
		
		controlView.unlockFocus();
	}
}