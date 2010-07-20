package ch.cyberduck.ui.cocoa.util;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import ch.cyberduck.ui.cocoa.application.NSColor;
import ch.cyberduck.ui.cocoa.application.NSFont;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSMutableAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSRange;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;

import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * From http://developer.apple.com/qa/qa2006/qa1487.html
 *
 * @version $Id$
 */
public class HyperlinkAttributedStringFactory {

    /**
     *
     * @param value
     * @param hyperlink
     * @return
     */
    public static NSAttributedString create(final NSMutableAttributedString value, final String hyperlink) {
        final NSRange range = NSRange.NSMakeRange(new NSUInteger(0), value.length());
        value.beginEditing();
        value.addAttributeInRange(NSMutableAttributedString.LinkAttributeName,
                hyperlink, range);
        // make the text appear in blue
        value.addAttributeInRange(NSMutableAttributedString.ForegroundColorAttributeName,
                NSColor.blueColor(), range);
        // system font
        value.addAttributeInRange(NSMutableAttributedString.FontAttributeName,
                NSFont.systemFontOfSize(NSFont.smallSystemFontSize()), range);
        // next make the text appear with an underline
        value.addAttributeInRange(NSMutableAttributedString.UnderlineStyleAttributeName,
                NSNumber.numberWithInt(NSMutableAttributedString.SingleUnderlineStyle), range);
        value.endEditing();
        return value;
    }
}
