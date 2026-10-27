package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.random.NonceGenerator;

import org.cryptomator.cryptolib.api.CryptoException;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Stream of the encrypted content of a file with the header followed by the encrypted chunks. Skipping over whole
 * chunks does not encrypt the content, allowing efficient access to an arbitrary offset in the ciphertext.
 */
public class CiphertextInputStream extends InputStream {

    private final InputStream proxy;
    private final FileContentCryptor cryptor;
    private final FileHeader header;
    private final NonceGenerator nonces;
    private final int cleartextChunkSize;
    private final int ciphertextChunkSize;

    /**
     * Encrypted content not yet read, starting with the header
     */
    private byte[] buffer;
    private int position;
    private long chunkIndex;
    private boolean eof;

    /**
     * @param proxy      Cleartext content from the start of the file
     * @param cryptor    Content cryptor
     * @param header     File header
     * @param encrypted  Encrypted file header written at the start of the ciphertext
     * @param nonces     Nonce for a chunk by index
     */
    public CiphertextInputStream(final InputStream proxy, final FileContentCryptor cryptor, final FileHeader header,
                                 final ByteBuffer encrypted, final NonceGenerator nonces) {
        this.proxy = proxy;
        this.cryptor = cryptor;
        this.header = header;
        this.nonces = nonces;
        this.cleartextChunkSize = cryptor.cleartextChunkSize();
        this.ciphertextChunkSize = cryptor.ciphertextChunkSize();
        this.buffer = Arrays.copyOf(encrypted.array(), encrypted.array().length);
    }

    @Override
    public int read() throws IOException {
        final byte[] b = new byte[1];
        if(-1 == this.read(b, 0, 1)) {
            return -1;
        }
        return b[0] & 0xFF;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if(0 == len) {
            return 0;
        }
        if(!this.available0()) {
            return -1;
        }
        final int read = Math.min(len, buffer.length - position);
        System.arraycopy(buffer, position, b, off, read);
        position += read;
        return read;
    }

    @Override
    public long skip(final long n) throws IOException {
        long remaining = n;
        while(remaining > 0) {
            if(position < buffer.length) {
                final int skipped = (int) Math.min(remaining, buffer.length - position);
                position += skipped;
                remaining -= skipped;
            }
            else if(eof) {
                break;
            }
            else if(remaining >= ciphertextChunkSize) {
                // Skip a whole chunk without encrypting the content
                final long skipped = this.skipFully(cleartextChunkSize);
                if(skipped < cleartextChunkSize) {
                    eof = true;
                }
                if(skipped > 0) {
                    remaining -= Math.min(remaining, cryptor.ciphertextSize(skipped));
                    chunkIndex++;
                }
            }
            else if(!this.available0()) {
                break;
            }
        }
        return n - remaining;
    }

    /**
     * @return False if no more content is available
     */
    private boolean available0() throws IOException {
        while(position >= buffer.length) {
            if(eof) {
                return false;
            }
            final byte[] cleartext = new byte[cleartextChunkSize];
            int read = 0;
            while(read < cleartextChunkSize) {
                final int r = proxy.read(cleartext, read, cleartextChunkSize - read);
                if(-1 == r) {
                    eof = true;
                    break;
                }
                read += r;
            }
            if(read > 0) {
                try {
                    final long index = chunkIndex++;
                    final ByteBuffer encrypted = cryptor.encryptChunk(
                        ByteBuffer.wrap(cleartext, 0, read), index, header, nonces.next(index));
                    buffer = new byte[encrypted.remaining()];
                    encrypted.get(buffer);
                    position = 0;
                }
                catch(CryptoException e) {
                    throw new IOException(e.getMessage(), new CryptoAuthenticationException(e.getMessage(), e));
                }
            }
            else {
                buffer = new byte[0];
                position = 0;
            }
        }
        return true;
    }

    private long skipFully(final long n) throws IOException {
        long skipped = 0;
        while(skipped < n) {
            final long s = proxy.skip(n - skipped);
            if(s <= 0) {
                // Skip may return zero before the end of the stream
                if(-1 == proxy.read()) {
                    break;
                }
                skipped++;
            }
            else {
                skipped += s;
            }
        }
        return skipped;
    }

    @Override
    public int available() {
        return buffer.length - position;
    }

    @Override
    public void close() throws IOException {
        proxy.close();
    }
}
