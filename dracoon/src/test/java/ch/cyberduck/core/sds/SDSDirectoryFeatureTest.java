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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSDirectoryFeatureTest extends AbstractSDSTest {

    @Test
    public void testCreateDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
                new SDSDirectS3MultipartWriteFeature(session, nodeid), new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        assertThrows(ConflictException.class, () -> new SDSDirectoryFeature(session, nodeid).mkdir(new SDSDirectS3MultipartWriteFeature(session, nodeid), room, new TransferStatus()));
        final Path test = new SDSDirectoryFeature(session, nodeid).mkdir(new SDSDirectS3MultipartWriteFeature(session, nodeid), new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertThrows(ConflictException.class, () -> new SDSDirectoryFeature(session, nodeid).mkdir(new SDSDirectS3MultipartWriteFeature(session, nodeid), test, new TransferStatus()));
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new DefaultFindFeature(session).find(test));
        // Replace directory on server with same name
        new NodesApi(session.getClient()).removeNode(Long.parseLong(test.attributes().getVersionId()), StringUtils.EMPTY);
        final CreateFolderRequest folderRequest = new CreateFolderRequest();
        folderRequest.setParentId(Long.parseLong(room.attributes().getVersionId()));
        folderRequest.setName(test.getName());
        // New node for directory with same nmae
        final Node node = new NodesApi(session.getClient()).createFolder(folderRequest, StringUtils.EMPTY, null);
        assertNotEquals(test.attributes().getVersionId(), node.getId().toString());
        // Attempt to create subdirectory referencing previous node id
        final Path subdir = new SDSDirectoryFeature(session, nodeid).mkdir(new SDSDirectS3MultipartWriteFeature(session, nodeid), new Path(test,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertEquals(node.getId().toString(), nodeid.getVersionId(test));
        assertEquals(test.attributes().getVersionId(), node.getId().toString());
        new SDSDeleteFeature(session, nodeid).delete(Arrays.asList(subdir, test, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCreateDataRoom() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
                new SDSDirectS3MultipartWriteFeature(session, nodeid), new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        assertNotNull(room.attributes().getVersionId());
        assertTrue(new DefaultFindFeature(session).find(room));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(room));
    }
}
