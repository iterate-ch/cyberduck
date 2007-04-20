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

import ch.cyberduck.core.DownloadTransfer;
import ch.cyberduck.core.SyncTransfer;
import ch.cyberduck.core.Transfer;
import ch.cyberduck.core.UploadTransfer;

import com.apple.cocoa.application.NSImage;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

/**
 * @version $Id$
 */
public class CDTransferIconCell extends CDTableCell {

    private Transfer transfer;

    public CDTransferIconCell() {
        super();
    }

    public void setObjectValue(Object q) {
        this.transfer = (Transfer) q;
    }

    private static final float SPACE = 4;

    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        if(null == transfer) {
            return;
        }
        NSPoint cellPoint = cellFrame.origin();
        NSImage typeIcon = null;
        if(transfer instanceof DownloadTransfer) {
            typeIcon = ARROW_DOWN_ICON;
        }
        else if(transfer instanceof UploadTransfer) {
            typeIcon = ARROW_UP_ICON;
        }
        else if(transfer instanceof SyncTransfer) {
            typeIcon = SYNC_ICON;
        }
        assert typeIcon != null;
        typeIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE,
                cellPoint.y() + 32),
                NSImage.CompositeSourceOver);
    }

    private static final NSImage ARROW_UP_ICON = NSImage.imageNamed("arrowUp.tiff");
    private static final NSImage ARROW_DOWN_ICON = NSImage.imageNamed("arrowDown.tiff");
    private static final NSImage SYNC_ICON = NSImage.imageNamed("sync32.tiff");

    static {
        ARROW_UP_ICON.setSize(new NSSize(32f, 32f));
        ARROW_DOWN_ICON.setSize(new NSSize(32f, 32f));
        SYNC_ICON.setSize(new NSSize(32f, 32f));
    }
}
