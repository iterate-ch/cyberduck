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

import com.apple.cocoa.application.NSGraphics;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSRect;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.StringUtils;

/**
 * @version $Id$
 */
public class CDBookmarkCell extends CDTableCell {

    private Host bookmark;

    public CDBookmarkCell() {
        super();
    }

    public void setObjectValue(final Object bookmark) {
        this.bookmark = (Host) bookmark;
    }

    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        super.drawInteriorWithFrameInView(cellFrame, controlView);
        if (bookmark != null) {
            NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getNickname(),
                    boldFont),
                    new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + 1,
                            cellFrame.size().width() - 5, cellFrame.size().height()));
            if(!Preferences.instance().getBoolean("browser.bookmarkDrawer.smallItems")) {
                NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getCredentials().getUsername(),
                        tinyFont),
                        new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + 14,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
                NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getProtocol()
                        + "://" + bookmark.getHostname()
                        + (StringUtils.hasText(bookmark.getDefaultPath()) ? Path.normalize(bookmark.getDefaultPath()) : ""),
                        tinyFont),
                        new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + 27,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
            }
        }
    }
}