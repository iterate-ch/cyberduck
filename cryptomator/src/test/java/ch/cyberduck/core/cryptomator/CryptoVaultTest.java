package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.PathDictionary;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.IOUtils;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.Masterkey;
import org.cryptomator.cryptolib.common.MasterkeyFile;
import org.cryptomator.cryptolib.common.MasterkeyFileAccess;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import static ch.cyberduck.core.cryptomator.CryptoVault.VAULT_VERSION;
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
                                    "  \"version\": 8,\n" +
                                    "  \"scryptSalt\": \"RVAAirkArDU=\",\n" +
                                    "  \"scryptCostParam\": 32768,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"+03NkJNWVsJ9Tb1CTpKhXyfINzjDirFFI+iJLOWIOySyxB+abpx34Q==\",\n" +
                                    "  \"hmacMasterKey\": \"aMoDtn7Y6kIXxyHo2zl47p5jCYTlRnfx3l3AMgULmIDSYAxVAraSgg==\",\n" +
                                    "  \"versionMac\": \"FzirA8UhwCmS5RsC4JvxbO+ZBxaCbIkzqD2Ocagd+A8=\"\n" +
                                    "}";

                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                            }
                            if("vault.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(createJWT(masterKey, VAULT_VERSION, CryptorProvider.Scheme.SIV_GCM, "vault123"), Charset.defaultCharset());
                            }
                            throw new NotfoundException(String.format("%s not found", file.getName()));
                        }

                        @Override
                        public boolean offset(final Path file) {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final Path home = new Path("/", EnumSet.of((Path.Type.directory)));
        final CryptoVault vault = new CryptoVault(home);
        vault.load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("vault123");
            }
        });
        assertTrue(vault.getFileContentCryptor().getClass().getName().contains("v2"));
        assertTrue(vault.getFileHeaderCryptor().getClass().getName().contains("v2"));
        assertEquals(Vault.State.open, vault.getState());
        assertNotSame(home, vault.encrypt(session, home));
        assertEquals(vault.encrypt(session, home), vault.encrypt(session, home));
        final Path directory = new Path(home, "dir", EnumSet.of(Path.Type.directory));
        assertNull(directory.attributes().getVault());
        assertEquals(home, vault.encrypt(session, directory).attributes().getVault());
        assertEquals(home, directory.attributes().getVault());
        assertEquals(vault.encrypt(session, directory), vault.encrypt(session, directory));
        assertEquals(new Path(home, directory.getName(), EnumSet.of(Path.Type.directory, Path.Type.decrypted)), vault.decrypt(session, vault.encrypt(session, directory, true)));
        final Path placeholder = new Path(home, "placeholder", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(vault.encrypt(session, placeholder, true).getType().contains(Path.Type.placeholder));
        assertTrue(vault.decrypt(session, vault.encrypt(session, placeholder, true)).getType().contains(Path.Type.placeholder));
        assertEquals(new Path(home, placeholder.getName(), EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.decrypted)), vault.decrypt(session, vault.encrypt(session, placeholder, true)));
        assertNotEquals(
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))),
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)), true)
        );
        assertEquals(
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))).attributes().getDirectoryId(),
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))).attributes().getDirectoryId()
        );
        assertEquals(
                vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)))).attributes().getDirectoryId(),
                vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)))).attributes().getDirectoryId()
        );
        assertNull(vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)), true).attributes().getDirectoryId());
        assertNull(vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))), true).attributes().getDirectoryId());
        assertNotEquals(
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))).attributes().getDirectoryId(),
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)), true).attributes().getDirectoryId()
        );
        vault.close();
        assertEquals(Vault.State.closed, vault.getState());
    }

    @Test
    public void testFind() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"version\": 8,\n" +
                                    "  \"scryptSalt\": \"RVAAirkArDU=\",\n" +
                                    "  \"scryptCostParam\": 32768,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"+03NkJNWVsJ9Tb1CTpKhXyfINzjDirFFI+iJLOWIOySyxB+abpx34Q==\",\n" +
                                    "  \"hmacMasterKey\": \"aMoDtn7Y6kIXxyHo2zl47p5jCYTlRnfx3l3AMgULmIDSYAxVAraSgg==\",\n" +
                                    "  \"versionMac\": \"FzirA8UhwCmS5RsC4JvxbO+ZBxaCbIkzqD2Ocagd+A8=\"\n" +
                                    "}";
                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                            }
                            if("vault.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(createJWT(masterKey, VAULT_VERSION, CryptorProvider.Scheme.SIV_GCM, "vault123"), Charset.defaultCharset());
                            }
                            throw new NotfoundException(String.format("%s not found", file.getName()));
                        }

                        @Override
                        public boolean offset(final Path file) {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final Path home = new Path("/", EnumSet.of((Path.Type.directory)));
        final CryptoVault vault = new CryptoVault(home);
        assertEquals(home, vault.load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("vault123");
            }
        }).getHome());
        assertTrue(vault.getFileContentCryptor().getClass().getName().contains("v2"));
        assertTrue(vault.getFileHeaderCryptor().getClass().getName().contains("v2"));
        assertEquals(Vault.State.open, vault.getState());
        vault.close();
    }

    @Test
    public void testSerializeVaultHome() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"version\": 8,\n" +
                                    "  \"scryptSalt\": \"RVAAirkArDU=\",\n" +
                                    "  \"scryptCostParam\": 32768,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"+03NkJNWVsJ9Tb1CTpKhXyfINzjDirFFI+iJLOWIOySyxB+abpx34Q==\",\n" +
                                    "  \"hmacMasterKey\": \"aMoDtn7Y6kIXxyHo2zl47p5jCYTlRnfx3l3AMgULmIDSYAxVAraSgg==\",\n" +
                                    "  \"versionMac\": \"FzirA8UhwCmS5RsC4JvxbO+ZBxaCbIkzqD2Ocagd+A8=\"\n" +
                                    "}";
                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                            }
                            if("vault.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(createJWT(masterKey, VAULT_VERSION, CryptorProvider.Scheme.SIV_GCM, "vault123"), Charset.defaultCharset());
                            }
                            throw new NotfoundException(String.format("%s not found", file.getName()));
                        }

                        @Override
                        public boolean offset(final Path file) {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final Path home = new Path("/", EnumSet.of((Path.Type.directory)));
        final CryptoVault vault = new CryptoVault(home);
        assertEquals(home, vault.load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("vault123");
            }
        }).getHome());
        assertEquals(Vault.State.open, vault.getState());
        assertEquals(home, new PathDictionary<>().deserialize(home.serialize(SerializerFactory.get())));
        vault.close();
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
                                    "  \"version\": 8,\n" +
                                    "  \"scryptSalt\": \"RVAAirkArDU=\",\n" +
                                    "  \"scryptCostParam\": 32768,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"+03NkJNWVsJ9Tb1CTpKhXyfINzjDirFFI+iJLOWIOySyxB+abpx34Q==\",\n" +
                                    "  \"hmacMasterKey\": \"aMoDtn7Y6kIXxyHo2zl47p5jCYTlRnfx3l3AMgULmIDSYAxVAraSgg==\",\n" +
                                    "  \"versionMac\": \"FzirA8UhwCmS5RsC4JvxbO+ZBxaCbIkzqD2Ocagd+A8=\"\n" +
                                    "}";
                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                            }
                            if("vault.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(createJWT(masterKey, VAULT_VERSION, CryptorProvider.Scheme.SIV_GCM, "vault123"), Charset.defaultCharset());
                            }
                            throw new NotfoundException(String.format("%s not found", file.getName()));
                        }

                        @Override
                        public boolean offset(final Path file) {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final AtomicBoolean prompt = new AtomicBoolean();
        final CryptoVault vault = new CryptoVault(new Path("/", EnumSet.of(Path.Type.directory)));
        try {
            vault.load(session, new DisabledPasswordCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                    if(!prompt.get()) {
                        assertEquals("Provide your passphrase to unlock the Cryptomator Vault /", reason);
                        prompt.set(true);
                        return new VaultCredentials("null");
                    }
                    else {
                        assertEquals("Failure to decrypt master key file. Provide your passphrase to unlock the Cryptomator Vault /.", reason);
                        throw new LoginCanceledException();
                    }
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
                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                            }
                            throw new NotfoundException(String.format("%s not found", file.getName()));
                        }

                        @Override
                        public boolean offset(final Path file) {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(new Path("/", EnumSet.of(Path.Type.directory)));
        try {
            vault.load(session, new DisabledPasswordCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
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
                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                            }
                            throw new NotfoundException(String.format("%s not found", file.getName()));
                        }

                        @Override
                        public boolean offset(final Path file) {
                            return false;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(new Path("/", EnumSet.of(Path.Type.directory)));
        try {
            vault.load(session, new DisabledPasswordCallback() {
                @Override
                public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                    return new VaultCredentials(null);
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
        vault.create(session, null, new VaultCredentials("test"));
    }

    @Test
    public void testCleartextSizeV6() throws Exception {
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
        final CryptoVault vault = new CryptoVault(
                home);
        vault.create(session, null, new VaultCredentials("test"), 6);
        // zero ciphertextFileSize
        try {
            vault.toCleartextSize(0L, 0);
            fail();
        }
        catch(CryptoInvalidFilesizeException e) {
        }
        // ciphertextFileSize == headerSize
        assertEquals(0L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize()));
        // ciphertextFileSize == headerSize + 1
        try {
            vault.toCleartextSize(0L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize()) + 1);
            fail();
        }
        catch(CryptoInvalidFilesizeException e) {
        }
        // ciphertextFileSize == headerSize + chunkHeaderSize + 1
        assertEquals(1L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize() + 48 + 1));
        // ciphertextFileSize == headerSize + (32768 + chunkHeaderSize) + (1 + chunkHeaderSize) + 1
        assertEquals(32769L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize() + (32768 + 48) + (1 + 48)));
    }

    @Test
    public void testCleartextSizeV8() throws Exception {
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
        final CryptoVault vault = new CryptoVault(
                home);
        vault.create(session, null, new VaultCredentials("test"));
        // zero ciphertextFileSize
        try {
            vault.toCleartextSize(0L, 0);
            fail();
        }
        catch(CryptoInvalidFilesizeException e) {
        }
        // ciphertextFileSize == headerSize
        assertEquals(0L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize()));
        // ciphertextFileSize == headerSize + 1
        try {
            vault.toCleartextSize(0L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize()) + 1);
            fail();
        }
        catch(CryptoInvalidFilesizeException e) {
        }
        // ciphertextFileSize == headerSize + chunkHeaderSize + 1
        assertEquals(1L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize() + 28 + 1));
        // ciphertextFileSize == headerSize + (32768 + chunkHeaderSize) + (1 + chunkHeaderSize) + 1
        assertEquals(32769L, vault.toCleartextSize(0L, vault.getFileHeaderCryptor().headerSize() + (32768 + 28) + (1 + 28)));
    }

    @Test
    public void testCleartextSizeCiphertextCalculation() throws Exception {
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
                        public boolean isSupported(final Path workdir, final String name) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault vault = new CryptoVault(
                home);
        vault.create(session, null, new VaultCredentials("test"));
        for(int i = 0; i < 26000000; i++) {
            assertEquals(i, vault.toCleartextSize(0L, vault.toCiphertextSize(0L, i)));
        }
    }

    public static String createJWT(final String masterkeyCryptomator,
                                   final int version,
                                   final CryptorProvider.Scheme cipher,
                                   final String passphrase) throws BackgroundException {
        try {
            final MasterkeyFile mkFile = MasterkeyFile.read(new StringReader(masterkeyCryptomator));
            final StringWriter writer = new StringWriter();
            mkFile.write(writer);
            final Masterkey masterkey = new MasterkeyFileAccess(PreferencesFactory.get().getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8),
                    FastSecureRandomProvider.get().provide()).load(new ByteArrayInputStream(writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8)), passphrase);
            final Algorithm algorithm = Algorithm.HMAC256(masterkey.getEncoded());
            return JWT.create()
                    .withJWTId(new UUIDRandomStringService().random())
                    .withKeyId(String.format("masterkeyfile:%s", DefaultVaultRegistry.DEFAULT_MASTERKEY_FILE_NAME))
                    .withClaim("format", version)
                    .withClaim("cipherCombo", cipher.toString())
                    .withClaim("shorteningThreshold", CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD)
                    .sign(algorithm);
        }
        catch(IOException e) {
            throw new BackgroundException(e);
        }
    }
}
