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
///		log.debug("drawInteriorWithFrameInView");
		
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
//			NSGraphics.fillRectListWithColors(new NSRect[]{new NSRect(cellPoint.x()-2, cellSize.height(), cellSize.width()+2, 1)}, new NSColor[]{NSColor.whiteColor()});
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
			//			NSGraphics.fillRectListWithColors(new NSRect[]{new NSRect(cellPoint.x()-2, cellSize.height(), cellSize.width()+2, 1)}, new NSColor[]{NSColor.darkGrayColor()});
		}
		
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
										new NSAttributedString(queue.getRoot().getDecodedName(), boldFont), 
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
		NSGraphics.drawAttributedString(
										new NSAttributedString(
															   //								  Status.getSizeAsString(queue.getRoot().status.getCurrent())+
															   //								  " "+NSBundle.localizedString("of")+
															   //								  " "+Status.getSizeAsString(queue.getRoot().status.getSize())+" ("+
															   queue.getCurrentAsString()
															   +" of "+
															   queue.getSizeAsString()
															   +" - "+//NSBundle.localizedString("Total")+"), "+
//															   queue.getSpeedAsString()+", "+
															   queue.getTimeLeft(),
															   tinyFont),
										new NSRect(cellPoint.x()+BORDER+SPACE, 
												   cellPoint.y()+33, 
												   cellSize.width()-BORDER-SPACE, 
												   cellSize.height())
										);
		
		NSGraphics.drawAttributedString(
										new NSAttributedString(
															   queue.getStatus(),
															   tinyFont),
										new NSRect(cellPoint.x()+BORDER+SPACE, 
												   cellPoint.y()+46, 
												   cellSize.width()-BORDER-SPACE, 
												   cellSize.height())
										);
		/*
		if(queue.isEmpty()) {
			NSGraphics.drawAttributedString(
											new NSAttributedString(
																   queue.getElapsedTime()+" "+
																   "Complete ("+(queue.completedJobs())+
																   " of "+
																   (queue.numberOfJobs())+
																   ")", 
																   tinyFont),
											new NSRect(cellPoint.x()+BORDER+SPACE, 
													   cellPoint.y()+46, 
													   cellSize.width()-BORDER-SPACE, 
													   cellSize.height())
											);
		}
		else {
			NSGraphics.drawAttributedString(
											new NSAttributedString(
																   Queue.KIND_DOWNLOAD == queue.kind() ? 
																   queue.getElapsedTime()+" "+"Downloading "+queue.getRoot().getDecodedName()+" ("+(queue.completedJobs())+" of "+(queue.numberOfJobs())+")" : 
																   queue.getElapsedTime()+" "+"Uploading "+queue.getRoot().getDecodedName()+" ("+(queue.completedJobs())+" of "+(queue.numberOfJobs())+")", 
																   tinyFont),
											new NSRect(cellPoint.x()+BORDER+SPACE, 
													   cellPoint.y()+46, 
													   cellSize.width()-BORDER-SPACE, 
													   cellSize.height())
											);
		}
		 */
		controlView.unlockFocus();
	}	
}
