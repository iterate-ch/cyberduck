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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphMoveFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphMoveFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testRenameNotFound() throws Exception {
        final Touch touch = new GraphTouchFeature(session, fileid);
        final Delete delete = new GraphDeleteFeature(session, fileid);
        final Path drive = new OneDriveHomeFinderService().find();
        final Path file = touch.touch(new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withMime("x-application/cyberduck"));
        final String fileid = file.attributes().getFileId();
        delete.delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final Path target = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertThrows(NotfoundException.class, () -> new GraphMoveFeature(session, this.fileid).move(file, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        file.attributes().withFileId(fileid);
        assertThrows(NotfoundException.class, () -> new GraphMoveFeature(session, this.fileid).move(file, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
    }

    @Test
    public void testRename() throws BackgroundException {
        final Touch touch = new GraphTouchFeature(session, fileid);
        final Move move = new GraphMoveFeature(session, fileid);
        final Delete delete = new GraphDeleteFeature(session, fileid);
        final AttributesFinder attributesFinder = new GraphAttributesFinderFeature(session, fileid);
        final Path drive = new OneDriveHomeFinderService().find();
        final Path file = touch.touch(new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withMime("x-application/cyberduck"));
        final PathAttributes attributes = attributesFinder.find(file);
        assertNotNull(attributes);
        assertEquals(file.attributes().getFileId(), attributes.getFileId());
        Path rename = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(file, Optional.of(rename)));
        final TransferStatus status = new TransferStatus();
        final Path target = move.move(file, rename, status, new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(attributes, target.attributes());
        assertEquals(attributes.getFileId(), target.attributes().getFileId());
        assertNotEquals(attributes.getETag(), attributesFinder.find(rename).getETag());
        assertEquals(target.attributes().getETag(), attributesFinder.find(rename).getETag());
        delete.delete(Collections.singletonList(rename), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMove() throws BackgroundException {
        final Directory directory = new GraphDirectoryFeature(session, fileid);
        final Touch touch = new GraphTouchFeature(session, fileid);
        final Move move = new GraphMoveFeature(session, fileid);
        final Delete delete = new GraphDeleteFeature(session, fileid);
        final AttributesFinder attributesFinder = new GraphAttributesFinderFeature(session, fileid);
        final Path drive = new OneDriveHomeFinderService().find();
        Path targetDirectory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(targetDirectory, new TransferStatus());
        assertNotNull(attributesFinder.find(targetDirectory));

        Path touchedFile = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(touchedFile, new TransferStatus().withMime("x-application/cyberduck"));
        final PathAttributes attributes = attributesFinder.find(touchedFile);

        Path rename = new Path(targetDirectory, touchedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(touchedFile, Optional.of(rename)));
        final Path target = move.move(touchedFile, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        final PathAttributes renamedAttributes = attributesFinder.find(rename);
        assertNotNull(renamedAttributes);
        assertEquals(attributes, renamedAttributes);
        assertNotEquals(attributes.getETag(), renamedAttributes.getETag());
        assertEquals(target.attributes().getETag(), renamedAttributes.getETag());

        delete.delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToRoot() throws BackgroundException {
        final Directory directory = new GraphDirectoryFeature(session, fileid);
        final Touch touch = new GraphTouchFeature(session, fileid);
        final Move move = new GraphMoveFeature(session, fileid);
        final Delete delete = new GraphDeleteFeature(session, fileid);
        final AttributesFinder attributesFinder = new GraphAttributesFinderFeature(session, fileid);
        final Path drive = new OneDriveHomeFinderService().find();
        Path targetDirectory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(targetDirectory, new TransferStatus());
        assertNotNull(attributesFinder.find(targetDirectory));

        Path touchedFile = new Path(targetDirectory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(touchedFile, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(touchedFile));

        Path rename = new Path(drive, touchedFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(touchedFile, Optional.of(rename)));
        move.move(touchedFile, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertNotNull(attributesFinder.find(rename));

        delete.delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        delete.delete(Collections.singletonList(rename), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveRename() throws BackgroundException {
        final Directory directory = new GraphDirectoryFeature(session, fileid);
        final Touch touch = new GraphTouchFeature(session, fileid);
        final Move move = new GraphMoveFeature(session, fileid);
        final Delete delete = new GraphDeleteFeature(session, fileid);
        final AttributesFinder attributesFinder = new GraphAttributesFinderFeature(session, fileid);
        final Path drive = new OneDriveHomeFinderService().find();
        Path targetDirectory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(targetDirectory, new TransferStatus());
        assertNotNull(attributesFinder.find(targetDirectory));

        Path touchedFile = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(touchedFile, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(touchedFile));

        Path rename = new Path(targetDirectory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(move.isSupported(touchedFile, Optional.of(rename)));
        move.move(touchedFile, rename, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertNotNull(attributesFinder.find(rename));

        delete.delete(Collections.singletonList(targetDirectory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToExistingFile() throws Exception {
        final Path folder = new GraphDirectoryFeature(session, fileid).mkdir(new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new GraphTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path temp = new GraphTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new GraphMoveFeature(session, fileid).move(temp, test, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new GraphItemListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertEquals(1, files.size());
        assertFalse(find.find(temp));
        assertTrue(find.find(test));
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testRenameCaseOnly() throws Exception {
        final Path home = new OneDriveHomeFinderService().find();
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new GraphTouchFeature(session, fileid).touch(new Path(home, StringUtils.capitalize(name), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path rename = new Path(home, StringUtils.lowerCase(name), EnumSet.of(Path.Type.file));
        new GraphMoveFeature(session, fileid).move(file, rename, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(rename), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
