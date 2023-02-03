package ch.cyberduck.core;

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

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class DirectoryDelimiterPathContainerServiceTest {

    @Test
    public void testIsContainer() {
        assertFalse(new DirectoryDelimiterPathContainerService().isContainer(new Path("/", EnumSet.of(Path.Type.directory))));
        assertTrue(new DirectoryDelimiterPathContainerService().isContainer(new Path("/bucket", EnumSet.of(Path.Type.directory))));
        assertFalse(new DirectoryDelimiterPathContainerService().isContainer(new Path("/bucket", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testGetContainer() {
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)),
                new DirectoryDelimiterPathContainerService().getContainer(new Path("/", EnumSet.of(Path.Type.directory))));
        assertEquals(new Path("/bucket", EnumSet.of(Path.Type.directory)),
                new DirectoryDelimiterPathContainerService().getContainer(new Path("/bucket", EnumSet.of(Path.Type.directory))));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)),
                new DirectoryDelimiterPathContainerService().getContainer(new Path("/bucket", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testGetKey() {
        assertNull(new DirectoryDelimiterPathContainerService().getKey(new Path("/", EnumSet.of(Path.Type.directory))));
        assertNull(new DirectoryDelimiterPathContainerService().getKey(new Path("/bucket", EnumSet.of(Path.Type.directory))));
        assertEquals("bucket",
                new DirectoryDelimiterPathContainerService().getKey(new Path("/bucket", EnumSet.of(Path.Type.file))));
    }
}