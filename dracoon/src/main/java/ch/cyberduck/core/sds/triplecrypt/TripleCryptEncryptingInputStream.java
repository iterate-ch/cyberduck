package ch.cyberduck.core.sds.triplecrypt;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.dracoon.sdk.crypto.FileEncryptionCipher;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.model.EncryptedDataContainer;
import com.dracoon.sdk.crypto.model.PlainDataContainer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class TripleCryptEncryptingInputStream extends ProxyInputStream {
    private static final Logger log = LogManager.getLogger(TripleCryptEncryptingInputStream.class);

    private final SDSSession session;
    private final InputStream proxy;
    private final FileEncryptionCipher cipher;
    private final TransferStatus status;

    // Buffer for encrypted content waiting to be read
    private ByteBuffer buffer = ByteBuffer.allocate(0);
    private boolean eof = false;

    /**
     * @param proxy the cleartext InputStream to use as source
     */
    public TripleCryptEncryptingInputStream(final SDSSession session, final InputStream proxy,
                                            final FileEncryptionCipher cipher, final TransferStatus status) {
        super(proxy);
        this.session = session;
        this.proxy = proxy;
        this.cipher = cipher;
        this.status = status;
    }

    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        try {
            int read = 0;
            if(buffer.hasRemaining()) {
                // Buffer still has encrypted content available
                read = Math.min(len, buffer.remaining());
                System.arraycopy(buffer.array(), buffer.position(), b, off, read);
                buffer.position(buffer.position() + read);
            }
            else if(eof) {
                // Buffer is consumed and EOF flag set
                return IOUtils.EOF;
            }
            if(buffer.hasRemaining()) {
                return read;
            }
            if(!eof) {
                this.fillBuffer(len);
            }
            return read;
        }
        catch(CryptoException e) {
            throw new IOException(e);
        }
    }

    private void fillBuffer(final int len) throws IOException, CryptoException {
        // Read ahead to the next chunk end
        final int allocate = (len / IOUtils.DEFAULT_BUFFER_SIZE) * IOUtils.DEFAULT_BUFFER_SIZE + IOUtils.DEFAULT_BUFFER_SIZE;
        buffer = ByteBuffer.allocate(allocate);
        final byte[] plain = new byte[allocate];
        final int read = IOUtils.read(proxy, plain, 0, allocate);
        if(read > 0) {
            int position = 0;
            for(int chunkOffset = 0; chunkOffset < read; chunkOffset += SDSSession.DEFAULT_CHUNKSIZE) {
                int chunkLen = Math.min(SDSSession.DEFAULT_CHUNKSIZE, read - chunkOffset);
                final byte[] bytes = Arrays.copyOfRange(plain, chunkOffset, chunkOffset + chunkLen);
                final PlainDataContainer data = TripleCryptKeyPair.createPlainDataContainer(bytes, bytes.length);
                final EncryptedDataContainer encrypted = cipher.processBytes(data);
                final byte[] encBuf = encrypted.getContent();
                System.arraycopy(encBuf, 0, buffer.array(), position, encBuf.length);
                position += encBuf.length;
            }
            buffer.limit(position);
        }
        if(read < allocate) {
            // EOF in proxy stream, finalize cipher and put remaining bytes into buffer
            eof = true;
            final EncryptedDataContainer encContainer = cipher.doFinal();
            final byte[] content = encContainer.getContent();
            buffer = this.combine(buffer, ByteBuffer.wrap(content));
            final String tag = TripleCryptConverter.byteArrayToBase64String(encContainer.getTag());
            final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
            final FileKey fileKey = reader.readValue(status.getFilekey().array());
            if(null == fileKey.getTag()) {
                // Only override if not already set pre-computed in bulk feature
                fileKey.setTag(tag);
                final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                writer.writeValue(out, fileKey);
                status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
            }
            else {
                log.warn(String.format("Skip setting tag in file key already found in %s", status));
            }
        }
    }

    private ByteBuffer combine(final ByteBuffer b1, final ByteBuffer b2) {
        final int pos = b1.position();
        final ByteBuffer allocate = ByteBuffer.allocate(b1.limit() + b2.limit()).put(b1).put(b2);
        allocate.position(pos);
        return allocate;
    }
}
