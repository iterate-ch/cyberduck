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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LaunchServicesApplicationFinderTest {

    @Test
    public void testFindAll() throws Exception {
        ApplicationFinder f = new LaunchServicesApplicationFinder();
        final List<Application> applications = f.findAll("file.txt");
        assertFalse(applications.isEmpty());
        assertTrue(applications.contains(new Application("com.apple.TextEdit", "T")));
//        assertTrue(applications.contains(new Application("com.macromates.textmate", "T")));
    }

    @Test
    public void testFindByFilename() throws Exception {
        ApplicationFinder f = new LaunchServicesApplicationFinder();
        assertEquals(new Application("com.apple.Preview", "Preview"), f.find("file.png"));
        assertEquals(Application.notfound, f.find("file.txt_"));
    }

    @Test
    public void testFindByBundleIdentifier() throws Exception {
        ApplicationFinder f = new LaunchServicesApplicationFinder();
        assertEquals(new Application("com.apple.Preview", "Preview"), f.getDescription("com.apple.Preview"));
        assertEquals(Application.notfound, f.getDescription("com.apple.Preview_"));
    }

    @Test
    public void testFindByName() throws Exception {
        ApplicationFinder f = new LaunchServicesApplicationFinder();
        assertEquals(new Application("com.apple.Preview", "Preview"), f.getDescription("Preview"));
        assertEquals(Application.notfound, f.getDescription("Preview_"));
    }

    @Test
    public void testInstalled() throws Exception {
        ApplicationFinder f = new LaunchServicesApplicationFinder();
        assertTrue(f.isInstalled(new Application("com.apple.Preview", "Preview")));
        assertFalse(f.isInstalled(new Application("com.apple.Preview_", "Preview")));
        assertFalse(f.isInstalled(Application.notfound));
    }
}
