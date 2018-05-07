package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.onedrive.features.OneDriveAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.OneDriveDeleteFeature;
import ch.cyberduck.core.onedrive.features.OneDriveDirectoryFeature;
import ch.cyberduck.core.onedrive.features.OneDriveFileIdProvider;
import ch.cyberduck.core.onedrive.features.OneDriveHomeFinderFeature;
import ch.cyberduck.core.onedrive.features.OneDriveMoveFeature;
import ch.cyberduck.core.onedrive.features.OneDriveTouchFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveMoveFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testRename() throws BackgroundException {
        final Touch touch = new OneDriveTouchFeature(session);
        final Move move = new OneDriveMoveFeature(session);
        final Delete delete = new OneDriveDeleteFeature(session);
        final AttributesFinder attributesFinder = new OneDriveAttributesFinderFeature(session);
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final Path file = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(file));
        Path rename = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(file, rename));
        move.move(file, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertNotNull(attributesFinder.find(rename));
        delete.delete(Collections.singletonList(rename), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMove() throws BackgroundException {
        final Directory directory = new OneDriveDirectoryFeature(session);
        final Touch touch = new OneDriveTouchFeature(session);
        final Move move = new OneDriveMoveFeature(session);
        final Delete delete = new OneDriveDeleteFeature(session);
        final AttributesFinder attributesFinder = new OneDriveAttributesFinderFeature(session);
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        Path targetDirectory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(targetDirectory, null, null);
        assertNotNull(attributesFinder.find(targetDirectory));

        Path touchedFile = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(touchedFile, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(touchedFile));

        Path rename = new Path(targetDirectory, touchedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(touchedFile, rename));
        move.move(touchedFile, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertNotNull(attributesFinder.find(rename));

        delete.delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveRename() throws BackgroundException {
        final Directory directory = new OneDriveDirectoryFeature(session);
        final Touch touch = new OneDriveTouchFeature(session);
        final Move move = new OneDriveMoveFeature(session);
        final Delete delete = new OneDriveDeleteFeature(session);
        final AttributesFinder attributesFinder = new OneDriveAttributesFinderFeature(session);
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        Path targetDirectory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(targetDirectory, null, null);
        assertNotNull(attributesFinder.find(targetDirectory));

        Path touchedFile = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(touchedFile, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(touchedFile));

        Path rename = new Path(targetDirectory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(touchedFile, rename));
        move.move(touchedFile, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertNotNull(attributesFinder.find(rename));

        delete.delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToExistingFile() throws Exception {
        final Path folder = new Path(new OneDriveHomeFinderFeature(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new OneDriveDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new OneDriveTouchFeature(session).touch(test, new TransferStatus());
        final Path temp = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new OneDriveTouchFeature(session).touch(temp, new TransferStatus());
        new OneDriveMoveFeature(session).move(temp, test, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new OneDriveListService(session, new OneDriveFileIdProvider(session)).list(folder, new DisabledListProgressListener());
        assertEquals(1, files.size());
        assertFalse(find.find(temp));
        assertTrue(find.find(test));
        new OneDriveDeleteFeature(session).delete(Arrays.asList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
