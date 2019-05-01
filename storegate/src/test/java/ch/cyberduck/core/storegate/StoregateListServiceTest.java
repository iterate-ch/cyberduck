package ch.cyberduck.core.storegate;

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

import static ch.cyberduck.core.AbstractPath.Type.directory;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class StoregateListServiceTest extends AbstractStoregateTest {

    @Test
    public void testList() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session).withCache(cache);
        final AttributedList<Path> list = new StoregateListService(session, nodeid).list(
            new Path("/Home/mduck", EnumSet.of(directory, Path.Type.volume)), new DisabledListProgressListener());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }
}
