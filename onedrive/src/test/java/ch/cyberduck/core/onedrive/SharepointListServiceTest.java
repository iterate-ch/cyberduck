package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class SharepointListServiceTest extends AbstractSharepointTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path directory = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SharepointListService(session, new GraphFileIdProvider(session)).list(directory, new DisabledListProgressListener());
    }

    @Test
    public void testListRoot() throws Exception {
        final AttributedList<Path> list = new SharepointListService(session, new GraphFileIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
    }

    @Test
    public void testListDefault() throws Exception {
        new SharepointListService(session, new GraphFileIdProvider(session)).list(SharepointListService.DEFAULT_NAME, new DisabledListProgressListener());
    }

    @Test
    public void testListGroups() throws Exception {
        new SharepointListService(session, new GraphFileIdProvider(session)).list(SharepointListService.GROUPS_NAME, new DisabledListProgressListener());
    }
}
