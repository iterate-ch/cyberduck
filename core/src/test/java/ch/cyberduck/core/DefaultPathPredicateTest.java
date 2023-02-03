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

import static org.junit.Assert.*;

public class DefaultPathPredicateTest {

    @Test
    public void testUnique() {
        final Path t_noregion = new Path("/", EnumSet.of(Path.Type.directory));
        assertEquals("[directory]-/", new DefaultPathPredicate(t_noregion).toString());
        final Path t_region = new Path("/", EnumSet.of(Path.Type.directory));
        t_region.attributes().setRegion("r");
        assertEquals("[directory]-/", new DefaultPathPredicate(t_region).toString());
        assertEquals(new DefaultPathPredicate(t_noregion), new DefaultPathPredicate(t_region));
    }

    @Test
    public void testUniqueSymbolicLInk() {
        final Path s = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.symboliclink));
        final Path t = new Path("/", EnumSet.of(Path.Type.directory));
        assertNotEquals(new DefaultPathPredicate(s), new DefaultPathPredicate(t));
    }

    @Test
    public void testtoStringContainer() {
        final Path t_noregion = new Path("/container", EnumSet.of(Path.Type.directory));
        final Path t_region = new Path("/container", EnumSet.of(Path.Type.directory));
        t_region.attributes().setRegion("r");
        assertNotEquals(new DefaultPathPredicate(t_noregion), new DefaultPathPredicate(t_region));
    }

    @Test
    public void testIgnoreVolumeFlag() {
        assertEquals(new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))),
            new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory))));
        assertEquals(new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))),
            new DefaultPathPredicate(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }

    @Test
    public void testIgnorePlaceholderFlag() {
        assertEquals(new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory, Path.Type.placeholder))),
            new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory))));
        assertEquals(new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory))),
            new DefaultPathPredicate(new Path("/container/p", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testPredicateTest() {
        final Path t = new Path("/f", EnumSet.of(Path.Type.file));
        assertTrue(new DefaultPathPredicate(t).test(t));
        assertFalse(new DefaultPathPredicate(t).test(new Path("/f/a", EnumSet.of(Path.Type.file))));
        assertFalse(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testPredicateVersionIdFile() {
        final Path t = new Path("/f", EnumSet.of(Path.Type.file), new PathAttributes().withVersionId("1"));
        assertTrue(new DefaultPathPredicate(t).test(t));
        assertTrue(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.file), new PathAttributes().withVersionId("1"))));
        assertFalse(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.file), new PathAttributes().withVersionId("2"))));
    }

    @Test
    public void testPredicateFileIdFile() {
        final Path t = new Path("/f", EnumSet.of(Path.Type.file), new PathAttributes().withFileId("1"));
        assertTrue(new DefaultPathPredicate(t).test(t));
        assertTrue(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.file), new PathAttributes().withFileId("1"))));
        assertFalse(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.file), new PathAttributes().withFileId("2"))));
    }

    @Test
    public void testPredicateVersionIdDirectory() {
        final Path t = new Path("/f", EnumSet.of(Path.Type.directory), new PathAttributes().withVersionId("1"));
        assertTrue(new DefaultPathPredicate(t).test(t));
        assertTrue(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.directory), new PathAttributes().withVersionId("1"))));
        assertTrue(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.directory), new PathAttributes().withVersionId("2"))));
    }

    @Test
    public void testPredicateFileIdDirectory() {
        final Path t = new Path("/f", EnumSet.of(Path.Type.directory), new PathAttributes().withFileId("1"));
        assertTrue(new DefaultPathPredicate(t).test(t));
        assertTrue(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.directory), new PathAttributes().withFileId("1"))));
        assertFalse(new DefaultPathPredicate(t).test(new Path("/f", EnumSet.of(Path.Type.directory), new PathAttributes().withFileId("2"))));
    }

    @Test
    public void testHashcodeCollision() {
        assertNotEquals(
                new DefaultPathPredicate(
                        new Path("19", EnumSet.of(Path.Type.file))
                ),
                new DefaultPathPredicate(
                        new Path("0X", EnumSet.of(Path.Type.file))
                )
        );
        assertFalse(new DefaultPathPredicate(
            new Path("19", EnumSet.of(Path.Type.file))
        ).test(
            new Path("0X", EnumSet.of(Path.Type.file))
        ));
    }
}
