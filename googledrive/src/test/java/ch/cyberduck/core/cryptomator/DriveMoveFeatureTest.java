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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoDirectoryFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoMoveFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveMoveFeature;
import ch.cyberduck.core.googledrive.DriveUploadFeature;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

@Category(IntegrationTest.class)
public class DriveMoveFeatureTest extends AbstractDriveTest {

    @Test
    public void testMove() throws Exception {
        final Path home = new DriveHomeFinderService(session).find();
        final CryptoVault cryptomator = new CryptoVault(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new DisabledPasswordStore());
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"));
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path folder = new CryptoDirectoryFeature<Void>(session, new DriveDirectoryFeature(session), new DriveWriteFeature(session), cryptomator).mkdir(
                new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path file = new CryptoTouchFeature<Void>(session, new DefaultTouchFeature<Void>(new DriveUploadFeature(new DriveWriteFeature(session))), new DriveWriteFeature(session), cryptomator).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        final CryptoMoveFeature move = new CryptoMoveFeature(session, new DriveMoveFeature(session), new DriveDeleteFeature(session), cryptomator);
        // rename file
        final Path fileRenamed = new Path(folder, "f1", EnumSet.of(Path.Type.file));
        move.move(file, fileRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(file));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamed));
        // rename folder
        final Path folderRenamed = new Path(vault, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        Assert.assertFalse(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folder));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "f1", EnumSet.of(Path.Type.file));
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(fileRenamedInRenamedFolder));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(
                fileRenamedInRenamedFolder, folderRenamed, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
