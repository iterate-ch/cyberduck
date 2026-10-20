package ch.cyberduck.core.transfer.upload;

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


import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UploadFilterChecksumTest {

    private static final Checksum MD5 = new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e");

    private final Host host = new Host(new TestProtocol());
    private final OverwriteFilter filter = new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(host),
            new UploadFilterOptions(host).withChecksum(true));
    private final Path file = new Path("/f", EnumSet.of(Path.Type.file));

    private TransferStatus status(final Checksum digest, final Checksum response) {
        return new TransferStatus().setDigest(digest).setResponse(new DefaultPathAttributes().setChecksum(response)).setComplete();
    }

    @Test
    public void testChecksumMatch() throws Exception {
        filter.complete(file, new NullLocal("f"), status(MD5, MD5), ProgressListener.noop);
    }

    @Test
    public void testChecksumMismatch() throws Exception {
        try {
            filter.complete(file, new NullLocal("f"), status(MD5, new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427f")), ProgressListener.noop);
            org.junit.Assert.fail();
        }
        catch(ChecksumException e) {
            assertEquals("Upload f failed", e.getMessage());
            assertEquals("Mismatch between MD5 hash d41d8cd98f00b204e9800998ecf8427e of transferred data and checksum d41d8cd98f00b204e9800998ecf8427f returned by the server.", e.getDetail());
        }
    }

    @Test
    public void testDifferentAlgorithm() throws Exception {
        filter.complete(file, new NullLocal("f"), status(MD5, new Checksum(HashAlgorithm.sha1, "da39a3ee5e6b4b0d3255bfef95601890afd80709")), ProgressListener.noop);
    }

    @Test
    public void testMissingChecksumInResponse() throws Exception {
        filter.complete(file, new NullLocal("f"), status(MD5, Checksum.NONE), ProgressListener.noop);
    }

    @Test
    public void testDigestNotCalculated() throws Exception {
        filter.complete(file, new NullLocal("f"), status(Checksum.NONE, MD5), ProgressListener.noop);
    }

    @Test
    public void testNotComplete() throws Exception {
        filter.complete(file, new NullLocal("f"), new TransferStatus().setDigest(MD5)
                .setResponse(new DefaultPathAttributes().setChecksum(new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427f"))), ProgressListener.noop);
    }

    @Test
    public void testChecksumMismatchVerificationDisabled() throws Exception {
        new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(host), new UploadFilterOptions(host).withChecksum(false)).complete(
                file, new NullLocal("f"), status(MD5, new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427f")), ProgressListener.noop);
    }

    @Test
    public void testPrepareVerify() throws Exception {
        assertTrue(filter.prepare(file, new NullLocal("f"), new TransferStatus(), ProgressListener.noop).isVerify());
    }

    @Test
    public void testPrepareVerifyDisabled() throws Exception {
        assertFalse(new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(host), new UploadFilterOptions(host).withChecksum(false))
                .prepare(file, new NullLocal("f"), new TransferStatus(), ProgressListener.noop).isVerify());
    }

    @Test
    public void testPrepareVerifyDirectory() throws Exception {
        assertFalse(filter.prepare(new Path("/d", EnumSet.of(Path.Type.directory)), new NullLocal("d"), new TransferStatus(), ProgressListener.noop).isVerify());
    }
}
