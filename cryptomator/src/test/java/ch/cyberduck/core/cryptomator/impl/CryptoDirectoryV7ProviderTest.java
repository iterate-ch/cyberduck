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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.CryptorCache;
import ch.cyberduck.core.exception.NotfoundException;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.PerpetualMasterkey;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CryptoDirectoryV7ProviderTest {

    @Test(expected = NotfoundException.class)
    public void testToEncryptedInvalidArgument() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final CryptorProvider crypto = CryptorProvider.forScheme(CryptorProvider.Scheme.SIV_GCM);
        final SecureRandom random = new SecureRandom();
        final Cryptor cryptor = crypto.provide(PerpetualMasterkey.generate(random), random);
        final CryptoDirectory provider = new CryptoDirectoryV7Provider(new CryptoVault(home), new CryptoFilenameV7Provider(), new CryptorCache(cryptor.fileNameCryptor()));
        provider.toEncrypted(new NullSession(new Host(new TestProtocol())), null, new Path("/vault/f", EnumSet.of(Path.Type.file)));
    }

    @Test(expected = NotfoundException.class)
    public void testToEncryptedInvalidPath() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final CryptorProvider crypto = CryptorProvider.forScheme(CryptorProvider.Scheme.SIV_GCM);
        final SecureRandom random = new SecureRandom();
        final Cryptor cryptor = crypto.provide(PerpetualMasterkey.generate(random), random);
        final CryptoDirectory provider = new CryptoDirectoryV7Provider(new CryptoVault(home), new CryptoFilenameV7Provider(), new CryptorCache(cryptor.fileNameCryptor()));
        provider.toEncrypted(new NullSession(new Host(new TestProtocol())), null, new Path("/", EnumSet.of(Path.Type.directory)));
    }

    @Test
    public void testToEncryptedDirectory() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final CryptorProvider crypto = CryptorProvider.forScheme(CryptorProvider.Scheme.SIV_GCM);
        final SecureRandom random = new SecureRandom();
        final Cryptor cryptor = crypto.provide(PerpetualMasterkey.generate(random), random);
        final CryptoDirectory provider = new CryptoDirectoryV7Provider(new CryptoVault(home), new CryptoFilenameV7Provider(), new CryptorCache(cryptor.fileNameCryptor()));
        assertNotNull(provider.toEncrypted(session, null, home));
        final Path f = new Path("/vault/f", EnumSet.of(Path.Type.directory));
        assertNotNull(provider.toEncrypted(session, null, f));
        assertEquals(provider.toEncrypted(session, null, f), provider.toEncrypted(session, null, f));
    }
}
