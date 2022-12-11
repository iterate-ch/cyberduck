package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import com.google.api.services.drive.model.File;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveAttributesFinderFeatureTest extends AbstractDriveTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, new DriveFileIdProvider(session));
        f.find(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testFindRoot() throws Exception {
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, new DriveFileIdProvider(session));
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory))));
    }

    @Test
    public void testFindMyDrive() throws Exception {
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, new DriveFileIdProvider(session));
        assertEquals(PathAttributes.EMPTY, f.find(DriveHomeFinderService.MYDRIVE_FOLDER));
    }

    @Test
    @Ignore
    public void testFindSharedDriveAsDefaultPath() throws Exception {
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, new DriveFileIdProvider(session));
        assertNotEquals(PathAttributes.EMPTY, f.find(new Path(DriveHomeFinderService.SHARED_DRIVES_NAME, "iterate", EnumSet.of(Path.Type.directory))));
    }

    @Test
    @Ignore
    public void testFindFolderInSharedDriveAsDefaultPath() throws Exception {
        final Path test = new Path(new Path(DriveHomeFinderService.SHARED_DRIVES_NAME, "iterate", EnumSet.of(Path.Type.directory)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, new DriveFileIdProvider(session));
        assertNotEquals(PathAttributes.EMPTY, f.find(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFind() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotNull(attributes.getFileId());
        assertNull(attributes.getVersionId());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDuplicatesWithSameName() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path version1 = new DriveTouchFeature(session, fileid).touch(
                new Path(folder, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, fileid);
        assertEquals(version1.attributes(), f.find(version1));
        final Path version2 = new DriveTouchFeature(session, fileid).touch(
                new Path(folder, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotEquals(f.find(version1), f.find(version2));
        final AttributedList<Path> listBeforeDelete = new DriveListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertTrue(listBeforeDelete.contains(version1));
        assertFalse(listBeforeDelete.find(new DefaultPathPredicate(version1)).attributes().isHidden());
        assertTrue(listBeforeDelete.contains(version2));
        new DriveTrashFeature(session, fileid).delete(Collections.singletonList(new Path(version1)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final AttributedList<Path> listAfterDelete = new DriveListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertTrue(listAfterDelete.contains(version1));
        assertTrue(listAfterDelete.find(new DefaultPathPredicate(version1)).attributes().isHidden());
        assertTrue(listAfterDelete.contains(version2));
        assertFalse(listAfterDelete.find(new DefaultPathPredicate(version2)).attributes().isHidden());
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(version1, version2, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path file = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveDirectoryFeature(session, fileid).mkdir(file, new TransferStatus());
        final PathAttributes attributes = new DriveAttributesFinderFeature(session, fileid).find(file);
        assertNotNull(attributes);
        assertEquals(-1L, attributes.getSize());
        assertNotEquals(-1L, attributes.getCreationDate());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getFileId());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMissingShortcutTarget() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(test);
        final File shortcut = session.getClient().files().create(new File()
            .setName(new AlphanumericRandomStringService().random())
            .setMimeType("application/vnd.google-apps.shortcut")
            .setShortcutDetails(new File.ShortcutDetails()
                .setTargetMimeType("text/plain")
                .setTargetId(fileid.getFileId(test))
            )
        ).execute();
        assertEquals(attributes, f.find(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, shortcut.getName(), EnumSet.of(Path.Type.file))));
        session.getClient().files().delete(fileid.getFileId(test))
            .setSupportsAllDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
        try {
            f.find(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, shortcut.getName(), EnumSet.of(Path.Type.file)));
        }
        catch(NotfoundException e) {
            // Expected. Can no longer resolve shortcut
        }
        final AttributedList<Path> list = new DriveListService(session, fileid).list(DriveHomeFinderService.MYDRIVE_FOLDER, new DisabledListProgressListener());
        assertFalse(list.contains(test));
        assertNull(list.find(new SimplePathPredicate(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, shortcut.getName(), EnumSet.of(Path.Type.file)))));
        session.getClient().files().delete(shortcut.getId())
            .setSupportsAllDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
    }

    @Test
    public void testChangedFileId() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path room = new DriveDirectoryFeature(session, fileid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String latestnodeid = test.attributes().getFileId();
        assertNotNull(latestnodeid);
        // Assume previously seen but changed on server
        fileid.cache(test, String.valueOf(RandomUtils.nextLong()));
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, fileid);
        assertEquals(latestnodeid, f.find(new Path(test).withAttributes(PathAttributes.EMPTY)).getFileId());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
