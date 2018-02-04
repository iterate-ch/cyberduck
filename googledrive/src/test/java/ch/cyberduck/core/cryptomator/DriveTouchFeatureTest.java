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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.features.CryptoDeleteFeature;
import ch.cyberduck.core.cryptomator.features.CryptoFindFeature;
import ch.cyberduck.core.cryptomator.features.CryptoTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveTouchFeature;
import ch.cyberduck.core.googledrive.DriveWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

@Category(IntegrationTest.class)
public class DriveTouchFeatureTest extends AbstractDriveTest {

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<Void>(session, new DriveTouchFeature(session), new DriveWriteFeature(session), cryptomator).touch(
                new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file)), new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(test));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilenameEncryptedDefaultFeature() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final CryptoVault cryptomator = new CryptoVault(
            new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)));
        final Path vault = cryptomator.create(session, null, new VaultCredentials("test"), new DisabledPasswordStore());
        session.withRegistry(new DefaultVaultRegistry(new DisabledPasswordStore(), new DisabledPasswordCallback(), cryptomator));
        final Path test = new CryptoTouchFeature<Void>(session, new DriveTouchFeature(session), new DriveWriteFeature(session), cryptomator).touch(
                new Path(vault, new RandomStringGenerator.Builder().build().generate(130), EnumSet.of(Path.Type.file)), new TransferStatus());
        Assert.assertTrue(new CryptoFindFeature(session, new DefaultFindFeature(session), cryptomator).find(test));
        new CryptoDeleteFeature(session, new DriveDeleteFeature(session), cryptomator).delete(Arrays.asList(test, vault), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
