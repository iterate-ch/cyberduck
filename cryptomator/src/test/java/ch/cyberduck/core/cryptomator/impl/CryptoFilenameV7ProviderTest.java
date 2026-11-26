package ch.cyberduck.core.cryptomator.impl;

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
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.AbstractCryptoTests;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.CryptoInvalidFilenameException;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class CryptoFilenameV7ProviderTest extends AbstractCryptoTests {

    /**
     * @return Provider with threshold as configured by vault, no limit for vaults in Universal Vault Format
     */
    private CryptoFilenameV7Provider provider(final AbstractVault vault) {
        return new CryptoFilenameV7Provider(vault, VaultVersion.Type.UVF == vaultVersion ? Integer.MAX_VALUE : CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD);
    }

    @Test
    public void testIsValidLength() throws Exception {
        final AbstractVault vault = this.unlockedVault();
        final CryptoFilenameV7Provider provider = this.provider(vault);
        // Encrypted name with 220 characters for 146 bytes in vault format 8
        assertTrue(provider.isValid(StringUtils.repeat("a", 146)));
        assertTrue(provider.isValid(StringUtils.repeat("ä", 73)));
        // Encrypted name exceeds threshold although clear text name is shorter
        assertEquals(VaultVersion.Type.UVF == vaultVersion, provider.isValid(StringUtils.repeat("a", 147)));
        assertEquals(VaultVersion.Type.UVF == vaultVersion, provider.isValid(StringUtils.repeat("ä", 74)));
        vault.close();
    }

    @Test
    public void testDeflateMatchesIsValid() throws Exception {
        final AbstractVault vault = this.unlockedVault();
        final CryptoFilenameV7Provider provider = this.provider(vault);
        final NullSession session = new NullSession(new Host(new TestProtocol()));
        for(int length = 140; length <= 150; length++) {
            final String filename = StringUtils.repeat("a", length);
            final String encrypted = vault.getRootDirectory().encryptName(filename).name();
            try {
                assertEquals(encrypted, provider.deflate(session, encrypted));
                assertTrue(provider.isValid(filename));
            }
            catch(CryptoInvalidFilenameException e) {
                assertFalse(provider.isValid(filename));
            }
        }
        vault.close();
    }
}
