package ch.cyberduck.core.cf;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CFPathTest extends AbstractTestCase {

    @Test
    public void testCreateRelative() throws Exception {
        final CFPath path = new CFPath(".CDN_ACCESS_LOGS", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE, path.attributes().getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(path.isContainer());
        assertFalse(path.isRoot());
    }

    @Test
    public void testCreateAbsolute() throws Exception {
        final CFPath path = new CFPath("/.CDN_ACCESS_LOGS", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        assertEquals("/.CDN_ACCESS_LOGS", path.getAbsolute());
        assertEquals(".CDN_ACCESS_LOGS", path.getName());
        assertEquals(Path.VOLUME_TYPE | Path.DIRECTORY_TYPE, path.attributes().getType());
        assertNotNull(path.getParent());
        assertEquals("/", path.getParent().getAbsolute());
        assertTrue(path.isContainer());
        assertFalse(path.isRoot());
    }
}