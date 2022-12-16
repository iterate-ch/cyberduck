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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SharepointSessionTest {
    private SharepointSession session;

    @Before
    public void setup() {
        session = new SharepointSession(new Host(new SharepointProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    @Test
    public void isAccessible() {
        assertFalse(session.isAccessible(new Path("/", EnumSet.of(Path.Type.directory))));
        assertFalse(session.isAccessible(SharepointListService.DEFAULT_NAME));
        assertFalse(session.isAccessible(SharepointListService.DEFAULT_NAME, false));
        final Path defaultSiteDrive =
                new Path(
                        new Path(
                                SharepointListService.DEFAULT_NAME, "Drives", EnumSet.of(AbstractPath.Type.directory)),
                        "Drive-Id", EnumSet.of(Path.Type.directory));
        assertTrue(session.isAccessible(defaultSiteDrive));
        assertFalse(session.isAccessible(defaultSiteDrive, false));

        assertFalse(session.isAccessible(SharepointListService.SITES_NAME));
        assertFalse(session.isAccessible(SharepointListService.SITES_NAME, false));
        assertFalse(session.isAccessible(SharepointListService.GROUPS_NAME));
        assertFalse(session.isAccessible(SharepointListService.GROUPS_NAME, false));
        final Path siteDrive =
                new Path(
                        new Path(
                                new Path(SharepointListService.SITES_NAME, "Site", EnumSet.of(AbstractPath.Type.directory)),
                                "Drives", EnumSet.of(AbstractPath.Type.directory)),
                        "Drive-Id", EnumSet.of(Path.Type.directory));
        assertTrue(session.isAccessible(siteDrive));
        assertFalse(session.isAccessible(siteDrive, false));
        final Path group = new Path(SharepointListService.GROUPS_NAME, "Group Name", EnumSet.of(Path.Type.directory));
        assertFalse(session.isAccessible(group));
        assertFalse(session.isAccessible(group, false));
        assertTrue(session.isAccessible(new Path(group, "Drive-Id", EnumSet.of(Path.Type.directory))));
        assertFalse(session.isAccessible(new Path(group, "Drive-Id", EnumSet.of(Path.Type.directory)), false));
    }

    @Test
    public void testContainerEquality() {
        final Path source = new Path("/Default/Drives/Docs", EnumSet.of(Path.Type.directory))
                .withAttributes(new PathAttributes()
                        .withFileId("File Id"));
        final Path target = new Path("/Default/Drives/Docs", EnumSet.of(Path.Type.directory));
        final GraphSession.ContainerItem sourceItem = session.getContainer(source);
        final GraphSession.ContainerItem targetItem = session.getContainer(target);
        assertEquals(sourceItem, targetItem);
    }
}
