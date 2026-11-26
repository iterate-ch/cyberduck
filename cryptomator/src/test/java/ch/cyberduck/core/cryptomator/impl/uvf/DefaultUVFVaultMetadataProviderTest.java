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

import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.lang3.StringUtils;
import org.cryptomator.cryptolib.api.LockedVault;
import org.cryptomator.cryptolib.api.NameShortener;
import org.cryptomator.cryptolib.api.UvfLockedVault;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;
import org.cryptomator.cryptolib.api.VaultKeyInvalidException;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultUVFVaultMetadataProviderTest {

    @Test
    public void testCreateAndUnlock() throws Exception {
        final UvfUnlockedVault created = new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase")).create();
        assertEquals(NameShortener.DISABLED, created.nameShorteningThreshold());
        final LockedVault locked = LockedVault.parse(created.serializeConfig());
        assertTrue(locked instanceof UvfLockedVault);
        final UvfUnlockedVault unlocked = new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase")).unlock((UvfLockedVault) locked);
        assertEquals(created.seedIds(), unlocked.seedIds());
        assertEquals(created.rootDirectory().dirPath(), unlocked.rootDirectory().dirPath());
        created.destroy();
        unlocked.destroy();
    }

    @Test(expected = VaultKeyInvalidException.class)
    public void testUnlockWrongPassphrase() throws Exception {
        final UvfUnlockedVault created = new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase")).create();
        final UvfLockedVault locked = (UvfLockedVault) LockedVault.parse(created.serializeConfig());
        created.destroy();
        new DefaultUVFVaultMetadataProvider(new VaultCredentials("other")).unlock(locked);
    }

    @Test
    public void testCloseResetsPassphrase() {
        final VaultCredentials credentials = new VaultCredentials("mypassphrase");
        new DefaultUVFVaultMetadataProvider(credentials).close();
        assertTrue(StringUtils.isEmpty(credentials.getPassword()));
    }
}
