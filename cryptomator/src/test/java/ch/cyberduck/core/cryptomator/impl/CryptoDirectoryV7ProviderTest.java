package ch.cyberduck.core.cryptomator.impl;

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
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.CryptoVaultTest;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.IOUtils;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.PerpetualMasterkey;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.EnumSet;

import static ch.cyberduck.core.cryptomator.AbstractVault.VAULT_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CryptoDirectoryV7ProviderTest {

    @Test(expected = NotfoundException.class)
    public void testToEncryptedInvalidArgument() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final CryptorProvider crypto = CryptorProvider.forScheme(CryptorProvider.Scheme.SIV_GCM);
        final SecureRandom random = new SecureRandom();
        final Cryptor cryptor = crypto.provide(PerpetualMasterkey.generate(random), random);
        final CryptoDirectory provider = new CryptoDirectoryV7Provider(new CryptoVault(home), new CryptoFilenameV7Provider());
        provider.toEncrypted(new NullSession(new Host(new TestProtocol())), new Path("/vault/f", EnumSet.of(Path.Type.file)));
    }

    @Test(expected = NotfoundException.class)
    public void testToEncryptedInvalidPath() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final CryptorProvider crypto = CryptorProvider.forScheme(CryptorProvider.Scheme.SIV_GCM);
        final SecureRandom random = new SecureRandom();
        final Cryptor cryptor = crypto.provide(PerpetualMasterkey.generate(random), random);
        final CryptoDirectory provider = new CryptoDirectoryV7Provider(new CryptoVault(home), new CryptoFilenameV7Provider());
        provider.toEncrypted(new NullSession(new Host(new TestProtocol())), new Path("/", EnumSet.of(Path.Type.directory)));
    }

    @Test
    public void testToEncryptedDirectory() throws Exception {
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
                                return IOUtils.toInputStream(CryptoVaultTest.createJWT(masterKey, VAULT_VERSION, CryptorProvider.Scheme.SIV_GCM, "vault123"), Charset.defaultCharset());
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
        final Path home = new Path("/vault", EnumSet.of((Path.Type.directory)));
        final CryptoVault vault = new CryptoVault(home);
        vault.load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("vault123");
            }
        });
        final CryptoDirectory provider = new CryptoDirectoryV7Provider(vault, new CryptoFilenameV7Provider());
        assertNotNull(provider.toEncrypted(session, home));
        final Path f = new Path("/vault/f", EnumSet.of(Path.Type.directory));
        assertNotNull(provider.toEncrypted(session, f));
        assertEquals(provider.toEncrypted(session, f), provider.toEncrypted(session, f));
    }
}
