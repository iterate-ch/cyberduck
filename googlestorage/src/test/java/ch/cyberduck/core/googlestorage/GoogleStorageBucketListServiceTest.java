package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class GoogleStorageBucketListServiceTest extends AbstractGoogleStorageTest {

    @Test
    public void testListContainers() throws Exception {
        final Path container = new Path("/", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> list = new GoogleStorageBucketListService(session).list(container, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path bucket : list) {
            assertEquals(bucket.attributes(), new GoogleStorageAttributesFinderFeature(session).find(bucket, new DisabledListProgressListener()));
        }
    }
}
