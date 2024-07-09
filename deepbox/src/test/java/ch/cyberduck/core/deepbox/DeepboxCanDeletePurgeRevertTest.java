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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
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
public class DeepboxCanDeletePurgeRevertTest extends AbstractDeepboxTest {

    @Test
    public void testNodeDeleteRoot() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
    }

    @Test
    public void testNodeDeleteDeepBox() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
    }

    @Test
    public void testNoDeleteBox() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path folder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(folder);
        assertEquals(Acl.EMPTY, attributes.getAcl());
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(folder.withAttributes(attributes)));
    }

    @Test
    public void testNoDeleteFile() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path file = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents/Property/RE-IN - Copy2.pdf", EnumSet.of(Path.Type.file));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(file);
        assertFalse(attributes.getAcl().containsKey(CANDELETE));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(file.withAttributes(attributes)));
    }

    @Test
    public void testNoDeleteFolder() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path file = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box1/Documents/Property/", EnumSet.of(Path.Type.directory));
        final PathAttributes attributes = new DeepboxAttributesFinderFeature(session, nodeid).find(file);
        assertFalse(attributes.getAcl().containsKey(CANDELETE));
        assertThrows(AccessDeniedException.class, () -> new DeepboxTrashFeature(session, nodeid).preflight(file.withAttributes(attributes)));
    }

    @Test
    public void testDeleteRevertDeletePurgeFile() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path parentFolder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Auditing", EnumSet.of(Path.Type.directory));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, AbstractPath.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(AbstractPath.Type.file));

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
    public void testDeleteRevertDeletePurgeFolder() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path parentFolder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Auditing", EnumSet.of(Path.Type.directory));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, AbstractPath.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(AbstractPath.Type.directory));

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
    public void testDeleteNoRevertFile() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path parentFolder = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box2/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, AbstractPath.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(AbstractPath.Type.file));

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
    public void testDeleteNoRevertFolder() throws BackgroundException {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path parentFolder = new Path("/ORG 1 - DeepBox Desktop App/ORG1:Box2/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, AbstractPath.Type.volume));
        final Path test = new Path(parentFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        final Path testInTrash = new Path(trash, test.getName(), EnumSet.of(AbstractPath.Type.directory));

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