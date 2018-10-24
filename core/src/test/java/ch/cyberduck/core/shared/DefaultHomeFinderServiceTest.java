package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class DefaultHomeFinderServiceTest {

    @Test
    public void testFindWithWorkdir() throws Exception {
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
            new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox"));
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
            new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "/sandbox"));
    }

    @Test
    public void testRelativeParent() throws Exception {
        final Path home = new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox/sub");
        assertEquals(new Path("/sandbox/sub", EnumSet.of(Path.Type.directory)), home);
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)), home.getParent());
    }

    @Test
    public void testHomeParent() throws Exception {
        final Path home = new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), String.format("%s/sandbox/sub", Path.HOME));
        assertEquals(new Path("/sandbox/sub", EnumSet.of(Path.Type.directory)), home);
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)), home.getParent());
    }

    @Test
    public void testDefaultLocalPathDriveLetter() throws Exception {
        assertEquals(new Path("/C:/Users/example/Documents/vault", EnumSet.of(Path.Type.directory)),
            new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "C:/Users/example/Documents/vault"));
    }

    @Test
    public void testDefaultLocalPathDriveLetterBackwardSlashes() throws Exception {
        assertEquals(new Path("/C:/Users/example/Documents/vault", EnumSet.of(Path.Type.directory)),
            new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "C:\\Users\\example\\Documents\\vault"));
    }
}
