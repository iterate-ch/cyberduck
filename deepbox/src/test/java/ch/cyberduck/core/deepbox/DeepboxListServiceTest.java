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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;


@Category(IntegrationTest.class)
public class DeepboxListServiceTest extends AbstractDeepboxTest {
    @Test
    public void testListDeepBoxes() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(directory, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());

        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(1, list.size());
        for(Path f : list) {
            assertSame(directory, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for DeepBoxes
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListBoxes() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);

        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(deepBox, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());

        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(1, list.size());
        for(Path f : list) {
            assertSame(deepBox, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for Boxes
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            //assertEquals(f.attributes(), new DeepboxListService(session, nodeid).find(f));
        }
    }

    @Test
    public void testListMyBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);

        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(box, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());

        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Inbox", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(3, list.size());
        for(Path f : list) {
            assertSame(box, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for Inbox/Documents/Trash virtual folder level
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);

        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(documents, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());

        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Documents/Auditing", EnumSet.of(Path.Type.directory)))));
        assertEquals(13, list.size());
        for(Path f : list) {
            assertSame(documents, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListAuditing() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);

        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(
                auditing, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());

        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Documents/Auditing/nix4.txt", EnumSet.of(Path.Type.file)))));
        assertEquals(1, list.size());
        for(Path f : list) {
            assertSame(auditing, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }
}