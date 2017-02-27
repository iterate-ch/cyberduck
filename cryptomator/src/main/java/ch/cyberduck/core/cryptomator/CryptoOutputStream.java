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
import ch.cyberduck.core.io.StatusOutputStream;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CryptoOutputStream<Reply> extends StatusOutputStream<Reply> {

    private final StatusOutputStream<Reply> proxy;
    private final Cryptor cryptor;
    private final FileHeader header;
    private final ByteBuffer buffer;

    /**
     * Position proxy content cryptor
     */
    private long chunkIndex = 0;

    public CryptoOutputStream(final StatusOutputStream<Reply> proxy, final Cryptor cryptor, final FileHeader header) {
        super(proxy);
        this.proxy = proxy;
        this.cryptor = cryptor;
        this.header = header;
        this.buffer = ByteBuffer.allocate(cryptor.fileContentCryptor().cleartextChunkSize());
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

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        int toWrite = len;
        while(toWrite > 0) {
            final int write = Math.min(toWrite, buffer.remaining());
            buffer.put(b, off, write);
            if(buffer.remaining() == 0) {
                this.encryptAndWriteBuffer();
                buffer.clear();
            }
            toWrite -= write;
        }
    }

    private void encryptAndWriteBuffer() throws IOException {
        buffer.flip();
        if(buffer.remaining() == 0) {
            return;
        }
        final ByteBuffer encryptedChunk = cryptor.fileContentCryptor().encryptChunk(buffer, chunkIndex++, header);
        proxy.write(encryptedChunk.array());
    }

    @Override
    public void close() throws IOException {
        this.encryptAndWriteBuffer();
        proxy.close();
    }
}
