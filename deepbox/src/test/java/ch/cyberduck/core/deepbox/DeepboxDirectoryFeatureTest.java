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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxDirectoryFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testRootFolder() {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testDeepBox() {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testBox() {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testInbox() {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Inbox", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testTrash() {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertThrows(AccessDeniedException.class, () -> directory.preflight(parent, folder.getName()));
    }

    @Test
    public void testDocuments() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final DeepboxDirectoryFeature directory = (DeepboxDirectoryFeature) session.getFeature(Directory.class);
        final Path parent = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(folder, new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(folder.withAttributes(new PathAttributes()), new DisabledListProgressListener()));
        assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        deleteAndPurge(folder);
        assertNull(nodeid.getFileId(folder.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folder.withAttributes(new PathAttributes())));
    }

    @Test
    public void testBookkeeping() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        final Path parent = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Bookkeeping", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(folder, new TransferStatus());
        assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        deleteAndPurge(folder);
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folder));
    }

    @Test
    public void testNoDuplicates() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxDirectoryFeature(session, fileid).mkdir(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        new DeepboxDirectoryFeature(session, fileid).preflight(documents.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).find(documents)), test.getName());
        assertTrue(new DeepboxFindFeature(session, fileid).find(test));
        deleteAndPurge(test);
    }
}