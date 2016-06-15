package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class UploadTargetFinderTest {

    @Test
    public void testFind() throws Exception {
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new UploadTargetFinder(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))).find(null));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new UploadTargetFinder(new Path("/", EnumSet.of(Path.Type.directory)))
                        .find(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new UploadTargetFinder(new Path("/", EnumSet.of(Path.Type.directory)))
                        .find(new Path("/f", EnumSet.of(Path.Type.file))));
        assertEquals(new Path("/d", EnumSet.of(Path.Type.directory)),
                new UploadTargetFinder(new Path("/", EnumSet.of(Path.Type.directory)))
                        .find(new Path("/d/f", EnumSet.of(Path.Type.file))));
        assertEquals(new Path("/d", EnumSet.of(Path.Type.directory)),
                new UploadTargetFinder(new Path("/", EnumSet.of(Path.Type.directory)))
                        .find(new Path("/d/f", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testFindContainerSelected() throws Exception {
        final Path container = new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals(container,
                new UploadTargetFinder(new Path("/", EnumSet.of(Path.Type.directory)))
                        .find(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new UploadTargetFinder(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)))
                        .find(null));
    }
}
