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

import ch.cyberduck.core.Host;
import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;
import org.apache.log4j.Logger;

public class CDBookmarkCell extends NSCell {
	private static Logger log = Logger.getLogger(CDBookmarkCell.class);

	private Host favorite;
	private	NSImage image = NSImage.imageNamed("cyberduck-document.icns");
	
	public void setObjectValue(Object favorite) {
//		log.debug("setObjectValue:"+favorite);
		if(favorite instanceof Host)
	        this.favorite = (Host)favorite;
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
		
/*		NSMutableRect mutableCellFrame = new NSMutableRect(cellFrame);
		
		NSMutableRect imageFrame = new NSMutableRect();
		NSSize imageSize = new NSSize(cellSize.height()/image.size().height()*image.size().width(), cellSize.height());
//		NSSize imageSize = image.size();
		mutableCellFrame.sliceRect(3 + imageSize.width(), NSRect.MinXEdge, imageFrame, mutableCellFrame);
//		mutableCellFrame.sliceRect(3 + image.size().width(), NSRect.MinXEdge, imageFrame, mutableCellFrame);
		imageFrame.setX(imageFrame.x() + 3);
		imageFrame.setSize(imageSize);
		if(controlView.isFlipped()) {
			imageFrame.setY(imageFrame.y() + (float)Math.ceil((mutableCellFrame.size().height() +
													  imageFrame.size().height()) / 2));
		} else {
			imageFrame.setY(imageFrame.y() - (float)Math.ceil((mutableCellFrame.size().height() +
													  imageFrame.size().height()) / 2));
		}
		image.compositeToPoint(imageFrame.origin(), NSImage.CompositeSourceOver);
*/
				
		NSGraphics.drawAttributedString(
								  new NSAttributedString(favorite.getNickname(), boldFont), 
								  new NSRect(cellPoint.x(), cellPoint.y()+1, cellSize.width()-5, cellSize.height())
								  );
		NSGraphics.drawAttributedString(
								  new NSAttributedString(favorite.getLogin().getUsername(), tinyFont),
								  new NSRect(cellPoint.x(), cellPoint.y()+14, cellSize.width()-5, cellSize.height())
								  );
		NSGraphics.drawAttributedString(
								  new NSAttributedString(favorite.getDefaultPath(), tinyFont),
								  new NSRect(cellPoint.x(), cellPoint.y()+28, cellSize.width()-5, cellSize.height())
								  );
		controlView.unlockFocus();
	}	
}
