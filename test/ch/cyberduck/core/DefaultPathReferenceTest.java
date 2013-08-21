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
        assertEquals("/-2", new DefaultPathReference(t).unique());
        t.attributes().setVersionId("1");
        assertEquals("/-21", new DefaultPathReference(t).unique());
        t.attributes().setRegion("r");
        assertEquals("/-2r1", new DefaultPathReference(t).unique());
        t.attributes().setVersionId(null);
        assertEquals("/-2r", new DefaultPathReference(t).unique());
    }
}
