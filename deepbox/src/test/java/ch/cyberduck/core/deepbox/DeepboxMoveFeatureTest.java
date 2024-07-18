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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.*;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxMoveFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testMove() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxTouchFeature(session, fileid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String sourceId = test.attributes().getFileId();
        assertNotNull(sourceId);
        test.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).find(test));
        assertNotEquals(TransferStatus.UNKNOWN_LENGTH, test.attributes().getSize());
        assertNotEquals(-1L, test.attributes().getModificationDate());

        final Path moved = new DeepboxMoveFeature(session, fileid).move(test,
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(sourceId, moved.attributes().getFileId());
        moved.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).find(moved));
        assertFalse(new DeepboxFindFeature(session, fileid).find(new Path(test).withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(moved));
        assertEquals(test.attributes().getModificationDate(), moved.attributes().getModificationDate());
        assertEquals(test.attributes().getChecksum(), moved.attributes().getChecksum());
        assertEquals(Comparison.equal, session.getHost().getProtocol().getFeature(ComparisonService.class).compare(Path.Type.file, moved.attributes(), new DeepboxAttributesFinderFeature(session, fileid).find(moved)));

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(moved), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveDirectory() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxDirectoryFeature(session, fileid).mkdir(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String sourceId = test.attributes().getFileId();
        assertNotNull(sourceId);
        new DeepboxTouchFeature(session, fileid).touch(new Path(test, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path moved = new DeepboxMoveFeature(session, fileid).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertEquals(sourceId, moved.attributes().getFileId());
        assertFalse(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(target));

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverrideFile() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));

        final Path test = new DeepboxTouchFeature(session, fileid).touch(
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new DeepboxTouchFeature(session, fileid).touch(
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path targetInTrash = new Path(trash, target.getName(), target.getType());

        final PathAttributes originalTestAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(test);
        final PathAttributes originalTargetAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(target);

        new DeepboxMoveFeature(session, fileid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(target.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(targetInTrash));

        final PathAttributes overriddenTargetAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(target.withAttributes(new PathAttributes()));
        assertNotNull(originalTestAttributes.getFileId());
        assertEquals(originalTestAttributes.getFileId(), overriddenTargetAttributes.getFileId());
        assertEquals(originalTestAttributes.getModificationDate(), overriddenTargetAttributes.getModificationDate());
        assertEquals(originalTestAttributes.getChecksum(), overriddenTargetAttributes.getChecksum());

        final PathAttributes trashedTargetAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(targetInTrash.withAttributes(new PathAttributes()));
        assertNotNull(originalTargetAttributes.getFileId());
        assertEquals(originalTargetAttributes.getFileId(), trashedTargetAttributes.getFileId());
        assertEquals(originalTargetAttributes.getModificationDate(), trashedTargetAttributes.getModificationDate());
        assertEquals(originalTargetAttributes.getChecksum(), trashedTargetAttributes.getChecksum());

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(targetInTrash), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverrideFolder() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));

        final Path test = new DeepboxDirectoryFeature(session, fileid).mkdir(
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path target = new DeepboxDirectoryFeature(session, fileid).mkdir(
                new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path targetInTrash = new Path(trash, target.getName(), target.getType());

        final PathAttributes originalTestAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(test);
        final PathAttributes originalTargetAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(target);

        new DeepboxMoveFeature(session, fileid).move(test, target, new TransferStatus().exists(true), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DeepboxFindFeature(session, fileid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(target.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, fileid).find(targetInTrash));

        final PathAttributes overriddenTargetAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(target.withAttributes(new PathAttributes()));
        assertNotNull(originalTestAttributes.getFileId());
        assertEquals(originalTestAttributes.getFileId(), overriddenTargetAttributes.getFileId());
        assertEquals(originalTestAttributes.getModificationDate(), overriddenTargetAttributes.getModificationDate());
        assertEquals(originalTestAttributes.getChecksum(), overriddenTargetAttributes.getChecksum());

        final PathAttributes trashedTargetAttributes = new DeepboxAttributesFinderFeature(session, fileid).find(targetInTrash.withAttributes(new PathAttributes()));
        assertNotNull(originalTargetAttributes.getFileId());
        assertEquals(originalTargetAttributes.getFileId(), trashedTargetAttributes.getFileId());
        assertEquals(originalTargetAttributes.getModificationDate(), trashedTargetAttributes.getModificationDate());
        assertEquals(originalTargetAttributes.getChecksum(), trashedTargetAttributes.getChecksum());

        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(targetInTrash), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveNotFound() {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path renamed = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertThrows(NotfoundException.class, () -> new DeepboxMoveFeature(session, fileid).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
    }

    @Test
    public void testNoMoveRenameDeepbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path deepBox = new Path("/ORG 1 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(deepBox);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(deepBox, new Path(String.format("/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path box = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(box);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(box, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(box, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path inbox = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Inbox", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(inbox);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(inbox, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(inbox, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(documents);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(documents, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(documents, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(trash);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(trash, new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(trash, new Path(String.format("/ORG 4 - DeepBox Desktop App/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testNoMoveRenameFileFromTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new DeepboxTouchFeature(session, nodeid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path fileInTrash = new Path(trash, file.getName(), EnumSet.of(Path.Type.file));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(fileInTrash);
        // we still must not move/rename because it's in trash, use restore only instead of moving from trash
        // the move operation would succeed in the DeepBox backend, however we follow the DeepBox web convention and only allow restore
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(fileInTrash, file));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(fileInTrash, new Path(trash, file.getName(), EnumSet.of(Path.Type.file))));
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(fileInTrash), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testNoMoveFileToTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new DeepboxTouchFeature(session, nodeid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path fileInTrash = new Path(trash, file.getName(), EnumSet.of(Path.Type.file));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(file);
        // we still must not move to trash, use delete only instead of moving to trash
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEWITHINBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANMOVEOUTOFBOX));
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANRENAME));
        assertThrows(AccessDeniedException.class, () -> new DeepboxMoveFeature(session, nodeid).preflight(file, fileInTrash));
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testNoMoveRenameFileAndFolderDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents/Invoices : Receipts", EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, "RE-IN 0.pdf", EnumSet.of(Path.Type.file));
        final Path fileRenamed = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path folderRenamed = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));

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