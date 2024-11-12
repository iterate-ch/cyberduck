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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryV7Feature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googlestorage.AbstractGoogleStorageTest;
import ch.cyberduck.core.googlestorage.GoogleStorageDeleteFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageDirectoryFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageFindFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageObjectListService;
import ch.cyberduck.core.googlestorage.GoogleStorageTouchFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageWriteFeature;
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

import com.google.api.services.storage.model.StorageObject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class GoogleStorageListServiceTest extends AbstractGoogleStorageTest {

    @Test
    public void testListCryptomator() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final CryptoVault cryptomator = new CryptoVault(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        assertTrue(new CryptoListService(session, new GoogleStorageObjectListService(session), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        final CryptoDirectoryV7Feature<StorageObject> mkdir = new CryptoDirectoryV7Feature<>(session, new GoogleStorageDirectoryFeature(session),
                new GoogleStorageWriteFeature(session), new GoogleStorageFindFeature(session), cryptomator);
        final Path directory1 = mkdir.mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(new CryptoListService(session, new GoogleStorageObjectListService(session), cryptomator).list(vault, new DisabledListProgressListener())
                .find(new SimplePathPredicate(directory1)));
        final CryptoTouchFeature<StorageObject> touch = new CryptoTouchFeature<>(session, new GoogleStorageTouchFeature(session),
                new GoogleStorageWriteFeature(session), cryptomator);
        final Path test = touch.touch(
                new Path(directory1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(new CryptoListService(session, new GoogleStorageObjectListService(session), cryptomator).list(directory1, new DisabledListProgressListener())
                .find(new SimplePathPredicate(test)));
        final Path directory2 = mkdir.mkdir(
                new Path(directory1, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(new CryptoListService(session, new GoogleStorageObjectListService(session), cryptomator).list(directory1, new DisabledListProgressListener())
                .find(new SimplePathPredicate(directory2)));
        cryptomator.getFeature(session, Delete.class, new GoogleStorageDeleteFeature(session))
                .delete(Arrays.asList(test, directory1, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
