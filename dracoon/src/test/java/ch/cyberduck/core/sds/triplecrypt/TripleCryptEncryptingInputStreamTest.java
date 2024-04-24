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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.sds.SDSApiClient;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.client.testing.http.apache.MockHttpClient;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class TripleCryptEncryptingInputStreamTest {

    @Test
    public void testEncryptDecryptWithContentSizeMultipleOfEncryptingBufferSize() throws Exception {
        final byte[] content = RandomUtils.nextBytes(1024 * 1024);
        final ByteArrayInputStream plain = new ByteArrayInputStream(content);
        final PlainFileKey key = Crypto.generateFileKey(PlainFileKey.Version.AES256GCM);
        final SDSSession session = new SDSSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public SDSApiClient getClient() {
                return new SDSApiClient(new MockHttpClient());
            }
        };
        final TransferStatus status = new TransferStatus();
        final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeValue(out, TripleCryptConverter.toSwaggerFileKey(key));
        status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
        final TripleCryptEncryptingInputStream encryptInputStream = new TripleCryptEncryptingInputStream(session, plain, Crypto.createFileEncryptionCipher(key), status);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).withLimit((long) content.length).withChunksize(32768).transfer(encryptInputStream, os);
        encryptInputStream.close();
        out.close();
        final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
        final FileKey fileKey = reader.readValue(status.getFilekey().array());
        assertNotNull(fileKey.getTag());
        final TripleCryptDecryptingInputStream cryptInputStream = new TripleCryptDecryptingInputStream(is,
                Crypto.createFileDecryptionCipher(TripleCryptConverter.toCryptoPlainFileKey(fileKey)),
                TripleCryptConverter.base64StringToByteArray(fileKey.getTag()));
        final byte[] compare = new byte[content.length];
        IOUtils.read(cryptInputStream, compare);
        assertArrayEquals(content, compare);
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        final byte[] content = RandomUtils.nextBytes(1024 * 1024 + 1);
        final ByteArrayInputStream plain = new ByteArrayInputStream(content);
        final PlainFileKey key = Crypto.generateFileKey(PlainFileKey.Version.AES256GCM);
        final SDSSession session = new SDSSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public SDSApiClient getClient() {
                return new SDSApiClient(new MockHttpClient());
            }
        };
        final TransferStatus status = new TransferStatus();
        final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeValue(out, TripleCryptConverter.toSwaggerFileKey(key));
        status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
        final TripleCryptEncryptingInputStream encryptInputStream = new TripleCryptEncryptingInputStream(session, plain, Crypto.createFileEncryptionCipher(key), status);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        new StreamCopier(StreamCancelation.noop, StreamProgress.noop).withLimit((long) content.length).withChunksize(32768).transfer(encryptInputStream, os);
        encryptInputStream.close();
        out.close();
        final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
        final FileKey fileKey = reader.readValue(status.getFilekey().array());
        assertNotNull(fileKey.getTag());
        final TripleCryptDecryptingInputStream cryptInputStream = new TripleCryptDecryptingInputStream(is,
                Crypto.createFileDecryptionCipher(TripleCryptConverter.toCryptoPlainFileKey(fileKey)),
                TripleCryptConverter.base64StringToByteArray(fileKey.getTag()));
        final byte[] compare = new byte[content.length];
        IOUtils.read(cryptInputStream, compare);
        assertArrayEquals(content, compare);
    }

    @Test
    public void testEncryptDecryptZeroBytes() throws Exception {
        final byte[] content = RandomUtils.nextBytes(0);
        final ByteArrayInputStream plain = new ByteArrayInputStream(content);
        final PlainFileKey key = Crypto.generateFileKey(PlainFileKey.Version.AES256GCM);
        final SDSSession session = new SDSSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public SDSApiClient getClient() {
                return new SDSApiClient(new MockHttpClient());
            }
        };
        final TransferStatus status = new TransferStatus();
        final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeValue(out, TripleCryptConverter.toSwaggerFileKey(key));
        status.setFilekey(ByteBuffer.wrap(out.toByteArray()));
        final TripleCryptEncryptingInputStream encryptInputStream = new TripleCryptEncryptingInputStream(session, plain, Crypto.createFileEncryptionCipher(key), status);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        IOUtils.copy(encryptInputStream, os, 42);
        encryptInputStream.close();
        out.close();
        final ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
        final FileKey fileKey = reader.readValue(status.getFilekey().array());
        final TripleCryptDecryptingInputStream cryptInputStream = new TripleCryptDecryptingInputStream(is,
                Crypto.createFileDecryptionCipher(TripleCryptConverter.toCryptoPlainFileKey(fileKey)),
                TripleCryptConverter.base64StringToByteArray(fileKey.getTag()));
        final byte[] compare = new byte[content.length];
        IOUtils.read(cryptInputStream, compare);
        assertArrayEquals(content, compare);
    }
}
