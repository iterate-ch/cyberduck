package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class CryptoOutputStreamTest {

    private CryptoVault getVault() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {

                        @Override
                        public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
                            assertTrue(folder.equals(home) || folder.isChild(home));
                            return folder;
                        }

                        @Override
                        public boolean isSupported(final Path workdir) {
                            return true;
                        }

                        @Override
                        public Directory withWriter(final Write writer) {
                            return this;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(home, new DisabledPasswordStore());
        vault.create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("pwd");
            }
        });
        return vault;
    }

    @Test
    public void testSmallChunksToWrite() throws Exception {
        final CryptoVault vault = this.getVault();
        final ByteOutputStream cipherText = new ByteOutputStream();
        final FileHeader header = vault.getCryptor().fileHeaderCryptor().create();
        final CryptoOutputStream<?> stream = new CryptoOutputStream<>(new StatusOutputStream<Void>(cipherText) {
            @Override
            public Void getStatus() throws BackgroundException {
                return null;
            }
        }, vault.getCryptor(), header);

        final byte[] part1 = RandomUtils.nextBytes(1024);
        final byte[] part2 = RandomUtils.nextBytes(1024);
        stream.write(part1, 0, part1.length);
        stream.write(part2, 0, part2.length);
        stream.close();

        final byte[] read = new byte[part1.length + part2.length];
        final byte[] expected = ByteBuffer.allocate(part1.length + part2.length).put(part1).put(part2).array();
        final CryptoInputStream cryptoInputStream = new CryptoInputStream(new ByteArrayInputStream(cipherText.getBytes()), vault.getCryptor(), header);
        cryptoInputStream.read(read);
        cryptoInputStream.close();

        assertArrayEquals(expected, read);
    }

    @Test
    public void testWriteWithChunkSize() throws Exception {
        final CryptoVault vault = this.getVault();
        final ByteOutputStream cipherText = new ByteOutputStream();
        final FileHeader header = vault.getCryptor().fileHeaderCryptor().create();
        final CryptoOutputStream<?> stream = new CryptoOutputStream<>(new StatusOutputStream<Void>(cipherText) {
            @Override
            public Void getStatus() throws BackgroundException {
                return null;
            }
        }, vault.getCryptor(), header);

        final byte[] cleartext = RandomUtils.nextBytes(vault.getCryptor().fileContentCryptor().cleartextChunkSize());
        stream.write(cleartext, 0, cleartext.length);
        stream.close();

        final byte[] read = new byte[cleartext.length];
        final CryptoInputStream cryptoInputStream = new CryptoInputStream(new ByteArrayInputStream(cipherText.getBytes()), vault.getCryptor(), header);
        cryptoInputStream.read(read);
        cryptoInputStream.close();

        assertArrayEquals(cleartext, read);
    }

    @Test
    public void testWriteLargeChunk() throws Exception {
        final CryptoVault vault = this.getVault();
        final ByteOutputStream cipherText = new ByteOutputStream();
        final FileHeader header = vault.getCryptor().fileHeaderCryptor().create();
        final CryptoOutputStream<?> stream = new CryptoOutputStream<>(new StatusOutputStream<Void>(cipherText) {
            @Override
            public Void getStatus() throws BackgroundException {
                return null;
            }
        }, vault.getCryptor(), header);

        final byte[] cleartext = RandomUtils.nextBytes(vault.getCryptor().fileContentCryptor().cleartextChunkSize() + 1);
        stream.write(cleartext, 0, cleartext.length);
        stream.close();

        final byte[] read = new byte[cleartext.length];
        final CryptoInputStream cryptoInputStream = new CryptoInputStream(new ByteArrayInputStream(cipherText.getBytes()), vault.getCryptor(), header);
        IOUtils.readFully(cryptoInputStream, read);
        cryptoInputStream.close();

        assertArrayEquals(cleartext, read);
    }
}