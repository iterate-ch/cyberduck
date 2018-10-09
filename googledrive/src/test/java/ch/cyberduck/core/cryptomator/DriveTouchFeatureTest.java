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
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveAttributesFinderFeature;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveFileidProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveListService;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveTouchFeatureTest extends AbstractDriveTest {

    @Test
    public void testTouch() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path test = new CryptoTouchFeature<VersionId>(session, new DefaultTouchFeature<>(new DefaultUploadFeature<>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
            new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(test, new DisabledListProgressListener()));
        final Path found = new CryptoListService(session, new DriveListService(session, fileid), cryptomator).withCache(cache).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        final String versionId = found.attributes().getVersionId();
        assertNotNull(versionId);
        assertEquals(test.attributes(), found.attributes());
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path test = new CryptoTouchFeature<VersionId>(session, new DefaultTouchFeature<>(new DefaultUploadFeature<>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(test, new DisabledListProgressListener()));
        final Path found = new CryptoListService(session, new DriveListService(session, fileid), cryptomator).withCache(cache).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        final String versionId = found.attributes().getVersionId();
        assertNotNull(versionId);
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilenameEncryptedDefaultFeature() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path test = new CryptoTouchFeature<VersionId>(session, new DefaultTouchFeature<>(new DefaultUploadFeature<>(new DriveWriteFeature(session, fileid)), new DriveAttributesFinderFeature(session, fileid)), new DriveWriteFeature(session, fileid), cryptomator).touch(
                new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).withCache(cache).find(test, new DisabledListProgressListener()));
        final Path found = new CryptoListService(session, new DriveListService(session, fileid), cryptomator).withCache(cache).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        final String versionId = found.attributes().getVersionId();
        assertNotNull(versionId);
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session, fileid), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
