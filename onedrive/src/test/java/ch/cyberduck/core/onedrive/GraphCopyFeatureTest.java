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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphCopyFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
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
public class GraphCopyFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testCopyNotFound() throws Exception {
        final Touch touch = new GraphTouchFeature(session, fileid);
        final Delete delete = new GraphDeleteFeature(session, fileid);
        final Path drive = new OneDriveHomeFinderService().find();
        final Path file = touch.touch(new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withMime("x-application/cyberduck"));
        final String fileid = file.attributes().getFileId();
        delete.delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final Path target = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertThrows(NotfoundException.class, () -> new GraphCopyFeature(session, this.fileid).copy(file, target, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener()));
        file.attributes().withFileId(fileid);
        assertThrows(NotfoundException.class, () -> new GraphCopyFeature(session, this.fileid).copy(file, target, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener()));
    }

    @Test
    public void testCopy() throws Exception {
        final Path drive = new OneDriveHomeFinderService().find();
        Path directory = new GraphDirectoryFeature(session, fileid).mkdir(
            new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(new GraphAttributesFinderFeature(session, fileid).find(directory));
        final TransferStatus status = new TransferStatus();
        Path file = new GraphTouchFeature(session, fileid).touch(new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status.withMime("x-application/cyberduck"));
        assertNotNull(new GraphAttributesFinderFeature(session, fileid).find(file));
        Path rename = new Path(directory, file.getName(), EnumSet.of(Path.Type.file));
        final GraphCopyFeature copy = new GraphCopyFeature(session, fileid);
        assertTrue(copy.isSupported(file, rename.getParent(), rename.getName()));
        final Path target = copy.copy(file, rename, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertNotEquals(file.attributes().getFileId(), target.attributes().getFileId());
        assertEquals(target.attributes().getFileId(), new GraphAttributesFinderFeature(session, fileid).find(rename).getFileId());
        new GraphDeleteFeature(session, fileid).delete(Arrays.asList(file, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final Path folder = new GraphDirectoryFeature(session, fileid).mkdir(
            new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new GraphTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new GraphTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new GraphCopyFeature(session, fileid).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener());
        final Find find = new DefaultFindFeature(session);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));
        new GraphDeleteFeature(session, fileid).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
