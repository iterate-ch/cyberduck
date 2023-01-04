package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ReverseLookupCacheTest {

    @Test
    public void testLookup() {
        final Cache<Path> cache = new ReverseLookupCache<>(new PathCache(1), 1);
        assertNull(cache.lookup(new DefaultPathPredicate(new Path("/", EnumSet.of(Path.Type.directory)))));
        final AttributedList<Path> list = new AttributedList<>();
        final Path directory = new Path("p", EnumSet.of(Path.Type.directory));
        final Path file1 = new Path(directory, "name1", EnumSet.of(Path.Type.file));
        list.add(file1);
        final Path file2 = new Path(directory, "name2", EnumSet.of(Path.Type.file));
        list.add(file2);
        cache.put(directory, list);
        assertNotNull(cache.lookup(new DefaultPathPredicate(file1)));
        assertNotNull(cache.lookup(new DefaultPathPredicate(file2)));
    }
}