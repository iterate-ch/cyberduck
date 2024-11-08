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
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CryptoWriteFeatureTest {

    @Test
    public void testCiphertextSize_CTR() throws Exception {
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
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(home);
        vault.create(session, null, new VaultCredentials("test"), CryptoVault.VAULT_VERSION_DEPRECATED);
        int headerSize = vault.getFileHeaderCryptor().headerSize();
        // zero file size
        assertEquals(headerSize, vault.toCiphertextSize(0L, 0));
        // one-byte file
        assertEquals(headerSize + 48 + 1, vault.toCiphertextSize(0L, 1));
        // file with chunk size length
        assertEquals(headerSize + vault.getFileContentCryptor().ciphertextChunkSize(), vault.toCiphertextSize(0L, vault.getFileContentCryptor().cleartextChunkSize()));
        // file with chunk size length + 1
        assertEquals(headerSize + vault.getFileContentCryptor().ciphertextChunkSize() + 48 + 1, vault.toCiphertextSize(0L, vault.getFileContentCryptor().cleartextChunkSize() + 1));
        // file with 2 * chunk size length
        assertEquals(headerSize + 2 * vault.getFileContentCryptor().ciphertextChunkSize(), vault.toCiphertextSize(0L, 2 * vault.getFileContentCryptor().cleartextChunkSize()));
        // file with 2 * chunk size length + 100
        assertEquals(headerSize + 2 * vault.getFileContentCryptor().ciphertextChunkSize() + 48 + 100, vault.toCiphertextSize(0L, 2 * vault.getFileContentCryptor().cleartextChunkSize() + 100));
    }

    @Test
    public void testCiphertextSize_GCM() throws Exception {
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
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(home);
        vault.create(session, null, new VaultCredentials("test"), CryptoVault.VAULT_VERSION);
        int headerSize = vault.getFileHeaderCryptor().headerSize();
        // zero file size
        assertEquals(headerSize, vault.toCiphertextSize(0L, 0));
        // one-byte file
        assertEquals(headerSize + 28 + 1, vault.toCiphertextSize(0L, 1));
        // file with chunk size length
        assertEquals(headerSize + vault.getFileContentCryptor().ciphertextChunkSize(), vault.toCiphertextSize(0L, vault.getFileContentCryptor().cleartextChunkSize()));
        // file with chunk size length + 1
        assertEquals(headerSize + vault.getFileContentCryptor().ciphertextChunkSize() + 28 + 1, vault.toCiphertextSize(0L, vault.getFileContentCryptor().cleartextChunkSize() + 1));
        // file with 2 * chunk size length
        assertEquals(headerSize + 2 * vault.getFileContentCryptor().ciphertextChunkSize(), vault.toCiphertextSize(0L, 2 * vault.getFileContentCryptor().cleartextChunkSize()));
        // file with 2 * chunk size length + 100
        assertEquals(headerSize + 2 * vault.getFileContentCryptor().ciphertextChunkSize() + 28 + 100, vault.toCiphertextSize(0L, 2 * vault.getFileContentCryptor().cleartextChunkSize() + 100));
    }
}
