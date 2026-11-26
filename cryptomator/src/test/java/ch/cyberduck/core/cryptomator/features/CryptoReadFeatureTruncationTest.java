package ch.cyberduck.core.cryptomator.features;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.AbstractCryptoTests;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.CryptoAuthenticationException;
import ch.cyberduck.core.cryptomator.CryptoOutputStream;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Downloads stop reading after the expected cleartext length. A file ending on a full chunk must still be checked for
 * the trailing empty chunk to detect truncation at a chunk boundary.
 */
@RunWith(Parameterized.class)
public class CryptoReadFeatureTruncationTest extends AbstractCryptoTests {

    /**
     * Internal vault path to skip filename encryption
     */
    private static final Path FILE = new Path("/vault/f", EnumSet.of(Path.Type.file, Path.Type.vault));

    @Test
    public void testDownloadComplete() throws Exception {
        final AbstractVault vault = this.unlockedVault();
        final byte[] cleartext = RandomUtils.nextBytes(2 * vault.getCleartextChunkSize());
        final byte[] ciphertext = this.encrypt(vault, cleartext);
        assertArrayEquals(cleartext, this.download(vault, ciphertext, new TransferStatus().setLength(cleartext.length)));
        vault.close();
    }

    @Test
    public void testDownloadMissingTrailingChunk() throws Exception {
        assumeTrue(VaultVersion.Type.UVF == vaultVersion);
        final AbstractVault vault = this.unlockedVault();
        final byte[] cleartext = RandomUtils.nextBytes(2 * vault.getCleartextChunkSize());
        final byte[] truncated = this.truncate(vault, this.encrypt(vault, cleartext));
        try {
            this.download(vault, truncated, new TransferStatus().setLength(cleartext.length));
            fail();
        }
        catch(BackgroundException e) {
            assertTruncation(e);
        }
        vault.close();
    }

    @Test
    public void testDownloadLastSegmentMissingTrailingChunk() throws Exception {
        assumeTrue(VaultVersion.Type.UVF == vaultVersion);
        final AbstractVault vault = this.unlockedVault();
        final int chunk = vault.getCleartextChunkSize();
        final byte[] truncated = this.truncate(vault, this.encrypt(vault, RandomUtils.nextBytes(2 * chunk)));
        final TransferStatus overall = new TransferStatus().setLength(2L * chunk);
        try {
            this.download(vault, truncated, new TransferStatus().setParent(overall).setSegment(true).setAppend(true).setOffset(chunk).setLength(chunk));
            fail();
        }
        catch(BackgroundException e) {
            assertTruncation(e);
        }
        vault.close();
    }

    @Test
    public void testDownloadFirstSegment() throws Exception {
        final AbstractVault vault = this.unlockedVault();
        final int chunk = vault.getCleartextChunkSize();
        final byte[] cleartext = RandomUtils.nextBytes(2 * chunk);
        // Segment ending on a chunk boundary followed by other segments is not checked for the trailing empty chunk
        final byte[] truncated = this.truncate(vault, this.encrypt(vault, cleartext));
        final TransferStatus overall = new TransferStatus().setLength(2L * chunk);
        assertArrayEquals(Arrays.copyOf(cleartext, chunk),
                this.download(vault, truncated, new TransferStatus().setParent(overall).setSegment(true).setAppend(true).setOffset(0L).setLength(chunk)));
        vault.close();
    }

    private static void assertTruncation(final BackgroundException e) {
        assertEquals(CryptoAuthenticationException.class, e.getClass());
        assertTrue(e.getDetail().contains("Missing trailing empty chunk"));
    }

    private byte[] encrypt(final AbstractVault vault, final byte[] cleartext) throws Exception {
        final ByteBuffer header = vault.createHeader();
        final ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();
        ciphertext.write(header.array());
        try(CryptoOutputStream out = new CryptoOutputStream(ciphertext, vault, vault.openFile(header), new RandomNonceGenerator(vault.getNonceSize()), 0L)) {
            out.write(cleartext);
        }
        return ciphertext.toByteArray();
    }

    /**
     * @return Ciphertext without the trailing empty chunk
     */
    private byte[] truncate(final AbstractVault vault, final byte[] ciphertext) {
        return Arrays.copyOf(ciphertext, ciphertext.length - (vault.getCiphertextChunkSize() - vault.getCleartextChunkSize()));
    }

    /**
     * Read as in DefaultDownloadFeature stopping at the expected length
     */
    private byte[] download(final AbstractVault vault, final byte[] ciphertext, final TransferStatus status) throws Exception {
        final Read proxy = new Read() {
            @Override
            public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) {
                final int offset = (int) status.getOffset();
                final int length = TransferStatus.UNKNOWN_LENGTH == status.getLength() ?
                        ciphertext.length - offset : (int) Math.min(status.getLength(), ciphertext.length - offset);
                return new ByteArrayInputStream(ciphertext, offset, length);
            }

            @Override
            public boolean offset(final Path file) {
                return true;
            }
        };
        final InputStream in = new CryptoReadFeature(new NullSession(new Host(new TestProtocol())), proxy, vault).read(FILE, status, null);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new StreamCopier(status, status).withOffset(0L).withLimit(status.getLength()).transfer(in, out);
        return out.toByteArray();
    }
}
