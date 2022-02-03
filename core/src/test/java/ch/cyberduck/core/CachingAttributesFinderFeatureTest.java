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

import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class CachingAttributesFinderFeatureTest {

    @Test
    public void findRoot() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path root = new Path("/", EnumSet.of(Path.Type.directory));
        cache.put(root, AttributedList.emptyList());
        assertTrue(cache.isCached(root));
        assertEquals(0, cache.get(root).size());
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache, (path, listener) -> path.attributes());
        assertEquals(PathAttributes.EMPTY, feature.find(root, new DisabledListProgressListener()));
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(root));
        assertEquals(0, cache.get(root).size());
    }

    @Test
    public void find() throws Exception {
        final PathCache cache = new PathCache(1);
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache, (path, listener) -> path.attributes());
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        assertNotNull(feature.find(file, new DisabledListProgressListener()));
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(directory));
        assertTrue(cache.get(directory).contains(file));
    }

    @Test
    public void findWithDefaultExist() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache,
            new DefaultAttributesFinderFeature(new NullSession(new Host(new TestProtocol())) {
                @Override
                public AttributedList<Path> list(final Path directory, final ListProgressListener listener) {
                    return new AttributedList<>(Collections.singletonList(new Path(directory, "f", EnumSet.of(Path.Type.file))));
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
    public void findWithDefault() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingAttributesFinderFeature feature = new CachingAttributesFinderFeature(cache,
            new DefaultAttributesFinderFeature(new NullSession(new Host(new TestProtocol()))));
        try {
            feature.find(file, new DisabledListProgressListener());
        }
        catch(NotfoundException e) {
            //
        }
        assertEquals(1, cache.size());
        assertTrue(cache.isCached(directory));
        assertFalse(cache.get(directory).contains(file));
    }
}
