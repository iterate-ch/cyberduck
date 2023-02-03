package ch.cyberduck.core.brick;

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

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BrickPreferencesRequestInterceptorTest {

    @Test
    public void testToString() {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put("fs.sync.mode", "online");
        properties.put("fs.sync.indexer.enable", "");
        properties.put("fs.lock.enable", "");
        properties.put("fs.buffer.enable", "");
        assertEquals("fs.sync.mode=online, fs.sync.indexer.enable, fs.lock.enable, fs.buffer.enable",
            new BrickPreferencesRequestInterceptor().toString(properties));
    }
}
