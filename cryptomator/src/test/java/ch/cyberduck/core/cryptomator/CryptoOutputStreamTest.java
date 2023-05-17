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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.cryptomator.cryptolib.api.FileHeader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import static org.junit.Assert.*;

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
                        public Path mkdir(final Path folder, final TransferStatus status) {
                            assertTrue(folder.equals(home) || folder.isChild(home));
                            return folder;
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
        final CryptoVault vault = new CryptoVault(home);
        vault.create(session, null, new VaultCredentials("test"));
        return vault;
    }

    @Test
    public void testSmallChunksToWrite() throws Exception {
        final CryptoVault vault = this.getVault();
        final ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
        final FileHeader header = vault.getFileHeaderCryptor().create();
        final CryptoOutputStream stream = new CryptoOutputStream(
                new ProxyOutputStream(cipherText), vault.getFileContentCryptor(), header, new RandomNonceGenerator(vault.getNonceSize()), 0);

        final byte[] part1 = RandomUtils.nextBytes(1024);
        final byte[] part2 = RandomUtils.nextBytes(1024);
        stream.write(part1, 0, part1.length);
        stream.write(part2, 0, part2.length);
        stream.close();

        final byte[] read = new byte[part1.length + part2.length];
        final byte[] expected = ByteBuffer.allocate(part1.length + part2.length).put(part1).put(part2).array();
        final CryptoInputStream cryptoInputStream = new CryptoInputStream(new ByteArrayInputStream(cipherText.toByteArray()), vault.getFileContentCryptor(), header, 0);
        assertEquals(expected.length, cryptoInputStream.read(read));
        cryptoInputStream.close();

        assertArrayEquals(expected, read);
    }

    @Test
    public void testWriteWithChunkSize() throws Exception {
        final CryptoVault vault = this.getVault();
        final ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
        final FileHeader header = vault.getFileHeaderCryptor().create();
        final CryptoOutputStream stream = new CryptoOutputStream(
                new ProxyOutputStream(cipherText), vault.getFileContentCryptor(), header, new RandomNonceGenerator(vault.getNonceSize()), 0);

        final byte[] cleartext = RandomUtils.nextBytes(vault.getFileContentCryptor().cleartextChunkSize());
        stream.write(cleartext, 0, cleartext.length);
        stream.close();

        final byte[] read = new byte[cleartext.length];
        final CryptoInputStream cryptoInputStream = new CryptoInputStream(new ByteArrayInputStream(cipherText.toByteArray()), vault.getFileContentCryptor(), header, 0);
        assertEquals(cleartext.length, cryptoInputStream.read(read));
        cryptoInputStream.close();

        assertArrayEquals(cleartext, read);
    }

    @Test
    public void testWriteLargeChunk() throws Exception {
        final CryptoVault vault = this.getVault();
        final ByteArrayOutputStream cipherText = new ByteArrayOutputStream();
        final FileHeader header = vault.getFileHeaderCryptor().create();
        final CryptoOutputStream stream = new CryptoOutputStream(
                new ProxyOutputStream(cipherText), vault.getFileContentCryptor(), header, new RandomNonceGenerator(vault.getNonceSize()), 0);

        final byte[] cleartext = RandomUtils.nextBytes(vault.getFileContentCryptor().cleartextChunkSize() + 1);
        stream.write(cleartext, 0, cleartext.length);
        stream.close();

        final byte[] read = new byte[cleartext.length];
        final CryptoInputStream cryptoInputStream = new CryptoInputStream(new ByteArrayInputStream(cipherText.toByteArray()), vault.getFileContentCryptor(), header, 0);
        IOUtils.readFully(cryptoInputStream, read);
        cryptoInputStream.close();

        assertArrayEquals(cleartext, read);
    }
}
