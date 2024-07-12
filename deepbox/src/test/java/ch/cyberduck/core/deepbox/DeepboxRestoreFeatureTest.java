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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.features.Restore;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxRestoreFeatureTest extends AbstractDeepboxTest {

    @Test
    public void restoreFile() throws BackgroundException {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path testInTrash = new Path(trash, test.getName(), test.getType());

        new DeepboxTouchFeature(session, fileid).touch(test, new TransferStatus());
        final String nodeId = new DeepboxAttributesFinderFeature(session, fileid).find(test).getFileId();

        assertTrue(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())));

        new DeepboxTrashFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())).getFileId());
        assertThrows(NotfoundException.class, () -> new DeepboxAttributesFinderFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())).getFileId());

        final DeepboxRestoreFeature restore = (DeepboxRestoreFeature) session.getFeature(Restore.class);
        restore.restore(testInTrash, new DisabledLoginCallback());
        assertTrue(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(test.withAttributes(new PathAttributes())).getFileId());
        assertThrows(NotfoundException.class, () -> new DeepboxAttributesFinderFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())));

        deleteAndPurge(test.withAttributes(new PathAttributes()));
    }

    @Test
    public void restoreDirectory() throws BackgroundException, ApiException {
        final DeepboxIdProvider fileid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));

        final Path subfolderWithContent = new Path(folder, new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(subfolderWithContent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path folderInTrash = new Path(trash, folder.getName(), folder.getType());
        final Path subfolderWithContentInTrash = new Path(folderInTrash, subfolderWithContent.getName(), EnumSet.of(Path.Type.directory));
        final Path fileInTrash = new Path(subfolderWithContentInTrash, file.getName(), EnumSet.of(Path.Type.file));

        new DeepboxDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        final String folderId = new DeepboxAttributesFinderFeature(session, fileid).find(folder).getFileId();
        assertTrue(new DeepboxFindFeature(session, fileid).find(folder));
        new CoreRestControllerApi(session.getClient()).getNodeInfo(folderId, null, null, null); // assert no fail

        final String subFolderId = new DeepboxDirectoryFeature(session, fileid).mkdir(subfolderWithContent, new TransferStatus()).attributes().getFileId();
        assertTrue(new DeepboxFindFeature(session, fileid).find(subfolderWithContent));

        new DeepboxTouchFeature(session, fileid).touch(file, new TransferStatus());
        final String nodeId = new DeepboxAttributesFinderFeature(session, fileid).find(file).getFileId();
        assertTrue(new DeepboxFindFeature(session, fileid).find(folder.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(file));
        assertFalse(new DeepboxFindFeature(session, fileid).find(folderInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(fileInTrash.withAttributes(new PathAttributes())));

        new DeepboxTrashFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());

        assertThrows(NotfoundException.class, () -> fileid.getFileId(folder.withAttributes(new PathAttributes())));
        assertThrows(NotfoundException.class, () -> fileid.getFileId(subfolderWithContent.withAttributes(new PathAttributes())));
        assertThrows(NotfoundException.class, () -> fileid.getFileId(file.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(folder.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(file.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(folderInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(fileInTrash.withAttributes(new PathAttributes())));
        assertEquals(folderId, new DeepboxAttributesFinderFeature(session, fileid).find(folderInTrash.withAttributes(new PathAttributes())).getFileId());
        assertEquals(subFolderId, new DeepboxAttributesFinderFeature(session, fileid).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())).getFileId());
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(fileInTrash.withAttributes(new PathAttributes())).getFileId());

        final DeepboxRestoreFeature restore = (DeepboxRestoreFeature) session.getFeature(Restore.class);
        restore.restore(folderInTrash, new DisabledLoginCallback());
        assertTrue(new DeepboxFindFeature(session, fileid).find(folder.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(file.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(folderInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(fileInTrash.withAttributes(new PathAttributes())));
        assertEquals(folderId, new DeepboxAttributesFinderFeature(session, fileid).find(folder.withAttributes(new PathAttributes())).getFileId());
        assertEquals(subFolderId, new DeepboxAttributesFinderFeature(session, fileid).find(subfolderWithContent.withAttributes(new PathAttributes())).getFileId());
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(file.withAttributes(new PathAttributes())).getFileId());

        deleteAndPurge(folder.withAttributes(new PathAttributes()));
    }
}