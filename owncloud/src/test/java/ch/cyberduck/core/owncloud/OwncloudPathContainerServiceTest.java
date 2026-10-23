package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class OwncloudPathContainerServiceTest {

    private final OwncloudPathContainerService container = new OwncloudPathContainerService();
    private final Path space = new Path(OwncloudPathMapper.SPACES_FOLDER, "iterate", EnumSet.of(Path.Type.directory));
    private final Path share = new Path(OwncloudPathMapper.SHARES_FOLDER, "s", EnumSet.of(Path.Type.directory));

    @Test
    public void testIsVirtual() {
        assertTrue(container.isVirtual(new Path("/", EnumSet.of(Path.Type.directory))));
        assertTrue(container.isVirtual(OwncloudPathMapper.SPACES_FOLDER));
        assertTrue(container.isVirtual(OwncloudPathMapper.SHARES_FOLDER));
        assertFalse(container.isVirtual(OwncloudPathMapper.PERSONAL_FOLDER));
        assertFalse(container.isVirtual(new Path(OwncloudPathMapper.SHARES_FOLDER, "s", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testIsContainer() {
        assertTrue(container.isContainer(OwncloudPathMapper.PERSONAL_FOLDER));
        assertTrue(container.isContainer(space));
        assertTrue(container.isContainer(share));
        assertFalse(container.isContainer(new Path(share, "a", EnumSet.of(Path.Type.file))));
        assertFalse(container.isContainer(new Path(OwncloudPathMapper.PERSONAL_FOLDER, "a", EnumSet.of(Path.Type.directory))));
        // Virtual folders
        assertFalse(container.isContainer(new Path("/", EnumSet.of(Path.Type.directory))));
        assertFalse(container.isContainer(OwncloudPathMapper.SPACES_FOLDER));
        assertFalse(container.isContainer(OwncloudPathMapper.SHARES_FOLDER));
    }

    @Test
    public void testGetContainer() {
        assertEquals(OwncloudPathMapper.PERSONAL_FOLDER.getAbsolute(),
                container.getContainer(new Path(OwncloudPathMapper.PERSONAL_FOLDER, "a/b", EnumSet.of(Path.Type.file))).getAbsolute());
        assertEquals(space.getAbsolute(), container.getContainer(new Path(space, "a/b", EnumSet.of(Path.Type.file))).getAbsolute());
        assertEquals(share.getAbsolute(), container.getContainer(new Path(share, "a", EnumSet.of(Path.Type.file))).getAbsolute());
        // Virtual folders
        assertTrue(container.getContainer(OwncloudPathMapper.SPACES_FOLDER).isRoot());
        assertTrue(container.getContainer(new Path("/", EnumSet.of(Path.Type.directory))).isRoot());
    }

    @Test
    public void testGetKey() {
        assertEquals("a/b", container.getKey(new Path(space, "a/b", EnumSet.of(Path.Type.file))));
        assertNull(container.getKey(space));
    }
}
