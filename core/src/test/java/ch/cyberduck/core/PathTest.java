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
        assertEquals(path, new PathDictionary<>().deserialize(path.serialize(SerializerFactory.get())));
    }

    @Test
    public void testDictionaryFile() {
        Path path = new Path("/path", EnumSet.of(Path.Type.file));
        assertEquals(path, new PathDictionary<>().deserialize((path.serialize(SerializerFactory.get()))));
    }

    @Test
    public void testDictionaryFileSymbolicLink() {
        Path path = new Path("/path", EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        assertEquals(path, new PathDictionary<>().deserialize(path.serialize(SerializerFactory.get())));
        assertEquals(EnumSet.of(Path.Type.file, Path.Type.symboliclink),
                new PathDictionary<>().deserialize(path.serialize(SerializerFactory.get())).getType());
    }

    @Test
    public void testDictionaryRegion() {
        Path path = new Path("/path/f", EnumSet.of(Path.Type.file));
        path.attributes().setRegion("r");
        final Path deserialized = new PathDictionary<>().deserialize(path.serialize(SerializerFactory.get()));
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
        assertEquals(path, new PathDictionary<>().deserialize(path.serialize(SerializerFactory.get())));
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
        assertTrue(p.isChild(d));
    }

    @Test
    public void testGetParent() {
        assertEquals(new Path("/b/t", EnumSet.of(Path.Type.directory)), new Path("/b/t/f.type", EnumSet.of(Path.Type.file)).getParent());
    }

    @Test
    public void testCreateRelative() {
        final Path path = new Path(".CDN_ACCESS_LOGS", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(EnumSet.of(Path.Type.volume, Path.Type.directory), path.getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(new DefaultPathContainerService().isContainer(path));
        assertFalse(path.isRoot());
    }

    @Test
    public void testCreateAbsolute() {
        final Path path = new Path("/.CDN_ACCESS_LOGS", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(EnumSet.of(Path.Type.volume, Path.Type.directory), path.getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(new DefaultPathContainerService().isContainer(path));
        assertFalse(path.isRoot());
    }

    @Test
    public void testPathContainer() {
        final Path path = new Path(new Path("test.cyberduck.ch",
                EnumSet.of(Path.Type.volume, Path.Type.directory)), "/test", EnumSet.of(Path.Type.directory));
        assertEquals("/test.cyberduck.ch/test", path.getAbsolute());
    }

    @Test
    public void testSetGetType() {
        Path attributes = new Path("/", EnumSet.of(Path.Type.file, Path.Type.symboliclink));
        assertTrue(attributes.isFile());
        assertTrue(attributes.isSymbolicLink());
        assertFalse(attributes.isDirectory());
        attributes.setType(EnumSet.of(Path.Type.directory, Path.Type.symboliclink));
        assertFalse(attributes.isFile());
        assertTrue(attributes.isSymbolicLink());
        assertTrue(attributes.isDirectory());
    }

    @Test
    public void testHashcodeCollision() {
        assertNotEquals(new Path("19.vcf.gz", EnumSet.of(Path.Type.file)), new Path("0X.vcf.gz", EnumSet.of(Path.Type.file)));
        assertNotEquals(new Path("/d/2R", EnumSet.of(Path.Type.directory)), new Path("/d/33", EnumSet.of(Path.Type.directory)));
    }
}
