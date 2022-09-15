package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphLockFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class GraphLockFeatureTest extends AbstractSharepointTest {

    @Test
    public void testLock() throws Exception {
        final ListService list = new SharepointListService(session, fileid);
        final AttributedList<Path> drives = list.list(new Path(SharepointListService.DEFAULT_NAME, "Drives", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        final Path drive = drives.get(0);
        final Path file = new GraphTouchFeature(session, fileid).touch(new Path(drive,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final long modified = new GraphAttributesFinderFeature(session, fileid).find(file).getModificationDate();
        final GraphLockFeature feature = new GraphLockFeature(session, fileid);
        final String token = feature.lock(file);
        assertEquals(modified, new GraphAttributesFinderFeature(session, fileid).find(file).getModificationDate());
        feature.unlock(file, token);
        assertEquals(modified, new GraphAttributesFinderFeature(session, fileid).find(file).getModificationDate());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}