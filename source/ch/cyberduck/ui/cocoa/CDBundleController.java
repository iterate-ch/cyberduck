package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSAttributedString;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class CDBundleController extends CDController {
    private static Logger log = Logger.getLogger(CDBundleController.class);

    private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

    static {
        lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
    }

    protected static final NSDictionary TRUNCATE_MIDDLE_ATTRIBUTES = new NSDictionary(
            new Object[]{NSFont.systemFontOfSize(NSFont.smallSystemFontSize()), lineBreakByTruncatingMiddleParagraph},
            new Object[]{NSAttributedString.FontAttributeName, NSAttributedString.ParagraphStyleAttributeName});

    protected static final NSDictionary FIXED_WITH_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{NSFont.userFixedPitchFontOfSize(NSFont.smallSystemFontSize())},
            new Object[]{NSAttributedString.FontAttributeName}
    );

    protected void loadBundle() {
        final String bundleName = this.getBundleName();
        if(null == bundleName) {
            log.debug("No bundle to load for "+this.toString());
        }
        else {
            this.loadBundle(bundleName);
        }
    }

    protected void loadBundle(final String bundleName) {
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed(bundleName, this)) {
                log.fatal("Couldn't load " + bundleName + ".nib");
            }
        }
    }

    protected abstract void awakeFromNib();

    /**
     * @return The top level view object or null if unknown
     */
    protected NSView view() {
        return null;
    }

    protected abstract String getBundleName();
}
