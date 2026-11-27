package ch.cyberduck.core.shared;

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

public class MetadataSizeFilterTest {

    private final Path file = new Path("/f", EnumSet.of(Path.Type.file));

    @Test
    public void testUnlimited() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("k", StringUtils.repeat('v', 10000));
        assertEquals(metadata, new MetadataSizeFilter().filter(file, metadata));
    }

    @Test
    public void testMaximumEntrySize() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("user_tags", "Red%0A6");
        metadata.put("xattr_large", StringUtils.repeat('v', 120));
        final Map<String, String> filtered = new MetadataSizeFilter().withMaximumEntrySize(124).filter(file, metadata);
        assertEquals(1, filtered.size());
        assertEquals("Red%0A6", filtered.get("user_tags"));
    }

    @Test
    public void testMaximumKeyAndValueSize() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put(StringUtils.repeat('k', 5), "v");
        metadata.put("k", StringUtils.repeat('v', 5));
        metadata.put("a", "b");
        final Map<String, String> filtered = new MetadataSizeFilter().withMaximumKeySize(4).withMaximumValueSize(4).filter(file, metadata);
        assertEquals(1, filtered.size());
        assertEquals("b", filtered.get("a"));
    }

    @Test
    public void testSizeInBytes() {
        final Map<String, String> metadata = new HashMap<>();
        // 2 bytes in UTF-8
        metadata.put("k", "éé");
        assertTrue(new MetadataSizeFilter().withMaximumValueSize(3).filter(file, metadata).isEmpty());
        assertEquals(metadata, new MetadataSizeFilter().withMaximumValueSize(4).filter(file, metadata));
    }

    @Test
    public void testMaximumTotalSizePreferSmallerEntries() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("a", StringUtils.repeat('v', 9));
        metadata.put("b", StringUtils.repeat('v', 4));
        metadata.put("c", StringUtils.repeat('v', 4));
        final Map<String, String> filtered = new MetadataSizeFilter().withMaximumTotalSize(10).filter(file, metadata);
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsKey("b"));
        assertTrue(filtered.containsKey("c"));
    }

    @Test
    public void testMaximumCount() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("a", "1");
        metadata.put("b", "2");
        metadata.put("c", "3");
        final Map<String, String> filtered = new MetadataSizeFilter().withMaximumCount(2).filter(file, metadata);
        assertEquals(2, filtered.size());
        assertFalse(filtered.containsKey("c"));
    }

    @Test
    public void testExcludedNotCounted() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", StringUtils.repeat('v', 100));
        metadata.put("a", "1");
        final Map<String, String> filtered = new MetadataSizeFilter().withMaximumTotalSize(2).withMaximumCount(1)
                .withExcluded(key -> key.equals("Content-Type")).filter(file, metadata);
        assertEquals(metadata, filtered);
    }

    @Test
    public void testRequiredCounted() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("mtime", StringUtils.repeat('1', 13));
        metadata.put("a", "1");
        final Map<String, String> filtered = new MetadataSizeFilter().withMaximumTotalSize(10)
                .withRequired(key -> key.equals("mtime")).filter(file, metadata);
        assertEquals(1, filtered.size());
        assertTrue(filtered.containsKey("mtime"));
    }

    @Test
    public void testEncodedValueSize() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("k", "a+b");
        assertEquals(metadata, new MetadataSizeFilter().withMaximumValueSize(3).filter(file, metadata));
        assertTrue(new MetadataSizeFilter().withMaximumValueSize(3).withEncoder(value -> value.replace("+", "%2B")).filter(file, metadata).isEmpty());
    }

    @Test
    public void testName() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("x-amz-meta-k", StringUtils.repeat('v', 9));
        // Prefix not counted and key kept in result
        assertEquals(metadata, new MetadataSizeFilter().withMaximumTotalSize(10).withMaximumKeySize(1)
                .withName(key -> StringUtils.removeStart(key, "x-amz-meta-")).filter(file, metadata));
        assertTrue(new MetadataSizeFilter().withMaximumTotalSize(10).filter(file, metadata).isEmpty());
    }

    @Test
    public void testOverhead() {
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("k", StringUtils.repeat('v', 4));
        assertEquals(metadata, new MetadataSizeFilter().withMaximumTotalSize(10).withOverhead(5).filter(file, metadata));
        metadata.put("k", StringUtils.repeat('v', 5));
        assertTrue(new MetadataSizeFilter().withMaximumTotalSize(10).withOverhead(5).filter(file, metadata).isEmpty());
    }
}
