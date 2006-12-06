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

import ch.cyberduck.core.Transfer;
import ch.cyberduck.core.SyncTransfer;

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

/**
 * @version $Id$
 */
public class CDIconCell extends CDTableCell {

    private Transfer transfer;

    public CDIconCell() {
        super();
    }

    public void setObjectValue(Object q) {
        this.transfer = (Transfer) q;
    }

    private static final NSImage MULTIPLE_DOCUMENTS_ICON = NSImage.imageNamed("multipleDocuments32.tiff");
    private static final NSImage FOLDER_ICON = NSImage.imageNamed("folder32.tiff");
    private static final NSImage NOT_FOUND_ICON = NSImage.imageNamed("notfound.tiff");

    private static final float SPACE = 4;

    static {
        MULTIPLE_DOCUMENTS_ICON.setSize(new NSSize(32f, 32f));
        FOLDER_ICON.setSize(new NSSize(32f, 32f));
        NOT_FOUND_ICON.setSize(new NSSize(32f, 32f));
    }

    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        if(null == transfer) {
            return;
        }
        NSPoint cellPoint = cellFrame.origin();
        NSImage fileIcon = NOT_FOUND_ICON;
        if(transfer instanceof SyncTransfer) {
            fileIcon = FOLDER_ICON;
        }
        else if(transfer.getRoot().getLocal().exists()) {
            if(transfer.numberOfRoots() == 1) {
                fileIcon = transfer.getRoot().getLocal().attributes.isFile() ? NSWorkspace.sharedWorkspace().iconForFile(
                        transfer.getRoot().getLocal().getAbsolute()) : FOLDER_ICON;
                fileIcon.setSize(new NSSize(32f, 32f));
            }
            else {
                fileIcon = MULTIPLE_DOCUMENTS_ICON;
            }
        }
        fileIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE,
                cellPoint.y() + 32 + SPACE),
                NSImage.CompositeSourceOver);
    }
}
