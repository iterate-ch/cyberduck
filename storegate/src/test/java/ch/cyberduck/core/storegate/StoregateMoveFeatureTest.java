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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class StoregateMoveFeatureTest extends AbstractStoregateTest {

    @Test
    public void testMove() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final TransferStatus status = new TransferStatus();
        final Path test = new StoregateTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final String fileid = test.attributes().getFileId();
        final Path target = new StoregateMoveFeature(session, nodeid).move(test, new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(fileid, target.attributes().getFileId());
        assertEquals(fileid, new StoregateAttributesFinderFeature(session, nodeid).find(target).getFileId());
        assertFalse(new DefaultFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(target));
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertEquals(Comparison.equal, session.getHost().getProtocol().getFeature(ComparisonService.class).compare(Path.Type.file, test.attributes(), new StoregateAttributesFinderFeature(session, nodeid).find(target)));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveWithLock() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new StoregateTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String fileid = test.attributes().getFileId();
        final String lockId = new StoregateLockFeature(session, nodeid).lock(test);
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertEquals(fileid,
            new StoregateMoveFeature(session, nodeid).move(test, target, new TransferStatus().withLockId(lockId), new Delete.DisabledCallback(), new DisabledConnectionCallback()).attributes().getFileId());
        assertFalse(new DefaultFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(target));
        assertEquals(0, session.getMetrics().get(Copy.class));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentFolder() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path folder1 = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder2 = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(folder1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Path target = new Path(folder2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateMoveFeature(session, nodeid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DefaultFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(target));
        assertEquals(0, session.getMetrics().get(Copy.class));
        new StoregateDeleteFeature(session, nodeid).delete(Arrays.asList(folder1, folder2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String foldername = new AlphanumericRandomStringService().random();
        final Path test = new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(room, foldername, EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path testsub = new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(test, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new StoregateMoveFeature(session, nodeid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new DefaultFindFeature(session).find(new Path(room, foldername, EnumSet.of(Path.Type.directory))));
        assertTrue(new DefaultFindFeature(session).find(target));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String filename = new AlphanumericRandomStringService().random();
        final Path test = new StoregateTouchFeature(session, nodeid).touch(new Path(room, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(target, new TransferStatus());
        new StoregateMoveFeature(session, nodeid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DefaultFindFeature(session).find(new Path(room, filename, EnumSet.of(Path.Type.file))));
        assertTrue(new DefaultFindFeature(session).find(target));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentParentAndRename() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String filename = new AlphanumericRandomStringService().random();
        final Path test = new StoregateTouchFeature(session, nodeid).touch(new Path(room, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(target, new TransferStatus());
        new StoregateMoveFeature(session, nodeid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DefaultFindFeature(session).find(new Path(room, filename, EnumSet.of(Path.Type.file))));
        assertTrue(new DefaultFindFeature(session).find(target));
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path room = new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        new StoregateMoveFeature(session, nodeid).move(test, new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }

}
