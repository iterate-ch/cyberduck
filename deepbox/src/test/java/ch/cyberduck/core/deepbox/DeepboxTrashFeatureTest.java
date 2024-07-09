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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxTrashFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testDeleteFile() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxTouchFeature(session, nodeid).touch(file, new TransferStatus());
        final String nodeId = new DeepboxAttributesFinderFeature(session, nodeid).find(file).getFileId();
        new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(nodeId), null, null, null); // assert no fail
        assertTrue(new DeepboxFindFeature(session, nodeid).find(file));
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(file));
        // file not in trash is deleted but not purged (i.e. moved to the trash)
        final Path fileInTrash = new Path(trash, file.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new DeepboxFindFeature(session, nodeid).find(fileInTrash));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, nodeid).find(fileInTrash).getFileId());
        // file in trash is purged (i.e. deleted permanently)
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(fileInTrash), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(fileInTrash));
        try {
            new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(nodeId), null, null, null);
        }
        catch(ApiException e) {
            // not found
            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void testDeleteFolder() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new DeepboxDirectoryFeature(session, nodeid).mkdir(new Path(documents, String.format(new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String nodeId = new DeepboxTouchFeature(session, nodeid).touch(folder, new TransferStatus()).attributes().getFileId();
        new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(nodeId), null, null, null); // assert no fail
        final Path subfolderWithContent = new DeepboxDirectoryFeature(session, nodeid).mkdir(new Path(folder, new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(subfolderWithContent));
        final Path file = new Path(subfolderWithContent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxTouchFeature(session, nodeid).touch(file, new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(file));
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folder.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(file.withAttributes(new PathAttributes())));
        // file not in trash is deleted but not purged (i.e. moved to the trash)
        final Path folderInTrash = new Path(trash, folder.getName(), EnumSet.of(Path.Type.directory));
        assertTrue(new DeepboxFindFeature(session, nodeid).find(folderInTrash));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, nodeid).find(folderInTrash).getFileId());
        new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(nodeId), null, null, null); // assert no fail
        // file in trash is purged (i.e. deleted permanently)
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(folderInTrash), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folderInTrash));
        try {
            new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(nodeId), null, null, null);
        }
        catch(ApiException e) {
            // not found
            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void testDeleteNotFound() {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertThrows(NotfoundException.class, () -> new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
    }
}
