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
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.*;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxCanMoveRenameTest extends AbstractDeepboxTest {

    @Test
    public void testNoMoveRenameDeepbox() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path deepBox = new Path("/ORG 1 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(deepBox);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(deepBox, new Path(String.format("/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameBox() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path box = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(box);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(box, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(box, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameInbox() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path inbox = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1/Inbox", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(inbox);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(inbox, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(inbox, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameDocuments() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(documents);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(documents, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(documents, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameTrash() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(trash);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(trash, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(trash, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameFileFromTrash() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new DeepboxTouchFeature(session, nodeid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus());
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path fileInTrash = new Path(trash, file.getName(), EnumSet.of(AbstractPath.Type.file));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(fileInTrash);
        // we still must not move/rename because it's in trash, use restore only instead of moving from trash
        // the move operation would succeed in the DeepBox backend, however we follow the DeepBox web convention and only allow restore
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(fileInTrash, file));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(fileInTrash, new Path(trash, file.getName(), EnumSet.of(AbstractPath.Type.file))));
        deleteAndPurge(fileInTrash);
    }

    @Test
    public void testNoMoveFileToTrash() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new DeepboxTouchFeature(session, nodeid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus());
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path fileInTrash = new Path(trash, file.getName(), EnumSet.of(AbstractPath.Type.file));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(file);
        // we still must not move to trash, use delete only instead of moving to trash
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(file, fileInTrash));
        deleteAndPurge(file);
    }

    @Test
    public void testNoMoveRenameFileAndFolderDocuments() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1/Documents/Invoices - Receipts", EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, "RE-IN 0.pdf", EnumSet.of(AbstractPath.Type.file));
        final Path fileRenamed = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));
        final Path folderRenamed = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));

        final PathAttributes fileAttributes = new DeepboxAttributesFinderFeature(session, nodeid).find(file);
        file.setAttributes(fileAttributes);
        // we still must not move to trash, use delete only instead of moving to trash
        assertFalse(fileAttributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(fileAttributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(fileAttributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(file, fileRenamed));

        final PathAttributes folderAttributes = new DeepboxAttributesFinderFeature(session, nodeid).find(file);
        folder.setAttributes(folderAttributes);
        // we still must not move to trash, use delete only instead of moving to trash
        assertFalse(folderAttributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(folderAttributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(folderAttributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(folder, folderRenamed));
    }
}