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

import com.apple.cocoa.application.NSGraphics;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

public class CDBookmarkCell extends CDTableCell {
	private Host bookmark;
	private NSImage image = NSImage.imageNamed("cyberduck-document.icns");

	public void setObjectValue(Object bookmark) {
		this.bookmark = (Host) bookmark;
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);
		//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and
		//coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
		//invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
//		controlView.lockFocus();

		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();

		NSGraphics.drawAttributedString(
		    new NSAttributedString(bookmark.getNickname(), boldFont),
		    new NSRect(cellPoint.x(), cellPoint.y() + 1, cellSize.width() - 5, cellSize.height())
		);
		NSGraphics.drawAttributedString(
		    new NSAttributedString(bookmark.getLogin().getUsername(), tinyFont),
		    new NSRect(cellPoint.x(), cellPoint.y() + 14, cellSize.width() - 5, cellSize.height())
		);
		NSGraphics.drawAttributedString(
		    new NSAttributedString(bookmark.getDefaultPath(), tinyFont),
		    new NSRect(cellPoint.x(), cellPoint.y() + 27, cellSize.width() - 5, cellSize.height())
		);
//		controlView.unlockFocus();
	}
}
