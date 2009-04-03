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

    private static final NSDictionary SMALL_BOLD_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(NSFont.smallSystemFontSize()),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    private static final NSDictionary HIGHLIGHTED_SMALL_BOLD_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.whiteColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    private static final NSDictionary LARGE_BOLD_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(NSFont.systemFontSize()),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL}, //objects
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName} //keys
    );

    private static final NSDictionary HIGHLIGHTED_LARGE_BOLD_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.boldSystemFontOfSize(NSFont.systemFontSize()),
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

    public static final int SMALL_BOOKMARK_SIZE = 16;
    public static final int MEDIUM_BOOKMARK_SIZE = 32;
    public static final int LARGE_BOOKMARK_SIZE = 64;

    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        if(bookmark != null) {
            boolean highlighted = this.isHighlighted()
                    && !this.highlightColorWithFrameInView(cellFrame, controlView).equals(
                    NSColor.secondarySelectedControlColor()
            );

            final int size = Preferences.instance().getInteger("bookmark.icon.size");

            NSDictionary nicknameFont;
            NSDictionary detailsFont;

            detailsFont = highlighted ? HIGHLIGHTED_SMALL_FONT_ATTRIBUTES : SMALL_FONT_ATTRIBUTES;

            if(CDBookmarkCell.LARGE_BOOKMARK_SIZE == size) {
                nicknameFont = highlighted ? HIGHLIGHTED_LARGE_BOLD_FONT_ATTRIBUTES : LARGE_BOLD_FONT_ATTRIBUTES;
            }
            else {
                nicknameFont = highlighted ? HIGHLIGHTED_SMALL_BOLD_FONT_ATTRIBUTES : SMALL_BOLD_FONT_ATTRIBUTES;
            }

            final NSLayoutManager l = new NSLayoutManager();
            float nicknameFontHeight = l.defaultLineHeightForFont(
                    (NSFont) nicknameFont.objectForKey(NSAttributedString.FontAttributeName)) + 2;
            float detailsFontHeight = l.defaultLineHeightForFont(
                    (NSFont) detailsFont.objectForKey(NSAttributedString.FontAttributeName)) + 2;

            if(StringUtils.isNotBlank(bookmark.getNickname())) {
                NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getNickname(),
                        nicknameFont),
                        new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + 1,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
            }
            if(CDBookmarkCell.SMALL_BOOKMARK_SIZE == size) {
                return;
            }
            if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
                NSGraphics.drawAttributedString(new NSAttributedString(bookmark.getCredentials().getUsername(),
                        detailsFont),
                        new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + nicknameFontHeight,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
            }
            NSGraphics.drawAttributedString(new NSAttributedString(bookmark.toURL()
                    + (StringUtils.isNotBlank(bookmark.getDefaultPath()) ? Path.normalize(bookmark.getDefaultPath()) : ""),
                    detailsFont),
                    new NSRect(cellFrame.origin().x(), cellFrame.origin().y() + nicknameFontHeight + detailsFontHeight,
                            cellFrame.size().width() - 5, cellFrame.size().height()));
        }
    }
}