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
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CryptoOutputStream extends ProxyOutputStream {

    public CryptoOutputStream(final OutputStream proxy, final FileContentCryptor cryptor, final FileHeader header,
                              final NonceGenerator nonces, final long chunkIndexOffset) {
        super(new MemorySegementingOutputStream(new EncryptingOutputStream(proxy, cryptor, header, nonces, chunkIndexOffset),
                cryptor.cleartextChunkSize()));
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
        private final FileContentCryptor cryptor;
        private final FileHeader header;
        private final int chunksize;
        private final NonceGenerator nonces;
        private long chunkIndexOffset;

        public EncryptingOutputStream(final OutputStream proxy, final FileContentCryptor cryptor, final FileHeader header,
                                      final NonceGenerator nonces, final long chunkIndexOffset) {
            super(proxy);
            this.cryptor = cryptor;
            this.header = header;
            this.chunksize = cryptor.cleartextChunkSize();
            this.nonces = nonces;
            this.chunkIndexOffset = chunkIndexOffset;
        }

        @Override
        public void write(final byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            try {
                for(int chunkOffset = off; chunkOffset < len; chunkOffset += chunksize) {
                    int chunkLen = Math.min(chunksize, len - chunkOffset);
                    final ByteBuffer encryptedChunk = cryptor.encryptChunk(
                        ByteBuffer.wrap(Arrays.copyOfRange(b, chunkOffset, chunkOffset + chunkLen)),
                        chunkIndexOffset++, header, nonces.next());
                    final byte[] encrypted = new byte[encryptedChunk.remaining()];
                    encryptedChunk.get(encrypted);
                    super.write(encrypted);
                }
            }
            catch(CryptoException e) {
                throw new IOException(e.getMessage(), new CryptoAuthenticationException(e.getMessage(), e));
            }
        }
    }
}
