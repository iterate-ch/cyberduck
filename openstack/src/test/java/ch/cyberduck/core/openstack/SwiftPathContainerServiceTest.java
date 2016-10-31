package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@Category(IntegrationTest.class)
public class SwiftPathContainerServiceTest {

    @Test
    public void testLookup() throws Exception {
        final Path c = new Path("/container", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertEquals(c, new SwiftPathContainerService().getContainer(c));
        assertSame(c, new SwiftPathContainerService().getContainer(c));
    }
}