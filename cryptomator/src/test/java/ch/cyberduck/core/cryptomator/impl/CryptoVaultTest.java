package ch.cyberduck.core.cryptomator.impl;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.CryptoInvalidFilesizeException;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class CryptoVaultTest {

    @Test
    public void testLoad() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"scryptSalt\": \"NrC7QGG/ouc=\",\n" +
                                    "  \"scryptCostParam\": 16384,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"Q7pGo1l0jmZssoQh9rXFPKJE9NIXvPbL+HcnVSR9CHdkeR8AwgFtcw==\",\n" +
                                    "  \"hmacMasterKey\": \"xzBqT4/7uEcQbhHFLC0YmMy4ykVKbuvJEA46p1Xm25mJNuTc20nCbw==\",\n" +
                                    "  \"versionMac\": \"hlNr3dz/CmuVajhaiGyCem9lcVIUjDfSMLhjppcXOrM=\",\n" +
                                    "  \"version\": 5\n" +
                                    "}";
                            return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                        }

                        @Override
                        public boolean offset(final Path file) throws BackgroundException {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final Path home = new Path("/", EnumSet.of((Path.Type.directory)));
        final CryptoVault vault = new CryptoVault(home, new DisabledPasswordStore());
        vault.load(session, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        });
        assertEquals(Vault.State.open, vault.getState());
        assertNotSame(home, vault.encrypt(session, home));
        assertEquals(vault.encrypt(session, home), vault.encrypt(session, home));
        final Path directory = new Path(home, "/dir", EnumSet.of((Path.Type.directory)));
        assertEquals(vault.encrypt(session, directory), vault.encrypt(session, directory));
        assertNotEquals(vault.encrypt(session, directory), vault.encrypt(session, directory, true));
        assertEquals(vault.encrypt(session, directory).attributes().getDirectoryId(), vault.encrypt(session, directory).attributes().getDirectoryId());
        assertNull(vault.encrypt(session, directory, true).attributes().getDirectoryId());
        assertNull(vault.encrypt(session, directory, true).attributes().getDirectoryId());
        assertNotEquals(vault.encrypt(session, directory).attributes().getDirectoryId(), vault.encrypt(session, directory, true).attributes().getDirectoryId());
        vault.close();
        assertEquals(Vault.State.closed, vault.getState());
    }

    @Test
    public void testLoadInvalidPassphrase() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"scryptSalt\": \"NrC7QGG/ouc=\",\n" +
                                    "  \"scryptCostParam\": 16384,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"Q7pGo1l0jmZssoQh9rXFPKJE9NIXvPbL+HcnVSR9CHdkeR8AwgFtcw==\",\n" +
                                    "  \"hmacMasterKey\": \"xzBqT4/7uEcQbhHFLC0YmMy4ykVKbuvJEA46p1Xm25mJNuTc20nCbw==\",\n" +
                                    "  \"versionMac\": \"hlNr3dz/CmuVajhaiGyCem9lcVIUjDfSMLhjppcXOrM=\",\n" +
                                    "  \"version\": 5\n" +
                                    "}";
                            return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                        }

                        @Override
                        public boolean offset(final Path file) throws BackgroundException {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final AtomicBoolean prompt = new AtomicBoolean();
        final CryptoVault vault = new CryptoVault(
                new Path("/", EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        try {
            vault.load(session, new DisabledPasswordCallback() {
                @Override
                public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    if(!prompt.get()) {
                        assertEquals("Provide your passphrase to unlock the Cryptomator Vault “/“", reason);
                        credentials.setPassword("null");
                    }
                    if(prompt.get()) {
                        assertEquals("Failure to decrypt master key file. Provide your passphrase to unlock the Cryptomator Vault “/“.", reason);
                        throw new LoginCanceledException();
                    }
                    prompt.set(true);
                }
            });
            fail();
        }
        catch(LoginCanceledException e) {
            //
        }
        assertTrue(prompt.get());
    }

    @Test
    public void testLoadCancel() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"scryptSalt\": \"NrC7QGG/ouc=\",\n" +
                                    "  \"scryptCostParam\": 16384,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"Q7pGo1l0jmZssoQh9rXFPKJE9NIXvPbL+HcnVSR9CHdkeR8AwgFtcw==\",\n" +
                                    "  \"hmacMasterKey\": \"xzBqT4/7uEcQbhHFLC0YmMy4ykVKbuvJEA46p1Xm25mJNuTc20nCbw==\",\n" +
                                    "  \"versionMac\": \"hlNr3dz/CmuVajhaiGyCem9lcVIUjDfSMLhjppcXOrM=\",\n" +
                                    "  \"version\": 5\n" +
                                    "}";
                            return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                        }

                        @Override
                        public boolean offset(final Path file) throws BackgroundException {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(
                new Path("/", EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        try {
            vault.load(session, new DisabledPasswordCallback() {
                @Override
                public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    throw new LoginCanceledException();
                }
            });
            fail();
        }
        catch(LoginCanceledException e) {
            //
        }
    }

    @Test
    public void testLoadEmptyPassword() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"scryptSalt\": \"NrC7QGG/ouc=\",\n" +
                                    "  \"scryptCostParam\": 16384,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"Q7pGo1l0jmZssoQh9rXFPKJE9NIXvPbL+HcnVSR9CHdkeR8AwgFtcw==\",\n" +
                                    "  \"hmacMasterKey\": \"xzBqT4/7uEcQbhHFLC0YmMy4ykVKbuvJEA46p1Xm25mJNuTc20nCbw==\",\n" +
                                    "  \"versionMac\": \"hlNr3dz/CmuVajhaiGyCem9lcVIUjDfSMLhjppcXOrM=\",\n" +
                                    "  \"version\": 5\n" +
                                    "}";
                            return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                        }

                        @Override
                        public boolean offset(final Path file) throws BackgroundException {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(
                new Path("/", EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        try {
            vault.load(session, new DisabledPasswordCallback() {
                @Override
                public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    credentials.setPassword(null);
                }
            });
            fail();
        }
        catch(LoginCanceledException e) {
            //
        }
    }

    @Test
    public void testCreate() throws Exception {
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
        final CryptoVault vault = new CryptoVault(
                home, new DisabledPasswordStore());
        vault.create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("pwd");
            }
        });
    }

    @Test
    public void testCleartextSize() throws Exception {
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
                            throw new UnsupportedOperationException();
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
        final CryptoVault vault = new CryptoVault(
                home, new DisabledPasswordStore());
        vault.create(session, null, new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("pwd");
            }
        });
        // zero ciphertextFileSize
        try {
            vault.toCleartextSize(0);
            fail();
        }
        catch(CryptoInvalidFilesizeException e) {
        }
        // ciphertextFileSize == headerSize
        assertEquals(0L, vault.toCleartextSize(vault.getCryptor().fileHeaderCryptor().headerSize()));
        // ciphertextFileSize == headerSize + 1
        try {
            vault.toCleartextSize(vault.toCleartextSize(vault.getCryptor().fileHeaderCryptor().headerSize()) + 1);
            fail();
        }
        catch(CryptoInvalidFilesizeException e) {
        }
        // ciphertextFileSize == headerSize + chunkHeaderSize + 1
        assertEquals(1L, vault.toCleartextSize(vault.getCryptor().fileHeaderCryptor().headerSize() + 48 + 1));
        // ciphertextFileSize == headerSize + (32768 + chunkHeaderSize) + (1 + chunkHeaderSize) + 1
        assertEquals(32769L, vault.toCleartextSize(vault.getCryptor().fileHeaderCryptor().headerSize() + (32768 + 48) + (1 + 48)));
    }
}