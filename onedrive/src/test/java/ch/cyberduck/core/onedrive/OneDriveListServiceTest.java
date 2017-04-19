package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

@Category(IntegrationTest.class)
public class OneDriveListServiceTest extends AbstractOneDriveTest {

    @Test
    public void testListDrives() throws Exception {
        final AttributedList<Path> list = new OneDriveListService(session).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), f.getParent());
        }
        assertTrue(list.contains(new OneDriveHomeFinderFeature(session).find()));
    }

    @Test
    public void testListDriveChildren() throws Exception {
        ListService listService = new OneDriveListService(session);
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final AttributedList<Path> list = listService.list(drive, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(drive.getName(), f.getParent().getName());
            final PathAttributes attributes = f.attributes();
            assertNotEquals(-1L, attributes.getSize());
            assertNotEquals(-1L, attributes.getCreationDate());
            assertNotEquals(-1L, attributes.getModificationDate());
            assertNotNull(attributes.getETag());
            assertNull(attributes.getVersionId());
            assertNotNull(attributes.getLink());
        }
    }
}
