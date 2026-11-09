package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.input.NullInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CRC32ChecksumComputeTest {

    @Test
    public void testCompute() throws Exception {
        assertEquals("00000000",
                new CRC32ChecksumCompute().compute(new NullInputStream(0L), new TransferStatus()).hash);
        assertEquals("d202ef8d",
                new CRC32ChecksumCompute().compute(new NullInputStream(1L), new TransferStatus()).hash);
    }

    @Test
    public void testDigest() throws Exception {
        final MessageDigest digest = new CRC32ChecksumCompute().digest();
        assertNotNull(digest);
        assertEquals("00000000", Hex.encodeHexString(digest.digest()));
        digest.update((byte) 0);
        assertEquals("d202ef8d", Hex.encodeHexString(digest.digest()));
        // Reset after digest
        digest.update("123456789".getBytes(StandardCharsets.UTF_8), 0, 9);
        assertEquals("cbf43926", Hex.encodeHexString(digest.digest()));
        digest.update("12345".getBytes(StandardCharsets.UTF_8));
        digest.reset();
        assertEquals("00000000", Hex.encodeHexString(digest.digest()));
    }

    @Test
    public void testDigestMatchesCompute() throws Exception {
        final byte[] content = new byte[40000];
        new Random(1L).nextBytes(content);
        final MessageDigest digest = new CRC32ChecksumCompute().digest();
        digest.update(content);
        assertEquals(new CRC32ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus()).hash,
                Hex.encodeHexString(digest.digest()));
    }
}