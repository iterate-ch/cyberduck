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

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class CryptoOutputStream extends OutputStream {

    private final OutputStream proxy;

    private final Cryptor cryptor;

    private final FileHeader header;

    private ByteBuffer buffer;

    /**
     * Position proxy content cryptor
     */
    private long chunkIndex = 0;
    private final int payloadSize;

    public CryptoOutputStream(final OutputStream proxy, final Cryptor cryptor, final FileHeader header) {
        this.proxy = proxy;
        this.cryptor = cryptor;
        this.header = header;
        this.payloadSize = cryptor.fileContentCryptor().cleartextChunkSize();
        this.buffer = ByteBuffer.allocate(payloadSize);
    }

    @Override
    public void write(final int b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        int toWrite = len;
        while(toWrite > 0) {
            final int write = Math.min(toWrite, buffer.remaining());
            buffer.put(b, buffer.position(), write);
            if(buffer.remaining() == 0) {
                this.encryptAndWriteBuffer();
                buffer = ByteBuffer.allocate(payloadSize);
            }
            toWrite -= write;
        }
    }

    private void encryptAndWriteBuffer() throws IOException {
        buffer.flip();
        final ByteBuffer encryptedChunk = cryptor.fileContentCryptor().encryptChunk(buffer, chunkIndex++, header);
        proxy.write(encryptedChunk.array());
    }

    @Override
    public void close() throws IOException {
        this.encryptAndWriteBuffer();
        proxy.close();
    }
}
