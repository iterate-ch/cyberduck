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
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class CryptoInputStream extends InputStream {

    private final InputStream proxy;
    private final Cryptor cryptor;
    private final FileHeader header;

    private ByteBuffer buffer = ByteBuffer.allocate(0);

    /**
     * Position proxy content cryptor
     */
    private long chunkIndex = 0;
    private final int chunkSize;

    public CryptoInputStream(final InputStream proxy, final Cryptor cryptor, final FileHeader header) throws IOException {
        this.proxy = proxy;
        this.cryptor = cryptor;
        this.header = header;
        this.chunkSize = cryptor.fileContentCryptor().ciphertextChunkSize();
    }

    public InputStream getProxy() {
        return proxy;
    }

    @Override
    public int read() throws IOException {
        if(!buffer.hasRemaining()) {
            this.readNextChunk();
        }
        return buffer.get();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] dst, final int off, final int len) throws IOException {
        if(!buffer.hasRemaining()) {
            final int read = this.readNextChunk();
            if(read == IOUtils.EOF) {
                return IOUtils.EOF;
            }
        }
        final int read = Math.min(len, buffer.remaining());
        buffer.get(dst, off, read);
        return read;
    }

    private int readNextChunk() throws IOException {
        final ByteBuffer ciphertextBuf = ByteBuffer.allocate(chunkSize);
        final int read = IOUtils.read(proxy, ciphertextBuf.array());
        if(read == 0) {
            return IOUtils.EOF;
        }
        ciphertextBuf.position(read);
        ciphertextBuf.flip();
        buffer = cryptor.fileContentCryptor().decryptChunk(ciphertextBuf, chunkIndex++, header, true);
        return read;
    }
}