package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.junit.Test;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.model.ObjectMetadata;

import static org.junit.Assert.assertEquals;

public class SwiftAttributesFinderFeatureManifestTest {

    private static final String MD5 = "0cc175b9c0f1b6a831c399e269772661";

    private static ObjectMetadata metadata(final String etag) {
        return new ObjectMetadata("application/octet-stream", "1", etag, "1700000000.00000");
    }

    @Test
    public void testChecksumObject() {
        assertEquals(new Checksum(HashAlgorithm.md5, MD5),
                new SwiftAttributesFinderFeature(null, null).toAttributes(metadata(MD5)).getChecksum());
    }

    @Test
    public void testChecksumStaticLargeObjectManifest() {
        final ObjectMetadata metadata = metadata(String.format("\"%s\"", MD5));
        metadata.addMetaData(Constants.X_STATIC_LARGE_OBJECT, "True");
        assertEquals(new Checksum(HashAlgorithm.md5_segments, MD5),
                new SwiftAttributesFinderFeature(null, null).toAttributes(metadata).getChecksum());
    }

    @Test
    public void testChecksumDynamicLargeObjectManifest() {
        final ObjectMetadata metadata = metadata(String.format("\"%s\"", MD5));
        metadata.addMetaData(Constants.MANIFEST_HEADER.toLowerCase(), "container/segments/");
        assertEquals(new Checksum(HashAlgorithm.md5_segments, MD5),
                new SwiftAttributesFinderFeature(null, null).toAttributes(metadata).getChecksum());
    }
}
