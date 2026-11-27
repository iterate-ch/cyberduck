package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SwiftMetadataFeatureFilterTest {

    @Test
    public void testFilter() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", StringUtils.repeat('t', 300));
        metadata.put("user_tags", "Red%0A6");
        metadata.put("xattr_fits", StringUtils.repeat('v', SwiftMetadataFeature.MAXIMUM_VALUE_SIZE));
        // Percent encoded to three bytes per character
        metadata.put("xattr_encoded", StringUtils.repeat('+', SwiftMetadataFeature.MAXIMUM_VALUE_SIZE / 2));
        final Map<String, String> filtered = SwiftMetadataFeature.filter(new Path("/c/f", EnumSet.of(Path.Type.file)), metadata);
        assertEquals(3, filtered.size());
        assertTrue(filtered.containsKey("Content-Type"));
        assertTrue(filtered.containsKey("user_tags"));
        assertTrue(filtered.containsKey("xattr_fits"));
    }
}
