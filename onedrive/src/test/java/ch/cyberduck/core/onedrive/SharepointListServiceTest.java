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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.onedrive.features.SharepointFileIdProvider;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class SharepointListServiceTest extends AbstractSharepointTest {
    @Test
    public void testListRoot() throws Exception {
        final AttributedList<Path> list = new SharepointListService(session, new SharepointFileIdProvider(session)).list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertEquals(2, list.size());
    }

    @Test
    public void testListDefault() throws Exception {
    }

    @Test
    public void testListGroups() throws Exception {
        final AttributedList<Path> list = new SharepointGroupListService(session).list(SharepointSession.GROUPS_NAME, new DisabledListProgressListener());
    }

    @Test
    public void testListGroup() throws Exception {
        final AttributedList<Path> list = new SharepointGroupDrivesListService(session)
            .list(new Path(
                SharepointSession.GROUPS_NAME, "bbe48dd5-3952-4940-9989-919042b8924c",
                EnumSet.of(Path.Type.directory), new PathAttributes().withVersionId("bbe48dd5-3952-4940-9989-919042b8924c")), new DisabledListProgressListener());
    }
}
