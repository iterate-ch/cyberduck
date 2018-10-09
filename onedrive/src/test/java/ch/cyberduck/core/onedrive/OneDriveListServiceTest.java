package ch.cyberduck.core.onedrive;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.onedrive.features.OneDriveHomeFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

@Category(IntegrationTest.class)
public class OneDriveListServiceTest extends AbstractOneDriveTest {

    @Test
    public void testListDrives() throws Exception {
        final AttributedList<Path> list = new OneDriveListService(session, new GraphFileIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), f.getParent());
        }
        assertTrue(list.contains(new OneDriveHomeFinderFeature(session).find()));
    }

    @Test
    public void testListDriveChildren() throws Exception {
        final Path file = new Path(new OneDriveHomeFinderFeature(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GraphTouchFeature(session).touch(file, new TransferStatus());
        assertNotNull(new GraphAttributesFinderFeature(session).find(file, new DisabledListProgressListener()));
        final OneDriveListService listService = new OneDriveListService(session, new GraphFileIdProvider(session));
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final AttributedList<Path> list = listService.list(drive, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertEquals(drive.getName(), f.getParent().getName());
            final PathAttributes attributes = f.attributes();
            assertNotEquals(-1L, attributes.getSize());
            assertNotEquals(-1L, attributes.getCreationDate());
            assertNotEquals(-1L, attributes.getModificationDate());
            assertNotNull(attributes.getETag());
            assertNotNull(attributes.getVersionId());
            assertNotNull(attributes.getLink());
        }
        new GraphDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWhitespacedChild() throws Exception {
        final RandomStringService randomStringService = new AlphanumericRandomStringService();
        final Path target = new GraphDirectoryFeature(session).mkdir(new Path(new OneDriveHomeFinderFeature(session).find(), String.format("%s %s", randomStringService.random(), randomStringService.random()), EnumSet.of(Path.Type.directory)), null, null);
        final AttributedList<Path> list = new OneDriveListService(session, new GraphFileIdProvider(session)).list(target, new DisabledListProgressListener());
        new GraphDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
