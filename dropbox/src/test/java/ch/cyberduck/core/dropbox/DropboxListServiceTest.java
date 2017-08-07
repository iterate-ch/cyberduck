package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxListServiceTest extends AbstractDropboxTest {

    @Test
    public void testList() throws Exception {

        final AttributedList<Path> list = new DropboxListService(session).list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testFilenameColon() throws Exception {

        final Path file = new Path(new DefaultHomeFinderService(session).find(), String.format("%s:name", UUID.randomUUID().toString()), EnumSet.of(Path.Type.file));
        final Path folder = new Path(new DefaultHomeFinderService(session).find(), String.format("%s:name", UUID.randomUUID().toString()), EnumSet.of(Path.Type.directory));
        new DropboxTouchFeature(session).touch(file, new TransferStatus());
        new DropboxDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        final AttributedList<Path> list = new DropboxListService(session).list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(file));
        assertTrue(list.contains(folder));
        new DropboxDeleteFeature(session).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}