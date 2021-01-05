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

import ch.cyberduck.core.AlphanumericRandomStringService;
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
import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftObjectListServiceTest extends AbstractSwiftTest {

    @Test
    public void testListDirectory() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path placeholder = new SwiftDirectoryFeature(session).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path f1 = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
            new Path(placeholder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path f2 = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
            new Path(placeholder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new SwiftObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertTrue(list.contains(f1));
        assertTrue(list.contains(f2));
        new SwiftDeleteFeature(session).delete(Arrays.asList(f1, f2, placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListDotInKey() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path placeholder = new SwiftDirectoryFeature(session).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path test = new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(
            new Path(placeholder, new AlphanumericRandomStringService().random() + "..", EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new SwiftObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertTrue(list.contains(test));
        new SwiftDeleteFeature(session).delete(Arrays.asList(test, placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotFoundFolder() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
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
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path placeholder = new SwiftDirectoryFeature(session).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new SwiftObjectListService(session).list(placeholder, new DisabledListProgressListener()).isEmpty());
        final Path placeholder2 = new SwiftDirectoryFeature(session).mkdir(new Path(placeholder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new SwiftObjectListService(session).list(placeholder2, new DisabledListProgressListener()).isEmpty());
        new SwiftDeleteFeature(session).delete(Arrays.asList(placeholder, placeholder2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderParent() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final String name = new AlphanumericRandomStringService().random();
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
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Path base = new SwiftTouchFeature(session, regionService).touch(
            new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path child = new SwiftTouchFeature(session, regionService).touch(
            new Path(base, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
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

    @Test
    public void testListLexicographicSortOrderAssumption() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Path directory = new SwiftDirectoryFeature(session, regionService).mkdir(
            new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new SwiftObjectListService(session, regionService).list(directory, new DisabledListProgressListener()).isEmpty());
        final List<String> files = Arrays.asList(
            "aa", "0a", "a", "AAA", "B", "~$a", ".c"
        );
        for(String f : files) {
            new SwiftTouchFeature(session, regionService).touch(new Path(directory, f, EnumSet.of(Path.Type.file)), new TransferStatus());
        }
        files.sort(session.getHost().getProtocol().getListComparator());
        final AttributedList<Path> list = new SwiftObjectListService(session, regionService).list(directory, new DisabledListProgressListener());
        for(int i = 0; i < list.size(); i++) {
            assertEquals(files.get(i), list.get(i).getName());
            new SwiftDeleteFeature(session, regionService).delete(Collections.singletonList(list.get(i)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new SwiftDeleteFeature(session, regionService).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
