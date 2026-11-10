package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.random.NonceGenerator;

import org.apache.commons.io.output.ProxyOutputStream;
import org.cryptomator.cryptolib.api.CryptoException;
import org.cryptomator.cryptolib.api.VaultFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Encrypts cleartext written in chunks of the vault's cleartext chunk size. Does not write the file header.
 */
public class CryptoOutputStream extends ProxyOutputStream {

    /**
     * @param proxy            Ciphertext output
     * @param vault            Vault to determine chunk sizes and whether a trailing empty chunk is required
     * @param file             File opened with the header of this file. Closed when this stream is closed.
     * @param nonces           Nonce for each chunk
     * @param chunkIndexOffset Index of first chunk written
     */
    public CryptoOutputStream(final OutputStream proxy, final AbstractVault vault, final VaultFile file,
                              final NonceGenerator nonces, final long chunkIndexOffset) {
        this(proxy, vault, file, nonces, chunkIndexOffset, true);
    }

    /**
     * @param last False for a segment followed by other segments of the same file. No trailing empty chunk is written.
     */
    public CryptoOutputStream(final OutputStream proxy, final AbstractVault vault, final VaultFile file,
                              final NonceGenerator nonces, final long chunkIndexOffset, final boolean last) {
        super(new MemorySegementingOutputStream(new EncryptingOutputStream(proxy, file, nonces, chunkIndexOffset,
                vault.getCleartextChunkSize(), vault.getCiphertextChunkSize(), last && vault.numberOfChunks(0L) > 0),
                vault.getCleartextChunkSize()));
    }

    @Override
    public void write(final int b) throws IOException {
        throw new IOException(new UnsupportedOperationException());
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    private static final class EncryptingOutputStream extends ProxyOutputStream {
        private final VaultFile file;
        private final NonceGenerator nonces;
        private final int cleartextChunkSize;
        private final int ciphertextChunkSize;
        /**
         * Vault format requires a trailing empty chunk when the size is a multiple of the chunk size
         */
        private final boolean eofChunk;

        private long chunkIndex;
        /**
         * True when no chunk was written yet or the last chunk written was a full chunk
         */
        private boolean pendingEofChunk = true;

        public EncryptingOutputStream(final OutputStream proxy, final VaultFile file, final NonceGenerator nonces, final long chunkIndexOffset,
                                      final int cleartextChunkSize, final int ciphertextChunkSize, final boolean eofChunk) {
            super(proxy);
            this.file = file;
            this.nonces = nonces;
            this.chunkIndex = chunkIndexOffset;
            this.cleartextChunkSize = cleartextChunkSize;
            this.ciphertextChunkSize = ciphertextChunkSize;
            this.eofChunk = eofChunk;
        }

        @Override
        public void write(final byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            for(int chunkOffset = off; chunkOffset < off + len; chunkOffset += cleartextChunkSize) {
                final int chunkLen = Math.min(cleartextChunkSize, off + len - chunkOffset);
                this.encrypt(ByteBuffer.wrap(b, chunkOffset, chunkLen));
                pendingEofChunk = chunkLen == cleartextChunkSize;
            }
        }

        private void encrypt(final ByteBuffer cleartext) throws IOException {
            try {
                final ByteBuffer ciphertext = ByteBuffer.allocate(ciphertextChunkSize);
                file.encryptChunk(cleartext, ciphertext, chunkIndex++, nonces.next());
                ciphertext.flip();
                super.write(ciphertext.array(), ciphertext.arrayOffset(), ciphertext.remaining());
            }
            catch(CryptoException | IllegalArgumentException e) {
                throw new IOException(e.getMessage(), new CryptoAuthenticationException(e.getMessage(), e));
            }
        }

        @Override
        public void close() throws IOException {
            try {
                if(eofChunk && pendingEofChunk) {
                    // Authenticates the file size
                    this.encrypt(ByteBuffer.allocate(0));
                    pendingEofChunk = false;
                }
                super.close();
            }
            finally {
                file.close();
            }
        }
    }
}
