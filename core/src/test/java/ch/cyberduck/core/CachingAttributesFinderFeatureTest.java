package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class CachingAttributesFinderFeatureTest {

    @Test
    public void testFindRoot() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path root = new Path("/", EnumSet.of(Path.Type.directory));
        cache.put(root, AttributedList.emptyList());
        assertTrue(cache.isCached(root));
        assertEquals(0, cache.get(root).size());
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
                listener.chunk(file.getParent(), new AttributedList<>(Collections.singletonList(file)));
                return file.attributes();
            }
        });
        assertEquals(PathAttributes.EMPTY, feature.find(root, new DisabledListProgressListener()));
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(root));
        assertEquals(0, cache.get(root).size());
    }

    @Test
    public void testFind() throws Exception {
        final PathCache cache = new PathCache(1);
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
                listener.chunk(file.getParent(), new AttributedList<>(Collections.singletonList(file)));
                return file.attributes();
            }
        });
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        assertNotNull(feature.find(file, new DisabledListProgressListener()));
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(directory));
        assertTrue(cache.get(directory).contains(file));
    }

    @Test
    public void testFindWithDefaultExist() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache,
                new DefaultAttributesFinderFeature(new NullSession(new Host(new TestProtocol())) {
                    @Override
                    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws ConnectionCanceledException {
                        final Path f = new Path(directory, "f", EnumSet.of(Path.Type.file));
                        listener.chunk(directory, new AttributedList<>(Collections.singletonList(f)));
                        return new AttributedList<>(Collections.singletonList(f));
                }
            }));
        assertNotNull(feature.find(file, new DisabledListProgressListener()));
        assertNotSame(file.attributes(), feature.find(file, new DisabledListProgressListener()));
        assertEquals(file.attributes(), feature.find(file, new DisabledListProgressListener()));
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(directory));
        assertTrue(cache.get(directory).contains(file));
    }

    @Test
    public void testFindNotfound() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache,
                new DefaultAttributesFinderFeature(new NullSession(new Host(new TestProtocol())) {
                    @Override
                    public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                        throw new NotfoundException(folder.getAbsolute());
                    }
                }));
        try {
            feature.find(file, new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        assertEquals(0, cache.size());
        assertFalse(cache.isCached(directory));
    }

    @Test
    public void testFindErrorWhilePagingDirectoryListing() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache,
                new DefaultAttributesFinderFeature(new NullSession(new Host(new TestProtocol())) {
                    @Override
                    public AttributedList<Path> list(final Path folder, final ListProgressListener listener) throws BackgroundException {
                        final AttributedList<Path> list = new AttributedList<>(Collections.singletonList(new Path(folder, "t", EnumSet.of(Path.Type.file))));
                        listener.chunk(folder, list);
                        throw new ConnectionTimeoutException(folder.getAbsolute());
                    }
                }));
        try {
            feature.find(file, new DisabledListProgressListener());
            fail();
        }
        catch(ConnectionTimeoutException e) {
            //
        }
        assertFalse(cache.isCached(directory));
    }
}
