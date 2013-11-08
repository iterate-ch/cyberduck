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

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DefaultPathReferenceTest extends AbstractTestCase {

    @Test
    public void testUnique() throws Exception {
        final Path t = new Path("/", Path.DIRECTORY_TYPE);
        assertEquals("2-/", new DefaultPathReference(t).unique());
        t.attributes().setVersionId("1");
        assertEquals("2-1/", new DefaultPathReference(t).unique());
        t.attributes().setRegion("r");
        assertEquals("2-1/", new DefaultPathReference(t).unique());
        t.attributes().setVersionId(null);
        assertEquals("2-/", new DefaultPathReference(t).unique());
    }

    @Test
    public void testUniqueContainer() throws Exception {
        final Path t = new Path("/container", Path.DIRECTORY_TYPE);
        assertEquals("2-/container", new DefaultPathReference(t).unique());
        t.attributes().setVersionId("1");
        assertEquals("2-1/container", new DefaultPathReference(t).unique());
        t.attributes().setRegion("r");
        assertEquals("2-r1/container", new DefaultPathReference(t).unique());
        t.attributes().setVersionId(null);
        assertEquals("2-r/container", new DefaultPathReference(t).unique());
    }

    @Test
    public void testAttributes() throws Exception {
        final Path t = new Path("/f", Path.FILE_TYPE);
        t.attributes().setRegion("r");
        assertEquals("r", new DefaultPathReference(t).attributes());
        t.attributes().setVersionId("1");
        assertEquals("r1", new DefaultPathReference(t).attributes());
    }
}
