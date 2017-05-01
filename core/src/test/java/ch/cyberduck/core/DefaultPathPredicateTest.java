package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class DefaultPathPredicateTest {

    @Test
    public void testUnique() throws Exception {
        final Path t = new Path("/", EnumSet.of(Path.Type.directory));
        assertEquals("[directory]-/", new DefaultPathPredicate(t).toString());
        t.attributes().setVersionId("1");
        assertEquals("[directory]-1/", new DefaultPathPredicate(t).toString());
        t.attributes().setRegion("r");
        assertEquals("[directory]-1/", new DefaultPathPredicate(t).toString());
        t.attributes().setVersionId(null);
        assertEquals("[directory]-/", new DefaultPathPredicate(t).toString());
    }

    @Test
    public void testUniqueSymbolicLInk() throws Exception {
        final Path t = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.symboliclink));
        assertEquals("[directory, symboliclink]-/", new DefaultPathPredicate(t).toString());
    }

    @Test
    public void testtoStringContainer() throws Exception {
        final Path t = new Path("/container", EnumSet.of(Path.Type.directory));
        assertEquals("[directory]-/container", new DefaultPathPredicate(t).toString());
        t.attributes().setVersionId("1");
        assertEquals("[directory]-1/container", new DefaultPathPredicate(t).toString());
        t.attributes().setRegion("r");
        assertEquals("[directory]-r1/container", new DefaultPathPredicate(t).toString());
        t.attributes().setVersionId(null);
        assertEquals("[directory]-r/container", new DefaultPathPredicate(t).toString());
    }

    @Test
    public void testAttributes() throws Exception {
        final Path t = new Path("/f", EnumSet.of(Path.Type.file));
        t.attributes().setRegion("r");
        assertEquals("r", new DefaultPathPredicate(t).attributes());
        t.attributes().setVersionId("1");
        assertEquals("r1", new DefaultPathPredicate(t).attributes());
    }

    @Test
    public void testIgnoreVolumeFlag() throws Exception {
        assertEquals(new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))),
                new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory))));
        assertEquals(new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))),
                new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testIgnorePlaceholderFlag() throws Exception {
        assertEquals(new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory, Path.Type.placeholder))),
                new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory))));
        assertEquals(new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory))),
                new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory))));
    }
}
