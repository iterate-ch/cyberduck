package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSBatchDeleteFeatureTest extends AbstractSDSTest {

    @Test
    public void testDeleteFiles() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file1 = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path file2 = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(file1, new TransferStatus());
        new SDSTouchFeature(session, nodeid).touch(file2, new TransferStatus());
        new SDSBatchDeleteFeature(session, nodeid).delete(Arrays.asList(file1, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SDSFindFeature(session, nodeid).find(file1));
        assertFalse(new SDSFindFeature(session, nodeid).find(file2));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteRecursively() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path file = new SDSTouchFeature(session, nodeid).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new SDSFindFeature(session, nodeid).find(file));
        assertNotNull(nodeid.getVersionId(file));
        new SDSBatchDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        file.attributes().setVersionId(null);
        folder.attributes().setVersionId(null);
        try {
            nodeid.getVersionId(file);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        try {
            nodeid.getVersionId(folder);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
    }

    @Test
    public void testDeleteFolderRoomWithContent() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room,
                new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new SDSFindFeature(session, nodeid).find(folder));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(file, new TransferStatus());
        assertTrue(new SDSFindFeature(session, nodeid).find(file));
        new SDSBatchDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SDSFindFeature(session, nodeid).find(folder));
        new SDSBatchDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new SDSFindFeature(session, nodeid).find(room));
    }

    @Test
    public void testIsRecursive() {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        assertTrue(new SDSBatchDeleteFeature(session, nodeid).isRecursive());
    }
}