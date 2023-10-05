package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultFindFeature;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class CachingFindFeatureTest {

    @Test
    public void testFindNotfound() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingFindFeature feature = new CachingFindFeature(Protocol.Case.sensitive, cache,
                new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
                    @Override
                    public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                        throw new NotfoundException(folder.getAbsolute());
                    }
                }));
        feature.find(file, new DisabledListProgressListener());
        assertEquals(0, cache.size());
        assertFalse(cache.isCached(directory));
    }

    @Test
    public void testFindCaseSensitive() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingFindFeature feature = new CachingFindFeature(Protocol.Case.sensitive, cache, new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws ConnectionCanceledException {
                listener.chunk(folder, new AttributedList<>(Collections.singleton(file)));
                return new AttributedList<>(Collections.singleton(file));
            }

            @Override
            public Protocol.Case getCaseSensitivity() {
                return Protocol.Case.sensitive;
            }
        }));
        assertTrue(feature.find(file, new DisabledListProgressListener()));
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(directory));
        assertFalse(feature.find(new Path(directory, "F", EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
    }

    @Test
    public void testFindCaseInsensitive() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingFindFeature feature = new CachingFindFeature(Protocol.Case.insensitive, cache, new DefaultFindFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws ConnectionCanceledException {
                listener.chunk(folder, new AttributedList<>(Collections.singleton(file)));
                return new AttributedList<>(Collections.singleton(file));
            }

            @Override
            public Protocol.Case getCaseSensitivity() {
                return Protocol.Case.insensitive;
            }
        }));
        assertTrue(feature.find(file, new DisabledListProgressListener()));
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(directory));
        assertTrue(feature.find(new Path(directory, "F", EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
    }
}