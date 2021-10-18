package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.AbstractPath.Type.directory;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudListServiceTest extends AbstractGmxcloudTest {

    @Test
    public void testListRoot() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path folder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(directory)), new TransferStatus());
        final AttributedList<Path> list = new GmxcloudListService(session, fileid).list(folder.getParent(), new DisabledListProgressListener());
        assertTrue(list.contains(folder));
        assertTrue(list.contains(new Path("Gelöschte Dateien", EnumSet.of(directory)).withAttributes(new PathAttributes().withFileId("TRASH"))));
        assertEquals(folder.attributes(), list.get(folder).attributes());
        new GmxcloudDeleteFeature(session, fileid, false).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListContainingFolder() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path folder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(directory)), new TransferStatus());
        assertTrue(new GmxcloudListService(session, fileid).list(folder, new DisabledListProgressListener()).isEmpty());
        final Path subfolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(directory)), new TransferStatus());
        assertTrue(new GmxcloudListService(session, fileid).list(subfolder, new DisabledListProgressListener()).isEmpty());
        final AttributedList<Path> list = new GmxcloudListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(subfolder));
        assertEquals(subfolder.attributes(), list.get(subfolder).attributes());
        new GmxcloudDeleteFeature(session, fileid, false).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new GmxcloudFindFeature(session, fileid).find(folder, new DisabledListProgressListener())));
        assertFalse((new GmxcloudFindFeature(session, fileid).find(subfolder, new DisabledListProgressListener())));
    }

    @Test
    public void testListContainingFile() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path folder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(directory)), new TransferStatus());
        assertTrue(new GmxcloudListService(session, fileid).list(folder, new DisabledListProgressListener()).isEmpty());
        final Path file = new GmxcloudTouchFeature(session, fileid)
                .touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        final AttributedList<Path> list = new GmxcloudListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(file));
        assertEquals(file.attributes(), list.get(file).attributes());
        new GmxcloudDeleteFeature(session, fileid, false).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new GmxcloudFindFeature(session, fileid).find(folder, new DisabledListProgressListener())));
        assertFalse((new GmxcloudFindFeature(session, fileid).find(file, new DisabledListProgressListener())));
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        new GmxcloudListService(session, fileid).list(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(directory)), new DisabledListProgressListener());
    }
}
