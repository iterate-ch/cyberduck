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
import ch.cyberduck.core.NullWriteFeature;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DisabledUploadSymlinkResolver;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractUploadFilterTest {

    private static final Checksum MD5 = new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427e");
    private static final Checksum MD5_OTHER = new Checksum(HashAlgorithm.md5, "d41d8cd98f00b204e9800998ecf8427f");
    private static final Checksum SHA1 = new Checksum(HashAlgorithm.sha1, "da39a3ee5e6b4b0d3255bfef95601890afd80709");

    private final Host host = new Host(new TestProtocol());
    private final Path file = new Path("/f", EnumSet.of(Path.Type.file));

    private OverwriteFilter filter(final boolean checksum, final boolean flag) {
        return new OverwriteFilter(new DisabledUploadSymlinkResolver(), new NullSession(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Write.class) {
                    return (T) new NullWriteFeature() {
                        @Override
                        public EnumSet<Flags> features(final Path file) {
                            return flag ? EnumSet.of(Flags.checksum) : EnumSet.noneOf(Flags.class);
                        }

                        @Override
                        public ChecksumCompute checksum(final Path file, final TransferStatus status) {
                            return new MD5ChecksumCompute();
                        }
                    };
                }
                return super._getFeature(type);
            }
        }, new UploadFilterOptions(host).withChecksum(checksum));
    }

    /**
     * Checksum calculated while streaming
     */
    private TransferStatus streamed(final Checksum digest, final Checksum response) {
        return new TransferStatus().setVerify(true).setDigest(digest)
                .setResponse(new DefaultPathAttributes().setChecksum(response)).setComplete();
    }

    /**
     * Checksum calculated prior to the transfer
     */
    private TransferStatus precalculated(final Checksum checksum, final Checksum response) {
        return new TransferStatus().setChecksum(checksum)
                .setResponse(new DefaultPathAttributes().setChecksum(response)).setComplete();
    }

    private void assertMismatch(final TransferStatus status) throws Exception {
        try {
            this.filter(true, false).complete(file, new NullLocal("f"), status, ProgressListener.noop);
            fail();
        }
        catch(ChecksumException e) {
            assertEquals("Upload f failed", e.getMessage());
            assertEquals("Mismatch between MD5 hash d41d8cd98f00b204e9800998ecf8427e of transferred data and checksum d41d8cd98f00b204e9800998ecf8427f returned by the server.", e.getDetail());
        }
    }

    @Test
    public void testDigestMatch() throws Exception {
        this.filter(true, false).complete(file, new NullLocal("f"), this.streamed(MD5, MD5), ProgressListener.noop);
    }

    @Test
    public void testDigestMismatch() throws Exception {
        this.assertMismatch(this.streamed(MD5, MD5_OTHER));
    }

    @Test
    public void testDigestDifferentAlgorithm() throws Exception {
        this.filter(true, false).complete(file, new NullLocal("f"), this.streamed(MD5, SHA1), ProgressListener.noop);
    }

    @Test
    public void testDigestMissingChecksumInResponse() throws Exception {
        this.filter(true, false).complete(file, new NullLocal("f"), this.streamed(MD5, Checksum.NONE), ProgressListener.noop);
    }

    @Test
    public void testDigestNotCalculated() throws Exception {
        this.filter(true, false).complete(file, new NullLocal("f"), this.streamed(Checksum.NONE, MD5), ProgressListener.noop);
    }

    @Test
    public void testPrecalculatedMatch() throws Exception {
        this.filter(true, true).complete(file, new NullLocal("f"), this.precalculated(MD5, MD5), ProgressListener.noop);
    }

    @Test
    public void testPrecalculatedMismatch() throws Exception {
        this.assertMismatch(this.precalculated(MD5, MD5_OTHER));
    }

    @Test
    public void testPrecalculatedDifferentAlgorithm() throws Exception {
        this.filter(true, true).complete(file, new NullLocal("f"), this.precalculated(MD5, SHA1), ProgressListener.noop);
    }

    @Test
    public void testPrecalculatedMissingChecksumInResponse() throws Exception {
        this.filter(true, true).complete(file, new NullLocal("f"), this.precalculated(MD5, Checksum.NONE), ProgressListener.noop);
    }

    @Test
    public void testPrecalculatedNotCalculated() throws Exception {
        this.filter(true, true).complete(file, new NullLocal("f"), this.precalculated(Checksum.NONE, MD5), ProgressListener.noop);
    }

    @Test
    public void testDigestIgnoredWithoutVerify() throws Exception {
        // Checksum calculated prior to the transfer is verified when not calculated while streaming
        this.filter(true, true).complete(file, new NullLocal("f"), new TransferStatus().setDigest(MD5_OTHER).setChecksum(MD5)
                .setResponse(new DefaultPathAttributes().setChecksum(MD5)).setComplete(), ProgressListener.noop);
    }

    @Test
    public void testChecksumIgnoredWithVerify() throws Exception {
        // Checksum calculated while streaming is verified
        this.filter(true, false).complete(file, new NullLocal("f"), new TransferStatus().setVerify(true).setDigest(MD5).setChecksum(MD5_OTHER)
                .setResponse(new DefaultPathAttributes().setChecksum(MD5)).setComplete(), ProgressListener.noop);
    }

    @Test
    public void testNotComplete() throws Exception {
        this.filter(true, false).complete(file, new NullLocal("f"), new TransferStatus().setVerify(true).setDigest(MD5)
                .setResponse(new DefaultPathAttributes().setChecksum(MD5_OTHER)), ProgressListener.noop);
    }

    @Test
    public void testMismatchVerificationDisabled() throws Exception {
        this.filter(false, false).complete(file, new NullLocal("f"), this.streamed(MD5, MD5_OTHER), ProgressListener.noop);
        this.filter(false, true).complete(file, new NullLocal("f"), this.precalculated(MD5, MD5_OTHER), ProgressListener.noop);
    }

    @Test
    public void testPrepareVerifyWithoutChecksumFlag() throws Exception {
        final TransferStatus status = this.filter(true, false).prepare(file, new NullLocal("f"), new TransferStatus(), ProgressListener.noop);
        assertTrue(status.isVerify());
        assertEquals(Checksum.NONE, status.getChecksum());
    }

    @Test
    public void testPrepareChecksumWithChecksumFlag() throws Exception {
        final TransferStatus status = this.filter(true, true).prepare(file, new NullLocal("f"), new TransferStatus(), ProgressListener.noop);
        assertFalse(status.isVerify());
        assertEquals(HashAlgorithm.md5, status.getChecksum().algorithm);
    }

    @Test
    public void testPrepareDisabled() throws Exception {
        for(boolean flag : new boolean[]{true, false}) {
            final TransferStatus status = this.filter(false, flag).prepare(file, new NullLocal("f"), new TransferStatus(), ProgressListener.noop);
            assertFalse(status.isVerify());
            assertEquals(Checksum.NONE, status.getChecksum());
        }
    }

    @Test
    public void testPrepareDirectory() throws Exception {
        for(boolean flag : new boolean[]{true, false}) {
            final TransferStatus status = this.filter(true, flag).prepare(new Path("/d", EnumSet.of(Path.Type.directory)), new NullLocal("d"), new TransferStatus(), ProgressListener.noop);
            assertFalse(status.isVerify());
            assertEquals(Checksum.NONE, status.getChecksum());
        }
    }
}
