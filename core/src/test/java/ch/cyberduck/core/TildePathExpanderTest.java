package ch.cyberduck.core;

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

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TildePathExpanderTest {

    @Test
    public void testExpand() {
        final String expanded = new TildePathExpander(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)))
                .expand("~/f", "~");
        assertEquals("/home/jenkins/f", expanded);
    }

    @Test
    public void testExpandPathWithDirectory() {
        final String expanded = new TildePathExpander(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)))
                .expand("/~/f/s");
        assertEquals("/home/jenkins/f/s", expanded);
    }

    @Test
    public void testNoExpand() {
        final String f = "/f";
        assertSame(f, new TildePathExpander(new Path("/home/jenkins", EnumSet.of(Path.Type.directory))).expand(f));
    }
}