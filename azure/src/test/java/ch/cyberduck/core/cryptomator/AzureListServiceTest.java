package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.azure.AbstractAzureTest;
import ch.cyberduck.core.azure.AzureDeleteFeature;
import ch.cyberduck.core.azure.AzureFindFeature;
import ch.cyberduck.core.azure.AzureObjectListService;
import ch.cyberduck.core.azure.AzureWriteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class AzureListServiceTest extends AbstractAzureTest {

    @Test
    public void testListCryptomator() throws Exception {
        final Path home = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertTrue(new CryptoListService(session, new AzureObjectListService(session, null), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(session), cryptomator).touch(new AzureWriteFeature(session, null), test, new TransferStatus());
        assertEquals(test, new CryptoListService(session, new AzureObjectListService(session, null), cryptomator).list(vault, new DisabledListProgressListener() {
            @Override
            public void cleanup(final Path directory, final AttributedList<Path> list, final Optional<BackgroundException> e) {
                assertEquals(vault, directory);
                for(Path f : list) {
                    assertTrue(f.getType().contains(Path.Type.decrypted));
                }
            }

            @Override
            public void chunk(final Path directory, final AttributedList<Path> list) {
                assertEquals(vault, directory);
                for(Path f : list) {
                    assertTrue(f.getType().contains(Path.Type.decrypted));
                }
            }
        }).get(0));
        {
            final Cache<Path> cache = new PathCache(1);
            assertTrue(new CachingFindFeature(session, cache, new CryptoFindFeature(session, new AzureFindFeature(session, null), cryptomator)).find(test));
            assertFalse(cache.isCached(vault));
        }
        {
            final Cache<Path> cache = new PathCache(1);
            assertTrue(new CachingFindFeature(session, cache, new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator)).find(test));
            assertTrue(cache.isCached(vault));
            final AttributedList<Path> list = cache.get(vault);
            assertFalse(list.isEmpty());
            for(Path f : list) {
                assertTrue(f.getType().contains(Path.Type.decrypted));
            }
        }
        cryptomator.getFeature(session, Delete.class, new AzureDeleteFeature(session, null)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
