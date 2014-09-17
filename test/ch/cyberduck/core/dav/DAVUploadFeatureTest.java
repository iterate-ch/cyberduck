package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;

import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import java.io.BufferedInputStream;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * @version $Id$
 */
public class DAVUploadFeatureTest extends AbstractTestCase {

    @Test
    public void testDecorate() throws Exception {
        final NullInputStream n = new NullInputStream(1L);
        assertSame(BufferedInputStream.class, new DAVUploadFeature(new DAVSession(new Host("h"))).decorate(n, null).getClass());
    }

    @Test
    public void testDigest() throws Exception {
        assertNull(new DAVUploadFeature(new DAVSession(new Host("h"))).digest());
    }
}
