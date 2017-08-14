package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

@Category(IntegrationTest.class)
public class MantaListServiceIT extends AbstractMantaTest {

    @Test
    public void testListBuckets() throws Exception {
        final AttributedList<Path> list = new MantaListService(session)
                .list(session.getAccountRoot(), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertSame(session.getAccountRoot(), f.getParent());
            assertEquals(session.getAccountRoot().getName(), f.getParent().getName());
        }
    }
}
