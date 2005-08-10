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

import junit.framework.TestCase;

import java.net.MalformedURLException;

/**
 * @version $Id$
 */
public class VersionTest extends TestCase {

    public VersionTest(String name) {
        super(name);
    }

    public void testVersion() {
        assertTrue(new Version("2.5").compareTo(new Version("2.4")) > 0);
        assertTrue(new Version("2.3").compareTo(new Version("2.4")) < 0);
        assertTrue(new Version("2.4.5").compareTo(new Version("2.4.6")) < 0);
        assertTrue(new Version("2.4.7").compareTo(new Version("2.4.6")) > 0);
        assertTrue(new Version("2.5b1").compareTo(new Version("2.5b2")) < 0);
        assertTrue(new Version("2.5b3").compareTo(new Version("2.5b2")) > 0);
        assertTrue(new Version("2.5").compareTo(new Version("2.5b2")) > 0);
    }
}
