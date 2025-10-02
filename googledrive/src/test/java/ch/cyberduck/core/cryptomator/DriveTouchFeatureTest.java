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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.features.CryptoListService;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveFileIdProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveListService;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.EnumSet;

import com.google.api.services.drive.model.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
@RunWith(value = Parameterized.class)
public class DriveTouchFeatureTest extends AbstractDriveTest {

    @Test
    public void testTouch() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<File>(session), cryptomator).touch(
                cryptomator.getFeature(session, Write.class, new DriveWriteFeature(session, fileid)), new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final Path found = new CryptoListService(session, new DriveListService(session, fileid), cryptomator).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        final String fileId = found.attributes().getFileId();
        assertNotNull(fileId);
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    //TODO
    @Test
    @Ignore("Filename shorterning not implemented yet")
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<File>(session), cryptomator).touch(
                cryptomator.getFeature(session, Write.class, new DriveWriteFeature(session, fileid)), new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final Path found = new CryptoListService(session, new DriveListService(session, fileid), cryptomator).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        final String fileId = found.attributes().getFileId();
        assertNotNull(fileId);
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    @Ignore("Filename shorterning not implemented yet")
    public void testTouchLongFilenameEncryptedDefaultFeature() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path vault = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final AbstractVault cryptomator = new CryptoVaultProvider(session).create(session, null, new VaultCredentials("test"),
                new VaultMetadata(vault, vaultVersion));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordCallback(), cryptomator));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path test = new CryptoTouchFeature<>(session, new DefaultTouchFeature<File>(session), cryptomator).touch(
                cryptomator.getFeature(session, Write.class, new DriveWriteFeature(session, fileid)), new Path(vault, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(cryptomator.getFeature(session, Find.class, new DefaultFindFeature(session)).find(test));
        final Path found = new CryptoListService(session, new DriveListService(session, fileid), cryptomator).list(test.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        final String fileId = found.attributes().getFileId();
        assertNotNull(fileId);
        cryptomator.getFeature(session, Delete.class, new DriveDeleteFeature(session, fileid)).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
