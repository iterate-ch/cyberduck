package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.ui.browser.SearchFilter;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class SearchWorkerTest {

    @Test
    public void testRun() throws Exception {
        final PathCache cache = new PathCache(Integer.MAX_VALUE);
        final AttributedList<Path> root = new AttributedList<>();
        root.add(new Path("/t1.png", EnumSet.of(Path.Type.file)));
        root.add(new Path("/t1.gif", EnumSet.of(Path.Type.file)));
        root.add(new Path("/folder", EnumSet.of(Path.Type.directory)));
        root.add(new Path("/folder2", EnumSet.of(Path.Type.directory)));
        cache.put(new Path("/", EnumSet.of(Path.Type.directory)), root);
        final AttributedList<Path> folder = new AttributedList<>();
        folder.add(new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.png", EnumSet.of(Path.Type.file)));
        folder.add(new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.gif", EnumSet.of(Path.Type.file)));
        cache.put(new Path("/folder", EnumSet.of(Path.Type.directory)), folder);
        final SearchWorker search = new SearchWorker(new Path("/", EnumSet.of(Path.Type.directory)),
                new SearchFilter(".png"), cache, new DisabledListProgressListener());
        final AttributedList<Path> found = search.run(new NullSession(new Host(new TestProtocol())));
        assertTrue(found.contains(new Path("/t1.png", EnumSet.of(Path.Type.file))));
        assertFalse(found.contains(new Path("/t1.gif", EnumSet.of(Path.Type.file))));
        assertFalse(found.contains(new Path("/t2.png", EnumSet.of(Path.Type.file))));
        assertFalse(found.contains(new Path("/t2.gif", EnumSet.of(Path.Type.file))));
        assertTrue(found.contains(new Path("/folder", EnumSet.of(Path.Type.directory))));
        assertFalse(found.contains(new Path("/folder2", EnumSet.of(Path.Type.directory))));
        final AttributedList<Path> children = cache.get(new Path("/folder", EnumSet.of(Path.Type.directory)));
        assertTrue(children.contains(new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.png", EnumSet.of(Path.Type.file))));
        assertFalse(children.contains(new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.gif", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testRepeatedRunWithCache() throws Exception {
        final PathCache cache = new PathCache(Integer.MAX_VALUE);

        final AttributedList<Path> root = new AttributedList<>();
        root.add(new Path("/t1.png", EnumSet.of(Path.Type.file)));
        root.add(new Path("/folder", EnumSet.of(Path.Type.directory)));
        cache.put(new Path("/", EnumSet.of(Path.Type.directory)), root);

        final AttributedList<Path> folder = new AttributedList<>();
        folder.add(new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.gif", EnumSet.of(Path.Type.file)));
        cache.put(new Path("/folder", EnumSet.of(Path.Type.directory)), folder);

        final AttributedList<Path> search1 = new SearchWorker(new Path("/", EnumSet.of(Path.Type.directory)),
                new SearchFilter(".png"), cache, new DisabledListProgressListener()).run(new NullSession(new Host(new TestProtocol())));

        assertTrue(search1.contains(new Path("/t1.png", EnumSet.of(Path.Type.file))));
        assertFalse(search1.contains(new Path("/folder", EnumSet.of(Path.Type.directory))));

        assertTrue(cache.get(new Path("/", EnumSet.of(Path.Type.directory))).contains(new Path("/t1.png", EnumSet.of(Path.Type.file))));
        assertFalse(cache.get(new Path("/", EnumSet.of(Path.Type.directory))).contains(new Path("/folder", EnumSet.of(Path.Type.directory))));
        assertFalse(cache.get(new Path("/folder", EnumSet.of(Path.Type.directory))).contains(
                new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.gif", EnumSet.of(Path.Type.file))));

        final AttributedList<Path> search2 = new SearchWorker(new Path("/", EnumSet.of(Path.Type.directory)),
                new NullFilter<Path>(), cache, new DisabledListProgressListener()).run(new NullSession(new Host(new TestProtocol())));
        assertTrue(search2.contains(new Path("/t1.png", EnumSet.of(Path.Type.file))));
        assertTrue(search2.contains(new Path("/folder", EnumSet.of(Path.Type.directory))));
        assertTrue(cache.get(new Path("/", EnumSet.of(Path.Type.directory))).contains(new Path("/t1.png", EnumSet.of(Path.Type.file))));
        assertTrue(cache.get(new Path("/", EnumSet.of(Path.Type.directory))).contains(new Path("/folder", EnumSet.of(Path.Type.directory))));
        assertTrue(cache.get(new Path("/folder", EnumSet.of(Path.Type.directory))).contains(
                new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.gif", EnumSet.of(Path.Type.file))));

        assertNotNull(cache.lookup(new DefaultPathPredicate(new Path("/folder", EnumSet.of(Path.Type.directory)))));
        assertNotNull(cache.lookup(new DefaultPathPredicate(new Path("/t1.png", EnumSet.of(Path.Type.file)))));
        assertNotNull(cache.lookup(new DefaultPathPredicate(new Path(new Path("/folder", EnumSet.of(Path.Type.directory)), "/t2.gif", EnumSet.of(Path.Type.file)))));
    }
}