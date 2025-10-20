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
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.cryptomator.features.CryptoWriteFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPDeleteFeature;
import ch.cyberduck.core.ftp.FTPFindFeature;
import ch.cyberduck.core.ftp.FTPWriteFeature;
import ch.cyberduck.core.ftp.list.FTPListService;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;
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
public class FTPListServiceTest extends AbstractFTPTest {

    @Test
    public void testListCryptomator() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        assertTrue(new CryptoListService(session, new FTPListService(session), cryptomator).list(vault).isEmpty());
        new CryptoTouchFeature<>(session, new DefaultTouchFeature<Void>(
                session), cryptomator).touch(new CryptoWriteFeature<>(session, new FTPWriteFeature(session), cryptomator), test, new TransferStatus());
        assertEquals(test, new CryptoListService(session, new FTPListService(session), cryptomator).list(vault, new DisabledListProgressListener() {
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
            assertTrue(new CachingFindFeature(session, cache, new CryptoFindFeature(session, new FTPFindFeature(session), cryptomator)).find(test));
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
        cryptomator.getFeature(session, Delete.class, new FTPDeleteFeature(session)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
