package ch.cyberduck.core.cache;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LRUCacheTest {

    @Test
    public void testExpiry() throws Exception {
        final LRUCache<Object, Object> cache = LRUCache.usingLoader(o -> new Object(), null, 2L, 1000L);
        final Object key = new Object();
        cache.put(key, new Object());
        assertTrue(cache.contains(key));
        Thread.sleep(1000L);
        assertFalse(cache.contains(key));
    }
}