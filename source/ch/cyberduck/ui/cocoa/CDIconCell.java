package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DownloadQueue;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.SyncQueue;
import ch.cyberduck.core.UploadQueue;

import com.apple.cocoa.application.NSEvent;
import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSText;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSCoder;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

/**
 * @version $Id$
 */
public class CDIconCell extends CDTableCell {

    private Queue queue;

    public CDIconCell() {
        super();
    }

    protected CDIconCell(NSCoder decoder, long token) {
        super(decoder, token);
    }

    protected void encodeWithCoder(NSCoder encoder) {
        super.encodeWithCoder(encoder);
    }

    public void setObjectValue(Object q) {
        this.queue = (Queue) q;
    }

    private static final NSImage ARROW_UP_ICON = NSImage.imageNamed("arrowUp.tiff");
    private static final NSImage ARROW_DOWN_ICON = NSImage.imageNamed("arrowDown.tiff");
    private static final NSImage SYNC_ICON = NSImage.imageNamed("sync32.tiff");
    private static final NSImage MULTIPLE_DOCUMENTS_ICON = NSImage.imageNamed("multipleDocuments32.tiff");
    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder32.tiff");
    private static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    private static final float SPACE = 4;

    static {
        ARROW_UP_ICON.setSize(new NSSize(32f, 32f));
        ARROW_DOWN_ICON.setSize(new NSSize(32f, 32f));
        SYNC_ICON.setSize(new NSSize(32f, 32f));
        NOT_FOUND_ICON.setSize(new NSSize(32f, 32f));
    }

    public void editWithFrameInView(NSRect nsRect, NSView nsView, NSText nsText, Object object, NSEvent nsEvent) {
        super.editWithFrameInView(nsRect, nsView, nsText, object, nsEvent);
    }

    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        if(queue != null) {
            NSPoint cellPoint = cellFrame.origin();
            NSImage fileIcon = NOT_FOUND_ICON;
            if(queue.getRoot().getLocal().exists()) {
                if(queue.numberOfRoots() == 1) {
                    fileIcon = queue.getRoot().getLocal().attributes.isFile() ? NSWorkspace.sharedWorkspace().iconForFile(
                            queue.getRoot().getLocal().getAbsolute()) : FOLDER_ICON;
                }
                else {
                    fileIcon = MULTIPLE_DOCUMENTS_ICON;
                }
            }
            NSImage arrowIcon = null;
            if(queue instanceof DownloadQueue) {
                arrowIcon = ARROW_DOWN_ICON;
            }
            else if(queue instanceof UploadQueue) {
                arrowIcon = ARROW_UP_ICON;
            }
            else if(queue instanceof SyncQueue) {
                arrowIcon = SYNC_ICON;
                fileIcon = FOLDER_ICON;
            }
            fileIcon.setScalesWhenResized(true);
            fileIcon.setSize(new NSSize(32f, 32f));
            fileIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE,
                    cellPoint.y() + 32 + SPACE),
                    NSImage.CompositeSourceOver);
            if(arrowIcon != null) {
                arrowIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE * 3,
                        cellPoint.y() + 32 + SPACE * 4),
                        NSImage.CompositeSourceOver);
            }
        }
    }
}
