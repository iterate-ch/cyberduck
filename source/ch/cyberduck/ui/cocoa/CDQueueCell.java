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
		this.queue = (Queue)queue;
    }
    
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
//		log.debug("drawInteriorWithFrameInView");
//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and 
//coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
//invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
		controlView.lockFocus();
		
		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();	

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
			NSGraphics.fillRectListWithColors(new NSRect[]{new NSRect(cellPoint.x()-2, cellSize.height(), cellSize.width()+2, 1)}, new NSColor[]{NSColor.whiteColor()});
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
			NSGraphics.fillRectListWithColors(new NSRect[]{new NSRect(cellPoint.x()-2, cellSize.height(), cellSize.width()+2, 1)}, new NSColor[]{NSColor.darkGrayColor()});
		}
		
		if(queue.isInitialized()) {
			// drawing file icon
			NSImage fileIcon = null;
			NSImage arrowIcon = null;
			switch(queue.kind()) {
				case Queue.KIND_DOWNLOAD:
					arrowIcon = NSImage.imageNamed("arrowDown.tiff");
					if(queue.getCurrentJob().isFile())
						fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getCurrentJob().getExtension());
					else
						fileIcon = NSImage.imageNamed("folder.icns");
					break;
				case Queue.KIND_UPLOAD:
					arrowIcon = NSImage.imageNamed("arrowUp.tiff");
					if(queue.getCurrentJob().getLocal().isFile())
						fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getCurrentJob().getExtension());
					else
						fileIcon = NSImage.imageNamed("folder.icns");
					break;
			}
			
			fileIcon.setSize(new NSSize(32f, 32f));
			arrowIcon.setSize(new NSSize(32f, 32f));
//			float alpha = (float)(queue.getCurrent()/queue.getSize());
//			fileIcon.dissolveToPoint(new NSPoint(cellPoint.x(), cellPoint.y()+32f+1), alpha);
			fileIcon.compositeToPoint(new NSPoint(cellPoint.x(), cellPoint.y()+32f+1), NSImage.CompositeSourceOver);
			arrowIcon.compositeToPoint(new NSPoint(cellPoint.x()+4, cellPoint.y()+32f+4+1), NSImage.CompositeSourceOver);
				
			// drawing path properties
   // local file
			NSGraphics.drawAttributedString(
								   new NSAttributedString(queue.getCurrentJob().getName(), boldFont), 
								   new NSRect(cellPoint.x()+40, cellPoint.y()+1, cellSize.width()-5, cellSize.height())
								   );
			// remote url
			NSGraphics.drawAttributedString(
								   new NSAttributedString(queue.getCurrentJob().getHost().getURL()+queue.getCurrentJob().getAbsolute(), tinyFont),
								   new NSRect(cellPoint.x()+40, cellPoint.y()+20, cellSize.width()-5, cellSize.height())
								   );
			// drawing status
			NSGraphics.drawAttributedString(
								   new NSAttributedString(
								  Status.getSizeAsString(queue.getCurrentJob().status.getCurrent())+
								  " "+NSBundle.localizedString("of")+
								  " "+Status.getSizeAsString(queue.getCurrentJob().status.getSize())+" ("+
								  queue.getCurrentAsString()+" of "+
								  queue.getSizeAsString()+" "+NSBundle.localizedString("Total")+"), "+
								  queue.getSpeedAsString()+", "+queue.getTimeLeft(),
								  tinyFont),
								   new NSRect(cellPoint.x()+40, cellPoint.y()+33, cellSize.width()-5, cellSize.height())
								   );
			if(queue.isEmpty()) {
				NSGraphics.drawAttributedString(
									new NSAttributedString(
								"Complete ("+(queue.completedJobs())+" of "+(queue.numberOfJobs())+")", 
								tinyFont),
									new NSRect(cellPoint.x()+40, cellPoint.y()+46, cellSize.width()-5, cellSize.height())
									);
			}
			else {
			NSGraphics.drawAttributedString(
								   new NSAttributedString(
								  Queue.KIND_DOWNLOAD == queue.kind() ? 
								  "Downloading "+queue.getCurrentJob().getName()+" ("+(queue.completedJobs())+" of "+(queue.numberOfJobs())+")" : 
								  "Uploading "+queue.getCurrentJob().getName()+" ("+(queue.completedJobs())+" of "+(queue.numberOfJobs())+")", 
								  tinyFont),
								   new NSRect(cellPoint.x()+40, cellPoint.y()+46, cellSize.width()-5, cellSize.height())
								   );
			}
			
			// drawing progress bar
//controlView.addSubview (Indicator); 
			
//Indicator.setIndeterminate (false); 
//Indicator.startAnimation (null); 
// Indicator.setFrame (cellFrame); 
//Indicator.displayRect (cellFrame); 
			
		}
		controlView.unlockFocus();
	}	
}
