package ch.cyberduck.core.s3;

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

import org.jets3t.service.model.MultipartPart;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class S3MultipartUploadServiceCompositeTest {

    // MD5 of "a" and "b"
    private static final String MD5_A = "0cc175b9c0f1b6a831c399e269772661";
    private static final String MD5_B = "92eb5ffee6ae2fec3ad71c777531578f";
    // MD5 of concatenated binary MD5 of "a" and "b"
    private static final String COMPOSITE_AB = "96e024ba2074fe77e8e965ba43a704be";

    private static MultipartPart part(final int number, final String etag) {
        return new MultipartPart(number, new Date(0L), etag, 1L);
    }

    @Test
    public void testComposite() {
        final Map<Integer, Checksum> checksums = new HashMap<>();
        checksums.put(1, new Checksum(HashAlgorithm.md5, MD5_A));
        checksums.put(2, new Checksum(HashAlgorithm.md5, MD5_B));
        final Checksum composite = S3MultipartUploadService.composite(Arrays.asList(part(1, "\"x\""), part(2, "\"y\"")), checksums);
        assertEquals(HashAlgorithm.md5_multipart, composite.algorithm);
        assertEquals(COMPOSITE_AB + "-2", composite.hash);
    }

    @Test
    public void testCompositeSortedByPartNumber() {
        final Map<Integer, Checksum> checksums = new HashMap<>();
        checksums.put(1, new Checksum(HashAlgorithm.md5, MD5_A));
        checksums.put(2, new Checksum(HashAlgorithm.md5, MD5_B));
        assertEquals(COMPOSITE_AB + "-2",
                S3MultipartUploadService.composite(Arrays.asList(part(2, "\"y\""), part(1, "\"x\"")), checksums).hash);
    }

    @Test
    public void testCompositeFallbackToQuotedEtagOfPartsNotUploaded() {
        // Parts uploaded in a previous transfer when resuming are not in the calculated checksums
        final Map<Integer, Checksum> checksums = Collections.singletonMap(2, new Checksum(HashAlgorithm.md5, MD5_B));
        assertEquals(COMPOSITE_AB + "-2",
                S3MultipartUploadService.composite(Arrays.asList(part(1, String.format("\"%s\"", MD5_A)), part(2, "\"y\"")), checksums).hash);
    }

    @Test
    public void testCompositeSinglePart() {
        // MD5 of the binary MD5 of "a"
        final Map<Integer, Checksum> checksums = Collections.singletonMap(1, new Checksum(HashAlgorithm.md5, MD5_A));
        assertEquals("b6ff9a06b7e20bcb2858c5b8ff744aea-1", S3MultipartUploadService.composite(Collections.singletonList(part(1, "\"x\"")), checksums).hash);
    }

    @Test
    public void testCompositeInvalidEtag() {
        // Not a hex encoded MD5 as with SSE-KMS
        assertEquals(Checksum.NONE,
                S3MultipartUploadService.composite(Collections.singletonList(part(1, "\"not-hex\"")), Collections.emptyMap()));
    }
}
