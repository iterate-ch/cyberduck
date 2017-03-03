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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.SegmentingOutputStream;
import ch.cyberduck.core.io.StatusOutputStream;

import org.apache.commons.io.output.ProxyOutputStream;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CryptoOutputStream<Reply> extends StatusOutputStream<Reply> {

    private final StatusOutputStream<Reply> proxy;

    public CryptoOutputStream(final StatusOutputStream<Reply> proxy, final Cryptor cryptor, final FileHeader header) {
        super(new SegmentingOutputStream(new EncrpytingOutputStream(proxy, cryptor, header), cryptor.fileContentCryptor().cleartextChunkSize()));
        this.proxy = proxy;
    }

    @Override
    public void write(final int b) throws IOException {
        throw new IOException(new UnsupportedOperationException());
    }

    @Override
    public Reply getStatus() throws BackgroundException {
        return proxy.getStatus();
    }

    @Override
    public void write(final byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    private static final class EncrpytingOutputStream extends ProxyOutputStream {
        private final Cryptor cryptor;
        private final FileHeader header;
        private final int chunksize;

        /**
         * Position proxy content cryptor
         */
        private long chunkIndex = 0;

        public EncrpytingOutputStream(final OutputStream proxy, final Cryptor cryptor, final FileHeader header) {
            super(proxy);
            this.cryptor = cryptor;
            this.header = header;
            this.chunksize = cryptor.fileContentCryptor().cleartextChunkSize();
        }

        @Override
        public void write(final byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            for(int chunkOffset = off; chunkOffset < len; chunkOffset += chunksize) {
                int chunkLen = Math.min(chunksize, len - chunkOffset);
                final ByteBuffer encryptedChunk = cryptor.fileContentCryptor().encryptChunk(
                        ByteBuffer.wrap(Arrays.copyOfRange(b, chunkOffset, chunkOffset + chunkLen)),
                        chunkIndex++, header);
                super.write(encryptedChunk.array());
            }
        }
    }
}
