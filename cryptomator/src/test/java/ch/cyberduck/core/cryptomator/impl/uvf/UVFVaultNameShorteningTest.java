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
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.cryptomator.cryptolib.api.NameShortener;
import org.cryptomator.cryptolib.api.ShortenedVaultNode;
import org.cryptomator.cryptolib.api.UvfKeyEncryptionKey;
import org.cryptomator.cryptolib.api.UvfRecoveryKey;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;
import org.junit.Test;

import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Vaults in Universal Vault Format with name shortening enabled are protected from names written unshortened
 */
public class UVFVaultNameShorteningTest {

    private static final String LONG_NAME = StringUtils.repeat("a", 200);

    private static UvfUnlockedVault create(final int threshold) {
        return UvfUnlockedVault.create(FastSecureRandomProvider.get().provide(),
                UvfKeyEncryptionKey.password("test"), UvfRecoveryKey.generate(FastSecureRandomProvider.get().provide()), threshold);
    }

    private static UVFVault open(final NullSession session, final UvfUnlockedVault unlocked) throws Exception {
        final UVFVault vault = new UVFVault(new Path("/vault", EnumSet.of(Path.Type.directory)));
        MethodUtils.invokeMethod(vault, true, "init", session, unlocked);
        return vault;
    }

    @Test(expected = CryptoInvalidFilenameException.class)
    public void testRejectLongNameWithShorteningEnabled() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final UVFVault vault = open(session, create(220));
        assertTrue(vault.getFilenameProvider().isValid("f"));
        assertNotNull(vault.getDirectoryProvider().toEncrypted(session, vault.getHome(), "f", EnumSet.of(Path.Type.file)));
        // Would be stored shortened by other clients
        assertFalse(vault.getFilenameProvider().isValid(LONG_NAME));
        try {
            vault.getDirectoryProvider().toEncrypted(session, vault.getHome(), LONG_NAME, EnumSet.of(Path.Type.file));
        }
        finally {
            vault.close();
        }
    }

    @Test
    public void testLongNameWithShorteningDisabled() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        final UvfUnlockedVault unlocked = create(NameShortener.DISABLED);
        final UVFVault vault = open(session, unlocked);
        assertTrue(vault.getFilenameProvider().isValid(LONG_NAME));
        assertEquals(unlocked.rootDirectory().encryptName(LONG_NAME).name(),
                vault.getDirectoryProvider().toEncrypted(session, vault.getHome(), LONG_NAME, EnumSet.of(Path.Type.file)));
        vault.close();
    }

    @Test
    public void testSkipSidecarInListing() {
        final UvfUnlockedVault unlocked = create(220);
        final ShortenedVaultNode shortened = unlocked.nameShortener().shorten(unlocked.rootDirectory().encryptName(LONG_NAME).name());
        assertNotNull(shortened);
        final Pattern skip = Pattern.compile(PreferencesFactory.get().getProperty("cryptomator.vault.skip.regex"));
        // Sidecar with full name is skipped silently
        assertTrue(skip.matcher(shortened.sidecarFile()).matches());
        // Shortened entry and regular entries are not skipped
        assertFalse(skip.matcher(shortened.shortenedName()).matches());
        assertFalse(skip.matcher(unlocked.rootDirectory().encryptName("f").name()).matches());
        unlocked.destroy();
    }
}
