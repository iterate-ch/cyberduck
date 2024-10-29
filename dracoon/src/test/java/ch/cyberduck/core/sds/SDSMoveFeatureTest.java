package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class SDSMoveFeatureTest extends AbstractSDSTest {

    @Test
    public void testMoveDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String foldername = new AlphanumericRandomStringService().random();
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, foldername, EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertThrows(NotfoundException.class, () -> new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(session, nodeid).find(new Path(room, foldername, EnumSet.of(Path.Type.directory))));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testMoveDirectoryToRoot() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final SDSMoveFeature move = new SDSMoveFeature(session, nodeid);
        assertFalse(move.isSupported(test, target));
        move.move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(session, nodeid).find(test));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveRoomToDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final SDSMoveFeature move = new SDSMoveFeature(session, nodeid);
        assertFalse(move.isSupported(test, target));
    }

    @Test
    public void testMoveSubroom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path subroom = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final SDSMoveFeature move = new SDSMoveFeature(session, nodeid);
        assertTrue(move.isSupported(subroom, target));
        move.move(subroom, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(0, session.getMetrics().get(Copy.class));
        subroom.attributes().setVersionId(null);
        assertFalse(new SDSFindFeature(session, nodeid).find(subroom));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDataRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final String directoryname = new AlphanumericRandomStringService().random();
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(directoryname, EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path target = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(0, session.getMetrics().get(Copy.class));
        assertFalse(new SDSFindFeature(session, nodeid).find(new Path(directoryname, EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }


    @Test
    public void testMoveOverride() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String filename = new AlphanumericRandomStringService().random();
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session, nodeid).find(new Path(room, filename, EnumSet.of(Path.Type.file))));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveToDifferentParentAndRename() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String filename = new AlphanumericRandomStringService().random();
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, filename, EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new SDSMoveFeature(session, nodeid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SDSFindFeature(session, nodeid).find(new Path(room, filename, EnumSet.of(Path.Type.file))));
        assertTrue(new SDSFindFeature(session, nodeid).find(target));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path room = new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        new SDSMoveFeature(session, nodeid).move(test, new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }
}