package ch.cyberduck.core.ctera.directio;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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

public class DirectIOInputStream extends ProxyInputStream {

    private InputStream decryptedInputStream;
    private final Decryptor decryptor;
    private final EncryptInfo encryptInfo;

    public DirectIOInputStream(final InputStream proxy, final EncryptInfo encryptInfo) {
        super(proxy);
        this.decryptor = new Decryptor();
        this.encryptInfo = encryptInfo;
    }

    @Override
    public int read() throws IOException {
        final byte[] b = new byte[1];
        return this.read(b) == IOUtils.EOF ? IOUtils.EOF : b[0];
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] dst, final int off, final int len) throws IOException {
        this.initStream();
        return decryptedInputStream.read(dst, off, len);
    }

    private void initStream() throws IOException {
        if(decryptedInputStream == null) {
            this.readNextChunk();
        }
    }

    @Override
    public long skip(final long len) throws IOException {
        return IOUtils.skip(this, len);
    }

    private void readNextChunk() throws IOException {
        decryptedInputStream = decryptor.decryptData(this.in, encryptInfo);
    }
}

