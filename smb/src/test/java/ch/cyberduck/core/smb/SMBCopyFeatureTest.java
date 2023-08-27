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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(TestcontainerTest.class)
public class SMBCopyFeatureTest extends AbstractSMBTest {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path file = new SMBTouchFeature(session).touch(new Path(home,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path destinationFolder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path copy = new Path(destinationFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SMBCopyFeature(session).copy(file, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        ListService list = new SMBListService(session);
        assertTrue(list.list(home, null).contains(file));
        assertTrue(list.list(destinationFolder, null).contains(copy));
        new SMBDeleteFeature(session).delete(Arrays.asList(file, copy, destinationFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path sourceFolder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path destinationFolder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new SMBTouchFeature(session).touch(new Path(sourceFolder,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path copy = new Path(destinationFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SMBTouchFeature(session).touch(copy, new TransferStatus());
        new SMBCopyFeature(session).copy(file, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener());
        ListService list = new SMBListService(session);
        assertTrue(list.list(sourceFolder, null).contains(file));
        assertTrue(list.list(destinationFolder, null).contains(copy));
        new SMBDeleteFeature(session).delete(Arrays.asList(file, sourceFolder, copy, destinationFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
