package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import org.junit.Ignore;
import org.junit.Test;
import org.rococoa.cocoa.foundation.NSRect;

import static org.junit.Assert.assertEquals;

public class NSTextFieldTest {

    @Test
    @Ignore
    public void testEquals() throws Exception {
        assertEquals(new NSRect(100, 16), NSTextField.textfieldWithFrame(new NSRect(100, 16)).frame());
    }

    @Test
    public void testTextfieldWithFrame() throws Exception {
        assertEquals(100, NSTextField.textfieldWithFrame(new NSRect(100, 16)).frame().size.width.intValue());
        assertEquals(16, NSTextField.textfieldWithFrame(new NSRect(100, 16)).frame().size.height.intValue());
    }
}
