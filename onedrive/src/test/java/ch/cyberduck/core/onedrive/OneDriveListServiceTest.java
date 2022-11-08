package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
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
        // "Drives" rather placeholders for "My Files" and "Shared".
        final AttributedList<Path> list = new OneDriveListService(session, fileid).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), f.getParent());
        }
        assertTrue(list.contains(new OneDriveHomeFinderService().find()));
    }

    @Test
    public void testListMyFiles() throws Exception {
        final AttributedList<Path> list = new OneDriveListService(session, fileid).list(OneDriveListService.MYFILES_NAME, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(OneDriveListService.MYFILES_NAME, f.getParent());
        }
    }

    @Test
    public void testListShared() throws Exception {
        final AttributedList<Path> list = new OneDriveListService(session, fileid).list(OneDriveListService.SHARED_NAME, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(OneDriveListService.SHARED_NAME, f.getParent());
        }
    }
}
