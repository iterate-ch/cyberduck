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

import com.apple.cocoa.application.NSBezierPath;
import com.apple.cocoa.application.NSColor;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSRect;

/**
 * @version $Id$
 */
public class CDErrorView extends NSView {

    public CDErrorView() {
        super();
    }

    public CDErrorView(NSRect rect) {
        super(rect);
    }

    public void drawRect(NSRect rect) {
        super.drawRect(rect);
        rect = this.bounds();
        NSColor.alternateSelectedControlColor().set();
        NSBezierPath.fillRect(rect);
    }

    /**
     *
     * @return True if the the whole rect of this view is painted by ourself
     */
    public boolean isOpaque() {
        // If the background color is opaque, return YES otherwise, return NO
        return NSColor.alternateSelectedControlColor().alphaComponent() >= 1.0;
    }
}
