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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DriveBatchDeleteFeatureTest extends AbstractDriveTest {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DriveBatchDeleteFeature(session, new DriveFileIdProvider(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteFolder() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER,
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        assertTrue(new DriveFindFeature(session, fileid).find(directory, new DisabledListProgressListener()));
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DriveFindFeature(session, fileid).find(file, new DisabledListProgressListener()));
        new DriveBatchDeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new DriveFindFeature(session, fileid).find(directory, new DisabledListProgressListener())));
        assertFalse((new DriveFindFeature(session, fileid).find(file, new DisabledListProgressListener())));
    }

    @Test
    public void testDeleteMultipleFiles() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file1 = new DriveTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path file2 = new DriveTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DriveFindFeature(session, fileid).find(file1));
        assertTrue(new DriveFindFeature(session, fileid).find(file2));
        new DriveBatchDeleteFeature(session, fileid).delete(Arrays.asList(file1, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new DriveFindFeature(session, fileid).find(file1, new DisabledListProgressListener())));
        assertFalse((new DriveFindFeature(session, fileid).find(file2, new DisabledListProgressListener())));
        assertTrue(new DriveFindFeature(session, fileid).find(folder, new DisabledListProgressListener()));
        new DriveBatchDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new DriveFindFeature(session, fileid).find(folder, new DisabledListProgressListener())));
    }
}
