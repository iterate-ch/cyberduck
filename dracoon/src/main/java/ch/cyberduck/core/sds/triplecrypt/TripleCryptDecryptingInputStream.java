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

import ch.cyberduck.core.sds.SDSSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.dracoon.sdk.crypto.FileDecryptionCipher;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.model.EncryptedDataContainer;
import com.dracoon.sdk.crypto.model.PlainDataContainer;

public class TripleCryptDecryptingInputStream extends ProxyInputStream {

    private final InputStream proxy;
    private final FileDecryptionCipher cipher;
    private final byte[] tag;

    private ByteBuffer buffer = ByteBuffer.allocate(0);
    private long lastread = -1;

    public TripleCryptDecryptingInputStream(final InputStream proxy, final FileDecryptionCipher cipher, final byte[] tag) {
        super(proxy);
        this.proxy = proxy;
        this.cipher = cipher;
        this.tag = tag;
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
        final ByteBuffer dest = ByteBuffer.allocate(len);
        int remaining = len;
        while(remaining > 0) {
            final int location = len - remaining;
            final int count = this.read(dest, off + location, remaining);
            if(IOUtils.EOF == count) {
                if(remaining == len) {
                    // nothing read before
                    return IOUtils.EOF;
                }
                break;
            }
            dest.get(dst, off + location, count);
            remaining -= count;
        }
        return len - remaining;
    }

    private int read(final ByteBuffer dst, final int off, final int len) throws IOException {
        if(!buffer.hasRemaining()) {
            final int read = this.readNextChunk();
            if(read == IOUtils.EOF) {
                return IOUtils.EOF;
            }
        }
        final int read = Math.min(len, buffer.remaining());
        System.arraycopy(buffer.array(), buffer.position(), dst.array(), off, read);
        buffer.position(buffer.position() + read);
        return read;
    }

    private int readNextChunk() throws IOException {
        final ByteBuffer ciphertextBuf = ByteBuffer.allocate(SDSSession.DEFAULT_CHUNKSIZE);
        final int read = IOUtils.read(proxy, ciphertextBuf.array());
        if(lastread == 0) {
            return IOUtils.EOF;
        }
        ciphertextBuf.position(read);
        ciphertextBuf.flip();
        try {
            final PlainDataContainer pDataContainer;
            if(read == 0) {
                final PlainDataContainer c1 = cipher.processBytes(createEncryptedDataContainer(ciphertextBuf.array(), read, null));
                final PlainDataContainer c2 = cipher.doFinal(new EncryptedDataContainer(null, tag));
                pDataContainer = new PlainDataContainer(ArrayUtils.addAll(c1.getContent(), c2.getContent()));
            }
            else {
                pDataContainer = cipher.processBytes(createEncryptedDataContainer(ciphertextBuf.array(), read, null));
            }
            final byte[] content = pDataContainer.getContent();
            buffer = ByteBuffer.allocate(content.length);
            buffer.put(content);
            buffer.flip();
            lastread = read;
            return content.length;
        }
        catch(CryptoException e) {
            throw new IOException(e);
        }
    }

    private static EncryptedDataContainer createEncryptedDataContainer(final byte[] bytes, final int len, final byte[] tag) {
        final byte[] b = new byte[len];
        System.arraycopy(bytes, 0, b, 0, len);
        return new EncryptedDataContainer(b, tag);
    }

    @Override
    public long skip(final long len) throws IOException {
        return IOUtils.skip(this, len);
    }
}
