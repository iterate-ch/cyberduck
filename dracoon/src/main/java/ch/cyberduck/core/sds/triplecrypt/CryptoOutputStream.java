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
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.ProxyOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import eu.ssp_europe.sds.crypto.CryptoException;
import eu.ssp_europe.sds.crypto.CryptoSystemException;
import eu.ssp_europe.sds.crypto.CryptoUtils;
import eu.ssp_europe.sds.crypto.FileEncryptionCipher;
import eu.ssp_europe.sds.crypto.model.EncryptedDataContainer;
import eu.ssp_europe.sds.crypto.model.PlainDataContainer;

public class CryptoOutputStream<VersionId> extends HttpResponseOutputStream<VersionId> {

    private final StatusOutputStream<VersionId> proxy;

    public CryptoOutputStream(final SDSSession session, final StatusOutputStream<VersionId> proxy, final FileEncryptionCipher cipher, final TransferStatus key) {
        super(new MemorySegementingOutputStream(new EncryptingOutputStream(session, proxy, cipher, key),
                SDSSession.DEFAULT_CHUNKSIZE));
        this.proxy = proxy;
    }

    @Override
    public void write(final int b) throws IOException {
        throw new IOException(new UnsupportedOperationException());
    }

    @Override
    public VersionId getStatus() throws BackgroundException {
        return proxy.getStatus();
    }

    @Override
    public void write(final byte b[]) throws IOException {
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
                    final PlainDataContainer data = createPlainDataContainer(bytes, bytes.length);
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
                final String tag = CryptoUtils.byteArrayToString(encrypted.getTag());
                final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                final FileKey fileKey = reader.readValue(status.getFilekey().array());
                fileKey.setTag(tag);
                final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                writer.writeValue(out, fileKey);
                status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
            }
            catch(CryptoSystemException e) {
                throw new IOException(e);
            }
            finally {
                super.close();
            }
        }

        private static PlainDataContainer createPlainDataContainer(final byte[] bytes, final int len) {
            final byte[] b = new byte[len];
            System.arraycopy(bytes, 0, b, 0, len);
            return new PlainDataContainer(b);
        }
    }
}
