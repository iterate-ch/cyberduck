package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@Category(IntegrationTest.class)
public class DeepboxListServiceWithRoleTest extends AbstractDeepboxTest {

    @Before
    public void setup() throws Exception {
        setup("deepbox.deepboxapp3.user");
    }

    @Test
    public void testListTrashInboxHidden() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path box = new Path("/ORG 1 - DeepBox Desktop App/Box2", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(box, new DisabledListProgressListener());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/ORG 1 - DeepBox Desktop App/Box2/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNull(list.find(new SimplePathPredicate(new Path("/ORG 1 - DeepBox Desktop App/Box2/Inbox", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNull(list.find(new SimplePathPredicate(new Path("/ORG 1 - DeepBox Desktop App/Box2/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
    }
}