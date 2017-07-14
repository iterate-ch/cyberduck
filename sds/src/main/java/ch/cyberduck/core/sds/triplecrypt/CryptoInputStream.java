package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import eu.ssp_europe.sds.crypto.CryptoException;
import eu.ssp_europe.sds.crypto.FileDecryptionCipher;
import eu.ssp_europe.sds.crypto.model.EncryptedDataContainer;
import eu.ssp_europe.sds.crypto.model.PlainDataContainer;

public class CryptoInputStream extends ProxyInputStream {

    private final InputStream proxy;

    private ByteBuffer buffer = ByteBuffer.allocate(0);

    private final FileDecryptionCipher cipher;
    private final long length;

    private long read;
    private final int chunkSize = 16;

    public CryptoInputStream(final InputStream proxy, final FileDecryptionCipher cipher, final long length) throws IOException {
        super(proxy);
        this.proxy = proxy;
        this.cipher = cipher;
        this.length = length;
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
        this.read += read;
        if(read == 0) {
            return IOUtils.EOF;
        }
        ciphertextBuf.position(read);
        ciphertextBuf.flip();
        try {
            final EncryptedDataContainer eDataContainer = createEncryptedDataContainer(ciphertextBuf.array(), read);
            final PlainDataContainer pDataContainer = cipher.decryptBlock(eDataContainer, this.read == length);
            buffer.put(pDataContainer.getContent());
        }
        catch(CryptoException e) {
            throw new IOException(e);
        }
        return read;
    }

    private static EncryptedDataContainer createEncryptedDataContainer(final byte[] bytes, final int len) {
        final byte[] b = new byte[len];
        System.arraycopy(bytes, 0, b, 0, len);
        return new EncryptedDataContainer(b);
    }
}