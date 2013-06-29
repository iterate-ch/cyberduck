package ch.cyberduck.core.local;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class LaunchServicesApplicationFinderTest extends AbstractTestCase {

    @Test
    public void testFindAll() throws Exception {
        ApplicationFinder f = new LaunchServicesApplicationFinder();
        final List<Application> applications = f.findAll("file.txt");
        assertFalse(applications.isEmpty());
        assertTrue(applications.contains(new Application("com.apple.TextEdit", "T")));
//        assertTrue(applications.contains(new Application("com.macromates.textmate", "T")));
    }

    @Test
    public void testFind() throws Exception {
        ApplicationFinder f = new LaunchServicesApplicationFinder();
        assertEquals(new Application("com.apple.Preview", "Preview"), f.find("file.png"));
    }
}
