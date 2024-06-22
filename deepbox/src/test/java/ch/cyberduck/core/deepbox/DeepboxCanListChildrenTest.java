package ch.cyberduck.core.deepbox;/*
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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANLISTCHILDREN;
import static org.junit.Assert.*;

public class DeepboxCanListChildrenTest extends AbstractDeepboxTest {
    @Before
    public void setup() throws Exception {
        setup("deepbox.deepboxapp3.user");
    }

    @Test
    // Documents always seem to be visible, despite BoxAccessPolicy#canListFilesRoot
    public void testNoListChildrenTrashInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path box = new Path("/ORG 1 - DeepBox Desktop App/Box2", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(box, new DisabledListProgressListener());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/ORG 1 - DeepBox Desktop App/Box2/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNull(list.find(new SimplePathPredicate(new Path("/ORG 1 - DeepBox Desktop App/Box2/Inbox", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNull(list.find(new SimplePathPredicate(new Path("/ORG 1 - DeepBox Desktop App/Box2/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
    }

    @Test
    public void testListChildrenInbox() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/Box1/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new BoxRestControllerApi(session.getClient()).getBox(ORG4, ORG4_BOX1).getBoxPolicy().isCanAddQueue());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANLISTCHILDREN));
        // assert no fail
        new DeepboxListService(session, nodeid).preflight(folder.withAttributes(attributes));
    }

    @Test
    public void testListChildrenDocuments() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new BoxRestControllerApi(session.getClient()).getBox(ORG4, ORG4_BOX1).getBoxPolicy().isCanAddFilesRoot());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANLISTCHILDREN));
        // assert no fail
        new DeepboxListService(session, nodeid).preflight(folder.withAttributes(attributes));
    }

    @Test
    public void testListChildrenTrash() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new BoxRestControllerApi(session.getClient()).getBox(ORG4, ORG4_BOX1).getBoxPolicy().isCanAddFilesRoot());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANLISTCHILDREN));
        // assert no fail
        new DeepboxListService(session, nodeid).preflight(folder.withAttributes(attributes));
    }

    @Test
    // N.B. all folders always seem to have canListChildren
    public void testListChildrenFolder() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/Auditing", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertTrue(new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(attributes.getFileId()), null, null, null).getNode().getPolicy().isCanListChildren());
        assertTrue(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANLISTCHILDREN));
        // assert no fail
        new DeepboxListService(session, nodeid).preflight(folder.withAttributes(attributes));
    }


    @Test
    public void testNoListChildrenFile() throws BackgroundException, ApiException {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/RE-IN - Copy1.pdf", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertFalse(new CoreRestControllerApi(session.getClient()).getNodeInfo(UUID.fromString(attributes.getFileId()), null, null, null).getNode().getPolicy().isCanListChildren());
        assertFalse(attributes.getAcl().get(new Acl.CanonicalUser()).contains(CANLISTCHILDREN));
        assertThrows(AccessDeniedException.class, () -> new DeepboxListService(session, nodeid).preflight(folder.withAttributes(attributes)));
    }
}