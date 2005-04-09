package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 Whitney Young. All rights reserved.
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

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSTextFieldCell;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSCoder;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;

import org.apache.log4j.Logger;

public class CDOutlineCell extends NSTextFieldCell {
	private static Logger log = Logger.getLogger(CDOutlineCell.class);
	
	public CDOutlineCell() {
		super();
	}
	
	protected CDOutlineCell(NSCoder decoder, long token) {
		super(decoder, token);
	}
	
	protected void encodeWithCoder(NSCoder encoder) {
		super.encodeWithCoder(encoder);
	}
	
	private NSImage icon;
	
	public void setIcon(NSImage icon) {
		this.icon = icon;
	}
	
	public NSImage icon() {
		return this.icon;
	}
	
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		if(this.icon() != null) {
			if(controlView.isFlipped()) {
				this.icon().compositeToPoint(new NSPoint(cellFrame.origin().x()+3,
														 cellFrame.origin().y()+(cellFrame.size().height()+this.icon().size().height())/2),
											 NSImage.CompositeSourceOver);
			}
			else {
				this.icon().compositeToPoint(new NSPoint(cellFrame.origin().x()+3,
														 cellFrame.origin().y()+(cellFrame.size().height()-this.icon().size().height())/2),
											 NSImage.CompositeSourceOver);
			}
			super.drawInteriorWithFrameInView(new NSRect(cellFrame.origin().x()+6+this.icon().size().width(),
														 cellFrame.origin().y(),
														 cellFrame.width()-6-this.icon().size().width(),
														 cellFrame.height()),
											  controlView);
		}
		else {
			super.drawInteriorWithFrameInView(cellFrame, controlView);
		}
	}

//	public NSSize cellSizeForBounds(NSRect rect) {
//		return new NSSize(this.icon() != null ? rect.width()+this.icon().size().width()+6 : rect.width()+6,
//						  super.cellSizeForBounds(rect).height());
//	}
}
