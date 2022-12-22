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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
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
public class DriveFileIdProviderTest extends AbstractDriveTest {

    @Test
    public void testGetFileidRoot() throws Exception {
        assertEquals("root", new DriveFileIdProvider(new DriveSession(new Host(new DriveProtocol(), ""), new DisabledX509TrustManager(), new DefaultX509KeyManager()))
            .getFileId(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertEquals("root", new DriveFileIdProvider(new DriveSession(new Host(new DriveProtocol(), ""), new DisabledX509TrustManager(), new DefaultX509KeyManager()))
            .getFileId(new Path("/My Drive", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testGetFileid() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        assertNotNull(fileid.getFileId(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testGetFileidAccentCharacter() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, String.format("%sà", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        assertNotNull(fileid.getFileId(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testGetFileidSingleQuoteCharacter() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, String.format("%s'", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider driveFileIdProvider = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, driveFileIdProvider).touch(test, new TransferStatus());
        assertNotNull(driveFileIdProvider.getFileId(test));
        new DriveDeleteFeature(session, driveFileIdProvider).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testGetFileidBackslashCharacter() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, String.format("%s\\", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        assertNotNull(fileid.getFileId(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testGetFileidDoubleBackslashCharacter() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, String.format("%s\\\\", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        assertNotNull(fileid.getFileId(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testGetFileidSameName() throws Exception {
        final String filename = new AlphanumericRandomStringService().random();
        final Path test1 = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, filename, EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path p1 = new DriveTouchFeature(session, fileid).touch(test1, new TransferStatus());
        assertEquals(p1.attributes().getFileId(), fileid.getFileId(test1));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test1), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        final Path test2 = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, filename, EnumSet.of(Path.Type.file));
        final Path p2 = new DriveTouchFeature(session, fileid).touch(test2, new TransferStatus());
        assertEquals(p2.attributes().getFileId(), fileid.getFileId(test2));
        session.getClient().files().delete(p2.attributes().getFileId());
    }

    @Test
    public void testFileIdCollision() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
            new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new TransferStatus());

        final Path path2R = new Path(directory, "2R", EnumSet.of(Path.Type.directory));
        final Path path33 = new Path(directory, "33", EnumSet.of(Path.Type.directory));

        final Directory directoryFeature = new DriveDirectoryFeature(session, fileid);
        final Path path2RWithId = directoryFeature.mkdir(path2R, new TransferStatus());
        assertNotNull(path2RWithId.attributes().getFileId());
        final Path path33WithId = directoryFeature.mkdir(path33, new TransferStatus());
        assertNotNull(path33WithId.attributes().getFileId());
        assertNotEquals(path2RWithId.attributes().getFileId(), path33WithId.attributes().getFileId());

        final String fileId = fileid.getFileId(path33);

        assertEquals(fileId, path33WithId.attributes().getFileId());
        assertNotEquals(fileId, path2RWithId.attributes().getFileId());
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(path2RWithId, path33WithId, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
