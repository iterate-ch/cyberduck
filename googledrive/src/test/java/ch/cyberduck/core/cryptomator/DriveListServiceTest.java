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
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDefaultListService;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveFileidProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveUploadFeature;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveListServiceTest extends AbstractDriveTest {

    @Test
    public void testListCryptomator() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        assertTrue(new CryptoListService(session, new DriveDefaultListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        final Path testFile = new CryptoTouchFeature<VersionId>(session, new DefaultTouchFeature<>(new DriveUploadFeature(new DriveWriteFeature(session, fileid))), new DriveWriteFeature(session, fileid), cryptomator).touch(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(testFile.attributes().getVersionId());
        assertEquals(testFile, new CryptoListService(session, new DriveDefaultListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener()).get(0));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Collections.singletonList(testFile), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final Path testDir = new CryptoDirectoryFeature<VersionId>(session, new DriveDirectoryFeature(session, fileid), new DriveWriteFeature(session, fileid), cryptomator).mkdir(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new CryptoListService(session, new DriveDefaultListService(session, fileid), cryptomator).list(testDir, new DisabledListProgressListener()).isEmpty());
        assertEquals(testDir, new CryptoListService(session, new DriveDefaultListService(session, fileid), cryptomator).list(vault, new DisabledListProgressListener()).get(0));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(testDir, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListCryptomatorCached() throws Exception {
        final PathCache cache = new PathCache(Integer.MAX_VALUE);
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final ListService listService = session._getFeature(ListService.class);
        cache.put(home, listService.withCache(cache).list(home, new DisabledListProgressListener()));
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        assertTrue(new CryptoListService(session, listService, cryptomator).list(vault, new DisabledListProgressListener()).isEmpty());
        new CryptoTouchFeature<VersionId>(session, new DefaultTouchFeature<VersionId>(new DefaultUploadFeature<>(new DriveWriteFeature(session, fileid))), new DriveWriteFeature(session, fileid), cryptomator).touch(test, new TransferStatus());
        assertEquals(new SimplePathPredicate(test), new SimplePathPredicate(new CryptoListService(session, listService, cryptomator).list(vault, new DisabledListProgressListener()).get(0)));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
