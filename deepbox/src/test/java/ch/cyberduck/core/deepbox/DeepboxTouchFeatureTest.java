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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANADDCHILDREN;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxTouchFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testTouchRoot() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, fileid).preflight(Home.ROOT, new AlphanumericRandomStringService().random()));
        assertThrows(NotfoundException.class, () -> new DeepboxTouchFeature(session, fileid).touch(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus()));
    }

    @Test
    public void testNoDuplicates() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxTouchFeature(session, fileid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DeepboxTouchFeature(session, fileid).preflight(documents.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).find(documents)), test.getName());
        assertTrue(new DeepboxFindFeature(session, fileid).find(test));
        deleteAndPurge(test);
    }

    @Test
    public void testAccents() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Insurance", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxTouchFeature(session, fileid).touch(new Path(documents, new AlphanumericRandomStringService().random() + "éf", EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, fileid).find(documents));
        deleteAndPurge(test);
    }

    @Test
    public void testNoAddChildrenDeepbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testAddChildrenInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new BoxRestControllerApi(session.getClient()).getBox(ORG4, ORG4_BOX1).getBoxPolicy().isCanAddQueue());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        // assert no fail
        new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
        // DeepBox inbox is flat
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new BoxRestControllerApi(session.getClient()).getBox(ORG1, ORG1_BOX1).getBoxPolicy().isCanAddQueue());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testAddChildrenDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new BoxRestControllerApi(session.getClient()).getBox(ORG4, ORG4_BOX1).getBoxPolicy().isCanAddFilesRoot());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        // assert no fail
        new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
        // assert no fail
        new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
    }

    @Test
    public void testNoAddChildrenDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new BoxRestControllerApi(session.getClient()).getBox(ORG1, ORG1_BOX1).getBoxPolicy().isCanAddFilesRoot());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testAddChildrenFolder() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Auditing", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new CoreRestControllerApi(session.getClient()).getNodeInfo(attributes.getFileId(), null, null, null).getNode().getPolicy().isCanAddChildren());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        // assert no fail
        new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
        // assert no fail
        new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
    }

    @Test
    public void testNoAddChildrenFolder() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new CoreRestControllerApi(session.getClient()).getNodeInfo(attributes.getFileId(), null, null, null).getNode().getPolicy().isCanAddChildren());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/RE-IN - Copy1.pdf", EnumSet.of(Path.Type.file));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new CoreRestControllerApi(session.getClient()).getNodeInfo(attributes.getFileId(), null, null, null).getNode().getPolicy().isCanAddChildren());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }
}