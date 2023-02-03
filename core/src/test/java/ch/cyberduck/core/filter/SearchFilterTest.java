package ch.cyberduck.core.filter;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.ui.browser.SearchFilter;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchFilterTest {

    @Test
    public void testFilter() {
        assertTrue(new SearchFilter("f").accept(new Path("/f.txt", EnumSet.of(Path.Type.file))));
        assertTrue(new SearchFilter("F").accept(new Path("/f.txt", EnumSet.of(Path.Type.file))));
        assertFalse(new SearchFilter("a").accept(new Path("/f.txt", EnumSet.of(Path.Type.file))));
        assertFalse(new SearchFilter(".*").accept(new Path("/f.txt", EnumSet.of(Path.Type.file))));
        assertTrue(new SearchFilter("f*").accept(new Path("/f.txt", EnumSet.of(Path.Type.file))));
        assertTrue(new SearchFilter("f*").accept(new Path("/F.txt", EnumSet.of(Path.Type.file))));
        assertTrue(new SearchFilter("*.txt").accept(new Path("/f.txt", EnumSet.of(Path.Type.file))));
        assertTrue(new SearchFilter("*.txt").accept(new Path("/f.TXT", EnumSet.of(Path.Type.file))));
        assertTrue(new SearchFilter("*").accept(new Path("/f.txt", EnumSet.of(Path.Type.file))));
    }
}