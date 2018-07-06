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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.AbstractOneDriveTest;
import ch.cyberduck.core.onedrive.OneDriveListService;
import ch.cyberduck.core.onedrive.features.OneDriveDeleteFeature;
import ch.cyberduck.core.onedrive.features.OneDriveFileIdProvider;
import ch.cyberduck.core.onedrive.features.OneDriveHomeFinderFeature;
import ch.cyberduck.core.onedrive.features.OneDriveWriteFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class OneDriveListServiceTest extends AbstractOneDriveTest {

    @Test
    public void testListCryptomator() throws Exception {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        assertTrue(new CryptoListService(session, new OneDriveListService(session, new OneDriveFileIdProvider(session)), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(test, new TransferStatus());
        assertEquals(new SimplePathPredicate(test), new SimplePathPredicate(new CryptoListService(session, new OneDriveListService(session, new OneDriveFileIdProvider(session)), cryptomator).list(vault, new DisabledListProgressListener()).get(0)));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListCryptomatorCached() throws Exception {
        final PathCache cache = new PathCache(Integer.MAX_VALUE);
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final ListService listService = session._getFeature(ListService.class);

        cache.put(home, listService.withCache(cache).list(home, new DisabledListProgressListener()));

        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        assertTrue(new CryptoListService(session, listService, cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DefaultUploadFeature<>(new OneDriveWriteFeature(session))), new OneDriveWriteFeature(session), cryptomator).touch(test, new TransferStatus());
        assertEquals(new SimplePathPredicate(test), new SimplePathPredicate(new CryptoListService(session, listService, cryptomator).list(vault, new DisabledListProgressListener()).get(0)));
        new CryptoDeleteFeature(session, new OneDriveDeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
