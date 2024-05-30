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
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANADDCHILDREN;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxCanAddChildrenTest extends AbstractDeepboxTest {

    @Test
    public void testNoAddChildrenDeepbox() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenBox() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testAddChildrenInbox() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new BoxRestControllerApi(session.getClient()).getBox(ORG4, ORG4_BOX1).getBoxPolicy().isCanAddQueue());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        // assert no fail
        new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
        // DeepBox inbox is flat
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenInbox() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new BoxRestControllerApi(session.getClient()).getBox(ORG1, ORG1_BOX1).getBoxPolicy().isCanAddQueue());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testAddChildrenDocuments() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new BoxRestControllerApi(session.getClient()).getBox(ORG4, ORG4_BOX1).getBoxPolicy().isCanAddFilesRoot());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        // assert no fail
        new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
        // assert no fail
        new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
    }

    @Test
    public void testNoAddChildrenDocuments() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new BoxRestControllerApi(session.getClient()).getBox(ORG1, ORG1_BOX1).getBoxPolicy().isCanAddFilesRoot());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenTrash() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testAddChildrenFolder() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Documents/Auditing", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(attributes.getFileId()), null, null, null).getNode().getPolicy().isCanAddChildren());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        // assert no fail
        new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
        // assert no fail
        new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random());
    }

    @Test
    public void testNoAddChildrenFolder() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 1 - DeepBox Desktop App/ORG1-Box1/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(attributes.getFileId()), null, null, null).getNode().getPolicy().isCanAddChildren());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }

    @Test
    public void testNoAddChildrenFile() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3-Box1/Documents/RE-IN - Copy1.pdf", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(attributes.getFileId()), null, null, null).getNode().getPolicy().isCanAddChildren());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANADDCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
        assertThrows(AccessDeniedException.class, () -> new DeepboxDirectoryFeature(session, nodeid).preflight(folder.withAttributes(attributes), new AlphanumericRandomStringService().random()));
    }
}