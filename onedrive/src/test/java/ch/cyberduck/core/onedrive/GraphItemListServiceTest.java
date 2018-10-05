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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.onedrive.features.OneDriveHomeFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class GraphItemListServiceTest extends AbstractOneDriveTest {

    @Test
    public void testListLexicographically() throws Exception {
        final Path directory = new GraphDirectoryFeature(session).mkdir(new Path(new OneDriveHomeFinderFeature(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path f2 = new GraphTouchFeature(session).touch(new Path(directory, "aa", EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path f1 = new GraphTouchFeature(session).touch(new Path(directory, "a", EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new GraphItemListService(session).list(directory, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertEquals(new SimplePathPredicate(f1), new SimplePathPredicate(list.get(0)));
        assertEquals(new SimplePathPredicate(f2), new SimplePathPredicate(list.get(1)));
        new GraphDeleteFeature(session).delete(Arrays.asList(f1, f2, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
