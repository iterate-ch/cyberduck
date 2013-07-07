package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class PathTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testDictionary() {
        final Session s = SessionFactory.createSession(new Host("localhost"));
        Path path = new Path("/path", Path.DIRECTORY_TYPE);
        assertEquals(path, new Path(path.getAsDictionary()));
    }

    @Test
    public void testNormalize() throws Exception {
        {
            final Path path = new Path(
                    "/path/to/remove/..", Path.DIRECTORY_TYPE);
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/to/remove/.././", Path.DIRECTORY_TYPE);
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/remove/../to/remove/.././", Path.DIRECTORY_TYPE);
            assertEquals("/path/to", path.getAbsolute());
        }
//        path.setPath("../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
//        path.setPath("/../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
        {
            final Path path = new Path(
                    "/path/to/remove/remove/../../", Path.DIRECTORY_TYPE);
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/././././to", Path.DIRECTORY_TYPE);
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "./.path/to", Path.DIRECTORY_TYPE);
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    ".path/to", Path.DIRECTORY_TYPE);
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/.to", Path.DIRECTORY_TYPE);
            assertEquals("/path/.to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path//to", Path.DIRECTORY_TYPE);
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path///to////", Path.DIRECTORY_TYPE);
            assertEquals("/path/to", path.getAbsolute());
        }
    }

    @Test
    public void testName() throws Exception {
        {
            Path path = new Path(
                    "/path/to/file/", Path.DIRECTORY_TYPE);
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
        {
            Path path = new Path(
                    "/path/to/file", Path.DIRECTORY_TYPE);
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
    }

    @Test
    @Ignore
    public void test1067() throws Exception {
        Path path = new Path(
                "\\\\directory", Path.DIRECTORY_TYPE);
        assertEquals("\\\\directory", path.getAbsolute());
        assertEquals("/", path.getParent().getAbsolute());
    }

    @Test
    public void testSymlink() {
        Path p = new NullPath("t", Path.FILE_TYPE);
        assertFalse(p.attributes().isSymbolicLink());
        assertNull(p.getSymlinkTarget());
        p.attributes().setType(Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        assertTrue(p.attributes().isSymbolicLink());
        p.setSymlinkTarget(new NullPath("s", Path.FILE_TYPE));
        assertEquals("/s", p.getSymlinkTarget().getAbsolute());
    }

    @Test
    public void testIsChild() {
        Path p = new NullPath("/a/t", Path.FILE_TYPE);
        assertTrue(p.isChild(new NullPath("/a", Path.DIRECTORY_TYPE)));
        assertTrue(p.isChild(new NullPath("/", Path.DIRECTORY_TYPE)));
        assertFalse(p.isChild(new NullPath("/a", Path.FILE_TYPE)));
        final NullPath d = new NullPath("/a", Path.DIRECTORY_TYPE);
        d.attributes().setVersionId("1");
        d.attributes().setDuplicate(true);
        assertFalse(p.isChild(d));
    }

    @Test
    public void testGetParent() {
        assertEquals(new NullPath("/b/t", Path.DIRECTORY_TYPE), new NullPath("/b/t/f.type", Path.FILE_TYPE).getParent());
    }

    @Test
    public void testCreatePath() throws Exception {
        for(Protocol p : ProtocolFactory.getKnownProtocols()) {
            final Path path = new Path("p", Path.FILE_TYPE);
            assertNotNull(path);
            assertEquals("/p", path.getAbsolute());
            assertEquals("/", path.getParent().getAbsolute());
        }
    }
}
