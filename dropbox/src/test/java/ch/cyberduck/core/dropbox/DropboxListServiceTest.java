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
import ch.cyberduck.core.AlphanumericRandomStringService;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxListServiceTest extends AbstractDropboxTest {

    @Test
    public void testListHome() throws Exception {
        final AttributedList<Path> list = new DropboxListService(session).list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        for(Path f : list) {
            assertEquals(f.attributes(), new DropboxAttributesFinderFeature(session).find(f));
        }
    }

    @Test
    public void testFilenameColon() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path file = new DropboxTouchFeature(session).touch(new Path(home, String.format("%s:name", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path folder = new DropboxDirectoryFeature(session).mkdir(new Path(home, String.format("%s:name", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final AttributedList<Path> list = new DropboxListService(session).list(home, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(file));
        assertSame(home, list.get(file).getParent());
        assertTrue(list.contains(folder));
        assertSame(home, list.get(folder).getParent());
        new DropboxDeleteFeature(session).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
