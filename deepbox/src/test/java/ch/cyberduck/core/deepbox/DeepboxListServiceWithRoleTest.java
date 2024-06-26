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
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.PathRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Folder;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeCopy;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;


@Category(IntegrationTest.class)
public class DeepboxListServiceWithRoleTest extends AbstractDeepboxTest {

    @Test
    public void testDuplicateFiles() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path virtualFolder = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(virtualFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));
        new DeepboxTouchFeature(session, nodeid).touch(file, new TransferStatus());

        final NodeCopy body = new NodeCopy();
        body.setTargetParentNodeId(UUID.fromString(nodeid.getFileId(folder)));
        new CoreRestControllerApi(session.getClient()).copyNode(body, UUID.fromString(nodeid.getFileId(file)));
        final NodeContent remote = new CoreRestControllerApi(session.getClient()).listNodeContent(UUID.fromString(nodeid.getFileId(folder)), 0, 50, null);
        assertEquals(2, remote.getNodes().size());
        try {
            assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        }
        finally {
            deleteAndPurge(folder);
        }
    }

    @Test
    public void testDuplicateFolders() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path virtualFolder = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(virtualFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(test, new TransferStatus());

        // /api/v1/nodes/{nodeId}/copy does not work for folders
        final Folder body = new Folder();
        body.setName(test.getName());
        new PathRestControllerApi(session.getClient()).addFolders(
                Collections.singletonList(body),
                UUID.fromString(nodeid.getDeepBoxNodeId(test)),
                UUID.fromString(nodeid.getBoxNodeId(test)),
                UUID.fromString(nodeid.getFileId(test.getParent()))
        );

        final NodeContent remote = new CoreRestControllerApi(session.getClient()).listNodeContent(UUID.fromString(nodeid.getFileId(folder)), 0, 50, null);
        assertEquals(2, remote.getNodes().size());
        try {
            assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        }
        finally {
            deleteAndPurge(folder);
        }
    }
}