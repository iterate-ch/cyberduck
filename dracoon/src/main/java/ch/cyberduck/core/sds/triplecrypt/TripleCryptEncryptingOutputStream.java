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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.sds.SDSAttributesAdapter;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.dracoon.sdk.crypto.FileEncryptionCipher;
import com.dracoon.sdk.crypto.error.CryptoException;
import com.dracoon.sdk.crypto.error.CryptoSystemException;
import com.dracoon.sdk.crypto.model.EncryptedDataContainer;
import com.dracoon.sdk.crypto.model.PlainDataContainer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class TripleCryptEncryptingOutputStream extends HttpResponseOutputStream<Node> {
    private static final Logger log = LogManager.getLogger(TripleCryptEncryptingOutputStream.class);

    private final StatusOutputStream<Node> proxy;

    public TripleCryptEncryptingOutputStream(final SDSSession session, final SDSNodeIdProvider nodeid, final StatusOutputStream<Node> proxy,
                                             final FileEncryptionCipher cipher, final TransferStatus status) {
        super(new MemorySegementingOutputStream(new EncryptingOutputStream(session, proxy, cipher, status),
                SDSSession.DEFAULT_CHUNKSIZE), new SDSAttributesAdapter(session), status);
        this.proxy = proxy;
    }

    @Override
    public void write(final int b) throws IOException {
        throw new IOException(new UnsupportedOperationException());
    }

    @Override
    public Node getStatus() throws BackgroundException {
        return proxy.getStatus();
    }

    @Override
    public void write(final byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    private static final class EncryptingOutputStream extends ProxyOutputStream {
        private final SDSSession session;
        private final FileEncryptionCipher cipher;
        private final TransferStatus status;

        public EncryptingOutputStream(final SDSSession session, final OutputStream proxy, final FileEncryptionCipher cipher,
                                      final TransferStatus key) {
            super(proxy);
            this.session = session;
            this.cipher = cipher;
            this.status = key;
        }

        @Override
        public void write(final byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            try {
                for(int chunkOffset = off; chunkOffset < len; chunkOffset += SDSSession.DEFAULT_CHUNKSIZE) {
                    int chunkLen = Math.min(SDSSession.DEFAULT_CHUNKSIZE, len - chunkOffset);
                    final byte[] bytes = Arrays.copyOfRange(b, chunkOffset, chunkOffset + chunkLen);
                    final PlainDataContainer data = TripleCryptKeyPair.createPlainDataContainer(bytes, bytes.length);
                    final EncryptedDataContainer encrypted = cipher.processBytes(data);
                    super.write(encrypted.getContent());
                }
            }
            catch(CryptoException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                final EncryptedDataContainer encrypted = cipher.doFinal();
                super.write(encrypted.getContent());
                final String tag = TripleCryptConverter.byteArrayToBase64String(encrypted.getTag());
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
                    log.warn("Skip setting tag in file key already found in {}", status);
                }
            }
            catch(CryptoSystemException e) {
                throw new IOException(e);
            }
            finally {
                super.close();
            }
        }
    }
}
