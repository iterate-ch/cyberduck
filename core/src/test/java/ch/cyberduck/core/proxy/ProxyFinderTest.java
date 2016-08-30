package ch.cyberduck.core.proxy;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Host;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProxyFinderTest {

    @Test
    public void testWildcard() {
        AbstractProxyFinder p = new AbstractProxyFinder() {
            @Override
            public Proxy find(final Host target) {
                throw new UnsupportedOperationException();
            }
        };
        assertTrue(p.matches("*.cyberduck.ch", "a.cyberduck.ch"));
        assertTrue(p.matches("*.cyberduck.ch", "a.b.cyberduck.ch"));
        assertFalse(p.matches("*.cyberduck.ch", "cyberduck.ch"));
    }
}
