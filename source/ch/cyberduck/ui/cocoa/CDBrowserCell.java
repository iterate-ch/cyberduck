package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 Whitney Young. All rights reserved.
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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import ch.cyberduck.core.Path;

/**
* @version $Id$
 */
public class CDBrowserCell extends NSBrowserCell {

	protected static final NSImage SYMLINK_ICON		= NSImage.imageNamed("symlink.tiff");
    protected static final NSImage FOLDER_ICON		= NSImage.imageNamed("folder16.tiff");
    protected static final NSImage NOT_FOUND_ICON	= NSImage.imageNamed("notfound.tiff");
	
	private Path path;
	
    public CDBrowserCell() {
        super();
    }
	
	public CDBrowserCell(NSImage image) {
		super(image);
	}

	public CDBrowserCell(String menu) {
		super(menu);
	}
	
    protected CDBrowserCell(NSCoder decoder, long token) {
        super(decoder, token);
    }

    protected void encodeWithCoder(NSCoder encoder) {
        super.encodeWithCoder(encoder);
    }
	
	private NSImage icon;
	
	public NSImage icon() {
		return this.icon;
	}
	
	public void setIcon(NSImage icon) {
		this.icon = icon;
	}
	
	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();
	
	static {
		lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
	}
	
	protected static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
																								new Object[]{NSAttributedString.ParagraphStyleAttributeName});
	
	public Path getPath() {
		return this.path;
	}
	
	public void setPath(Path path) {
		this.path = path;
		this.setLeaf(path.attributes.isFile());
		this.setAttributedStringValue(new NSAttributedString(this.path.getName(),
															 TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
		NSImage image;
		if (path.attributes.isSymbolicLink()) {
			image = SYMLINK_ICON;
		}
		else if (path.attributes.isDirectory()) {
			image = FOLDER_ICON;
		}
		else if (path.attributes.isFile()) {
			image = CDIconCache.instance().get(path.getExtension());
		}
		else {
			image = NOT_FOUND_ICON;
		}
		image.setSize(new NSSize(16f, 16f));
		this.setIcon(image);
	}
	
	protected static final float HEIGHT = 19f;

    public void editWithFrameInView(NSRect rect, NSView controlView, NSText text, Object object, NSEvent event) {
        if(controlView instanceof NSMatrix) {
            ((NSMatrix)controlView).sendAction();
        }
        super.editWithFrameInView(rect, controlView, text, object, event);
    }

    public void drawWithFrameInView(NSRect cellFrame, NSView controlView) {
		super.drawWithFrameInView(new NSRect(cellFrame.x(), cellFrame.y(), 
											 cellFrame.width(), CDBrowserCell.HEIGHT), controlView);
	}
	
	public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
		if(this.icon() != null) {
            cellFrame = new NSRect(cellFrame.x(), cellFrame.y(),
                    cellFrame.width(), CDBrowserCell.HEIGHT);
            NSRect iconRect = new NSRect(cellFrame.x(), cellFrame.y(),
                    this.icon().size().width()+4,
                    CDBrowserCell.HEIGHT);
            NSRect textRect = new NSRect(cellFrame.x()+4+this.icon().size().width()+4,
                    cellFrame.y(),
                    cellFrame.width()-4-this.icon().size().width()-4,
                    CDBrowserCell.HEIGHT);
            super.drawInteriorWithFrameInView(textRect,
                    controlView);
            if(this.isHighlighted()) {
                NSRect selectionRect = new NSRect(cellFrame.x(), cellFrame.y(),
                        this.icon().size().width()+4+4, CDBrowserCell.HEIGHT);
                NSColor background = this.highlightColorInView(controlView); background.set();
                NSBezierPath.fillRect(selectionRect);
            }
            if(controlView.isFlipped()) {
				this.icon().compositeToPoint(new NSPoint(iconRect.x()+4,
														 iconRect.y()+(iconRect.size().height()+this.icon().size().height())/2),
											 NSImage.CompositeSourceOver);
            }
            else {
				this.icon().compositeToPoint(new NSPoint(iconRect.x()+4,
														 iconRect.y()+(iconRect.size().height()-this.icon().size().height())/2),
											 NSImage.CompositeSourceOver);
            }
		}
	}
}