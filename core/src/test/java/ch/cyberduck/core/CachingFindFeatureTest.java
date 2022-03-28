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

import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.shared.DefaultFindFeature;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class CachingFindFeatureTest {

    @Test
    public void findNotfound() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory));
        final Path file = new Path(directory, "f", EnumSet.of(Path.Type.file));
        final CachingFindFeature feature = new CachingFindFeature(cache,
                new DefaultFindFeature(new NullSession(new Host(new TestProtocol()))));
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