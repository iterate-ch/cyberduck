package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class StoregateDeleteFeatureTest extends AbstractStoregateTest {

    @Test
    public void testDeleteFile() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new Path("/My files", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path fileInRoom = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(new StoregateWriteFeature(session, nodeid), fileInRoom, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(fileInRoom));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(fileInRoom), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(fileInRoom));
    }

    @Test
    public void testDeleteFileWithLock() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new Path("/My files", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path fileInRoom = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(new StoregateWriteFeature(session, nodeid), fileInRoom, new TransferStatus());
        final String lock = new StoregateLockFeature(session, nodeid).lock(fileInRoom);
        assertTrue(new DefaultFindFeature(session).find(fileInRoom));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonMap(fileInRoom, new TransferStatus().setLockId(lock)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(fileInRoom));
    }

    @Test
    public void testDeleteFolderRoomWithContent() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(new StoregateWriteFeature(session, nodeid), new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new StoregateDirectoryFeature(session, nodeid).mkdir(new StoregateWriteFeature(session, nodeid), new Path(room,
            new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(new StoregateWriteFeature(session, nodeid), file, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(folder));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(room));
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final Path test = new Path(String.format("/My files/%s", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        new StoregateDeleteFeature(session, new StoregateIdProvider(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
