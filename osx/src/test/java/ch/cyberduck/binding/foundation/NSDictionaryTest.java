package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSFont;

import org.junit.Test;

import static ch.cyberduck.binding.BundleController.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE;
import static org.junit.Assert.assertEquals;

public class NSDictionaryTest {

    @Test
    public void dictionaryWithObjectsForKeys() {
        final NSDictionary dict = NSDictionary.dictionaryWithObjectsForKeys(
                NSArray.arrayWithObjects(NSFont.menuFontOfSize(NSFont.smallSystemFontSize()),
                        PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE,
                        NSColor.secondaryLabelColor()),
                NSArray.arrayWithObjects(NSAttributedString.FontAttributeName,
                        NSAttributedString.ParagraphStyleAttributeName,
                        NSAttributedString.ForegroundColorAttributeName)
        );
        assertEquals(3, dict.allKeys().count().intValue());
        assertEquals(3, dict.allValues().count().intValue());
    }
}