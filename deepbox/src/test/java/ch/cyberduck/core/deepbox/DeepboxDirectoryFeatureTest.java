package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxDirectoryFeatureTest extends AbstractDeepboxTest {
    @Test
    public void testRootFolder() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testDeepBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/Mountainduck Buddies/", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/Mountainduck Buddies/My Box", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/Mountainduck Buddies/My Box/Inbox", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/Mountainduck Buddies/My Box/Trash", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/Mountainduck Buddies/My Box/Documents", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(folder, new TransferStatus());
        assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        // TODO (-1) what about duplicate names with different nodeId?
        // Can create again regardless if exists
        //directory.mkdir(folder, new TransferStatus());
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folder));
    }

    @Test
    public void testBookkeeping() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/Mountainduck Buddies/My Box/Documents/Bookkeeping", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(folder, new TransferStatus());
        assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        // TODO (-1) what about duplicate names with different nodeId?
        // Can create again regardless if exists
        //directory.mkdir(folder, new TransferStatus());
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folder));
    }
}