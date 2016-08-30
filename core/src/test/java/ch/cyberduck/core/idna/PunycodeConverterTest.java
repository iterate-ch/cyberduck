package ch.cyberduck.core.idna;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PunycodeConverterTest {

    @Test
    public void testConvert() throws Exception {
        assertEquals("host.localdomain", new PunycodeConverter().convert("host.localdomain"));
        assertEquals(null, new PunycodeConverter().convert(null));
        assertEquals("", new PunycodeConverter().convert(""));
        assertEquals("xn--4ca", new PunycodeConverter().convert("ä"));
    }

    @Test
    public void testConvertWhitespace() throws Exception {
        assertEquals("host.localdomain", new PunycodeConverter().convert("host.localdomain "));
    }

    @Test
    public void testHostnameStartsWithDot() throws Exception {
        assertEquals(".blob.core.windows.net", new PunycodeConverter().convert(".blob.core.windows.net"));
    }
}
