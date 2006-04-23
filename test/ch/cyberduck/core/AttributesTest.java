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
import junit.framework.TestCase;

public class AttributesTest extends TestCase {
    public AttributesTest(String name) {
        super(name);
    }

    private Attributes attributes;

    public void setUp() throws Exception {
        super.setUp();
        this.attributes = new Attributes();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        this.attributes = null;
    }

    public void testClone() throws Exception {
        Attributes clone = (Attributes)attributes.clone();
        assertNotSame(clone, attributes);

        assertNotSame(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getPermission(), attributes.getPermission());

        assertNotSame(clone.getTimestamp(), attributes.getTimestamp());
        assertEquals(clone.getTimestamp(), attributes.getTimestamp());
    }

    public void testGetAsDictionary() throws Exception {
        ;
    }

    public void testSetGetSize() throws Exception {
        ;
    }

    public void testSetGetTimestamp() throws Exception {
        ;
    }

    public void testSetTimestamp1() throws Exception {
        ;
    }

    public void testGetTimestampAsCalendar() throws Exception {
        ;
    }

    public void testGetTimestampAsString() throws Exception {
        ;
    }

    public void testGetTimestampAsShortString() throws Exception {
        ;
    }

    public void testSetGetPermission() throws Exception {
        ;
    }

    public void testSetGetType() throws Exception {
        ;
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
