package ch.cyberduck.core;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AttributesTest extends AbstractTestCase {
    public AttributesTest(String name) {
        super(name);
    }

    private PathAttributes attributes;

    @Override
    public void setUp() {
        super.setUp();
        this.attributes = new PathAttributes();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        this.attributes = null;
    }

    public void testClone() throws Exception {
        PathAttributes clone = new PathAttributes(attributes.getAsDictionary());
        assertNotSame(clone, attributes);

        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
    }

    public void testGetAsDictionary() throws Exception {
        ;
    }

    public void testSetGetSize() throws Exception {
        ;
    }

    public void testSetGetType() throws Exception {
        attributes.setType(Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        assertTrue(attributes.isFile());
        assertTrue(attributes.isSymbolicLink());
        assertFalse(attributes.isDirectory());
        attributes.setType(Path.DIRECTORY_TYPE | Path.SYMBOLIC_LINK_TYPE);
        assertFalse(attributes.isFile());
        assertTrue(attributes.isSymbolicLink());
        assertTrue(attributes.isDirectory());
    }

    public void testSetGetOwner() throws Exception {
        ;
    }

    public void testSetGetGroup() throws Exception {
        ;
    }

    public static Test suite() {
        return new TestSuite(AttributesTest.class);
    }
}
