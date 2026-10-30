package ch.cyberduck.core.io;

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


import ch.cyberduck.core.dropbox.DropboxChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.Assert.*;

public class ChecksumComputeDigestTest {

    private static final byte[] CONTENT = "test".getBytes(StandardCharsets.UTF_8);

    private void assertDigest(final ChecksumCompute compute) throws Exception {
        final MessageDigest digest = compute.digest();
        assertNotNull(digest);
        digest.update(CONTENT);
        assertEquals(compute.compute(new ByteArrayInputStream(CONTENT), new TransferStatus()).hash, Hex.encodeHexString(digest.digest()));
    }

    @Test
    public void testDigest() throws Exception {
        assertDigest(new MD5ChecksumCompute());
        assertDigest(new MD5FastChecksumCompute());
        assertDigest(new SHA1ChecksumCompute());
        assertDigest(new SHA256ChecksumCompute());
        assertDigest(new SHA512ChecksumCompute());
        assertDigest(new DropboxChecksumCompute());
        assertDigest(new CRC32ChecksumCompute());
    }

    @Test
    public void testAlgorithm() throws Exception {
        assertEquals("MD5", new MD5ChecksumCompute().digest().getAlgorithm());
        assertEquals("SHA-1", new SHA1ChecksumCompute().digest().getAlgorithm());
        assertEquals("SHA-256", new SHA256ChecksumCompute().digest().getAlgorithm());
    }

    @Test
    public void testNoDigest() throws Exception {
        assertNull(new DisabledChecksumCompute().digest());
    }
}
