package ch.cyberduck.ui.cocoa;

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

import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.preferences.PreferencesFactory;

public final class TableCellAttributes {

    private TableCellAttributes() {
        //
    }

    public static final NSDictionary BROWSER_FONT_ATTRIBUTES_LEFT_ALIGNMENT = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.systemFontOfSize(PreferencesFactory.get().getFloat("browser.font.size")), BundleController.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ParagraphStyleAttributeName)
    );

    public static NSDictionary browserFontLeftAlignment() {
        return BROWSER_FONT_ATTRIBUTES_LEFT_ALIGNMENT;
    }

    public static final NSDictionary BROWSER_FONT_ATTRIBUTES_RIGHT_ALIGNMENT = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.systemFontOfSize(PreferencesFactory.get().getFloat("browser.font.size")), BundleController.PARAGRAPH_STYLE_RIGHT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ParagraphStyleAttributeName)
    );

    public static NSDictionary browserFontRightAlignment() {
        return BROWSER_FONT_ATTRIBUTES_RIGHT_ALIGNMENT;
    }
}
