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

import ch.cyberduck.core.serializer.PathDictionary;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class PathTest {

    @Test
    public void testDictionaryDirectory() {
        Path path = new Path("/path", EnumSet.of(Path.Type.directory));
        assertEquals(path, new PathDictionary().deserialize(path.serialize(SerializerFactory.get())));
    }

    @Test
    public void testDictionaryFile() {
        Path path = new Path("/path", EnumSet.of(Path.Type.file));
        assertEquals(path, new PathDictionary().deserialize((path.serialize(SerializerFactory.get()))));
    }

    @Test
    public void testDictionaryFileSymbolicLink() {
        Path path = new Path("/path", EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        assertEquals(path, new PathDictionary().deserialize(path.serialize(SerializerFactory.get())));
        assertEquals(EnumSet.of(Path.Type.file, Path.Type.symboliclink),
                new PathDictionary().deserialize(path.serialize(SerializerFactory.get())).getType());
    }

    @Test
    public void testDictionaryRegion() {
        Path path = new Path("/path/f", EnumSet.of(Path.Type.file));
        path.attributes().setRegion("r");
        final Path deserialized = new PathDictionary().deserialize(path.serialize(SerializerFactory.get()));
        assertEquals(path, deserialized);
        assertEquals("r", deserialized.attributes().getRegion());
        assertEquals("r", deserialized.getParent().attributes().getRegion());
    }

    @Test
    public void testPopulateRegion() {
        {
            Path container = new Path("test", EnumSet.of(Path.Type.directory));
            container.attributes().setRegion("DFW");
            Path path = new Path(container, "f", EnumSet.of(Path.Type.file));
            assertEquals("DFW", path.attributes().getRegion());
        }
        {
            Path container = new Path("test", EnumSet.of(Path.Type.directory));
            container.attributes().setRegion("DFW");
            assertEquals("DFW", container.attributes().getRegion());
        }
    }

    @Test
    public void testDictionaryRegionParentOnly() {
        Path path = new Path("/root/path", EnumSet.of(Path.Type.file));
        path.getParent().attributes().setRegion("r");
        assertEquals(path, new PathDictionary().deserialize(path.serialize(SerializerFactory.get())));
    }

    @Test
    public void testNormalize() throws Exception {
        {
            final Path path = new Path(
                    "/path/to/remove/..", EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/to/remove/.././", EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/remove/../to/remove/.././", EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/to/remove/remove/../../", EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/././././to", EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "./.path/to", EnumSet.of(Path.Type.directory));
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    ".path/to", EnumSet.of(Path.Type.directory));
            assertEquals("/.path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path/.to", EnumSet.of(Path.Type.directory));
            assertEquals("/path/.to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path//to", EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
        {
            final Path path = new Path(
                    "/path///to////", EnumSet.of(Path.Type.directory));
            assertEquals("/path/to", path.getAbsolute());
        }
    }

    @Test
    public void testName() throws Exception {
        {
            Path path = new Path(
                    "/path/to/file/", EnumSet.of(Path.Type.directory));
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
        {
            Path path = new Path(
                    "/path/to/file", EnumSet.of(Path.Type.directory));
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
    }

    @Test
    public void testSymlink() {
        Path p = new Path("t", EnumSet.of(Path.Type.file));
        assertFalse(p.isSymbolicLink());
        assertNull(p.getSymlinkTarget());
        p.setType(EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        assertTrue(p.isSymbolicLink());
        p.setSymlinkTarget(new Path("s", EnumSet.of(Path.Type.file)));
        assertEquals("/s", p.getSymlinkTarget().getAbsolute());
    }

    @Test
    public void testIsChild() {
        Path p = new Path("/a/t", EnumSet.of(Path.Type.file));
        assertTrue(p.isChild(new Path("/a", EnumSet.of(Path.Type.directory))));
        assertTrue(p.isChild(new Path("/", EnumSet.of(Path.Type.directory))));
        assertFalse(p.isChild(new Path("/a", EnumSet.of(Path.Type.file))));
        final Path d = new Path("/a", EnumSet.of(Path.Type.directory));
        d.attributes().setVersionId("1");
        d.attributes().setDuplicate(true);
        assertFalse(p.isChild(d));
    }

    @Test
    public void testGetParent() {
        assertEquals(new Path("/b/t", EnumSet.of(Path.Type.directory)), new Path("/b/t/f.type", EnumSet.of(Path.Type.file)).getParent());
    }

    @Test
    public void testCreateRelative() throws Exception {
        final Path path = new Path(".CDN_ACCESS_LOGS", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(EnumSet.of(Path.Type.volume, Path.Type.directory), path.getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(new PathContainerService().isContainer(path));
        assertFalse(path.isRoot());
    }

    @Test
    public void testCreateAbsolute() throws Exception {
        final Path path = new Path("/.CDN_ACCESS_LOGS", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(EnumSet.of(Path.Type.volume, Path.Type.directory), path.getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(new PathContainerService().isContainer(path));
        assertFalse(path.isRoot());
    }

    @Test
    public void testPathContainer() throws Exception {
        final Path path = new Path(new Path("test.cyberduck.ch",
                EnumSet.of(Path.Type.volume, Path.Type.directory)), "/test", EnumSet.of(Path.Type.directory));
        assertEquals("/test.cyberduck.ch/test", path.getAbsolute());
    }

    @Test
    public void testSetGetType() throws Exception {
        Path attributes = new Path("/", EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        assertTrue(attributes.isFile());
        assertTrue(attributes.isSymbolicLink());
        assertFalse(attributes.isDirectory());
        attributes.setType(EnumSet.of(Path.Type.directory, Path.Type.symboliclink));
        assertFalse(attributes.isFile());
        assertTrue(attributes.isSymbolicLink());
        assertTrue(attributes.isDirectory());
    }
}
