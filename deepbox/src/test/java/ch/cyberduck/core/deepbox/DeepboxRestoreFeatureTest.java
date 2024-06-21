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
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxRestoreFeatureTest extends AbstractDeepboxTest {

    @Test
    public void restoreFile() throws BackgroundException {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path test = new Path(auditing, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path testInTrash = new Path(trash, test.getName(), test.getType());

        final String nodeId = new DeepboxTouchFeature(session, fileid).touch(test, new TransferStatus()).attributes().getFileId();
        assertTrue(new DefaultFindFeature(session).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DefaultFindFeature(session).find(testInTrash.withAttributes(new PathAttributes())));

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DefaultFindFeature(session).find(testInTrash.withAttributes(new PathAttributes())));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())).getFileId());
        assertThrows(NotfoundException.class, () -> new DeepboxAttributesFinderFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())).getFileId());

        new DeepboxRestoreFeature(session, fileid).restore(testInTrash, new DisabledLoginCallback());
        assertTrue(new DefaultFindFeature(session).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DefaultFindFeature(session).find(testInTrash.withAttributes(new PathAttributes())));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(test.withAttributes(new PathAttributes())).getFileId());
        assertThrows(NotfoundException.class, () -> new DeepboxAttributesFinderFeature(session, fileid).find(testInTrash.withAttributes(new PathAttributes())));

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void restoreDirectory() throws BackgroundException, ApiException {
        // TODO
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path folder = new Path(auditing, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));

        final Path subfolderWithContent = new Path(folder, new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(subfolderWithContent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path folderInTrash = new Path(trash, folder.getName(), folder.getType());
        final Path subfolderWithContentInTrash = new Path(folderInTrash, subfolderWithContent.getName(), EnumSet.of(Path.Type.directory));
        final Path fileInTrash = new Path(subfolderWithContentInTrash, file.getName(), EnumSet.of(Path.Type.file));


        final String folderId = new DeepboxDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus()).attributes().getFileId();
        assertTrue(new DefaultFindFeature(session).find(folder));
        new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(folderId), null, null, null); // assert no fail

        final String subFolderId = new DeepboxDirectoryFeature(session, fileid).mkdir(subfolderWithContent, new TransferStatus()).attributes().getFileId();
        assertTrue(new DefaultFindFeature(session).find(subfolderWithContent));

        final String nodeId = new DeepboxTouchFeature(session, fileid).touch(file, new TransferStatus()).attributes().getFileId();
        assertTrue(new DefaultFindFeature(session).find(folder.withAttributes(new PathAttributes())));
        assertTrue(new DefaultFindFeature(session).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertTrue(new DefaultFindFeature(session).find(file));
        assertFalse(new DefaultFindFeature(session).find(folderInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DefaultFindFeature(session).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DefaultFindFeature(session).find(fileInTrash.withAttributes(new PathAttributes())));

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());

        assertNull(fileid.getFileId(folder.withAttributes(new PathAttributes())));
        assertNull(fileid.getFileId(subfolderWithContent.withAttributes(new PathAttributes())));
        assertNull(fileid.getFileId(file.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(folder.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(file.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(folderInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(fileInTrash.withAttributes(new PathAttributes())));
        assertEquals(folderId, new DeepboxAttributesFinderFeature(session, fileid).find(folderInTrash.withAttributes(new PathAttributes())).getFileId());
        assertEquals(subFolderId, new DeepboxAttributesFinderFeature(session, fileid).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())).getFileId());
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(fileInTrash.withAttributes(new PathAttributes())).getFileId());

        new DeepboxRestoreFeature(session, fileid).restore(folderInTrash, new DisabledLoginCallback());
        assertTrue(new DeepboxFindFeature(session, fileid).find(folder.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(file));
        assertFalse(new DeepboxFindFeature(session, fileid).find(folderInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(subfolderWithContentInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, fileid).find(fileInTrash.withAttributes(new PathAttributes())));
        assertEquals(folderId, new DeepboxAttributesFinderFeature(session, fileid).find(folder.withAttributes(new PathAttributes())).getFileId());
        assertEquals(subFolderId, new DeepboxAttributesFinderFeature(session, fileid).find(subfolderWithContent.withAttributes(new PathAttributes())).getFileId());
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, fileid).find(file.withAttributes(new PathAttributes())).getFileId());

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}