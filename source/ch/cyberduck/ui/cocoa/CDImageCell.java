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

public class CDImageCell extends NSCell {
	private static Logger log = Logger.getLogger(CDImageCell.class);
	
	private Queue queue;
	
	public void setObjectValue(Object queue) {
		log.debug("setObjectValue:"+queue);
		this.queue = (Queue)queue;
    }
		
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		log.debug("drawInteriorWithFrameInView");
		//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and 
  //coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
  //invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
		controlView.lockFocus();
		
		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();	
		
		// drawing file icon
		NSImage fileIcon = null;
		NSImage arrowIcon = null;
		switch(queue.kind()) {
			case Queue.KIND_DOWNLOAD:
				arrowIcon = NSImage.imageNamed("arrowDown.tiff");
				if(queue.getRoot().isFile())
					fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getRoot().getExtension());
				else
					fileIcon = NSImage.imageNamed("folder.icns");
				break;
			case Queue.KIND_UPLOAD:
				arrowIcon = NSImage.imageNamed("arrowUp.tiff");
				if(queue.getRoot().getLocal().isFile())
					fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getRoot().getExtension());
				else
					fileIcon = NSImage.imageNamed("folder.icns");
				break;
		}
		
		fileIcon.setSize(new NSSize(32f, 32f));
		arrowIcon.setSize(new NSSize(32f, 32f));
		//			float alpha = (float)(queue.getCurrent()/queue.getSize());
		//			fileIcon.dissolveToPoint(new NSPoint(cellPoint.x(), cellPoint.y()+32+1), alpha);
		fileIcon.compositeToPoint(new NSPoint(cellPoint.x(), cellPoint.y()+32+1), NSImage.CompositeSourceOver);
		arrowIcon.compositeToPoint(new NSPoint(cellPoint.x()+4, cellPoint.y()+32+4+1), NSImage.CompositeSourceOver);
		
		controlView.unlockFocus();
	}	
}
