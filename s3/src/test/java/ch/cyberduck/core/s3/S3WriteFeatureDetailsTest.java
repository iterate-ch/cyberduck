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


import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.S3Object;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class S3WriteFeatureDetailsTest {

    private static final String SHA256_EMPTY = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final String SHA256_EMPTY_BASE64 = "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";

    private S3Object details(final TransferStatus status) {
        final S3Session session = new S3Session(new Host(new S3Protocol()));
        return new S3WriteFeature(session, new S3AccessControlListFeature(session)).getDetails(
                new Path("/bucket/key", EnumSet.of(Path.Type.file)), status);
    }

    @Test
    public void testNoChecksum() {
        final S3Object object = details(new TransferStatus());
        // Payload excluded from signature and no additional checksum
        assertEquals("UNSIGNED-PAYLOAD", object.getMetadata("x-amz-content-sha256"));
        assertFalse(object.containsMetadata("x-amz-checksum-sha256"));
    }

    @Test
    public void testSha256() {
        final S3Object object = details(new TransferStatus().setChecksum(new Checksum(HashAlgorithm.sha256, SHA256_EMPTY, SHA256_EMPTY_BASE64)));
        // Hex payload hash for signature and base64 additional checksum
        assertEquals(SHA256_EMPTY, object.getMetadata("x-amz-content-sha256"));
        assertEquals(SHA256_EMPTY_BASE64, object.getMetadata("x-amz-checksum-sha256"));
    }

    @Test
    public void testSha256Segment() {
        final S3Object object = details(new TransferStatus().setSegment(true).setChecksum(new Checksum(HashAlgorithm.sha256, SHA256_EMPTY, SHA256_EMPTY_BASE64)));
        // No additional checksum for object with part checksum
        assertEquals(SHA256_EMPTY, object.getMetadata("x-amz-content-sha256"));
        assertFalse(object.containsMetadata("x-amz-checksum-sha256"));
    }

    @Test
    public void testOtherAlgorithm() {
        final S3Object object = details(new TransferStatus().setChecksum(new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e")));
        assertEquals("UNSIGNED-PAYLOAD", object.getMetadata("x-amz-content-sha256"));
        assertFalse(object.containsMetadata("x-amz-checksum-sha256"));
    }
}
