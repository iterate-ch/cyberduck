package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecursiveSearchFilterTest {

    @Test
    public void testAcceptFile() {
        final RecursiveSearchFilter f = new RecursiveSearchFilter(new AttributedList<>(Arrays.asList(new Path("/f", EnumSet.of(Path.Type.file)))));
        assertTrue(f.accept(new Path("/f", EnumSet.of(Path.Type.file))));
        assertFalse(f.accept(new Path("/a", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testAcceptDirectory() {
        final RecursiveSearchFilter f = new RecursiveSearchFilter(new AttributedList<>(Arrays.asList(
                new Path("/d", EnumSet.of(Path.Type.directory)),
                new Path("/d/f", EnumSet.of(Path.Type.file)))
        ));
        assertTrue(f.accept(new Path("/d", EnumSet.of(Path.Type.directory))));
        assertTrue(f.accept(new Path("/d/f", EnumSet.of(Path.Type.file))));
        assertFalse(f.accept(new Path("/d/f2", EnumSet.of(Path.Type.file))));
        assertFalse(f.accept(new Path("/a", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testAcceptDirectoryChildrenOnly() {
        final RecursiveSearchFilter f = new RecursiveSearchFilter(new AttributedList<>(Arrays.asList(
                new Path("/d/f", EnumSet.of(Path.Type.file)))
        ));
        assertTrue(f.accept(new Path("/d", EnumSet.of(Path.Type.directory))));
        assertTrue(f.accept(new Path("/d/f", EnumSet.of(Path.Type.file))));
        assertFalse(f.accept(new Path("/d/f2", EnumSet.of(Path.Type.file))));
        assertFalse(f.accept(new Path("/a", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testAcceptFileVersions() {
        final RecursiveSearchFilter f = new RecursiveSearchFilter(new AttributedList<>(Arrays.asList(new Path("/f", EnumSet.of(Path.Type.file)))));
        assertTrue(f.accept(new Path("/f", EnumSet.of(Path.Type.file), new PathAttributes().withVersionId("1"))));
    }
}
