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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.Queue;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

public class CDQueueCell extends NSCell {
	private static Logger log = Logger.getLogger(CDQueueCell.class);

	private Queue queue;
	
	public void setObjectValue(Object queue) {
//		log.debug("setObjectValue:"+transfer);
		if(queue instanceof Queue)
	        this.queue = (Queue)queue;
    }
    
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
//		log.debug("drawInteriorWithFrameInView");
		NSMutableParagraphStyle paragraphStyle = new NSMutableParagraphStyle();
		paragraphStyle.setParagraphStyle(NSParagraphStyle.defaultParagraphStyle()); 
		paragraphStyle.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);

//		Methods supporting the drawing of NSAttributedStrings are found in the Application Kit class NSGraphics.
		NSDictionary boldFont;
		NSDictionary tinyFont;
		// cell is selected (white font)
		if (this.isHighlighted() && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(NSColor.secondarySelectedControlColor())) {
			boldFont = new NSDictionary(
							   new Object[]{
								   NSFont.boldSystemFontOfSize(11.0f), 
								   NSColor.whiteColor(), 
								   paragraphStyle}, //objects
							   new Object[]{
								   NSAttributedString.FontAttributeName, 
								   NSAttributedString.ForegroundColorAttributeName, 
								   NSAttributedString.ParagraphStyleAttributeName} //keys
							   );
			
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
		}
		// cell is not selected (black font)
		else {
			boldFont = new NSDictionary(
							   new Object[]{
								   NSFont.boldSystemFontOfSize(11.0f), 
//								   NSColor.darkGrayColor(), 
								   paragraphStyle}, //objects
							   new Object[]{
								   NSAttributedString.FontAttributeName, 
//								   NSAttributedString.ForegroundColorAttributeName, 
								   NSAttributedString.ParagraphStyleAttributeName} //keys
							   );
			
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
		}

		//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and 
  //coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
  //invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
		controlView.lockFocus();
		
		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();	
		
		// drawing file icon
		NSImage fileIcon = null;
		switch(queue.kind()) {
			case Queue.KIND_DOWNLOAD:
				if(queue.getCurrentJob().isFile())
					fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getCurrentJob().getExtension());
				else
					fileIcon = NSImage.imageNamed("folder.icns");
				break;
			case Queue.KIND_UPLOAD:
				if(queue.getCurrentJob().getLocal().isFile())
					fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getCurrentJob().getExtension());
				else
					fileIcon = NSImage.imageNamed("folder.icns");
				break;
		}
		
		fileIcon.setSize(new NSSize(16f, 16f));
		fileIcon.compositeToPoint(new NSPoint(cellPoint.x(), cellPoint.y()+16+1), NSImage.CompositeSourceOver);
		
		// drawing path properties
		// local file
		NSGraphics.drawAttributedString(
								  new NSAttributedString(queue.getCurrentJob().getName(), boldFont), 
								  new NSRect(cellPoint.x()+20, cellPoint.y()+1, cellSize.width()-5, cellSize.height())
								  );
		// remote url
		NSGraphics.drawAttributedString(
								  new NSAttributedString(queue.getCurrentJob().getHost().getURL()+queue.getCurrentJob().getAbsolute(), tinyFont),
								  new NSRect(cellPoint.x()+20, cellPoint.y()+20, cellSize.width()-5, cellSize.height())
								  );
		// drawing status
		NSGraphics.drawAttributedString(
								  new NSAttributedString(
								 (queue.getCurrentJob().status.getCurrent()/1024)+
								  " "+NSBundle.localizedString("of")+
								  " "+(queue.getCurrentJob().status.getSize()/1024)+"kB ("+
								  (queue.getCurrent()/1024)+" of "+
								  (queue.getSize()/1024)+"kB "+NSBundle.localizedString("Total")+"), "+
								  Status.parseLong(queue.getSpeed()/1024) + "kB/s, "+queue.getTimeLeft(),
								  tinyFont),
								  new NSRect(cellPoint.x()+20, cellPoint.y()+33, cellSize.width()-5, cellSize.height())
								  );
		
		NSGraphics.drawAttributedString(
								  new NSAttributedString(
								 Queue.KIND_DOWNLOAD == queue.kind() ? 
								 "Downloading "+queue.getCurrentJob().getName()+" ("+(queue.processedJobs())+" of "+(queue.numberOfJobs())+")" : 
								 "Uploading "+queue.getCurrentJob().getName()+" ("+(queue.processedJobs())+" of "+(queue.numberOfJobs())+")", 
								 tinyFont),
								  new NSRect(cellPoint.x()+20, cellPoint.y()+46, cellSize.width()-5, cellSize.height())
								  );
		controlView.unlockFocus();
	}	
}
