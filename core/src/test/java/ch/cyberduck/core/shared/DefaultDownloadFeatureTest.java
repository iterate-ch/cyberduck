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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.local.DefaultTemporaryFileService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DefaultDownloadFeatureTest {

    private final byte[] content = RandomUtils.nextBytes(1000);

    private Checksum md5(final byte[] data) throws Exception {
        return ChecksumComputeFactory.get(HashAlgorithm.md5).compute(new ByteArrayInputStream(data), new TransferStatus());
    }

    private void download(final Local local, final TransferStatus status, final int offset) throws Exception {
        new DefaultDownloadFeature(null).download(new Read() {
            @Override
            public java.io.InputStream read(final Path file, final TransferStatus s, final ch.cyberduck.core.ConnectionCallback callback) {
                return new ByteArrayInputStream(content, offset, content.length - offset);
            }
        }, new Path("/f", java.util.EnumSet.of(Path.Type.file)), local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledConnectionCallback());
    }

    @Test
    public void testDigest() throws Exception {
        final Local local = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        final TransferStatus status = new TransferStatus().setVerify(HashAlgorithm.md5).setLength(content.length);
        this.download(local, status, 0);
        assertEquals(md5(content), status.getDigest());
    }

    @Test
    public void testDigestDisabled() throws Exception {
        final Local local = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        final TransferStatus status = new TransferStatus().setLength(content.length);
        this.download(local, status, 0);
        assertEquals(Checksum.NONE, status.getDigest());
    }

    @Test
    public void testDigestResume() throws Exception {
        final Local local = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        try(java.io.OutputStream out = local.getOutputStream(false)) {
            out.write(content, 0, 400);
        }
        final TransferStatus status = new TransferStatus().setVerify(HashAlgorithm.md5).setAppend(true).setOffset(400L).setLength(content.length - 400L);
        this.download(local, status, 400);
        assertEquals(md5(content), status.getDigest());
        assertEquals(content.length, local.attributes().getSize());
    }

    @Test
    public void testNoDigestForSegment() throws Exception {
        final Local local = new DefaultTemporaryFileService().create(new AlphanumericRandomStringService().random());
        final TransferStatus status = new TransferStatus().setVerify(HashAlgorithm.md5).setSegment(true).setAppend(true).setOffset(0L).setLength(content.length);
        this.download(local, status, 0);
        assertEquals(Checksum.NONE, status.getDigest());
        assertEquals(true, Arrays.equals(content, java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(local.getAbsolute()))));
    }
}
