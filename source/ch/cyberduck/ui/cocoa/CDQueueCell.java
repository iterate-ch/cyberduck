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
import ch.cyberduck.core.Queue;

import com.apple.cocoa.application.NSMenu;
import com.apple.cocoa.application.NSGraphics;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

import org.apache.log4j.Logger;

public class CDQueueCell extends CDTableCell {
	private static Logger log = Logger.getLogger(CDQueueCell.class);

	private Queue queue;

	public void setObjectValue(Object queue) {
		this.queue = (Queue) queue;
	}

	public static NSMenu defaultMenu() {
		return new NSMenu("Queue Item");
	}

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);

//		log.debug("Redrawing queue cell...");
		NSPoint cellPoint = cellFrame.origin();
		NSSize cellSize = cellFrame.size();

		//Locks the focus on the receiver, so subsequent commands take effect in the receiver’s window and
		//coordinate system. If you don’t use a display... method to draw an NSView, you must invoke lockFocus before
		//invoking methods that send commands to the window server, and must balance it with an unlockFocus message when finished.
//		controlView.lockFocus();

		// drawing file icon
		NSImage fileIcon = null;
		NSImage arrowIcon = null;
		switch (queue.kind()) {
			case Queue.KIND_DOWNLOAD:
				arrowIcon = NSImage.imageNamed("arrowDown.tiff");
				if (queue.getRoot().isFile())
					fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getRoot().getExtension());
				else
					fileIcon = NSImage.imageNamed("folder.icns");
				break;
			case Queue.KIND_UPLOAD:
				arrowIcon = NSImage.imageNamed("arrowUp.tiff");
				if (queue.getRoot().getLocal().isFile())
					fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getRoot().getExtension());
				else
					fileIcon = NSImage.imageNamed("folder.icns");
				break;
		}

		fileIcon.setSize(new NSSize(32f, 32f));
		arrowIcon.setSize(new NSSize(32f, 32f));

		final float BORDER = 40;
		final float SPACE = 5;

		fileIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE, cellPoint.y() + 32 + SPACE), NSImage.CompositeSourceOver);
		arrowIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE * 2, cellPoint.y() + 32 + SPACE * 2), NSImage.CompositeSourceOver);

		// drawing path properties
		// local file
		NSGraphics.drawAttributedString(
		    new NSAttributedString(Codec.decode(Codec.decode(queue.getRoot().getName())),
		        boldFont),
		    new NSRect(cellPoint.x() + BORDER + SPACE,
		        cellPoint.y() + SPACE,
		        cellSize.width() - BORDER - SPACE,
		        cellSize.height())
		);
		// remote url
		NSGraphics.drawAttributedString(
		    new NSAttributedString(queue.getRoot().getHost().getProtocol()+"://"+
								   queue.getRoot().getHost().getHostname() + 
								   Codec.decode(queue.getRoot().getAbsolute()),
		        tinyFont),
		    new NSRect(cellPoint.x() + BORDER + SPACE,
		        cellPoint.y() + 20,
		        cellSize.width() - BORDER - SPACE,
		        cellSize.height())
		);
		NSGraphics.drawAttributedString(
		    new NSAttributedString(
		        queue.getStatus(),
		        tinyFont),
		    new NSRect(cellPoint.x() + BORDER + SPACE,
		        cellPoint.y() + 33,
		        cellSize.width() - BORDER - SPACE,
		        cellSize.height())
		);
//		controlView.unlockFocus();
	}
}
