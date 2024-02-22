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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveMoveFeatureTest extends AbstractDriveTest {

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path workdir = new DefaultHomeFinderService(session).find();
        final Path test = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DriveMoveFeature(session, fileid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }

    @Test
    public void testMoveFile() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String id = test.attributes().getFileId();
        final Path folder = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DriveDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        final Path target = new DriveMoveFeature(session, fileid).move(test, new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(id, target.attributes().getFileId());
        final Find find = new DefaultFindFeature(session);
        assertFalse(find.find(test));
        assertTrue(find.find(target));
        final PathAttributes targetAttr = new DriveAttributesFinderFeature(session, fileid).find(target);
        assertEquals(Comparison.equal, session.getHost().getProtocol().getFeature(ComparisonService.class).compare(Path.Type.file, target.attributes(), targetAttr));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(target, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToExistingFile() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String firstVersion = test.attributes().getFileId();
        final Path temp = new DriveTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new DriveMoveFeature(session, fileid).move(temp, test, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(test.attributes().getFileId(), target.attributes().getFileId());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new DriveListService(session, fileid).list(folder, new DisabledListProgressListener());
        // Replaced file is trashed
        assertEquals(2, files.size());
        assertTrue(files.get(new Path(test).withAttributes(new PathAttributes().withFileId(firstVersion))).attributes().isHidden());
        assertFalse(files.get(target).attributes().isHidden());
        assertTrue(find.find(target));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final Path sourceDirectory = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetDirectory = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveDirectoryFeature(session, fileid).mkdir(sourceDirectory, new TransferStatus());
        final Path sourceFile = new DriveTouchFeature(session, fileid).touch(new Path(sourceDirectory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path targetFile = new Path(targetDirectory, sourceFile.getName(), EnumSet.of(Path.Type.file));
        new DriveMoveFeature(session, fileid).move(sourceDirectory, targetDirectory, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        final Find find = new DefaultFindFeature(session);
        assertFalse(find.find(sourceDirectory));
        assertTrue(find.find(targetDirectory));
        assertTrue(find.find(targetFile));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(targetFile, targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
