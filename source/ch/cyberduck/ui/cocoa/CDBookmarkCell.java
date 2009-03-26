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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSRect;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public class CDBookmarkCell extends NSCell {

    private Host bookmark;

    public void setObjectValue(final Object bookmark) {
        this.bookmark = (Host) bookmark;
    }

    private static final int NSCellHitContentArea = 1;

    public int hitTestForEvent(NSEvent event, NSRect cellFrame, NSView controlView) {
        return NSCellHitContentArea;
    }

    private static final NSDictionary BOLD_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(NSFont.smallSystemFontSize()),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    private static final NSDictionary HIGHLIGHTED_BOLD_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.whiteColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    private static final NSDictionary SMALL_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(NSFont.labelFontSize()),
                    NSColor.darkGrayColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    private static final NSDictionary HIGHLIGHTED_SMALL_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(NSFont.labelFontSize()),
                    NSColor.whiteColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        if(bookmark != null) {
            boolean highlighted = this.isHighlighted()
                    && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(
                    NSColor.secondarySelectedControlColor()
            );

            NSDictionary boldFont;
            NSDictionary tinyFont;
            if(highlighted) { // cell is selected (white font)
                boldFont = HIGHLIGHTED_BOLD_FONT_ATTRIBUTES;
                tinyFont = HIGHLIGHTED_SMALL_FONT_ATTRIBUTES;
            }
            else { // cell is not selected (black font)
                boldFont = BOLD_FONT_ATTRIBUTES;
                tinyFont = SMALL_FONT_ATTRIBUTES;
            }
            if(StringUtils.isNotBlank(bookmark.getNickname())) {
                NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getNickname(),
                        boldFont),
                        new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + 1,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
            }
            if(!Preferences.instance().getBoolean("browser.bookmarkDrawer.smallItems")) {
                if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
                    NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getCredentials().getUsername(),
                            tinyFont),
                            new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + 14,
                                    cellFrame.size().width() - 5, cellFrame.size().height()));
                }
                NSGraphics.drawAttributedString(new NSAttributedString(bookmark.toURL()
                        + (StringUtils.isNotBlank(bookmark.getDefaultPath()) ? Path.normalize(bookmark.getDefaultPath()) : ""),
                        tinyFont),
                        new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + 27,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
            }
        }
    }
}