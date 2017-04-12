package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class OneDriveDirectoryFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testMkdir() throws Exception {
        final OneDriveListService listService = new OneDriveListService(session);
        final OneDriveAttributesFinderFeature attributesFinderFeature = new OneDriveAttributesFinderFeature(session);
        final OneDriveDirectoryFeature directoryFeature = new OneDriveDirectoryFeature(session);
        final OneDriveDeleteFeature deleteFeature = new OneDriveDeleteFeature(session);
        final AttributedList<Path> list = listService.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            final Path target = directoryFeature.mkdir(new Path(f, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, null);
            assertNotNull(attributesFinderFeature.find(target).getETag());
            deleteFeature.delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }
}
