package ch.cyberduck.core.transfer;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MD5ChecksumCompute;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.Assert.*;

public class TransferStatusTest {

    @Test
    public void testSetResume() {
        TransferStatus status = new TransferStatus();
        status.setOffset(1024);
        status.setAppend(true);
        assertEquals(1024, status.getOffset());
        status.setAppend(false);
        assertEquals(0, status.getOffset());
    }

    @Test
    public void testSetSetComplete() throws Exception {
        TransferStatus status = new TransferStatus();
        status.setLength(1L);
        assertFalse(status.isComplete());
        status.setOffset(1L);
        assertFalse(status.isComplete());
        status.setComplete();
        assertTrue(status.isComplete());
        assertTrue(status.await());
    }

    @Test
    public void testEquals() {
        assertEquals(new TransferStatus(), new TransferStatus());
        assertEquals(new TransferStatus().hashCode(), new TransferStatus().hashCode());
    }

    private static Local local(final String content) {
        return new Local("/f") {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            }
        };
    }

    @Test
    public void testCompute() throws Exception {
        final Local local = local("0123456789");
        final Checksum expected = new MD5ChecksumCompute().compute(new ByteArrayInputStream("2345".getBytes(StandardCharsets.UTF_8)), new TransferStatus());
        final TransferStatus segment = new TransferStatus().setOffset(2L).setLength(4L).setParent(new TransferStatus());
        // Content with offset and length of the segment
        assertEquals(expected, segment.compute(local, HashAlgorithm.md5));
        assertNotNull(segment.digest(HashAlgorithm.md5));
    }

    @Test
    public void testComputeTransformedByOverallStatus() throws Exception {
        final Local local = local("0123456789");
        final Checksum transformed = new Checksum(HashAlgorithm.md5, "00");
        final TransferStatus overall = new TransferStatus() {
            @Override
            public Checksum compute(final Local local, final HashAlgorithm algorithm, final TransferStatus segment) {
                return transformed;
            }

            @Override
            public MessageDigest digest(final HashAlgorithm algorithm) {
                return null;
            }
        };
        final TransferStatus segment = new TransferStatus().setParent(overall);
        assertSame(transformed, segment.compute(local, HashAlgorithm.md5));
        assertNull(segment.digest(HashAlgorithm.md5));
        // Parent of overall status
        assertSame(transformed, new TransferStatus().setParent(new TransferStatus().setParent(overall)).compute(local, HashAlgorithm.md5));
    }
}
