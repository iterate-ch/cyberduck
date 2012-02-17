package ch.cyberduck.core;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatusTest extends AbstractTestCase {

    @Test
    public void testGetSizeAsString() throws Exception {
        assertEquals("1.0 KB", Status.getSizeAsString(1024));
        assertEquals("1.5 KB", Status.getSizeAsString(1500));
        assertEquals("2.0 KB", Status.getSizeAsString(2000));
        assertEquals("1.0 MB", Status.getSizeAsString(1048576));
        assertEquals("1.0 GB", Status.getSizeAsString(1073741824));
        assertEquals("375.3 MB", Status.getSizeAsString(393495974));
    }

    @Test
    public void testSetResume() throws Exception {
        Status status = new Status();
        status.setCurrent(1024);
        status.setResume(true);
        assertEquals(1024, status.getCurrent());
        status.setResume(false);
        assertEquals(0, status.getCurrent());
    }
}