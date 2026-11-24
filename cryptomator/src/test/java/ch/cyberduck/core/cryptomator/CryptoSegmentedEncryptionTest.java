package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.impl.uvf.DefaultUVFVaultMetadataProvider;
import ch.cyberduck.core.cryptomator.impl.uvf.UVFVault;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.random.NonceGenerator;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.cryptomator.cryptolib.api.Cvf8UnlockedVault;
import org.cryptomator.cryptolib.api.CvfMasterkey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * Encrypt segments of a file on their own as in a segmented upload and decrypt the concatenated ciphertext
 */
@RunWith(value = Parameterized.class)
public class CryptoSegmentedEncryptionTest extends AbstractCryptoTests {

    /**
     * Preferred segment length not aligned to the chunk size of any vault format
     */
    private static final long SEGMENT_LENGTH = 100000L;

    private AbstractVault getVault() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        switch(vaultVersion) {
            case UVF: {
                final AbstractVault vault = new UVFVault(home);
                FieldUtils.writeField(vault, "vault", new DefaultUVFVaultMetadataProvider(new VaultCredentials("test")).create(), true);
                return vault;
            }
            default: {
                final AbstractVault vault = new CryptomatorVault(home);
                try(CvfMasterkey masterkey = CvfMasterkey.generate(FastSecureRandomProvider.get().provide())) {
                    FieldUtils.writeField(vault, "vault", Cvf8UnlockedVault.create(masterkey,
                            CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD, FastSecureRandomProvider.get().provide()), true);
                }
                return vault;
            }
        }
    }

    @Test
    public void testSegmentsWithPartialLastChunk() throws Exception {
        final AbstractVault vault = this.getVault();
        this.roundtrip(vault, 2 * new TransferStatus().setSegmentAlignment(vault.getCleartextChunkSize()).alignSegment(SEGMENT_LENGTH) + 1000L);
        vault.close();
    }

    @Test
    public void testSegmentsWithFullLastChunk() throws Exception {
        final AbstractVault vault = this.getVault();
        // Last segment ends with a full chunk and requires a trailing empty chunk for UVF
        this.roundtrip(vault, 3 * new TransferStatus().setSegmentAlignment(vault.getCleartextChunkSize()).alignSegment(SEGMENT_LENGTH));
        vault.close();
    }

    private void roundtrip(final AbstractVault vault, final long length) throws Exception {
        final byte[] cleartext = RandomUtils.nextBytes((int) length);
        final ByteBuffer header = vault.createHeader();
        final NonceGenerator nonces = new RandomNonceGenerator(vault.getNonceSize());
        final TransferStatus overall = new TransferStatus().setLength(length).setSegmentAlignment(vault.getCleartextChunkSize());
        final ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();
        long offset = 0L;
        long remaining = length;
        int segments = 0;
        while(remaining > 0) {
            // Segment length as determined by upload services
            final long segmentLength = Math.min(overall.alignSegment(SEGMENT_LENGTH), remaining);
            final TransferStatus segment = new TransferStatus().setParent(overall).setSegment(true).setOffset(offset).setLength(segmentLength);
            final boolean last = CryptoTransferStatus.isLastSegment(segment);
            final ByteArrayOutputStream part = new ByteArrayOutputStream();
            if(0L == offset) {
                part.write(header.array());
            }
            try(CryptoOutputStream out = new CryptoOutputStream(new ProxyOutputStream(part), vault, vault.openFile(header),
                    nonces, vault.chunkIndex(offset), last)) {
                out.write(cleartext, (int) offset, (int) segmentLength);
            }
            assertEquals(vault.toCiphertextSize(offset, segmentLength, last), part.size());
            assertEquals(vault.toCiphertextOffset(offset), ciphertext.size());
            ciphertext.write(part.toByteArray());
            offset += segmentLength;
            remaining -= segmentLength;
            segments++;
        }
        assertEquals(3, segments);
        final byte[] encrypted = ciphertext.toByteArray();
        assertEquals(vault.toCiphertextSize(0L, length), encrypted.length);
        final int headerSize = vault.getHeaderSize();
        try(CryptoInputStream in = new CryptoInputStream(new ByteArrayInputStream(encrypted, headerSize, encrypted.length - headerSize),
                vault, vault.openFile(ByteBuffer.wrap(Arrays.copyOf(encrypted, headerSize))), 0L)) {
            assertArrayEquals(cleartext, IOUtils.toByteArray(in));
        }
    }
}
