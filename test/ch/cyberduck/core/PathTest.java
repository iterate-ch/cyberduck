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

import junit.framework.Test;
import junit.framework.TestSuite;

public class PathTest extends AbstractTestCase {
    public PathTest(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNormalize() throws Exception {
        Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                "/path/to/remove/..", Path.DIRECTORY_TYPE);
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path/to/remove/.././");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path/remove/../to/remove/.././");
        assertEquals("/path/to", path.getAbsolute());
//        path.setPath("../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
//        path.setPath("/../path/to");
//        assertEquals( "/path/to", path.getAbsolute());
        path.setPath("/path/to/remove/remove/../../");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path/././././to");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("./.path/to");
        assertEquals("/.path/to", path.getAbsolute());
        path.setPath(".path/to");
        assertEquals("/.path/to", path.getAbsolute());
        path.setPath("/path/.to");
        assertEquals("/path/.to", path.getAbsolute());
        path.setPath("/path//to");
        assertEquals("/path/to", path.getAbsolute());
        path.setPath("/path///to////");
        assertEquals("/path/to", path.getAbsolute());


        assertEquals(Path.normalize("relative/path", false), "relative/path");
        assertEquals(Path.normalize("/absolute/path", true), "/absolute/path");
        assertEquals(Path.normalize("/absolute/path", false), "/absolute/path");
    }

    public void testName() throws Exception {
        {
            Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                    "/path/to/file/", Path.DIRECTORY_TYPE);
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
        {
            Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                    "/path/to/file", Path.DIRECTORY_TYPE);
            assertEquals("file", path.getName());
            assertEquals("/path/to/file", path.getAbsolute());
        }
    }

    public void test1067() throws Exception {
        Path path = PathFactory.createPath(SessionFactory.createSession(new Host("localhost")),
                "\\\\directory", Path.DIRECTORY_TYPE);
        assertEquals("\\\\directory", path.getAbsolute());
        assertEquals("/", path.getParent().getAbsolute());
    }

    public void test972() throws Exception {
        assertEquals("//home/path", Path.normalize("//home/path"));
    }

    public static Test suite() {
        return new TestSuite(PathTest.class);
    }
}
