package ch.cyberduck.core.box;

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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class BoxDeleteFeatureTest extends AbstractBoxTest {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteFolder() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path directory = new BoxDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        assertTrue(new BoxFindFeature(session, fileid).find(directory, new DisabledListProgressListener()));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new BoxFindFeature(session, fileid).find(directory, new DisabledListProgressListener())));
    }

    @Test
    public void testDeleteMultipleFiles() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path file1 = new BoxTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path file2 = new BoxTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new BoxFindFeature(session, fileid).find(file1));
        assertTrue(new BoxFindFeature(session, fileid).find(file2));
        new BoxDeleteFeature(session, fileid).delete(Arrays.asList(file1, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new BoxFindFeature(session, fileid).find(file1, new DisabledListProgressListener())));
        assertFalse((new BoxFindFeature(session, fileid).find(file2, new DisabledListProgressListener())));
        assertTrue(new BoxFindFeature(session, fileid).find(folder, new DisabledListProgressListener()));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new BoxFindFeature(session, fileid).find(folder, new DisabledListProgressListener())));
    }
}