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

import ch.cyberduck.core.Codec;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Queue;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

public class CDQueueCell extends CDTableCell {
	private static Logger log = Logger.getLogger(CDQueueCell.class);

	private Queue queue;
	
	public void setObjectValue(Object queue) {
//		log.debug("setObjectValue:"+queue);
		this.queue = (Queue)queue;
    }
    
// catch key events when subclassing nstableview
//	public void keyDown(NSEvent event) {
//		log.debug("keyDown:"+event);
//		String keys = event.characters();
//		if (keys.length()==1 && keys.charAt(0)==NSText.DeleteCharacter) {
//			log.debug("delete event!");
//		}
//		else	
//			super.keyDown(event);
//	}
		
	// ---------------------------------------------------------
 	//  Context Menu
 	// ---------------------------------------------------------
	
	//	public NSMenu menuForEvent(NSEvent event, NSRect cellFrame, NSView aView) {
// does not seem to work
//	public NSMenu menu() {
//		log.debug("menu");
//		NSMenu menu = new NSMenu();
//		menu.addItem("Stop", new NSSelector("stopButtonClicked", new Class[] {Object.class}), "");
//		return menu;
//	}
	
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);
///		log.debug("drawInteriorWithFrameInView");
		
		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();	
		
		//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and 
		//coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
		//invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
		controlView.lockFocus();
		
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
		
		final float BORDER = 40;
		final float SPACE = 5;
		
		fileIcon.compositeToPoint(new NSPoint(cellPoint.x()+SPACE, cellPoint.y()+32+SPACE), NSImage.CompositeSourceOver);
		arrowIcon.compositeToPoint(new NSPoint(cellPoint.x()+SPACE*2, cellPoint.y()+32+SPACE*2), NSImage.CompositeSourceOver);
		
		// drawing path properties
		// local file
		NSGraphics.drawAttributedString(
										new NSAttributedString(Codec.encode(queue.getRoot().getName()), boldFont), 
										new NSRect(cellPoint.x()+BORDER+SPACE, 
												   cellPoint.y()+SPACE,
												   cellSize.width()-BORDER-SPACE, 
												   cellSize.height())
										);
		// remote url
		NSGraphics.drawAttributedString(
										new NSAttributedString(queue.getRoot().getHost().getURL()+queue.getRoot().getAbsolute(), tinyFont),
										new NSRect(cellPoint.x()+BORDER+SPACE, 
												   cellPoint.y()+20, 
												   cellSize.width()-BORDER-SPACE, 
												   cellSize.height())
										);
		// drawing status
/*		NSGraphics.drawAttributedString(
										new NSAttributedString(
															   queue.getCurrentAsString()
															   +" of "+
															   queue.getSizeAsString()
															   +" - "+//NSBundle.localizedString("Total")+"), "+
															   queue.getTimeLeft(),
															   tinyFont),
										new NSRect(cellPoint.x()+BORDER+SPACE, 
												   cellPoint.y()+33, 
												   cellSize.width()-BORDER-SPACE, 
												   cellSize.height())
										);
*/
		
		NSGraphics.drawAttributedString(
										new NSAttributedString(
															   queue.getStatus(),
															   tinyFont),
										new NSRect(cellPoint.x()+BORDER+SPACE, 
												   cellPoint.y()+33, 
//												   cellPoint.y()+46, 
												   cellSize.width()-BORDER-SPACE, 
												   cellSize.height())
										);
		controlView.unlockFocus();
	}	
}
