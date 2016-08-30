package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LaunchServicesFileDescriptorTest {

    @Test
    public void testGetKind() throws Exception {
        assertTrue(new LaunchServicesFileDescriptor().getKind("/tmp/t.txt").startsWith("Plain"));
    }

    @Test
    public void testGetKindWithoutExtension() throws Exception {
        assertTrue(new LaunchServicesFileDescriptor().getKind("txt").startsWith("Plain"));
    }
}
