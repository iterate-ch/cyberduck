package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSGraphics;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.Host;

/**
 * @version $Id$
 */
public class CDBookmarkCell extends CDTableCell {
	private Host bookmark;

	public CDBookmarkCell() {
		super();
	}

	protected CDBookmarkCell(NSCoder decoder, long token) {
		super(decoder, token);
	}

	protected void encodeWithCoder(NSCoder encoder) {
		super.encodeWithCoder(encoder);
	}

	public void setObjectValue(Object bookmark) {
		this.bookmark = (Host)bookmark;
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);
		if(bookmark != null) {
			NSPoint cellPoint = cellFrame.origin();
			NSSize cellSize = cellFrame.size();
			NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getNickname(), boldFont),
			    new NSRect(cellPoint.x(), cellPoint.y()+1, cellSize.width()-5, cellSize.height()));
			NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getCredentials().getUsername(), tinyFont),
			    new NSRect(cellPoint.x(), cellPoint.y()+14, cellSize.width()-5, cellSize.height()));
			NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getHostname()+"/"+bookmark.getDefaultPath(), tinyFont),
			    new NSRect(cellPoint.x(), cellPoint.y()+27, cellSize.width()-5, cellSize.height()));
		}
	}
}
