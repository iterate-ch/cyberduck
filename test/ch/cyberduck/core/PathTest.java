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
import org.junit.Test;

import static org.junit.Assert.*;

public class PathTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        NSObjectPathReference.register();
    }

    @Test
    public void testDictionary() {
        Path path = new Path("/path", Path.DIRECTORY_TYPE);
        assertEquals(path, new Path(path.serialize(SerializerFactory.get())));
    }

    @Test
    public void testDictionaryRegion() {
        Path path = new Path("/path/f", Path.FILE_TYPE);
        path.attributes().setRegion("r");
        final Path deserialized = new Path(path.serialize(SerializerFactory.get()));
        assertEquals(path, deserialized);
        assertEquals("r", deserialized.attributes().getRegion());
        assertEquals("r", deserialized.getParent().attributes().getRegion());
    }

    @Test
    public void testPopulateRegion() {
        {
            Path container = new Path("test", Path.DIRECTORY_TYPE);
            container.attributes().setRegion("DFW");
            Path path = new Path(container, "f", Path.FILE_TYPE);
            assertEquals("DFW", path.attributes().getRegion());
        }
        {
            Path container = new Path("test", Path.DIRECTORY_TYPE);
            container.attributes().setRegion("DFW");
            Path path = new Path(container, new NullLocal("/", "f"));
            assertEquals("DFW", path.attributes().getRegion());
        }
    }

    @Test
    public void testDictionaryRegionParentOnly() {
        Path path = new Path("/root/path", Path.FILE_TYPE);
        path.getParent().attributes().setRegion("r");
        assertEquals(path, new Path(path.serialize(SerializerFactory.get())));
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
    public void testSymlink() {
        Path p = new Path("t", Path.FILE_TYPE);
        assertFalse(p.attributes().isSymbolicLink());
        assertNull(p.getSymlinkTarget());
        p.attributes().setType(Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        assertTrue(p.attributes().isSymbolicLink());
        p.setSymlinkTarget(new Path("s", Path.FILE_TYPE));
        assertEquals("/s", p.getSymlinkTarget().getAbsolute());
    }

    @Test
    public void testIsChild() {
        Path p = new Path("/a/t", Path.FILE_TYPE);
        assertTrue(p.isChild(new Path("/a", Path.DIRECTORY_TYPE)));
        assertTrue(p.isChild(new Path("/", Path.DIRECTORY_TYPE)));
        assertFalse(p.isChild(new Path("/a", Path.FILE_TYPE)));
        final Path d = new Path("/a", Path.DIRECTORY_TYPE);
        d.attributes().setVersionId("1");
        d.attributes().setDuplicate(true);
        assertFalse(p.isChild(d));
    }

    @Test
    public void testGetParent() {
        assertEquals(new Path("/b/t", Path.DIRECTORY_TYPE), new Path("/b/t/f.type", Path.FILE_TYPE).getParent());
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

    @Test
    public void testCreateRelative() throws Exception {
        final Path path = new Path(".CDN_ACCESS_LOGS", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE, path.attributes().getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(new PathContainerService().isContainer(path));
        assertFalse(path.isRoot());
    }

    @Test
    public void testCreateAbsolute() throws Exception {
        final Path path = new Path("/.CDN_ACCESS_LOGS", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE, path.attributes().getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(new PathContainerService().isContainer(path));
        assertFalse(path.isRoot());
    }

    @Test
    public void testPathContainer() throws Exception {
        final Path path = new Path(new Path("test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE), "/test", Path.DIRECTORY_TYPE);
        assertEquals("/test.cyberduck.ch/test", path.getAbsolute());
    }
}
