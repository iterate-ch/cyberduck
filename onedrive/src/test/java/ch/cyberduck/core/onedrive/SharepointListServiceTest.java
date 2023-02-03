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
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.PathAttributesHomeFeature;
import ch.cyberduck.core.shared.RootPathContainerService;
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
        new SharepointListService(session, fileid).list(directory, new DisabledListProgressListener());
    }

    @Test
    public void testListRoot() throws Exception {
        final AttributedList<Path> list = new SharepointListService(session, fileid).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertEquals(3, list.size());
    }

    @Test
    public void testListDefault() throws Exception {
        new SharepointListService(session, fileid).list(SharepointListService.DEFAULT_NAME, new DisabledListProgressListener());
    }

    @Test
    public void testListDefaultDriveOverwrite() throws Exception {
        final ListService list = new SharepointListService(session, fileid);
        final AttributedList<Path> drives = list.list(new Path(SharepointListService.DEFAULT_NAME, "Drives", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        final Path drive = drives.get(0);
        new PathAttributesHomeFeature(session, () -> drive, new GraphAttributesFinderFeature(session, fileid), new RootPathContainerService()).find();
        list.list(drive, new DisabledListProgressListener());
    }

    @Test
    public void testListGroups() throws Exception {
        new SharepointListService(session, fileid).list(SharepointListService.GROUPS_NAME, new DisabledListProgressListener());
    }
}
