package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.features.CryptoAttributesFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFileIdProvider;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveAttributesFinderFeature;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveFileIdProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveListService;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class DriveAttributesFinderFeatureTest extends AbstractDriveTest {

    @Test
    public void testFindCustomAttributesFinderCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider idProvider = new DriveFileIdProvider(session);
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, idProvider)), new DriveWriteFeature(session, idProvider), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        test.attributes().setSize(0L);
        final PathAttributes attributes = new CryptoAttributesFeature(session, new DriveAttributesFinderFeature(session, idProvider), cryptomator).find(test);
        assertNotNull(attributes);
        assertEquals(0L, attributes.getSize());
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, idProvider)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDefaultAttributesFinderCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider idProvider = new DriveFileIdProvider(session);
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, idProvider)), new DriveWriteFeature(session, idProvider), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        test.attributes().setSize(0L);
        final PathAttributes attributes = new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator).find(test);
        assertNotNull(attributes);
        assertEquals(0L, attributes.getSize());
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, idProvider)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectoryDefaultAttributesFinderCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider idProvider = new DriveFileIdProvider(session);
        final Path test = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, idProvider)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String fileId = test.attributes().getFileId();
        assertNotNull(fileId);
        final PathAttributes attributes = new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator).find(test);
        assertEquals(fileId, attributes.getFileId());
        assertEquals(fileId, cryptomator.encrypt(session, test, true).attributes().getFileId());
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, idProvider)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDefaultAttributesFinderWithCacheCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider idProvider = new DriveFileIdProvider(session);
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<>(new DriveWriteFeature(session, idProvider)), new DriveWriteFeature(session, idProvider), cryptomator).touch(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path found = new CryptoListService(session, new DriveListService(session, idProvider), cryptomator).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertEquals(0L, found.attributes().getSize());
        final Cache<Path> cache = new PathCache(1);
        final AttributedList<Path> list = new AttributedList<>();
        list.add(found);
        cache.put(vault, list);
        final PathAttributes attributes = new CachingAttributesFinderFeature(session, cache, new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator)).find(test);
        assertNotNull(attributes);
        assertEquals(0L, attributes.getSize());
        assertEquals(0L, cache.get(vault).get(0).attributes().getSize());
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, idProvider)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectoryDefaultAttributesFinderWithCacheCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, new VaultCredentials("test"), vaultVersion);
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider idProvider = new DriveFileIdProvider(session);
        assertEquals(new CryptoFileIdProvider(session, idProvider, cryptomator).getFileId(vault),
                idProvider.getFileId(vault));
        final Path test = cryptomator.getFeature(session, Directory.class, new DriveDirectoryFeature(session, idProvider)).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String fileId = test.attributes().getFileId();
        assertNotNull(fileId);
        final Path found = new CryptoListService(session, new DriveListService(session, idProvider), cryptomator).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertNull(found.attributes().getFileId());
        assertEquals(fileId, new CryptoFileIdProvider(session, idProvider, cryptomator).getFileId(found));
        final Cache<Path> cache = new PathCache(1);
        final AttributedList<Path> list = new AttributedList<>();
        list.add(test);
        cache.put(vault, list);
        final PathAttributes attributes = new CachingAttributesFinderFeature(session, cache, new CryptoAttributesFeature(session, new DefaultAttributesFinderFeature(session), cryptomator)).find(test);
        assertEquals(fileId, attributes.getFileId());
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, idProvider)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
