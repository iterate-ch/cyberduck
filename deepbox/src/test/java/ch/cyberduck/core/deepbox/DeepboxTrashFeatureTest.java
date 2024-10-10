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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.*;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxTrashFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testDeleteFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxTouchFeature(session, nodeid).touch(file, new TransferStatus());
        final String nodeId = new DeepboxAttributesFinderFeature(session, nodeid).find(file).getFileId();
        new CoreRestControllerApi(session.getClient()).getNodeInfo(nodeId, null, null, null); // assert no fail
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
            new CoreRestControllerApi(session.getClient()).getNodeInfo(nodeId, null, null, null);
        }
        catch(ApiException e) {
            // not found
            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void testDeleteFolder() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new DeepboxDirectoryFeature(session, nodeid).mkdir(new Path(documents, String.format(new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String nodeId = nodeid.getFileId(folder);
        new CoreRestControllerApi(session.getClient()).getNodeInfo(nodeId, null, null, null); // assert no fail
        final Path subfolderWithContent = new DeepboxDirectoryFeature(session, nodeid).mkdir(new Path(folder, new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(subfolderWithContent));
        final Path file = new Path(subfolderWithContent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DeepboxTouchFeature(session, nodeid).touch(file, new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(file));
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folder.withAttributes(new PathAttributes())));
        assertFalse(new DefaultFindFeature(session).find(folder));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(subfolderWithContent.withAttributes(new PathAttributes())));
        assertFalse(new DefaultFindFeature(session).find(subfolderWithContent));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(file.withAttributes(new PathAttributes())));
        // file not in trash is deleted but not purged (i.e. moved to the trash)
        final Path folderInTrash = new Path(trash, folder.getName(), EnumSet.of(Path.Type.directory));
        assertTrue(new DeepboxFindFeature(session, nodeid).find(folderInTrash));
        assertEquals(nodeId, new DeepboxAttributesFinderFeature(session, nodeid).find(folderInTrash).getFileId());
        new CoreRestControllerApi(session.getClient()).getNodeInfo(nodeId, null, null, null); // assert no fail
        // file in trash is purged (i.e. deleted permanently)
        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(folderInTrash), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(folderInTrash));
        try {
            new CoreRestControllerApi(session.getClient()).getNodeInfo(nodeId, null, null, null);
        }
        catch(ApiException e) {
            // not found
            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void testDeleteNotFound() {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertThrows(NotfoundException.class, () -> new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
    }

    @Test
    public void testNodeDeleteRoot() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
    }

    @Test
    public void testNodeDeleteCompany() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
        assertThrows(NotfoundException.class, () -> new DeepboxTrashFeature(session, nodeid).delete(
                Collections.singletonList(folder.withAttributes(attributes)), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
    }

    @Test
    public void testNodeDeleteDeepBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).delete(
                Collections.singletonList(folder.withAttributes(attributes)), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
    }

    @Test
    public void testNoDeleteBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).delete(
                Collections.singletonList(folder.withAttributes(attributes)), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
    }

    @Test
    public void testNoDeleteFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path file = new Path("/ORG 1 - DeepBox Desktop App/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents/Property/RE-IN - Copy2.pdf", EnumSet.of(Path.Type.file));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(file);
        assertTrue(attributes.getAcl().containsKey(new Acl.CanonicalUser()));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(file.withAttributes(attributes)));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).delete(
                Collections.singletonList(file.withAttributes(attributes)), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
    }

    @Test
    public void testNoDeleteFolder() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents/Property/", EnumSet.of(Path.Type.directory));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(attributes.getAcl().containsKey(new Acl.CanonicalUser()));
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).delete(
                Collections.singletonList(folder.withAttributes(attributes)), new DisabledPasswordCallback(), new Delete.DisabledCallback()));
    }

    @Test
    public void testDeleteRevertDeletePurgeFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path parentFolder = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Auditing", EnumSet.of(Path.Type.directory));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(Path.Type.file));

        new DeepboxTouchFeature(session, nodeid).touch(test, new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test.withAttributes(new PathAttributes())), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxRestoreFeature(session, nodeid).restore(testInTrash.withAttributes(new PathAttributes()), new DisabledLoginCallback());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test.withAttributes(new PathAttributes())), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(testInTrash.withAttributes(new PathAttributes())), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
    }

    @Test
    public void testDeleteRevertDeletePurgeFolder() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path parentFolder = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Auditing", EnumSet.of(Path.Type.directory));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(Path.Type.directory));

        new DeepboxDirectoryFeature(session, nodeid).mkdir(test, new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test.withAttributes(new PathAttributes())), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxRestoreFeature(session, nodeid).restore(testInTrash.withAttributes(new PathAttributes()), new DisabledLoginCallback());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));


        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test.withAttributes(new PathAttributes())), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(testInTrash).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(testInTrash.withAttributes(new PathAttributes())), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
    }

    @Test
    // Trash not listable
    public void testDeleteNoRevertFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path parentFolder = new Path("/ORG 1 - DeepBox Desktop App/ORG 1 - DeepBox Desktop App/ORG1:Box2/Documents/Bookkeeping", EnumSet.of(Path.Type.directory));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(Path.Type.file));

        new DeepboxTouchFeature(session, nodeid).touch(test, new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
    }

    @Test
    // Trash not listable
    public void testDeleteNoRevertFolder() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path parentFolder = new Path("/ORG 1 - DeepBox Desktop App/ORG 1 - DeepBox Desktop App/ORG1:Box2/Documents/Bookkeeping", EnumSet.of(Path.Type.directory));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(Path.Type.directory));

        new DeepboxDirectoryFeature(session, nodeid).mkdir(test, new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANDELETE));
        assertTrue(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANPURGE));
        assertFalse(new DeepboxAttributesFinderFeature(session, nodeid).find(test).getAcl().get(new Acl.CanonicalUser()).contains(CANREVERT));

        new DeepboxTrashFeature(session, nodeid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DeepboxFindFeature(session, nodeid).find(test.withAttributes(new PathAttributes())));
        assertFalse(new DeepboxFindFeature(session, nodeid).find(testInTrash.withAttributes(new PathAttributes())));
    }
}
