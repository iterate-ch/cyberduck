package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.impl.v8.MasterkeyVaultMetadataProvider;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(value = Parameterized.class)
public class CryptoTransferStatusTest extends AbstractCryptoTests {

    @Test
    public void testGetParentOfWrappedSegmentReturnsPlaintextLength() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final AbstractVault vault = new CryptomatorVault(home);
        vault.create(session, null, new MasterkeyVaultMetadataProvider(new VaultCredentials("test")));
        final Path file = new Path(home, "f", EnumSet.of(Path.Type.file));

        final int clearLengthPerChunk = vault.getFileContentCryptor().cleartextChunkSize();
        final long clearLength = clearLengthPerChunk + 100L;
        final long cipherLength = vault.toCiphertextSize(0L, clearLength);

        // Simulate CryptoUploadFeature#upload attaching the whole file ciphertext status
        final TransferStatus overall = new TransferStatus().setLength(clearLength);
        overall.setParent(new CryptoTransferStatus(vault, file, overall));
        assertEquals(clearLength, overall.getLength());
        // The correct ciphertext whole file length is only reachable through one more hop
        assertEquals(cipherLength, overall.getParent().getLength());

        // Simulate e.g. TusUploadFeature#submit creating a per segment status pointing at overall
        final TransferStatus segment = new TransferStatus()
                .setSegment(true)
                .setParent(overall)
                .setOffset(0L)
                .setLength(clearLengthPerChunk);
        assertEquals(clearLengthPerChunk, segment.getLength());
        assertEquals(clearLength, segment.getParent().getLength());

        // Simulate CryptoWriteFeature#write wrapping the segment status again
        final CryptoTransferStatus cipherSegment = new CryptoTransferStatus(vault, file, segment);

        // overall.getParent(), the true ciphertext whole file status.
        assertSame(overall.getParent(), cipherSegment.getParent());
        assertEquals(cipherLength, cipherSegment.getParent().getLength());
    }
}
