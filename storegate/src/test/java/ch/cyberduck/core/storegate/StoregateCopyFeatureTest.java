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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class StoregateCopyFeatureTest extends AbstractStoregateTest {

    @Test
    public void testCopyFileServerSide() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new StoregateTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus()), test.getName(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        new StoregateTouchFeature(session, nodeid).touch(copy, status);
        final StoregateCopyFeature feature = new StoregateCopyFeature(session, nodeid);
        assertTrue(feature.isSupported(test, Optional.of(copy)));
        assertNotEquals(test.attributes().getFileId(), new StoregateCopyFeature(session, nodeid).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes().getFileId());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(copy));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileWithRename() throws Exception {
        final StoregateIdProvider fileid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, fileid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new StoregateTouchFeature(session, fileid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(new StoregateDirectoryFeature(session, fileid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus()), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertNotEquals(test.attributes().getFileId(), new StoregateCopyFeature(session, fileid).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes().getFileId());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(copy));
        new StoregateDeleteFeature(session, fileid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyServerSideToExistingFile() throws Exception {
        final StoregateIdProvider fileid = new StoregateIdProvider(session);
        final Path top = new StoregateDirectoryFeature(session, fileid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path sourceFolder = new Path(top, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFolder = new Path(top, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new StoregateDirectoryFeature(session, fileid).mkdir(sourceFolder, new TransferStatus());
        new StoregateDirectoryFeature(session, fileid).mkdir(targetFolder, new TransferStatus());
        final Path test = new StoregateTouchFeature(session, fileid).touch(new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(targetFolder, test.getName(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, fileid).touch(copy, new TransferStatus());
        final StoregateCopyFeature feature = new StoregateCopyFeature(session, fileid);
        assertTrue(feature.isSupported(test, Optional.of(copy)));
        assertNotEquals(test.attributes().getFileId(), new StoregateCopyFeature(session, fileid).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes().getFileId());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new StoregateListService(session, fileid).list(targetFolder, new DisabledListProgressListener());
        assertTrue(find.find(copy));
        new StoregateDeleteFeature(session, fileid).delete(Collections.singletonList(top), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyWithRenameToExistingFile() throws Exception {
        final StoregateIdProvider fileid = new StoregateIdProvider(session);
        final Path top = new StoregateDirectoryFeature(session, fileid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new Path(top, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new StoregateDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        final Path test = new StoregateTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path test2 = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, fileid).touch(test2, new TransferStatus());
        assertNotEquals(test.attributes().getFileId(), new StoregateCopyFeature(session, fileid).copy(test, test2, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener()).attributes().getFileId());
        final Find find = new DefaultFindFeature(session);
        final AttributedList<Path> files = new StoregateListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertTrue(find.find(test));
        assertTrue(find.find(test2));
        new StoregateDeleteFeature(session, fileid).delete(Collections.singletonList(top), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyDirectoryServerSide() throws Exception {
        final StoregateIdProvider fileid = new StoregateIdProvider(session);
        final Path top = new StoregateDirectoryFeature(session, fileid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path directory = new StoregateDirectoryFeature(session, fileid).mkdir(new Path(top, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final TransferStatus status = new TransferStatus();
        final Path file = new StoregateTouchFeature(session, fileid).touch(new Path(directory, name, EnumSet.of(Path.Type.file)), status);
        final Path target_parent = new StoregateDirectoryFeature(session, fileid).mkdir(new Path(top, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new Path(target_parent, directory.getName(), EnumSet.of(Path.Type.directory));
        final StoregateCopyFeature feature = new StoregateCopyFeature(session, fileid);
        assertTrue(feature.isSupported(directory, Optional.of(target)));
        final Path copy = new StoregateCopyFeature(session, fileid).copy(directory, target, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertNotEquals(file.attributes().getFileId(), copy.attributes().getFileId());
        assertTrue(new DefaultFindFeature(session).find(file));
        assertTrue(new DefaultFindFeature(session).find(target));
        assertTrue(new DefaultFindFeature(session).find(copy));
        new StoregateDeleteFeature(session, fileid).delete(Collections.singletonList(top), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}
