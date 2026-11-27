package ch.cyberduck.core.s3;

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

public class S3MetadataFeatureFilterTest {

    @Test
    public void testFilter() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", "text/plain");
        metadata.put("x-amz-meta-Mtime", "1780553043");
        metadata.put("user_tags", "Red%0A6");
        metadata.put("xattr_large", StringUtils.repeat('v', S3MetadataFeature.MAXIMUM_TOTAL_SIZE));
        final Map<String, String> filtered = S3MetadataFeature.filter(new Path("/b/f", EnumSet.of(Path.Type.file)), metadata);
        assertEquals(3, filtered.size());
        assertTrue(filtered.containsKey("Content-Type"));
        assertTrue(filtered.containsKey("x-amz-meta-Mtime"));
        assertTrue(filtered.containsKey("user_tags"));
    }

    @Test
    public void testFilterPrefixNotCounted() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("x-amz-meta-k", StringUtils.repeat('v', S3MetadataFeature.MAXIMUM_TOTAL_SIZE - 1));
        assertEquals(metadata, S3MetadataFeature.filter(new Path("/b/f", EnumSet.of(Path.Type.file)), metadata));
    }
}
