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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.api.services.drive.model.File;

import static ch.cyberduck.core.googledrive.DriveHomeFinderService.MYDRIVE_FOLDER;
import static ch.cyberduck.core.googledrive.DriveHomeFinderService.SHARED_DRIVES_NAME;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveDefaultListServiceTest extends AbstractDriveTest {

    @Test
    public void testListMyDrive() throws Exception {
        final Path directory = MYDRIVE_FOLDER;
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertSame(directory, f.getParent());
            assertEquals(f.attributes(), new DriveAttributesFinderFeature(session, fileid).find(f));
            if(!f.isVolume()) {
                assertNotNull(f.attributes().getFileId());
            }
        }
    }

    @Test
    @Ignore
    public void testListSharedDrive() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(
            new Path(SHARED_DRIVES_NAME, "iterate", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertNotEquals(PathAttributes.EMPTY, new DriveAttributesFinderFeature(session, fileid).find(f));
            break;
        }
    }

    @Test
    @Ignore
    public void testListSharedDriveFolder() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
            new Path(new Path(SHARED_DRIVES_NAME, "iterate", EnumSet.of(Path.Type.directory)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(file.attributes(), new DriveAttributesFinderFeature(session, fileid).find(f));
            break;
        }
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListMissingFileidOnFolder() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path f2 = new DriveTouchFeature(session, fileid).touch(new Path(directory, "aa", EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path f1 = new DriveTouchFeature(session, fileid).touch(new Path(directory, "a", EnumSet.of(Path.Type.file)), new TransferStatus());
        fileid.cache(directory, null);
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertEquals(f1, list.get(0));
        assertEquals(f2, list.get(1));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(f1, f2, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListLexicographicallyLetters() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path f2 = new DriveTouchFeature(session, fileid).touch(new Path(directory, "aa", EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path f1 = new DriveTouchFeature(session, fileid).touch(new Path(directory, "a", EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(f1.getName().compareTo(f2.getName()) < 0);
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertEquals(f1, list.get(0));
        assertEquals(f2, list.get(1));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(f1, f2, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListLexicographicallyNumbers() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path f2 = new DriveTouchFeature(session, fileid).touch(new Path(directory, "103", EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path f1 = new DriveTouchFeature(session, fileid).touch(new Path(directory, "101", EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(f1.getName().compareTo(f2.getName()) < 0);
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertEquals(f1, list.get(0));
        assertEquals(f2, list.get(1));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(f1, f2, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFilenameSingleQuote() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path f1 = new DriveTouchFeature(session, fileid).touch(new Path(directory, String.format("%s'", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertEquals(f1, list.get(0));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(f1, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFilenameBackslash() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path f1 = new DriveTouchFeature(session, fileid).touch(new Path(directory, String.format("%s\\", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new DriveDefaultListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertEquals(f1, list.get(0));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(f1, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFilenameColon() throws Exception {
        final Path file = new Path(MYDRIVE_FOLDER, String.format("%s:name", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final Path folder = new Path(MYDRIVE_FOLDER, String.format("%s:name", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(file, new TransferStatus());
        new DriveDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        assertTrue(new DefaultFindFeature(session).find(folder));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSameFoldername() throws Exception {
        final String f1 = new AlphanumericRandomStringService().random();
        final String f2 = new AlphanumericRandomStringService().random();
        final Path parent = new Path(MYDRIVE_FOLDER, f1, EnumSet.of(Path.Type.directory));
        final Path folder = new Path(parent, f2, EnumSet.of(Path.Type.directory));
        final DriveFileIdProvider provider = new DriveFileIdProvider(session);
        new DriveDirectoryFeature(session, provider).mkdir(parent, new TransferStatus());
        new DriveDirectoryFeature(session, provider).mkdir(folder, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        assertEquals(1, new DriveDefaultListService(session, provider).list(parent, new DisabledListProgressListener()).size());
        final String fileid = provider.getFileId(folder);
        final File body = new File();
        body.set("trashed", true);
        session.getClient().files().update(fileid, body).execute();
        new DriveDirectoryFeature(session, provider).mkdir(folder, new TransferStatus());
        assertEquals(2, new DriveDefaultListService(session, provider).list(parent, new DisabledListProgressListener()).size());
        new DriveDeleteFeature(session, provider).delete(Collections.singletonList(parent), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListEmptyFolder() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final AtomicBoolean callback = new AtomicBoolean();
        assertTrue(new DriveDefaultListService(session, fileid).list(folder, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, final AttributedList<Path> list) {
                assertNotSame(AttributedList.EMPTY, list);
                callback.set(true);
            }
        }).isEmpty());
        assertTrue(callback.get());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
