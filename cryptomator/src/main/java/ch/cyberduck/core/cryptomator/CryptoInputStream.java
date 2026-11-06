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
 * This program is distributed proxy the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.cryptomator.cryptolib.api.CryptoException;
import org.cryptomator.cryptolib.api.VaultFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Decrypts ciphertext chunks read from the proxy stream positioned at the beginning of a chunk after the file header.
 * When the end of the ciphertext is reached, the structure of the last chunks is validated.
 */
public class CryptoInputStream extends ProxyInputStream {

    private final InputStream proxy;
    private final VaultFile file;
    private final int ciphertextChunkSize;
    private final int cleartextChunkSize;
    /**
     * Vault format requires a trailing empty chunk when the size is a multiple of the chunk size
     */
    private final boolean eofChunk;

    private ByteBuffer buffer = ByteBuffer.allocate(0);

    /**
     * Index of next chunk read from proxy
     */
    private long chunkIndex;
    /**
     * An empty chunk is only valid in the final position
     */
    private boolean emptyChunkRead = false;
    /**
     * True when no chunk was read yet or the last chunk read was a full chunk
     */
    private boolean lastChunkFull = true;
    private boolean eof = false;

    /**
     * @param proxy            Ciphertext positioned at the beginning of the chunk with the given index
     * @param vault            Vault to determine chunk sizes and whether a trailing empty chunk is required
     * @param file             File opened with the header of this file. Closed when this stream is closed.
     * @param chunkIndexOffset Index of first chunk read
     */
    public CryptoInputStream(final InputStream proxy, final AbstractVault vault, final VaultFile file, final long chunkIndexOffset) {
        super(proxy);
        this.proxy = proxy;
        this.file = file;
        this.ciphertextChunkSize = vault.getCiphertextChunkSize();
        this.cleartextChunkSize = vault.getCleartextChunkSize();
        this.eofChunk = vault.numberOfChunks(0L) > 0;
        this.chunkIndex = chunkIndexOffset;
    }

    @Override
    public int read() throws IOException {
        final byte[] b = new byte[1];
        final int read = this.read(b, 0, 1);
        if(read == IOUtils.EOF) {
            return IOUtils.EOF;
        }
        return b[0] & 0xFF;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] dst, final int off, final int len) throws IOException {
        if(len == 0) {
            return 0;
        }
        // Skip empty trailing chunk
        while(!buffer.hasRemaining()) {
            if(!this.readNextChunk()) {
                return IOUtils.EOF;
            }
        }
        final int read = Math.min(len, buffer.remaining());
        buffer.get(dst, off, read);
        return read;
    }

    @Override
    public long skip(final long len) throws IOException {
        return IOUtils.skip(this, len);
    }

    /**
     * @return False when end of ciphertext is reached
     */
    private boolean readNextChunk() throws IOException {
        if(eof) {
            return false;
        }
        final ByteBuffer ciphertext = ByteBuffer.allocate(ciphertextChunkSize);
        final int read = IOUtils.read(proxy, ciphertext.array());
        if(read == 0) {
            eof = true;
            this.validate();
            return false;
        }
        if(emptyChunkRead) {
            // Reordered or spliced ciphertext
            throw this.failure(String.format("Empty chunk in non-final position %d", chunkIndex - 1), null);
        }
        ciphertext.limit(read);
        final ByteBuffer cleartext = ByteBuffer.allocate(cleartextChunkSize);
        try {
            file.decryptChunk(ciphertext, cleartext, chunkIndex++);
        }
        catch(CryptoException | IllegalArgumentException e) {
            throw this.failure(e.getMessage(), e);
        }
        cleartext.flip();
        buffer = cleartext;
        emptyChunkRead = !cleartext.hasRemaining();
        lastChunkFull = cleartext.remaining() == cleartextChunkSize;
        return true;
    }

    /**
     * Detect truncated file missing the trailing empty chunk
     */
    private void validate() throws IOException {
        if(eofChunk && !emptyChunkRead && lastChunkFull) {
            throw this.failure("Missing trailing empty chunk. File may be truncated", null);
        }
    }

    private IOException failure(final String message, final Throwable cause) {
        return new IOException(message, new CryptoAuthenticationException(message, cause));
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        }
        finally {
            file.close();
        }
    }
}
