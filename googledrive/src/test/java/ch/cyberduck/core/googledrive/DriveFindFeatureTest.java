package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveFindFeatureTest extends AbstractDriveTest {

    @Test
    public void testFindFileNotFound() throws Exception {
        final DriveFindFeature f = new DriveFindFeature(session, new DriveFileIdProvider(session));
        assertFalse(f.find(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindDirectory() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DriveFindFeature(session, fileid).find(folder));
        assertFalse(new DriveFindFeature(session, fileid).find(new Path(folder.getAbsolute(), EnumSet.of(Path.Type.file))));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindFile() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path file = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DriveTouchFeature(session, fileid).touch(file, new TransferStatus());
        assertTrue(new DriveFindFeature(session, fileid).find(file));
        assertFalse(new DriveFindFeature(session, fileid).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFind() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new DriveTouchFeature(session, fileid).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String id = file.attributes().getFileId();
        assertTrue(new DriveFindFeature(session, fileid).find(file));
        new DriveTrashFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(file));
        assertFalse(new DriveFindFeature(session, fileid).find(file));
        // When searching with version "2", find trashed file
        final Path trashed = new DriveListService(session, fileid).list(folder, new DisabledListProgressListener()).find(new SimplePathPredicate(file));
        assertNotNull(trashed);
        assertEquals(id, trashed.attributes().getFileId());
        assertTrue(new DefaultFindFeature(session).find(trashed));
        assertTrue(new DriveFindFeature(session, fileid).find(trashed));
        // Recreate file
        final Path version2 = new DriveTouchFeature(session, fileid).touch(file, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(version2));
        assertTrue(new DriveFindFeature(session, fileid).find(version2));
        assertEquals(version2.attributes(), new DriveAttributesFinderFeature(session, fileid).find(version2));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(version2, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
