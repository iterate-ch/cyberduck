package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveDefaultListServiceTest extends AbstractDriveTest {

    @Test
    public void testList() throws Exception {
        final AttributedList<Path> list = new DriveDefaultListService(session).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), f.getParent());
            if(!f.isVolume()) {
                assertNotNull(f.attributes().getVersionId());
            }
        }
    }

    @Test
    public void testListLexicographically() throws Exception {
        final Path directory = new DriveDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path f2 = new DriveTouchFeature(session).touch(new Path(directory, "aa", EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path f1 = new DriveTouchFeature(session).touch(new Path(directory, "a", EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new DriveDefaultListService(session).list(directory, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertEquals(f1, list.get(0));
        assertEquals(f2, list.get(1));
        new DriveDeleteFeature(session).delete(Arrays.asList(f1, f2, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFilenameColon() throws Exception {
        final Path file = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, String.format("%s:name", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final Path folder = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, String.format("%s:name", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory));
        new DriveTouchFeature(session).touch(file, new TransferStatus());
        new DriveDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        assertTrue(new DefaultFindFeature(session).find(folder));
        new DriveDeleteFeature(session).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
