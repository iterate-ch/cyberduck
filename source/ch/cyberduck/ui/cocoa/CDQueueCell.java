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

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class CDQueueCell extends CDTableCell {
	private static Logger log = Logger.getLogger(CDQueueCell.class);

	private Queue queue;
	
//	private static Map icons = new HashMap();

	public void setObjectValue(Object q) {
		this.queue = (Queue)q;
//		icons.put(this.queue.getRoot().getExtension(), NSWorkspace.sharedWorkspace().iconForFileType(this.queue.getRoot().getExtension()));
	}

	public static NSMenu defaultMenu() {
		return new NSMenu("Queue Item");
	}
	
	private static final NSImage arrowUpIcon = NSImage.imageNamed("arrowUp.tiff");
	private static final NSImage arrowDownIcon = NSImage.imageNamed("arrowDown.tiff");
	private static final NSImage folderIcon = NSImage.imageNamed("folder32.tiff");
	
	static {
		arrowUpIcon.setSize(new NSSize(32f, 32f));
		arrowDownIcon.setSize(new NSSize(32f, 32f));
	}

	private NSImage fileIcon = null;
	private NSImage arrowIcon = null;

	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawInteriorWithFrameInView(cellFrame, controlView);
		if(queue != null) {
			//		log.debug("Redrawing queue cell...");
			NSPoint cellPoint = cellFrame.origin();
			NSSize cellSize = cellFrame.size();
			
			// drawing file icon
			switch (queue.kind()) {
				case Queue.KIND_DOWNLOAD:
					arrowIcon = arrowDownIcon;
					if (queue.getRoot().isFile())
//						fileIcon = (NSImage)icons.get(queue.getRoot().getExtension());
						fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getRoot().getExtension());
					else if (queue.getRoot().isDirectory())
						fileIcon = folderIcon;
					break;
				case Queue.KIND_UPLOAD:
					arrowIcon = arrowUpIcon;
					if (queue.getRoot().getLocal().isFile())
//						fileIcon = (NSImage)icons.get(queue.getRoot().getExtension());
						fileIcon = NSWorkspace.sharedWorkspace().iconForFileType(queue.getRoot().getExtension());
					else if (queue.getRoot().getLocal().isDirectory())
						fileIcon = folderIcon;
					break;
			}
			
			final float BORDER = 40;
			final float SPACE = 5;
			
			if(fileIcon != null) {
//				fileIcon.setScalesWhenResized(true);
				fileIcon.setSize(new NSSize(32f, 32f));
				
				fileIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE, cellPoint.y() + 32 + SPACE), NSImage.CompositeSourceOver);
				arrowIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE * 2, cellPoint.y() + 32 + SPACE * 2), NSImage.CompositeSourceOver);
			}
			
			// drawing path properties
			// local file
			NSGraphics.drawAttributedString(
											new NSAttributedString(queue.getRoot().getName(),
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
																   queue.getRoot().getAbsolute(),
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
		}
	}
}
