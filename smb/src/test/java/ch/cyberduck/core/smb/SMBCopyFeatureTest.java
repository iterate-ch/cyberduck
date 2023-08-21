package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(TestcontainerTest.class)
public class SMBCopyFeatureTest extends AbstractSMBTest {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path file = new Path(home, "userTest.txt", EnumSet.of(Path.Type.file));
        final Path destinationFolder = new Path(home, "other_folder", EnumSet.of(Path.Type.directory));

        final Path copy = new Path(destinationFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SMBCopyFeature(session).copy(file, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());

        ListService list = new SMBListService(session);
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
        new SMBTouchFeature(session).touch(copy, new TransferStatus());

        new SMBCopyFeature(session).copy(file, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener());

        ListService list = new SMBListService(session);
        assertTrue(list.list(sourceFolder, null).contains(file));
        assertTrue(list.list(destinationFolder, null).contains(copy));
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path source = new Path(home, "empty_folder", EnumSet.of(Path.Type.directory));
        final Path destinationFolder = new Path(home, "other_folder", EnumSet.of(Path.Type.directory));
        final Path destination = new Path(destinationFolder, "new_empty_folder", EnumSet.of(Path.Type.directory));

        new SMBCopyFeature(session).copy(source, destination, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());

        Find find = new DefaultFindFeature(session);
        assertTrue(find.find(source));
        assertTrue(find.find(destinationFolder));
        final Path newLocation = new Path(destinationFolder, "new_empty_folder", EnumSet.of(Path.Type.directory));
        assertTrue(find.find(newLocation));
    }
}
