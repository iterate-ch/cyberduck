package ch.cyberduck.core.cryptomator.impl.uvf;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.cryptomator.CryptoInvalidFilenameException;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.cryptomator.cryptolib.api.NameShortener;
import org.cryptomator.cryptolib.api.UvfKeyEncryptionKey;
import org.cryptomator.cryptolib.api.UvfRecoveryKey;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * Decrypted names are untrusted input
 */
public class UVFVaultCleartextFilenameTest {

    private static final Path HOME = new Path("/vault", EnumSet.of(Path.Type.directory));

    @Test
    public void testNormalizationForm() {
        final UVFVault vault = new UVFVault(HOME);
        assertTrue(vault.isValidCleartextFilename("ä"));
        // Decomposed form is never looked up as names are encrypted in NFC
        assertFalse(vault.isValidCleartextFilename("ä"));
        assertFalse(vault.isValidCleartextFilename(".."));
    }

    @Test
    public void testDecrypt() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final UVFVault vault = new UVFVault(HOME) {
            @Override
            protected boolean isValidCleartextFilename(final String filename) {
                return super.isValidCleartextFilename(filename) && !"invalid".equals(filename);
            }
        };
        MethodUtils.invokeMethod(vault, true, "init", session, UvfUnlockedVault.create(FastSecureRandomProvider.get().provide(),
                UvfKeyEncryptionKey.password("test"), UvfRecoveryKey.generate(FastSecureRandomProvider.get().provide()), NameShortener.DISABLED));
        try {
            final Path parent = vault.encrypt(session, HOME);
            final Path valid = new Path(parent, vault.getDirectoryProvider().toEncrypted(session, HOME, "valid", EnumSet.of(Path.Type.file)),
                    EnumSet.of(Path.Type.file, Path.Type.encrypted));
            assertEquals(new Path(HOME, "valid", EnumSet.of(Path.Type.file)), vault.decrypt(session, valid));
            final Path invalid = new Path(parent, vault.getDirectoryProvider().toEncrypted(session, HOME, "invalid", EnumSet.of(Path.Type.file)),
                    EnumSet.of(Path.Type.file, Path.Type.encrypted));
            try {
                vault.decrypt(session, invalid);
                fail();
            }
            catch(CryptoInvalidFilenameException e) {
                assertEquals(invalid, e.getFile());
            }
        }
        finally {
            vault.close();
        }
    }
}
