package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SMBCopyFeatureTest extends AbstractSMBTest {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path file = new Path(home, "userTest.txt", EnumSet.of(Path.Type.file));
        final Path destinationFolder = new Path(home, "other_folder", EnumSet.of(Path.Type.directory));

        final Path copy = new Path(destinationFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.getFeature(Copy.class).copy(file, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());

        ListService list = session.getFeature(ListService.class);
        assertTrue(list.list(home, null).contains(file));
        assertTrue(list.list(destinationFolder, null).contains(copy));
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path sourceFolder = new Path(home, "folder", EnumSet.of(Path.Type.directory));
        final Path destinationFolder = new Path(home, "other_folder", EnumSet.of(Path.Type.directory));
        final Path file = new Path(home, "folder/L0-file.txt", EnumSet.of(Path.Type.file));
        final Path copy = new Path(destinationFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(copy, new TransferStatus());

        session.getFeature(Copy.class).copy(file, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener());

        ListService list = session.getFeature(ListService.class);
        assertTrue(list.list(sourceFolder, null).contains(file));
        assertTrue(list.list(destinationFolder, null).contains(copy));
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new Path(home, "folder", EnumSet.of(Path.Type.directory));
        final Path original = new Path(folder, "L1", EnumSet.of(Path.Type.directory));
        final Path destinationFolder = new Path(home, "other_folder", EnumSet.of(Path.Type.directory));
        final Path copy = new Path(destinationFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));

        session.getFeature(Copy.class).copy(original, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());

        Find find = new DefaultFindFeature(session);
        assertTrue(find.find(original));
        assertTrue(find.find(copy));
        final Path l1FileCopy = new Path(copy, "L1-file.txt", EnumSet.of(Path.Type.file));
        final Path l2DirectoryCopy = new Path(copy, "L2", EnumSet.of(Path.Type.directory));
        final Path l2FileCopy = new Path(l2DirectoryCopy, "L2-file.txt", EnumSet.of(Path.Type.file));
        final Path l3DirectoryCopy = new Path(l2DirectoryCopy, "L3", EnumSet.of(Path.Type.directory));
        final Path l3FileCopy = new Path(l3DirectoryCopy, "L3-file.txt", EnumSet.of(Path.Type.file));
        assertTrue(find.find(l1FileCopy));
        assertTrue(find.find(l2DirectoryCopy));
        assertTrue(find.find(l2FileCopy));
        assertTrue(find.find(l3DirectoryCopy));
        assertTrue(find.find(l3FileCopy));
    }
}
