package ch.cyberduck.core;

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

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class PathContainerServiceTest extends AbstractTestCase {

    @Test
    public void testIsContainer() throws Exception {
        final PathContainerService s = new PathContainerService();
        assertFalse(s.isContainer(new Path("/", Path.VOLUME_TYPE)));
        assertTrue(s.isContainer(new Path("/t", Path.VOLUME_TYPE)));
        assertTrue(s.isContainer(new Path("/t/", Path.VOLUME_TYPE)));
        assertFalse(s.isContainer(new Path("/t/a", Path.VOLUME_TYPE)));
    }

    @Test
    public void testGetContainerName() throws Exception {
        final PathContainerService s = new PathContainerService();
        assertEquals("t", s.getContainer(new Path("/t", Path.DIRECTORY_TYPE)).getName());
        assertEquals("t", s.getContainer(new Path("/t/a", Path.FILE_TYPE)).getName());
    }

    @Test
    public void testGetContainer() throws Exception {
        final PathContainerService s = new PathContainerService();
        assertEquals("/t", s.getContainer(new Path("/t", Path.DIRECTORY_TYPE)).getAbsolute());
        assertNull(s.getContainer(new Path("/", Path.DIRECTORY_TYPE)));
    }

    @Test
    public void testGetKey() throws Exception {
        assertEquals("d/f", new PathContainerService().getKey(new Path("/c/d/f", Path.DIRECTORY_TYPE)));
        assertNull(new PathContainerService().getKey(new Path("/", Path.DIRECTORY_TYPE)));
    }
}
