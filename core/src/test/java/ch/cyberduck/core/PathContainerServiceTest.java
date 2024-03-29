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

import java.util.EnumSet;

import static org.junit.Assert.*;

public class PathContainerServiceTest {

    @Test
    public void testIsContainer() {
        final DefaultPathContainerService s = new DefaultPathContainerService();
        assertFalse(s.isContainer(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(s.isContainer(new Path("/t", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(s.isContainer(new Path(PathNormalizer.normalize("/t/"), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertFalse(s.isContainer(new Path("/t/a", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetContainerName() {
        final DefaultPathContainerService s = new DefaultPathContainerService();
        assertEquals("t", s.getContainer(new Path("/t", EnumSet.of(Path.Type.directory))).getName());
        assertEquals("t", s.getContainer(new Path("/t/a", EnumSet.of(Path.Type.file))).getName());
    }

    @Test
    public void testGetContainer() {
        final DefaultPathContainerService s = new DefaultPathContainerService();
        assertEquals("/t", s.getContainer(new Path("/t", EnumSet.of(Path.Type.directory))).getAbsolute());
        final Path root = new Path("/", EnumSet.of(Path.Type.directory));
        assertSame(root, s.getContainer(root));
    }

    @Test
    public void testGetKey() {
        assertEquals("d/f", new DefaultPathContainerService().getKey(new Path("/c/d/f", EnumSet.of(Path.Type.directory))));
        assertNull(new DefaultPathContainerService().getKey(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
