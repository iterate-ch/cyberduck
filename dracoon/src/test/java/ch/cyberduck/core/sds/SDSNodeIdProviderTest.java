package ch.cyberduck.core.sds;

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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSNodeIdProviderTest extends AbstractSDSTest {

    @Test
    public void getFileIdFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String name = String.format("%s%s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random());
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(room, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        nodeid.clear();
        final String nodeId = nodeid.getNodeId(new Path(room, name, EnumSet.of(Path.Type.file)), 1);
        assertNotNull(nodeId);
        assertEquals(nodeId, nodeid.getNodeId(new Path(room.withAttributes(PathAttributes.EMPTY), name, EnumSet.of(Path.Type.file)), 1));
        assertEquals(nodeId, nodeid.getNodeId(new Path(room, StringUtils.upperCase(name), EnumSet.of(Path.Type.file)), 1));
        assertEquals(nodeId, nodeid.getNodeId(new Path(room, StringUtils.lowerCase(name), EnumSet.of(Path.Type.file)), 1));
        try {
            assertNull(nodeid.getNodeId(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), 1));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        try {
            assertNull(nodeid.getNodeId(new Path(room, name, EnumSet.of(Path.Type.directory)), 1));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(file, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void getFileIdFileVersions() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(room, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final String versionIdTouch = file.attributes().getVersionId();
        nodeid.clear();
        assertEquals(versionIdTouch, nodeid.getNodeId(new Path(room, name, EnumSet.of(Path.Type.file)), 1));
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setExists(true);
        final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
        final StatusOutputStream<Node> out = writer.write(file, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(file.attributes().getVersionId());
        assertNotEquals(versionIdTouch, file.attributes().getVersionId());
        nodeid.clear();
        assertEquals(file.attributes().getVersionId(), nodeid.getNodeId(new Path(room, name, EnumSet.of(Path.Type.file)), 1));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void getFileIdDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, name, EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(folder.attributes().getVersionId());
        final Path file = new SDSTouchFeature(session, nodeid).touch(new Path(folder, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(file.attributes().getVersionId());
        nodeid.clear();
        assertEquals(folder.attributes().getVersionId(), nodeid.getNodeId(new Path(room, name, EnumSet.of(Path.Type.directory)), 1));
        try {
            assertNull(nodeid.getNodeId(new Path(room, name, EnumSet.of(Path.Type.file)), 1));
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        assertEquals(file.attributes().getVersionId(), nodeid.getNodeId(new Path(folder, file.getName(), EnumSet.of(Path.Type.file)), 1));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void getFileIdRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final String roomname = new AlphanumericRandomStringService().random();
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(roomname, EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        assertNotNull(room.attributes().getVersionId());
        final String subroomname = new AlphanumericRandomStringService().random();
        final Path subroom = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(room, subroomname, EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        assertNotNull(subroom.attributes().getVersionId());
        nodeid.clear();
        assertEquals(room.attributes().getVersionId(), nodeid.getNodeId(new Path(roomname, EnumSet.of(Path.Type.directory)), 1));
        assertEquals(subroom.attributes().getVersionId(), nodeid.getNodeId(new Path(room, subroomname, EnumSet.of(Path.Type.directory)), 1));
        try {
            assertNull(nodeid.getNodeId(new Path(room, subroomname, EnumSet.of(Path.Type.file)), 1));
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
