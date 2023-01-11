package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class StoregateListServiceTest extends AbstractStoregateTest {

    @Test
    public void testListRoot() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path directory = new Path("/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new StoregateListService(session, nodeid).list(
                directory, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Common files", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNotNull(list.find(new SimplePathPredicate(new Path("/My files", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        for(Path f : list) {
            assertSame(directory, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new StoregateAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testList() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new Path("/My files", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final TransferStatus status = new TransferStatus();
        status.setHidden(true);
        final Path file = new StoregateTouchFeature(session, nodeid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final AttributedList<Path> list = new StoregateListService(session, nodeid).list(folder, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertTrue(list.contains(file));
        assertSame(folder, list.get(file).getParent());
        assertTrue(list.get(file).attributes().isHidden());
        assertSame(folder, list.get(file).getParent());
        new StoregateDeleteFeature(session, nodeid).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListWithHiddenFile() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new Path("/My files", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        status.setHidden(true);
        new StoregateTouchFeature(session, nodeid).touch(file, status);
        final AttributedList<Path> list = new StoregateListService(session, nodeid).list(folder, new IndexedListProgressListener() {
            @Override
            public void message(final String message) {
                //
            }

            @Override
            public void visit(final AttributedList<Path> list, final int index, final Path file) {
                if(file.attributes().isHidden()) {
                    list.remove(index);
                }
            }
        });
        assertNotSame(AttributedList.emptyList(), list);
        assertTrue(list.isEmpty());
        new StoregateDeleteFeature(session, nodeid).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
