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
import com.apple.cocoa.application.NSCell;
import com.apple.cocoa.foundation.NSPoint;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSize;

/**
 * @version $Id$
 */
public class CDTransferIconCell extends NSCell {

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
            typeIcon = CDIconCache.instance().iconForName("arrowDown", 32);
        }
        else if(transfer instanceof UploadTransfer) {
            typeIcon = CDIconCache.instance().iconForName("arrowUp", 32);
        }
        else if(transfer instanceof SyncTransfer) {
            typeIcon = CDIconCache.instance().iconForName("sync", 32);
        }
        assert typeIcon != null;
        typeIcon.compositeToPoint(new NSPoint(cellPoint.x() + SPACE,
                cellPoint.y() + 32),
                NSImage.CompositeSourceOver);
    }
}
