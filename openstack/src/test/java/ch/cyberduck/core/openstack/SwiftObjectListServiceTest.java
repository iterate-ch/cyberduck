package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftObjectListServiceTest extends AbstractSwiftTest {

    @Test
    public void testList() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final AttributedList<Path> list = new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            if(p.isFile()) {
                assertNotEquals(-1L, p.attributes().getModificationDate());
                assertNotEquals(-1L, p.attributes().getSize());
                assertNotNull(p.attributes().getChecksum().hash);
                assertNull(p.attributes().getETag());
            }
            else if(p.isDirectory()) {
                assertFalse(p.isPlaceholder());
            }
            else {
                fail();
            }
        }
    }

    @Test(expected = NotfoundException.class)
    public void testListNotFoundFolder() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        new SwiftObjectListService(session).list(new Path(container, "notfound", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfoundContainer() throws Exception {
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
    }

    @Test
    public void testListPlaceholder() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path placeholder = new SwiftDirectoryFeature(session).mkdir(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new SwiftObjectListService(session).list(placeholder, new DisabledListProgressListener()).isEmpty());
        final Path placeholder2 = new SwiftDirectoryFeature(session).mkdir(new Path(placeholder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new SwiftObjectListService(session).list(placeholder2, new DisabledListProgressListener()).isEmpty());
        new SwiftDeleteFeature(session).delete(Arrays.asList(placeholder, placeholder2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderParent() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String name = UUID.randomUUID().toString();
        final Path placeholder = new Path(container, name, EnumSet.of(Path.Type.directory));
        new SwiftDirectoryFeature(session).mkdir(placeholder, null, new TransferStatus());
        final AttributedList<Path> list = new SwiftObjectListService(session).list(placeholder.getParent(), new DisabledListProgressListener());
        assertTrue(list.contains(placeholder));
        assertTrue(list.contains(new Path(container, name, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertSame(list.get(placeholder), list.get(new Path(container, name, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        new SwiftDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testPlaceholderAndObjectSameName() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path base = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
            new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path child = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
            new Path(base, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        {
            final AttributedList<Path> list = new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
            assertTrue(list.contains(base));
            assertEquals(EnumSet.of(Path.Type.file), list.get(base).getType());
            final Path placeholder = new Path(container, base.getName(), EnumSet.of(Path.Type.directory));
            assertTrue(list.contains(placeholder));
            assertEquals(EnumSet.of(Path.Type.directory), list.get(placeholder).getType());
        }
        {
            final AttributedList<Path> list = new SwiftObjectListService(session).list(new Path(container, base.getName(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
            assertTrue(list.contains(child));
            assertEquals(EnumSet.of(Path.Type.file), list.get(child).getType());
        }
        {
            final AttributedList<Path> list = new SwiftObjectListService(session).list(new Path(container, base.getName(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
            assertTrue(list.contains(child));
            assertEquals(EnumSet.of(Path.Type.file), list.get(child).getType());
        }
        new SwiftDeleteFeature(session).delete(Arrays.asList(base, child), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
