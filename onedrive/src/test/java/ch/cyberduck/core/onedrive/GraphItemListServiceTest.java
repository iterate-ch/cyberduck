package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphItemListServiceTest extends AbstractOneDriveTest {

    @Test
    public void testListLexicographically() throws Exception {
        final Path directory = new GraphDirectoryFeature(session, fileid).mkdir(new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path f2 = new GraphTouchFeature(session, fileid).touch(new Path(directory, "aa", EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path f1 = new GraphTouchFeature(session, fileid).touch(new Path(directory, "a", EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new GraphItemListService(session, fileid).list(directory, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertEquals(new SimplePathPredicate(f1), new SimplePathPredicate(list.get(0)));
        assertEquals(new SimplePathPredicate(f2), new SimplePathPredicate(list.get(1)));
        new GraphDeleteFeature(session, fileid).delete(Arrays.asList(f1, f2, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path directory = new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new GraphItemListService(session, fileid).list(directory, new DisabledListProgressListener());
    }

    @Test
    public void testListDriveChildren() throws Exception {
        final Path drive = new OneDriveHomeFinderService().find();
        final Path file = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GraphTouchFeature(session, fileid).touch(file, new TransferStatus());
        assertNotNull(new GraphAttributesFinderFeature(session, fileid).find(file));
        final AttributedList<Path> list = new GraphItemListService(session, fileid).list(drive, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(file));
        assertSame(drive, list.get(file).getParent());
        for(Path f : list) {
            assertSame(drive, f.getParent());
            assertEquals(drive.getName(), f.getParent().getName());
            final PathAttributes attributes = f.attributes();
            assertNotEquals(-1L, attributes.getSize());
            assertNotEquals(-1L, attributes.getCreationDate());
            assertNotEquals(-1L, attributes.getModificationDate());
            assertNotNull(attributes.getETag());
            assertNotNull(attributes.getFileId());
            assertNotNull(attributes.getLink());
        }
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWhitespacedChild() throws Exception {
        final RandomStringService randomStringService = new AlphanumericRandomStringService();
        final Path target = new GraphDirectoryFeature(session, fileid).mkdir(new Path(new OneDriveHomeFinderService().find(), String.format("%s %s", randomStringService.random(), randomStringService.random()), EnumSet.of(Path.Type.directory)), null);
        final AttributedList<Path> list = new GraphItemListService(session, fileid).list(target, new DisabledListProgressListener());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
