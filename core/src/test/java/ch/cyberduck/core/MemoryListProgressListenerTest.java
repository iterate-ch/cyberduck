package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MemoryListProgressListenerTest {

    @Test
    public void testContentsAvailableAfterCleanup() throws Exception {
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> list = new AttributedList<>(Collections.singletonList(new Path(directory, "f", EnumSet.of(Path.Type.file))));
        final PathCache cache = new PathCache(1);
        final MemoryListProgressListener memory = new MemoryListProgressListener(new CachingListProgressListener(cache));
        memory.chunk(directory, list);
        memory.cleanup(directory, AttributedList.emptyList(), Optional.empty());
        // Notified proxy with captured contents
        assertEquals(list, cache.get(directory));
        // Contents available to caller after notifying
        assertSame(list, memory.getContents());
    }

    @Test
    public void testResetClearsContents() throws Exception {
        final Path directory = new Path("/d", EnumSet.of(Path.Type.directory));
        final MemoryListProgressListener memory = new MemoryListProgressListener();
        memory.chunk(directory, new AttributedList<>(Collections.singletonList(new Path(directory, "f", EnumSet.of(Path.Type.file)))));
        memory.reset();
        assertSame(AttributedList.<Path>emptyList(), memory.getContents());
    }
}
